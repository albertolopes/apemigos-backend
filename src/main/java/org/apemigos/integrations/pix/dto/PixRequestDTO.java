package org.apemigos.integrations.pix.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PixRequest", description = "Dados para gerar um QR Code PIX estático")
public class PixRequestDTO {

    @Schema(description = "Valor a ser cobrado (ex: 25.50)")
    private Double amount;
}
