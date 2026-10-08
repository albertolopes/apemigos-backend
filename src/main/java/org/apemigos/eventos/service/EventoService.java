package org.apemigos.eventos.service;

import lombok.RequiredArgsConstructor;
import org.apemigos.eventos.dto.EventoDTO;
import org.apemigos.eventos.dto.EventoPublicoDTO;
import org.apemigos.eventos.entity.Evento;
import org.apemigos.eventos.entity.EventoCampo;
import org.apemigos.eventos.enums.EventoInscricaoStatus;
import org.apemigos.eventos.enums.EventoStatus;
import org.apemigos.eventos.repository.EventoCampoRepository;
import org.apemigos.eventos.repository.EventoInscricaoRepository;
import org.apemigos.eventos.repository.EventoRepository;
import org.apemigos.exceptions.BusinessRuleException;
import org.apemigos.exceptions.ObjectNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final EventoCampoRepository campoRepository;
    private final EventoInscricaoRepository inscricaoRepository;
    private final EventoMapperService mapper;

    public Page<EventoDTO> findAdmin(EventoStatus status, String keyword, Pageable pageable) {
        return eventoRepository.findAdmin(status, keyword, pageable).map(evento -> mapper.toDto(evento, false));
    }

    public Page<EventoPublicoDTO> findPublicos(Pageable pageable) {
        return eventoRepository.findAllByStatus(EventoStatus.PUBLICADO, pageable)
                .map(evento -> mapper.toPublicoDto(evento, List.of(), inscricoesAbertas(evento)));
    }

    public EventoDTO findAdminById(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Evento não encontrado"));
        EventoDTO dto = mapper.toDto(evento, false);
        dto.setCampos(campoRepository.findByEventoIdOrderByOrdemAscIdAsc(id).stream().map(mapper::toCampoDto).toList());
        return dto;
    }

    public EventoPublicoDTO findPublicoById(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Evento não encontrado"));
        return toPublicoPublicado(evento);
    }

    public EventoPublicoDTO findPublicoBySlug(String slug) {
        Evento evento = eventoRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new ObjectNotFoundException("Evento não encontrado"));
        return toPublicoPublicado(evento);
    }

    @Transactional
    public EventoDTO create(EventoDTO dto) {
        Evento evento = new Evento();
        applyEventoData(evento, dto);
        if (evento.getStatus() == null) {
            evento.setStatus(EventoStatus.RASCUNHO);
        }
        return mapper.toDto(eventoRepository.save(evento), false);
    }

    @Transactional
    public EventoDTO update(Long id, EventoDTO dto) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Evento não encontrado"));
        applyEventoData(evento, dto);
        return mapper.toDto(eventoRepository.save(evento), false);
    }

    @Transactional
    public EventoDTO updateStatus(Long id, EventoStatus status) {
        if (status == null) {
            throw new BusinessRuleException("Status do evento é obrigatório");
        }
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Evento não encontrado"));
        evento.setStatus(status);
        return mapper.toDto(eventoRepository.save(evento), false);
    }

    public boolean inscricoesAbertas(Evento evento) {
        if (evento.getStatus() != EventoStatus.PUBLICADO) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (evento.getInicioInscricoes() != null && now.isBefore(evento.getInicioInscricoes())) {
            return false;
        }
        if (evento.getFimInscricoes() != null && now.isAfter(evento.getFimInscricoes())) {
            return false;
        }
        if (evento.getLimiteInscricoes() != null) {
            long total = inscricaoRepository.countByEventoIdAndStatus(evento.getId(), EventoInscricaoStatus.CONFIRMADA);
            return total < evento.getLimiteInscricoes();
        }
        return true;
    }

    private void applyEventoData(Evento evento, EventoDTO dto) {
        evento.setTitulo(dto.getTitulo());
        evento.setDescricao(dto.getDescricao());
        evento.setImagem(dto.getImagem());
        evento.setLocal(dto.getLocal());
        evento.setDataInicio(dto.getDataInicio());
        evento.setDataFim(dto.getDataFim());
        evento.setInicioInscricoes(dto.getInicioInscricoes());
        evento.setFimInscricoes(dto.getFimInscricoes());
        evento.setLimiteInscricoes(dto.getLimiteInscricoes());
        evento.setSlug(resolveSlug(evento.getId(), dto.getSlug(), dto.getTitulo()));
        if (dto.getStatus() != null) {
            evento.setStatus(dto.getStatus());
        }
        validarEvento(evento);
    }

    private EventoPublicoDTO toPublicoPublicado(Evento evento) {
        if (evento.getStatus() != EventoStatus.PUBLICADO) {
            throw new ObjectNotFoundException("Evento não encontrado");
        }
        List<EventoCampo> campos = campoRepository.findByEventoIdAndAtivoTrueOrderByOrdemAscIdAsc(evento.getId());
        return mapper.toPublicoDto(evento, campos, inscricoesAbertas(evento));
    }

    private void validarEvento(Evento evento) {
        if (evento.getTitulo() == null || evento.getTitulo().isBlank()) {
            throw new BusinessRuleException("Título do evento é obrigatório");
        }
        if (evento.getSlug() == null || evento.getSlug().isBlank()) {
            throw new BusinessRuleException("Slug do evento é obrigatório");
        }
        if (evento.getDataInicio() != null && evento.getDataFim() != null && evento.getDataFim().isBefore(evento.getDataInicio())) {
            throw new BusinessRuleException("Data final do evento não pode ser anterior à data inicial");
        }
        if (evento.getInicioInscricoes() != null && evento.getFimInscricoes() != null && evento.getFimInscricoes().isBefore(evento.getInicioInscricoes())) {
            throw new BusinessRuleException("Fim das inscrições não pode ser anterior ao início das inscrições");
        }
        if (evento.getLimiteInscricoes() != null && evento.getLimiteInscricoes() < 1) {
            throw new BusinessRuleException("Limite de inscrições deve ser maior que zero");
        }
    }

    private String resolveSlug(Long eventoId, String requestedSlug, String titulo) {
        String baseSlug = normalizeSlug(requestedSlug != null && !requestedSlug.isBlank() ? requestedSlug : titulo);
        if (baseSlug.isBlank()) {
            throw new BusinessRuleException("Slug do evento é obrigatório");
        }
        if (eventoId != null) {
            if (eventoRepository.existsBySlugIgnoreCaseAndIdNot(baseSlug, eventoId)) {
                throw new BusinessRuleException("Já existe um evento com este slug");
            }
            return baseSlug;
        }

        String slug = baseSlug;
        int suffix = 2;
        while (eventoRepository.existsBySlugIgnoreCase(slug)) {
            slug = baseSlug + "-" + suffix;
            suffix++;
        }
        return slug;
    }

    private String normalizeSlug(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
