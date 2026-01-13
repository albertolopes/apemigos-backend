package org.apemigos.integrations.email.client;

import org.apemigos.integrations.email.dto.EmailResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = "mailgun",
        url = "https://api.mailgun.net",  // URL fixa ou do properties
        configuration = {MailgunConfig.class, FeignMultipartConfig.class}
)
public interface MailgunClient {

    @PostMapping(
            value = "/v3/{domain}/messages",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    EmailResponseDTO sendMessage(
            @PathVariable("domain") String domain,
            @RequestPart("from") String from,
            @RequestPart("to") String to,
            @RequestPart("subject") String subject,
            @RequestPart(value = "text", required = false) String text,
            @RequestPart(value = "html", required = false) String html,
            @RequestPart(value = "attachment", required = false) MultipartFile[] attachments
    );
}

