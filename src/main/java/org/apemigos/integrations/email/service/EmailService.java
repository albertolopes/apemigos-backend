package org.apemigos.integrations.email.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apemigos.integrations.email.client.MailgunClient;
import org.apemigos.integrations.email.dto.EmailResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailService {

    private final MailgunClient mailgunClient;


    @Value("${email.mailgun.domain}")
    private String domain;

    @Value("${email.mailgun.from-name}")
    private String fromName ;

    @Async
    @SneakyThrows
    public void sendEmail(String to, String subject, String body, List<MultipartFile> attachments) {
        try {

            String from = String.format("%s <noreply@%s>", fromName, domain);

            MultipartFile[] attachmentsArray = null;
            if (attachments != null && !attachments.isEmpty()) {
                attachmentsArray = attachments.toArray(new MultipartFile[0]);
            }

            EmailResponseDTO response = mailgunClient.sendMessage(
                    domain,
                    from,
                    to,
                    subject,
                    null,
                    body,
                    attachmentsArray
            );

            // 4. Log adequado
            log.info("✅ Email enviado para: {} | Assunto: {} | ID: {} | Message: {}",
                    to, subject, response.getId(), response.getMessage());

        } catch (Exception e) {
            log.error("❌ Erro ao enviar email para: {} | Erro: {}", to, e.getMessage(), e);
            throw e; // Re-throw para o @Async capturar
        }
    }

    public void sendTestEmail() {
        String htmlBody = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; }
                    .container { padding: 20px; background-color: #f4f4f4; }
                    .header { background-color: #4CAF50; color: white; padding: 10px; }
                    .content { padding: 20px; background-color: white; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Teste de Email - Apemigos</h1>
                    </div>
                    <div class="content">
                        <p>Este é um email de teste enviado pelo Spring Boot.</p>
                        <p><strong>Data:</strong> %s</p>
                        <p><strong>Status:</strong> ✅ Funcionando!</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(java.time.LocalDateTime.now());

        sendEmail(
                "allbertollopes@gmail.com",
                "🚀 Teste de Email - Spring Boot",
                htmlBody,
                null
        );
    }
}
