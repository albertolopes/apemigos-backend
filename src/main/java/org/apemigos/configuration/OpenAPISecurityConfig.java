package org.apemigos.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;
import java.util.List;

@Configuration
public class OpenAPISecurityConfig {

    @Value("${spring.application.name:Apemigos Backend}")
    private String appName;

    @Value("${spring.application.description:API para gerenciamento da plataforma Apemigos}")
    private String appDescription;

    @Value("${spring.application.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-key",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira o token JWT no formato: Bearer {token}")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearer-key"))
                .info(new Info()
                        .title(appName)
                        .description(appDescription)
                        .version(appVersion)
                        .contact(new Contact()
                                .name("Suporte Apemigos")
                                .email("suporte@apemigos.org")
                                .url("https://apemigos.org")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                        )
                        .termsOfService("https://apemigos.org/terms")
                        .extensions(Map.of(
                                "x-api-version", "1.0",
                                "x-supported-languages", List.of("pt-BR", "en")
                        ))
                );
    }
}