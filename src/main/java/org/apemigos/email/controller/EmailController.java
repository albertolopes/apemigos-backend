package org.apemigos.email.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apemigos.email.dto.EmailRequestDTO;
import org.apemigos.email.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;

/**
 * Controller responsável pelo envio de emails.
 *
 * Endpoints:
 * - POST /api/email : envia um email (body HTML permitido). Retorna 204 No Content quando enviado.
 */
@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
@Tag(name = "Email", description = "Envio de e-mails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;


    @Operation(summary = "Enviar email", description = "Envia um email (HTML permitido). Retorna 204 No Content em sucesso.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email enviado com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no payload", content = @Content),
            @ApiResponse(responseCode = "418", description = "Erro de integração com provedor de email", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> sendEmail(@Valid @RequestBody EmailRequestDTO request) {
        emailService.sendEmail(request);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping("/test")
    public String sendTestEmail() {
        try {
            emailService.sendTestEmail();
            return "✅ Email de teste enviado! Verifique no Ethereal.";
        } catch (Exception e) {
            return "❌ Erro: " + e.getMessage();
        }
    }
}
