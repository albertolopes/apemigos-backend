package org.apemigos.email.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apemigos.email.dto.EmailRequestDTO;
import org.apemigos.exceptions.IntegrationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private static final String DEFAULT_FROM = "55-61981181419.527@zohomail.com";

    @SneakyThrows
    public void sendEmail(EmailRequestDTO request) {
        sendEmail(request.getTo(), request.getSubject(), request.getBody());
    }

    @SneakyThrows
    public void sendEmail(String to, String subject, String body) {
        sendEmail(to, subject, body, true); // true = HTML por padrão
    }

    @SneakyThrows
    public void sendEmail(String to, String subject, String body, boolean isHtml) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(DEFAULT_FROM);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, isHtml);

        mailSender.send(message);
        log.info("Email enviado para: {}", to);
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
                "seu_email_pessoal@gmail.com",
                "🚀 Teste de Email - Spring Boot",
                htmlBody
        );
    }
}
