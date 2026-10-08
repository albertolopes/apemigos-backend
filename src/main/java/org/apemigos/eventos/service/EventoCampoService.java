package org.apemigos.eventos.service;

import lombok.RequiredArgsConstructor;
import org.apemigos.eventos.dto.EventoCampoDTO;
import org.apemigos.eventos.dto.EventoCampoOpcaoDTO;
import org.apemigos.eventos.entity.Evento;
import org.apemigos.eventos.entity.EventoCampo;
import org.apemigos.eventos.entity.EventoCampoOpcao;
import org.apemigos.eventos.enums.EventoCampoTipo;
import org.apemigos.eventos.repository.EventoCampoRepository;
import org.apemigos.eventos.repository.EventoInscricaoRespostaRepository;
import org.apemigos.eventos.repository.EventoRepository;
import org.apemigos.exceptions.BusinessRuleException;
import org.apemigos.exceptions.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EventoCampoService {

    private final EventoRepository eventoRepository;
    private final EventoCampoRepository campoRepository;
    private final EventoInscricaoRespostaRepository respostaRepository;
    private final EventoMapperService mapper;

    public List<EventoCampoDTO> findByEvento(Long eventoId) {
        ensureEventoExists(eventoId);
        return campoRepository.findByEventoIdOrderByOrdemAscIdAsc(eventoId).stream()
                .map(mapper::toCampoDto)
                .toList();
    }

    @Transactional
    public List<EventoCampoDTO> replaceCampos(Long eventoId, List<EventoCampoDTO> dtos) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ObjectNotFoundException("Evento não encontrado"));

        if (dtos == null) {
            throw new BusinessRuleException("Lista de campos é obrigatória");
        }

        Set<String> chaves = new HashSet<>();
        for (EventoCampoDTO dto : dtos) {
            String chave = normalizeChave(dto.getChave() != null ? dto.getChave() : dto.getLabel());
            if (!chaves.add(chave)) {
                throw new BusinessRuleException("Chave de campo duplicada: " + chave);
            }
        }

        List<EventoCampo> atuais = campoRepository.findByEventoIdOrderByOrdemAscIdAsc(eventoId);
        for (EventoCampo campoAtual : atuais) {
            boolean recebido = dtos.stream().anyMatch(dto -> dto.getId() != null && dto.getId().equals(campoAtual.getId()));
            if (!recebido && respostaRepository.existsByCampoId(campoAtual.getId())) {
                campoAtual.setAtivo(false);
            } else if (!recebido) {
                campoRepository.delete(campoAtual);
            }
        }

        for (int i = 0; i < dtos.size(); i++) {
            EventoCampoDTO dto = dtos.get(i);
            EventoCampo campo = resolveCampo(evento, dto);
            boolean hasRespostas = campo.getId() != null && respostaRepository.existsByCampoId(campo.getId());
            applyCampoData(campo, dto, i, hasRespostas);
            campoRepository.save(campo);
        }

        return findByEvento(eventoId);
    }

    private EventoCampo resolveCampo(Evento evento, EventoCampoDTO dto) {
        if (dto.getId() == null) {
            EventoCampo campo = new EventoCampo();
            campo.setEvento(evento);
            return campo;
        }
        EventoCampo campo = campoRepository.findById(dto.getId())
                .orElseThrow(() -> new ObjectNotFoundException("Campo do evento não encontrado"));
        if (!campo.getEvento().getId().equals(evento.getId())) {
            throw new BusinessRuleException("Campo não pertence ao evento informado");
        }
        return campo;
    }

    private void applyCampoData(EventoCampo campo, EventoCampoDTO dto, int index, boolean hasRespostas) {
        if (dto.getLabel() == null || dto.getLabel().isBlank()) {
            throw new BusinessRuleException("Label do campo é obrigatório");
        }
        if (dto.getTipo() == null) {
            throw new BusinessRuleException("Tipo do campo é obrigatório");
        }

        String chave = normalizeChave(dto.getChave() != null ? dto.getChave() : dto.getLabel());
        if (hasRespostas && campo.getTipo() != null && campo.getTipo() != dto.getTipo()) {
            throw new BusinessRuleException("Não é possível alterar o tipo de um campo que já possui respostas");
        }

        campo.setLabel(dto.getLabel());
        campo.setChave(chave);
        campo.setTipo(dto.getTipo());
        campo.setObrigatorio(Boolean.TRUE.equals(dto.getObrigatorio()));
        campo.setUnico(Boolean.TRUE.equals(dto.getUnico()));
        campo.setOrdem(dto.getOrdem() != null ? dto.getOrdem() : index);
        campo.setPlaceholder(dto.getPlaceholder());
        campo.setTextoAjuda(dto.getTextoAjuda());
        campo.setAtivo(dto.getAtivo() == null || dto.getAtivo());

        applyOpcoes(campo, dto.getOpcoes());
        validateOptions(campo);
    }

    private void applyOpcoes(EventoCampo campo, List<EventoCampoOpcaoDTO> opcoesDto) {
        campo.getOpcoes().clear();
        if (opcoesDto == null) {
            return;
        }
        for (int i = 0; i < opcoesDto.size(); i++) {
            EventoCampoOpcaoDTO opcaoDto = opcoesDto.get(i);
            if (opcaoDto.getLabel() == null || opcaoDto.getLabel().isBlank()) {
                throw new BusinessRuleException("Label da opção é obrigatório");
            }
            String valor = opcaoDto.getValor() == null || opcaoDto.getValor().isBlank()
                    ? normalizeChave(opcaoDto.getLabel())
                    : opcaoDto.getValor().trim();
            EventoCampoOpcao opcao = EventoCampoOpcao.builder()
                    .campo(campo)
                    .label(opcaoDto.getLabel())
                    .valor(valor)
                    .ordem(opcaoDto.getOrdem() != null ? opcaoDto.getOrdem() : i)
                    .build();
            campo.getOpcoes().add(opcao);
        }
    }

    private void validateOptions(EventoCampo campo) {
        boolean requiresOptions = campo.getTipo() == EventoCampoTipo.SELECT
                || campo.getTipo() == EventoCampoTipo.RADIO
                || campo.getTipo() == EventoCampoTipo.CHECKBOX;
        if (requiresOptions && campo.getOpcoes().isEmpty()) {
            throw new BusinessRuleException("Campos do tipo " + campo.getTipo() + " precisam de opções");
        }
        Set<String> valores = new HashSet<>();
        for (EventoCampoOpcao opcao : campo.getOpcoes()) {
            if (!valores.add(opcao.getValor())) {
                throw new BusinessRuleException("Opção duplicada no campo " + campo.getLabel() + ": " + opcao.getValor());
            }
        }
    }

    private void ensureEventoExists(Long eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new ObjectNotFoundException("Evento não encontrado");
        }
    }

    private String normalizeChave(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException("Chave do campo é obrigatória");
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
