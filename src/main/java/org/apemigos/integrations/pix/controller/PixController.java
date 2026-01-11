package org.apemigos.integrations.pix.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apemigos.integrations.pix.dto.PixRequestDTO;
import org.apemigos.integrations.pix.dto.PixResponseDTO;
import org.apemigos.integrations.pix.service.PixService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pix")
@RequiredArgsConstructor
@Tag(name = "PIX", description = "Geração de QR Codes PIX estáticos")
public class PixController {

    private final PixService pixService;

    @Operation(summary = "Gera um QR Code PIX estático para um valor específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR Code gerado com sucesso", content = @Content(schema = @Schema(implementation = PixResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno")
    })
    @PostMapping(value = "/static", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PixResponseDTO> generateStatic(@RequestBody PixRequestDTO request) throws Exception {
        PixResponseDTO response = pixService.generateStaticPix(request);
        return ResponseEntity.ok(response);
    }
}

