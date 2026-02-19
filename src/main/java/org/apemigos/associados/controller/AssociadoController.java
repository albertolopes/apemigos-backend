package org.apemigos.associados.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apemigos.associados.dto.AssociadoFileResponseDTO;
import org.apemigos.associados.dto.AssociadoRequisicaoDTO;
import org.apemigos.associados.dto.AssociadoResponseDTO;
import org.apemigos.associados.entity.Associado;
import org.apemigos.associados.enums.StatusCarteirinha;
import org.apemigos.associados.service.AssociadoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/associados")
@CrossOrigin(origins = "*")
@Tag(name = "Associados", description = "Gerenciamento de associados")
@RequiredArgsConstructor
public class AssociadoController {

    private final AssociadoService associadoService;

    @Operation(
            summary = "Listar associados",
            description = "Retorna uma lista paginada de associados com filtro opcional por palavra-chave e status"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Associados listados com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping
    public ResponseEntity<Page<AssociadoResponseDTO>> getAllAssociados(
            @Parameter(in = ParameterIn.QUERY, description = "Palavra-chave para busca (nome, sobrenome, cpf, email, cidade)", required = false,
                    schema = @Schema(type = "string", nullable = true))
            @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(in = ParameterIn.QUERY, description = "Filtro por status da carteirinha", required = false,
                    schema = @Schema(implementation = StatusCarteirinha.class, nullable = true))
            @RequestParam(name = "status", required = false) StatusCarteirinha status,
            @Parameter(description = "Número da página (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(associadoService.findAll(status, keyword, pageable));
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Associado> createAssociadoMulti(
            @ModelAttribute AssociadoRequisicaoDTO dto
    ) {
            return ResponseEntity
                    .status(201)
                    .body(associadoService.saveFromMultipart(dto));
    }

    @Operation(
            summary = "Atualizar status da carteirinha",
            description = "Atualiza o status da carteirinha de um associado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Associado não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<AssociadoResponseDTO> updateStatus(
            @Parameter(description = "ID do associado", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody StatusCarteirinha status
    ) {
        return ResponseEntity.ok(associadoService.updateStatus(id, status));
    }

    @Operation(
            summary = "Confirmar entrega da carteirinha",
            description = "Confirma a entrega da carteirinha informando o CPF do associado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrega confirmada ou já realizada"),
            @ApiResponse(responseCode = "404", description = "Associado não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/confirmar-entrega")
    public ResponseEntity<Map<String, String>> confirmarEntrega(
            @RequestBody Map<String, String> payload
    ) {
        String cpf = payload.get("cpf");
        if (cpf == null || cpf.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "CPF é obrigatório"));
        }
        String mensagem = associadoService.confirmarEntregaCarteirinha(cpf);
        return ResponseEntity.ok(Map.of("message", mensagem));
    }

    @Operation(
            summary = "Listar arquivos do associado",
            description = "Retorna uma lista de arquivos de um associado específico pelo ID do associado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Arquivos listados com sucesso",
                    content = @Content(schema = @Schema(implementation = AssociadoFileResponseDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Associado não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/{associadoId}/files")
    public ResponseEntity<List<AssociadoFileResponseDTO>> getAssociadoFiles(
            @Parameter(description = "ID do associado", required = true, example = "1")
            @PathVariable Long associadoId) {
        List<AssociadoFileResponseDTO> files = associadoService.findFilesByAssociadoId(associadoId);
        return ResponseEntity.ok(files);
    }
}
