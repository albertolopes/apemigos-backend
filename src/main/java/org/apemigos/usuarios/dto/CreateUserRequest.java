package org.apemigos.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apemigos.usuarios.entity.User;

@Data
public class CreateUserRequest {
    
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;
    
    @Email
    @NotBlank
    private String email;
    
    @NotBlank
    @Size(min = 6)
    private String password;
    
    private User.UserRole role;
}