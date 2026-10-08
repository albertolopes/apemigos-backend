package org.apemigos.eventos.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apemigos.eventos.dto.EventoInscricaoDTO;
import org.apemigos.eventos.dto.EventoInscricaoRequestDTO;
import org.apemigos.eventos.dto.EventoPublicoDTO;
import org.apemigos.eventos.service.EventoInscricaoService;
import org.apemigos.eventos.service.EventoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/eventos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Eventos Públicos", description = "Consulta pública de eventos e inscrições")
public class EventoPublicController {

    private final EventoService eventoService;
    private final EventoInscricaoService inscricaoService;

    @GetMapping
    public ResponseEntity<Page<EventoPublicoDTO>> findEventosPublicos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataInicio").ascending().and(Sort.by("id").descending()));
        return ResponseEntity.ok(eventoService.findPublicos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoPublicoDTO> findEventoPublico(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.findPublicoById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<EventoPublicoDTO> findEventoPublicoBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(eventoService.findPublicoBySlug(slug));
    }

    @GetMapping("/{id}/formulario")
    public ResponseEntity<EventoPublicoDTO> findFormulario(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.findPublicoById(id));
    }

    @GetMapping("/slug/{slug}/formulario")
    public ResponseEntity<EventoPublicoDTO> findFormularioBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(eventoService.findPublicoBySlug(slug));
    }

    @PostMapping("/{id}/inscricoes")
    public ResponseEntity<EventoInscricaoDTO> createInscricao(
            @PathVariable Long id,
            @RequestBody EventoInscricaoRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inscricaoService.createPublica(id, request));
    }
}
