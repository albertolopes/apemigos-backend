package org.apemigos.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "EmailAttachment", description = "Representa um anexo de email (bytes)")
public class EmailAttachment {
    @Schema(description = "Nome do arquivo (ex: laudo.pdf)")
    private String filename;

    @Schema(description = "Content-Type do arquivo (ex: application/pdf)")
    private String contentType;

    @Schema(description = "Conteúdo do arquivo em bytes")
    private byte[] content;
}

