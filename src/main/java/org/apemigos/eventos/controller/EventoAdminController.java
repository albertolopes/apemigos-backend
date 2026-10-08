package org.apemigos.eventos.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apemigos.eventos.dto.EventoCampoDTO;
import org.apemigos.eventos.dto.EventoDTO;
import org.apemigos.eventos.dto.EventoInscricaoDTO;
import org.apemigos.eventos.enums.EventoStatus;
import org.apemigos.eventos.service.EventoCampoService;
import org.apemigos.eventos.service.EventoInscricaoService;
import org.apemigos.eventos.service.EventoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/eventos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Eventos Admin", description = "Gerenciamento administrativo de eventos")
public class EventoAdminController {

    private final EventoService eventoService;
    private final EventoCampoService campoService;
    private final EventoInscricaoService inscricaoService;

    @GetMapping
    public ResponseEntity<Page<EventoDTO>> findEventos(
            @RequestParam(required = false) EventoStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(eventoService.findAdmin(status, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoDTO> findEvento(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.findAdminById(id));
    }

    @PostMapping
    public ResponseEntity<EventoDTO> createEvento(@RequestBody EventoDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoDTO> updateEvento(@PathVariable Long id, @RequestBody EventoDTO request) {
        return ResponseEntity.ok(eventoService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EventoDTO> updateStatus(@PathVariable Long id, @RequestBody EventoStatus status) {
        return ResponseEntity.ok(eventoService.updateStatus(id, status));
    }

    @GetMapping("/{eventoId}/campos")
    public ResponseEntity<List<EventoCampoDTO>> findCampos(@PathVariable Long eventoId) {
        return ResponseEntity.ok(campoService.findByEvento(eventoId));
    }

    @PutMapping("/{eventoId}/campos")
    public ResponseEntity<List<EventoCampoDTO>> replaceCampos(
            @PathVariable Long eventoId,
            @RequestBody List<EventoCampoDTO> campos
    ) {
        return ResponseEntity.ok(campoService.replaceCampos(eventoId, campos));
    }

    @GetMapping("/{eventoId}/inscricoes")
    public ResponseEntity<Page<EventoInscricaoDTO>> findInscricoes(
            @PathVariable Long eventoId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(inscricaoService.findByEvento(eventoId, keyword, cpf, email, telefone, dataInicio, dataFim, pageable));
    }

    @GetMapping("/{eventoId}/inscricoes/{inscricaoId}")
    public ResponseEntity<EventoInscricaoDTO> findInscricao(@PathVariable Long eventoId, @PathVariable Long inscricaoId) {
        return ResponseEntity.ok(inscricaoService.findById(eventoId, inscricaoId));
    }

    @GetMapping("/{eventoId}/inscricoes/count")
    public ResponseEntity<Map<String, Long>> countInscricoes(@PathVariable Long eventoId) {
        return ResponseEntity.ok(Map.of("total", inscricaoService.countByEvento(eventoId)));
    }
}
