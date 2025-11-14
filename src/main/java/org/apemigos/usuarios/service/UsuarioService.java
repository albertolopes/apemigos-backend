package org.apemigos.usuarios.service;

import lombok.RequiredArgsConstructor;
import org.apemigos.usuarios.dto.CreateUserRequest;
import org.apemigos.usuarios.entity.Usuario;
import org.apemigos.usuarios.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já está em uso");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senhaHash(passwordEncoder.encode(request.getSenha()))
                .role(request.getRole() != null ? request.getRole() : Usuario.UserRole.USUARIO)
                .isActive(true)
                .build();

        return userRepository.save(usuario);
    }

    public List<Usuario> getAllUsers() {
        return userRepository.findAll();
    }

    public Usuario getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public Usuario updateUser(Long id, CreateUserRequest request) {
        Usuario user = getUserById(id);
        user.setNome(request.getNome());
        user.setEmail(request.getEmail());
        
        if (request.getSenha() != null && !request.getSenha().isEmpty()) {
            user.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        }
        
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        Usuario user = getUserById(id);
        user.setIsActive(false);
        userRepository.save(user);
    }

    public Usuario getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}