package org.apemigos.configuration;

import com.cloudinary.Cloudinary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Slf4j
@Configuration
public class CloudinaryConfig {

    @Value("${integracao.cloudinary.url:}")
    private String cloudinaryUrl;

    @Bean
    public Cloudinary cloudinary() {
        String finalCloudinaryUrl = getCloudinaryUrl();

        if (finalCloudinaryUrl == null || finalCloudinaryUrl.isEmpty() ||
                finalCloudinaryUrl.contains("your_api_key")) {
            log.warn("CLOUDINARY_URL não configurada corretamente");
            log.info("Configure a variável de ambiente CLOUDINARY_URL");
            return createEmptyCloudinary();
        }

        Cloudinary cloudinary = new Cloudinary(finalCloudinaryUrl);
        log.info("Cloudinary configurado");
        return cloudinary;
    }

    private String getCloudinaryUrl() {
        if (cloudinaryUrl != null && !cloudinaryUrl.isEmpty() &&
                !cloudinaryUrl.contains("your_api_key")) {
            log.info("CLOUDINARY_URL do application.yaml encontrado");
            return cloudinaryUrl;
        }

        log.warn("Nenhuma configuração do Cloudinary encontrada");
        return null;
    }

    private Cloudinary createEmptyCloudinary() {
        return new Cloudinary(new HashMap<String, Object>());
    }
}