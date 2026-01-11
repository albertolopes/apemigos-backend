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
@Schema(name = "PixResponse", description = "Resposta com payload PIX e QR Code base64")
public class PixResponseDTO {

    @Schema(description = "Payload do PIX (texto pronto para gerar o QR Code)")
    private String payload;

    @Schema(description = "QR Code em base64 (data:image/png;base64,...)")
    private String qrCodeBase64;

    @Schema(description = "TXID usado")
    private String txid;
}

