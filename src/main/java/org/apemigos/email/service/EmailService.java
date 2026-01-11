package org.apemigos.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apemigos.email.dto.EmailAttachment;
import org.apemigos.email.dto.EmailRequestDTO;
import org.apemigos.exceptions.IntegrationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private static final String DEFAULT_FROM = "55-61981181419.527@zohomail.com";
    private static final int MAIL_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

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
        sendEmail(to, subject, body, isHtml, null, null);
    }

    @SneakyThrows
    public void sendEmail(String to, String subject, String body, boolean isHtml, List<String> attachmentUrls) {
        sendEmail(to, subject, body, isHtml, attachmentUrls, null);
    }

    @Async
    @SneakyThrows
    public void sendEmail(String to, String subject, String body, boolean isHtml, List<String> attachmentUrls, List<EmailAttachment> attachments) {
        MimeMessage message = mailSender.createMimeMessage();
        // multipart = true para permitir anexos e HTML
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(DEFAULT_FROM);
        helper.setTo(to);
        helper.setSubject(subject);

        String finalBody = body;

        // Se houver attachments (URLs geradas pelo Cloudinary), adicionamos uma seção ao final do HTML com links
        if (attachmentUrls != null && !attachmentUrls.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(body == null ? "" : body);
            sb.append("<hr/><p><strong>Anexos:</strong></p><ul>");
            for (String url : attachmentUrls) {
                sb.append("<li><a href=\"").append(url).append("\">").append(url).append("</a></li>");
            }
            sb.append("</ul>");
            finalBody = sb.toString();
        }

        helper.setText(finalBody, isHtml);

        // Anexar arquivos físicos caso tenham sido fornecidos
        if (attachments != null && !attachments.isEmpty()) {
            for (EmailAttachment att : attachments) {
                if (att != null && att.getContent() != null) {
                    try {
                        helper.addAttachment(att.getFilename(), new org.springframework.core.io.ByteArrayResource(att.getContent()), att.getContentType());
                    } catch (MessagingException me) {
                        log.error("Falha ao anexar arquivo {}: {}", att.getFilename(), me.getMessage());
                        throw new IntegrationException("Falha ao anexar arquivo: " + att.getFilename(), me);
                    }
                }
            }
        }

        // Ajusta timeouts para o JavaMailSenderImpl (se for a implementação)
        if (mailSender instanceof JavaMailSenderImpl) {
            try {
                JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;
                Properties props = impl.getJavaMailProperties();
                props.put("mail.smtp.connectiontimeout", String.valueOf(MAIL_TIMEOUT_MS));
                props.put("mail.smtp.timeout", String.valueOf(MAIL_TIMEOUT_MS));
                // Some SMTP providers support write timeout
                props.put("mail.smtp.writetimeout", String.valueOf(MAIL_TIMEOUT_MS));
                // Also set debug optional
                // props.put("mail.debug", "true");
            } catch (Exception e) {
                log.warn("Não foi possível ajustar timeouts do JavaMailSender: {}", e.getMessage());
            }
        }

        mailSender.send(message);
        log.info("Email enviado para: {} (anexosUrls={}, anexosBytes={})", to, (attachmentUrls == null ? 0 : attachmentUrls.size()), (attachments == null ? 0 : attachments.size()));
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
                htmlBody
        );
    }
}
