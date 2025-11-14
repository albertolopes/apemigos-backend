package org.apemigos.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    // Chave secreta para assinar os tokens JWT (mínimo 32 caracteres)
    private String secret;

    // Tempo de expiração do token de usuário em milissegundos (padrão: 24 horas)
    private Long expirationMs = 86400000L;

    // Tempo de expiração do token de serviço em milissegundos (padrão: 1 ano)
    private Long serviceExpirationMs = 31536000000L;

    // Chave de serviço para autenticação de tokens de serviço
    private String serviceKey;

    // Issuer do token (opcional)
    private String issuer;

    // Prefixo do token no header (padrão: "Bearer ")
    private String tokenPrefix = "Bearer ";

    // Header onde o token será enviado (padrão: "Authorization")
    private String header = "Authorization";

    public Duration getExpirationDuration() {
        return Duration.ofMillis(expirationMs);
    }

    public Duration getServiceExpirationDuration() {
        return Duration.ofMillis(serviceExpirationMs);
    }

    public boolean isSecretValid() {
        return secret != null && secret.length() >= 32;
    }

    public Long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    public Long getServiceExpirationSeconds() {
        return serviceExpirationMs / 1000;
    }
}