package org.apemigos.integrations.email.client;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MailgunConfig {

    @Value("${email.mailgun.api-key}")
    private String apiKey;

    @Bean
    public BasicAuthRequestInterceptor mailgunAuthInterceptor() {
        return new BasicAuthRequestInterceptor("api", apiKey);
    }
}

