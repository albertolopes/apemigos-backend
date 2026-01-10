package org.apemigos.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "EmailRequest", description = "Payload para envio de email")
public class EmailRequestDTO {

    @NotBlank
    @Email
    @Schema(description = "Endereço de email do destinatário", example = "contato@exemplo.com")
    private String to;

    @NotBlank
    @Schema(description = "Assunto do email", example = "Bem-vindo à Apemigos")
    private String subject;

    @NotBlank
    @Schema(description = "Corpo do email (HTML permitido)", example = "<p>Olá, este é um email de teste</p>")
    private String body;

}
