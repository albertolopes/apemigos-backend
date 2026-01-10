package org.apemigos.auth.dto;

import org.apemigos.usuarios.entity.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para resposta de autenticação JWT")
public class JwtResponse {

    @Schema(description = "Token JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Tipo do token", example = "Bearer")
    private String type = "Bearer";

    @Schema(description = "ID do usuário (null para serviços)", example = "1")
    private Long id;

    @Schema(description = "Email do usuário ou serviço", example = "usuario@apemigos.org")
    private String email;

    @Schema(description = "Nome do usuário ou serviço", example = "João Silva")
    private String name;

    @Schema(description = "Role do usuário ou serviço", example = "USER")
    private Usuario.UserRole role;

    @Schema(description = "Indica se é um token de serviço", example = "false")
    private boolean isService;

    // Construtor para usuário normal
    public JwtResponse(String token, Usuario user) {
        this.token = token;
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getNome();
        this.role = user.getRole();
        this.isService = false;
    }

    // Construtor para usuário normal com todos os campos
    public JwtResponse(String token, Long id, String email, String name, Usuario.UserRole role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.isService = false;
    }

    // Construtor para serviço
    public static JwtResponse forService(String token, String email, String name) {
        JwtResponse response = new JwtResponse();
        response.setToken(token);
        response.setId(null);
        response.setEmail(email);
        response.setName(name);
        response.setRole(Usuario.UserRole.SERVICE);
        response.setService(true);
        return response;
    }

    // Construtor para serviço com role customizada
    public static JwtResponse forService(String token, String email, String name, Usuario.UserRole role) {
        JwtResponse response = new JwtResponse();
        response.setToken(token);
        response.setId(null);
        response.setEmail(email);
        response.setName(name);
        response.setRole(role);
        response.setService(true);
        return response;
    }

    // Método utilitário para verificar se é usuário
    public boolean isUser() {
        return !isService && id != null;
    }

    // Método utilitário para verificar se é serviço
    public boolean isService() {
        return isService;
    }
}