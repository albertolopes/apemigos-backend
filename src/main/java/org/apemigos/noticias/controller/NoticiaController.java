package org.apemigos.noticias.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apemigos.noticias.dto.NoticiaConteudoDTO;
import org.apemigos.noticias.dto.NoticiaDTO;
import org.apemigos.noticias.service.NoticiaConteudoService;
import org.apemigos.noticias.service.NoticiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/noticias")
@Tag(name = "Notícias", description = "Gerenciamento de notícias")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NoticiaController {

    private final NoticiaService noticiaService;
    private final NoticiaConteudoService noticiaConteudoService;

    @Operation(
            summary = "Listar todas as notícias",
            description = "Retorna uma lista paginada de notícias ordenadas por data (mais recentes primeiro)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notícias listadas com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping
    public ResponseEntity<Page<NoticiaDTO>> getAllNoticias(
            @Parameter(description = "Número da página (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Quantidade de itens por página", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return ResponseEntity.ok(noticiaService.findAll(pageable));
    }

    @Operation(
            summary = "Buscar notícias por palavra-chave",
            description = "Busca notícias por palavra-chave no título, descrição ou slug"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetro de busca inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<NoticiaDTO>> searchNoticias(
            @Parameter(in = ParameterIn.QUERY, description = "Palavra-chave para busca", required = false, allowEmptyValue = true,
                    schema = @Schema(type = "string", nullable = true), example = "esclerose")
            @RequestParam(name = "keyword", required = false) String keyword,

            @Parameter(description = "Número da página (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Quantidade de itens por página", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return ResponseEntity.ok(noticiaService.findByKeyword(keyword, pageable));
    }

    @Operation(
            summary = "Buscar notícia por ID",
            description = "Retorna uma notícia específica pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notícia encontrada",
                    content = @Content(schema = @Schema(implementation = NoticiaDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Notícia não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<NoticiaDTO> getNoticiaById(
            @Parameter(description = "ID da notícia", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(noticiaService.findById(id));
    }

    @Operation(
            summary = "Buscar notícia por slug",
            description = "Retorna uma notícia específica pelo seu slug (URL amigável)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notícia encontrada",
                    content = @Content(schema = @Schema(implementation = NoticiaDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Notícia não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/slug/{slug}")
    public ResponseEntity<NoticiaDTO> getNoticiaBySlug(
            @Parameter(description = "Slug da notícia", required = true, example = "campanha-consciencia")
            @PathVariable String slug) {
        return ResponseEntity.ok(noticiaService.findBySlug(slug));
    }

    @Operation(
            summary = "Criar nova notícia",
            description = "Cria uma nova notícia no sistema"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Notícia criada com sucesso",
                    content = @Content(schema = @Schema(implementation = NoticiaDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados da notícia inválidos"),
            @ApiResponse(responseCode = "409", description = "Slug já existe"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    public ResponseEntity<NoticiaDTO> createNoticia(
            @Parameter(description = "Dados da notícia a ser criada", required = true)
            @RequestBody NoticiaDTO noticia) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noticiaService.save(noticia));
    }

    @Operation(
            summary = "Atualizar notícia",
            description = "Atualiza uma notícia existente"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notícia atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = NoticiaDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Notícia não encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados da notícia inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<NoticiaDTO> updateNoticia(
            @Parameter(description = "ID da notícia a ser atualizada", required = true, example = "1")
            @PathVariable Long id,

            @Parameter(description = "Dados atualizados da notícia", required = true)
            @RequestBody NoticiaDTO noticiaDetails) {
        return ResponseEntity.ok(noticiaService.update(noticiaDetails));
    }

    @Operation(
            summary = "Excluir notícia",
            description = "Exclui uma notícia do sistema"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notícia excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Notícia não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoticia(
            @Parameter(description = "ID da notícia a ser excluída", required = true, example = "1")
            @PathVariable Long id) {
        if (noticiaService.delete(id))
            return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Buscar conteúdo por ID da notícia",
            description = "Retorna o conteúdo completo de uma notícia específica pelo ID da notícia"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conteúdo encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = NoticiaConteudoDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Conteúdo não encontrado para a notícia"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/conteudo/{noticiaId}")
    public ResponseEntity<NoticiaConteudoDTO> getByNoticiaId(
            @Parameter(description = "ID da notícia", required = true, example = "1")
            @PathVariable Long noticiaId) {
        NoticiaConteudoDTO conteudo = noticiaConteudoService.findByNoticiaId(noticiaId);
        return ResponseEntity.ok(conteudo);
    }

    @Operation(
            summary = "Buscar conteúdo por slug da notícia",
            description = "Retorna o conteúdo completo de uma notícia específica pelo slug (URL amigável) da notícia"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conteúdo encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = NoticiaConteudoDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Conteúdo não encontrado para a notícia"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/conteudo/slug/{slug}")
    public ResponseEntity<NoticiaConteudoDTO> getByNoticiaSlug(
            @Parameter(description = "Slug da notícia", required = true, example = "campanha-consciencia")
            @PathVariable String slug) {
        NoticiaConteudoDTO conteudo = noticiaConteudoService.findByNoticiaSlug(slug);
        return ResponseEntity.ok(conteudo);
    }

    @Operation(
            summary = "Criar novo conteúdo",
            description = "Cria um novo conteúdo para uma notícia. Cada notícia pode ter apenas um conteúdo."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conteúdo criado com sucesso",
                    content = @Content(schema = @Schema(implementation = NoticiaConteudoDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados do conteúdo inválidos"),
            @ApiResponse(responseCode = "404", description = "Notícia não encontrada"),
            @ApiResponse(responseCode = "409", description = "Já existe conteúdo para esta notícia"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/conteudo")
    public ResponseEntity<NoticiaConteudoDTO> createContent(
            @Parameter(description = "Dados do conteúdo a ser criado", required = true)
            @RequestBody NoticiaConteudoDTO conteudoDTO) {
        NoticiaConteudoDTO saved = noticiaConteudoService.save(conteudoDTO);
        return ResponseEntity.ok(saved);
    }

    @Operation(
            summary = "Atualizar conteúdo completo",
            description = "Atualiza todo o conteúdo de uma notícia específica pelo ID da notícia"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conteúdo atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = NoticiaConteudoDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Conteúdo não encontrado para a notícia"),
            @ApiResponse(responseCode = "400", description = "Dados do conteúdo inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PutMapping("/conteudo/{noticiaId}")
    public ResponseEntity<NoticiaConteudoDTO> updateContent(
            @Parameter(description = "ID da notícia", required = true, example = "1")
            @PathVariable Long noticiaId,

            @Parameter(description = "Dados atualizados do conteúdo", required = true)
            @RequestBody NoticiaConteudoDTO conteudoDTO) {
        NoticiaConteudoDTO updated = noticiaConteudoService.update(noticiaId, conteudoDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Atualizar apenas o texto do conteúdo",
            description = "Atualiza apenas o texto (longDescription) do conteúdo de uma notícia específica"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Texto do conteúdo atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = NoticiaConteudoDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Conteúdo não encontrado para a notícia"),
            @ApiResponse(responseCode = "400", description = "Texto do conteúdo inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PatchMapping("/conteudo/{noticiaId}")
    public ResponseEntity<NoticiaConteudoDTO> updateContentText(
            @Parameter(description = "ID da notícia", required = true, example = "1")
            @PathVariable Long noticiaId,

            @Parameter(
                    description = "Novo texto do conteúdo em formato HTML",
                    required = true,
                    example = "<p>Novo conteúdo da notícia...</p>"
            )
            @RequestBody String longDescription) {
        NoticiaConteudoDTO updated = noticiaConteudoService.updateByNoticiaId(noticiaId, longDescription);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Excluir conteúdo por ID",
            description = "Exclui um conteúdo específico pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conteúdo excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conteúdo não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/conteudo/{id}")
    public ResponseEntity<Void> deleteContent(
            @Parameter(description = "ID do conteúdo a ser excluído", required = true, example = "1")
            @PathVariable Long id) {
        noticiaConteudoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Excluir conteúdo por ID da notícia",
            description = "Exclui o conteúdo de uma notícia específica pelo ID da notícia"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conteúdo excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conteúdo não encontrado para a notícia"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/conteudo/{noticiaId}")
    public ResponseEntity<Void> deleteContentByNoticiaId(
            @Parameter(description = "ID da notícia", required = true, example = "1")
            @PathVariable Long noticiaId) {
        noticiaConteudoService.deleteByNoticiaId(noticiaId);
        return ResponseEntity.noContent().build();
    }
}