package org.apemigos.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para requisição de login")
public class LoginRequest {

    @Schema(
            description = "Email do usuário (obrigatório para login de usuário)",
            example = "usuario@apemigos.org"
    )
    @Email(message = "O email deve ser válido")
    private String email;

    @Schema(
            description = "Senha do usuário (obrigatório para login de usuário)",
            example = "senha123",
            minLength = 6
    )
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    private String password;

    @Schema(
            description = "Chave de serviço para autenticação como serviço",
            example = "apemigos-service-key-2024"
    )
    private String serviceKey;

    public boolean isUserLogin() {
        return email != null && !email.trim().isEmpty() &&
                password != null && !password.trim().isEmpty();
    }

    public boolean isServiceLogin() {
        return serviceKey != null && !serviceKey.trim().isEmpty();
    }

    public boolean isValid() {
        return isUserLogin() || isServiceLogin();
    }
}