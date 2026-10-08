package org.apemigos.eventos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apemigos.eventos.dto.EventoInscricaoDTO;
import org.apemigos.eventos.dto.EventoInscricaoRequestDTO;
import org.apemigos.eventos.dto.EventoInscricaoRespostaRequestDTO;
import org.apemigos.eventos.entity.Evento;
import org.apemigos.eventos.entity.EventoCampo;
import org.apemigos.eventos.entity.EventoInscricao;
import org.apemigos.eventos.entity.EventoInscricaoResposta;
import org.apemigos.eventos.enums.EventoCampoTipo;
import org.apemigos.eventos.enums.EventoInscricaoStatus;
import org.apemigos.eventos.repository.EventoCampoRepository;
import org.apemigos.eventos.repository.EventoInscricaoRepository;
import org.apemigos.eventos.repository.EventoInscricaoRespostaRepository;
import org.apemigos.eventos.repository.EventoRepository;
import org.apemigos.exceptions.BusinessRuleException;
import org.apemigos.exceptions.ObjectNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EventoInscricaoService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final EventoRepository eventoRepository;
    private final EventoCampoRepository campoRepository;
    private final EventoInscricaoRepository inscricaoRepository;
    private final EventoInscricaoRespostaRepository respostaRepository;
    private final EventoService eventoService;
    private final EventoMapperService mapper;
    private final ObjectMapper objectMapper;

    public Page<EventoInscricaoDTO> findByEvento(Long eventoId,
                                                 String keyword,
                                                 String cpf,
                                                 String email,
                                                 String telefone,
                                                 LocalDateTime dataInicio,
                                                 LocalDateTime dataFim,
                                                 Pageable pageable) {
        ensureEventoExists(eventoId);
        return inscricaoRepository.findByFiltros(eventoId, keyword, onlyDigits(cpf), normalizeEmail(email), onlyDigits(telefone), dataInicio, dataFim, pageable)
                .map(inscricao -> mapper.toInscricaoDto(inscricao, false));
    }

    public EventoInscricaoDTO findById(Long eventoId, Long inscricaoId) {
        ensureEventoExists(eventoId);
        EventoInscricao inscricao = inscricaoRepository.findByIdAndEventoId(inscricaoId, eventoId)
                .orElseThrow(() -> new ObjectNotFoundException("Inscrição não encontrada"));
        return mapper.toInscricaoDto(inscricao, true);
    }

    public long countByEvento(Long eventoId) {
        ensureEventoExists(eventoId);
        return inscricaoRepository.countByEventoIdAndStatus(eventoId, EventoInscricaoStatus.CONFIRMADA);
    }

    @Transactional
    public EventoInscricaoDTO createPublica(Long eventoId, EventoInscricaoRequestDTO request) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ObjectNotFoundException("Evento não encontrado"));
        if (!eventoService.inscricoesAbertas(evento)) {
            throw new BusinessRuleException("Evento não está recebendo inscrições");
        }

        List<EventoCampo> campos = campoRepository.findByEventoIdAndAtivoTrueOrderByOrdemAscIdAsc(eventoId);
        Map<Long, EventoCampo> camposById = new HashMap<>();
        Map<String, EventoCampo> camposByChave = new HashMap<>();
        for (EventoCampo campo : campos) {
            camposById.put(campo.getId(), campo);
            camposByChave.put(campo.getChave(), campo);
        }

        Map<Long, EventoInscricaoRespostaRequestDTO> respostas = normalizeRequest(request, camposById, camposByChave);
        validateRequired(campos, respostas);

        EventoInscricao inscricao = EventoInscricao.builder()
                .evento(evento)
                .status(EventoInscricaoStatus.CONFIRMADA)
                .build();

        for (EventoCampo campo : campos) {
            EventoInscricaoRespostaRequestDTO respostaRequest = respostas.get(campo.getId());
            if (respostaRequest == null || isEmpty(respostaRequest.getValor())) {
                continue;
            }

            NormalizedValue normalized = normalizeValue(campo, respostaRequest.getValor());
            if (Boolean.TRUE.equals(campo.getUnico())) {
                validateUnique(eventoId, campo, normalized.valorTexto());
            }
            fillInscricaoShortcut(inscricao, campo, normalized.valorTexto());

            EventoInscricaoResposta resposta = EventoInscricaoResposta.builder()
                    .inscricao(inscricao)
                    .campo(campo)
                    .chaveCampo(campo.getChave())
                    .valorTexto(normalized.valorTexto())
                    .valorJson(normalized.valorJson())
                    .build();
            inscricao.getRespostas().add(resposta);
        }

        return mapper.toInscricaoDto(inscricaoRepository.save(inscricao), true);
    }

    private Map<Long, EventoInscricaoRespostaRequestDTO> normalizeRequest(EventoInscricaoRequestDTO request,
                                                                          Map<Long, EventoCampo> camposById,
                                                                          Map<String, EventoCampo> camposByChave) {
        if (request == null || request.getRespostas() == null) {
            throw new BusinessRuleException("Respostas da inscrição são obrigatórias");
        }
        Map<Long, EventoInscricaoRespostaRequestDTO> respostas = new HashMap<>();
        for (EventoInscricaoRespostaRequestDTO resposta : request.getRespostas()) {
            EventoCampo campo = null;
            if (resposta.getCampoId() != null) {
                campo = camposById.get(resposta.getCampoId());
            } else if (resposta.getChaveCampo() != null) {
                campo = camposByChave.get(resposta.getChaveCampo());
            }
            if (campo == null) {
                throw new BusinessRuleException("Campo informado não pertence ao evento ou não está ativo");
            }
            respostas.put(campo.getId(), resposta);
        }
        return respostas;
    }

    private void validateRequired(List<EventoCampo> campos, Map<Long, EventoInscricaoRespostaRequestDTO> respostas) {
        for (EventoCampo campo : campos) {
            EventoInscricaoRespostaRequestDTO resposta = respostas.get(campo.getId());
            if (Boolean.TRUE.equals(campo.getObrigatorio()) && (resposta == null || isEmpty(resposta.getValor()))) {
                throw new BusinessRuleException("Campo obrigatório não preenchido: " + campo.getLabel());
            }
        }
    }

    private NormalizedValue normalizeValue(EventoCampo campo, Object value) {
        String valorTexto;
        String valorJson = null;
        switch (campo.getTipo()) {
            case EMAIL -> {
                valorTexto = normalizeEmail(asString(value));
                if (valorTexto == null || !EMAIL_PATTERN.matcher(valorTexto).matches()) {
                    throw new BusinessRuleException("Email inválido no campo " + campo.getLabel());
                }
            }
            case CPF -> {
                valorTexto = onlyDigits(asString(value));
                if (valorTexto == null || valorTexto.length() != 11) {
                    throw new BusinessRuleException("CPF inválido no campo " + campo.getLabel());
                }
            }
            case PHONE -> {
                valorTexto = onlyDigits(asString(value));
                if (valorTexto == null || valorTexto.length() < 8) {
                    throw new BusinessRuleException("Telefone inválido no campo " + campo.getLabel());
                }
            }
            case NUMBER -> {
                valorTexto = asString(value);
                try {
                    Double.parseDouble(valorTexto);
                } catch (NumberFormatException e) {
                    throw new BusinessRuleException("Número inválido no campo " + campo.getLabel());
                }
            }
            case DATE -> {
                valorTexto = asString(value);
                try {
                    LocalDate.parse(valorTexto);
                } catch (Exception e) {
                    throw new BusinessRuleException("Data inválida no campo " + campo.getLabel());
                }
            }
            case SELECT, RADIO -> {
                valorTexto = asString(value);
                validateOption(campo, valorTexto);
            }
            case CHECKBOX -> {
                List<String> values = asStringList(value);
                if (values.isEmpty()) {
                    valorTexto = "";
                } else {
                    values.forEach(v -> validateOption(campo, v));
                    valorTexto = String.join(",", values);
                    valorJson = toJson(values);
                }
            }
            default -> valorTexto = asString(value);
        }
        return new NormalizedValue(valorTexto, valorJson);
    }

    private void validateUnique(Long eventoId, EventoCampo campo, String valorTexto) {
        if (valorTexto == null || valorTexto.isBlank()) {
            return;
        }
        if (respostaRepository.existsUniqueValue(eventoId, campo.getId(), valorTexto, EventoInscricaoStatus.CONFIRMADA)) {
            throw new BusinessRuleException("Já existe uma inscrição para este evento com o campo " + campo.getLabel() + " informado.");
        }
    }

    private void fillInscricaoShortcut(EventoInscricao inscricao, EventoCampo campo, String valorTexto) {
        String chave = campo.getChave();
        if ("nome".equals(chave) || "nome_completo".equals(chave)) {
            inscricao.setNomeInscrito(valorTexto);
        } else if ("email".equals(chave) || campo.getTipo() == EventoCampoTipo.EMAIL) {
            inscricao.setEmailInscrito(valorTexto);
        } else if ("telefone".equals(chave) || "celular".equals(chave) || campo.getTipo() == EventoCampoTipo.PHONE) {
            inscricao.setTelefoneInscrito(valorTexto);
        } else if ("cpf".equals(chave) || campo.getTipo() == EventoCampoTipo.CPF) {
            inscricao.setCpfInscrito(valorTexto);
        }
    }

    private void validateOption(EventoCampo campo, String valor) {
        boolean valid = campo.getOpcoes().stream().anyMatch(opcao -> opcao.getValor().equals(valor));
        if (!valid) {
            throw new BusinessRuleException("Opção inválida no campo " + campo.getLabel());
        }
    }

    private boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String str) {
            return str.isBlank();
        }
        if (value instanceof Collection<?> collection) {
            return collection.isEmpty();
        }
        return false;
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value).trim();
    }

    private List<String> asStringList(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).map(String::trim).filter(v -> !v.isBlank()).toList();
        }
        String stringValue = asString(value);
        if (stringValue == null || stringValue.isBlank()) {
            return List.of();
        }
        return Arrays.stream(stringValue.split(",")).map(String::trim).filter(v -> !v.isBlank()).toList();
    }

    private String onlyDigits(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\D", "");
    }

    private String normalizeEmail(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessRuleException("Não foi possível processar valor informado");
        }
    }

    private void ensureEventoExists(Long eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new ObjectNotFoundException("Evento não encontrado");
        }
    }

    private record NormalizedValue(String valorTexto, String valorJson) {
    }
}
