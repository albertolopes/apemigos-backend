package org.apemigos.integrations.cloudinary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apemigos.integrations.cloudinary.dto.CloudinaryUploadDTO;
import org.apemigos.integrations.cloudinary.service.CloudinaryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Image Management", description = "APIs para gerenciamento de imagens no Cloudinary")
public class ImageController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload de imagem",
            description = "Faz upload de uma imagem para o Cloudinary. Formatos suportados: JPEG, PNG, GIF, WebP. Tamanho máximo: 10MB."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = UploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou muito grande"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<CloudinaryUploadDTO> uploadImage(
            @Parameter(description = "Arquivo de imagem", required = true)
            MultipartFile file,

            @Parameter(description = "Pasta de destino no Cloudinary", example = "noticias")
            @RequestParam(value = "folder", defaultValue = "noticias") String folder) {

        return ResponseEntity.ok(cloudinaryService.uploadImagem(file, folder));
    }

    @PostMapping("/upload-from-url")
    @Operation(
            summary = "Upload de imagem a partir de URL",
            description = "Faz upload de uma imagem para o Cloudinary a partir de uma URL externa."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "URL inválida"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<CloudinaryUploadDTO> uploadImageFromUrl(
            @Parameter(description = "URL da imagem", required = true, example = "https://exemplo.com/imagem.jpg")
            @RequestParam("url") String imageUrl,

            @Parameter(description = "Pasta de destino no Cloudinary", example = "noticias")
            @RequestParam(value = "folder", defaultValue = "noticias") String folder) {

        return ResponseEntity.ok(cloudinaryService.uploadImageFromUrl(imageUrl, folder));
    }

    @GetMapping("/details/{publicId}")
    @Operation(
            summary = "Obter detalhes da imagem",
            description = "Retorna informações detalhadas sobre uma imagem específica, incluindo análise de qualidade."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalhes recuperados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Imagem não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<?> getImageDetails(
            @Parameter(description = "Public ID da imagem no Cloudinary", required = true, example = "abc123")
            @PathVariable String publicId) {

        return ResponseEntity.ok(cloudinaryService.getImageDetails(publicId));
    }

    @GetMapping("/transform/{publicId}")
    @Operation(
            summary = "Gerar imagem transformada",
            description = "Gera uma URL para uma versão transformada da imagem (redimensionada e ajustada)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL transformada gerada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "404", description = "Imagem não encontrada")
    })
    public ResponseEntity<?> getTransformedImage(
            @Parameter(description = "Public ID da imagem no Cloudinary", required = true, example = "abc123")
            @PathVariable String publicId,

            @Parameter(description = "Largura da imagem transformada", example = "300")
            @RequestParam(defaultValue = "300") int width,

            @Parameter(description = "Altura da imagem transformada", example = "400")
            @RequestParam(defaultValue = "400") int height) {

        return ResponseEntity.ok(cloudinaryService.generateTransformedImage(publicId, width, height));
    }

    @DeleteMapping("/delete")
    @Operation(
            summary = "Deletar imagem",
            description = "Remove uma imagem do Cloudinary usando sua URL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagem deletada com sucesso"),
            @ApiResponse(responseCode = "400", description = "URL inválida"),
            @ApiResponse(responseCode = "404", description = "Imagem não encontrada")
    })
    public ResponseEntity<?> deleteImage(
            @Parameter(description = "URL completa da imagem no Cloudinary", required = true,
                    example = "https://res.cloudinary.com/cloudname/image/upload/v123/abc123.jpg")
            @RequestParam("url") String imageUrl) {

        return ResponseEntity.ok(cloudinaryService.deleteImage(imageUrl));
    }

    @GetMapping("/health")
    @Operation(
            summary = "Health check do serviço",
            description = "Verifica o status de conectividade com o Cloudinary."
    )
    @ApiResponse(responseCode = "200", description = "Status do serviço retornado")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(cloudinaryService.healthCheck());
    }

    // ========== CLASSES SCHEMA PARA SWAGGER ==========

    @Schema(name = "UploadResponse", description = "Resposta de upload bem-sucedido")
    public static class UploadResponse {
        @Schema(description = "Indica sucesso na operação", example = "true")
        public boolean success;

        @Schema(description = "URL pública da imagem", example = "https://res.cloudinary.com/cloud/image/upload/v123/abc.jpg")
        public String url;

        @Schema(description = "Public ID da imagem", example = "abc123")
        public String public_id;

        @Schema(description = "Pasta onde a imagem foi salva", example = "noticias")
        public String folder;

        @Schema(description = "Mensagem de sucesso", example = "Imagem salva com sucesso")
        public String message;
    }

    @Schema(name = "ErrorResponse", description = "Resposta de erro")
    public static class ErrorResponse {
        @Schema(description = "Indica falha na operação", example = "false")
        public boolean success;

        @Schema(description = "Mensagem de erro", example = "Arquivo muito grande")
        public String error;

        @Schema(description = "Timestamp do erro", example = "1672531200000")
        public long timestamp;
    }

    @Schema(name = "HealthResponse", description = "Resposta de health check")
    public static class HealthResponse {
        @Schema(description = "Nome do serviço", example = "cloudinary")
        public String service;

        @Schema(description = "Indica se o serviço está configurado", example = "true")
        public boolean configured;

        @Schema(description = "Status do serviço", example = "healthy")
        public String status;

        @Schema(description = "Nome do cloud no Cloudinary", example = "apemigos-app")
        public String cloud_name;

        @Schema(description = "Timestamp da verificação", example = "1672531200000")
        public long timestamp;
    }
}