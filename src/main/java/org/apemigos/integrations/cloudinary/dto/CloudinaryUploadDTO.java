package org.apemigos.integrations.cloudinary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "CloudinaryUploadResult", description = "Resultado tipado do upload para o Cloudinary")
public class CloudinaryUploadDTO {

    @Schema(description = "Indica sucesso na operação", example = "true")
    private boolean success;

    @Schema(description = "URL pública do recurso", example = "https://res.cloudinary.com/cloud/image/upload/v123/abc.jpg")
    private String url;

    @Schema(description = "Public ID do recurso", example = "abc123")
    private String publicId;

    @Schema(description = "Pasta onde o recurso foi salvo", example = "noticias")
    private String folder;

    @Schema(description = "Mensagem de status", example = "Imagem salva com sucesso")
    private String message;

    @Schema(description = "Tamanho do arquivo (bytes)")
    private Long fileSize;

    @Schema(description = "Tamanho original (bytes), quando aplicável")
    private Long originalSize;

    @Schema(description = "Tamanho otimizado (bytes), quando aplicável")
    private Long optimizedSize;

    @Schema(description = "Taxa de compressão aproximada (%)", example = "45")
    private Integer compressionRatio;

    @Schema(description = "Formato otimizado (ex: jpeg)")
    private String optimizedFormat;

    @Schema(description = "Content type do recurso")
    private String contentType;

    @Schema(description = "Nome do cloud", example = "apemigos")
    private String cloudName;
}

