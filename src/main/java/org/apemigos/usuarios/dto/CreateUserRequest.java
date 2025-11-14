package org.apemigos.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apemigos.usuarios.entity.Usuario;

@Data
public class CreateUserRequest {
    
    @NotBlank
    @Size(min = 2, max = 100)
    private String nome;
    
    @Email
    @NotBlank
    private String email;
    
    @NotBlank
    @Size(min = 6)
    private String senha;
    
    private Usuario.UserRole role;
}