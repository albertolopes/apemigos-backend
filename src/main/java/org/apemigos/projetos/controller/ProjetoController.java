package org.apemigos.projetos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apemigos.projetos.dto.ProjetoDTO;
import org.apemigos.projetos.service.ProjetoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projetos")
@Tag(name = "Projetos", description = "Gerenciamento de projetos")
@RequiredArgsConstructor
public class ProjetoController {

    private final ProjetoService projetoService;

    @Operation(summary = "Listar todos os projetos", description = "Retorna uma lista paginada de projetos ordenados por id (mais recentes primeiro)")
    @GetMapping
    public ResponseEntity<Page<ProjetoDTO>> getAllProjetos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(projetoService.findAll(pageable));
    }

    @Operation(summary = "Buscar projetos por palavra-chave")
    @GetMapping("/search")
    public ResponseEntity<Page<ProjetoDTO>> searchProjetos(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(projetoService.findByKeyword(keyword, pageable));
    }

    @Operation(summary = "Buscar projeto por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProjetoDTO> getProjetoById(@PathVariable Long id) {
        return ResponseEntity.ok(projetoService.findById(id));
    }

    @Operation(summary = "Buscar projeto por slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProjetoDTO> getProjetoBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(projetoService.findBySlug(slug));
    }

    @Operation(summary = "Criar novo projeto")
    @PostMapping
    public ResponseEntity<ProjetoDTO> createProjeto(@RequestBody ProjetoDTO projeto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projetoService.save(projeto));
    }

    @Operation(summary = "Atualizar projeto")
    @PutMapping("/{id}")
    public ResponseEntity<ProjetoDTO> updateProjeto(@PathVariable Long id, @RequestBody ProjetoDTO projetoDetails) {
        projetoDetails.setId(id);
        return ResponseEntity.ok(projetoService.update(projetoDetails));
    }

    @Operation(summary = "Excluir projeto")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjeto(@PathVariable Long id) {
        if (projetoService.delete(id))
            return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }
}
