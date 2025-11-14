package org.apemigos.usuarios.service;

import lombok.RequiredArgsConstructor;
import org.apemigos.usuarios.dto.CreateUserRequest;
import org.apemigos.usuarios.entity.User;
import org.apemigos.usuarios.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já está em uso");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : User.UserRole.USER)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public User updateUser(Long id, CreateUserRequest request) {
        User user = getUserById(id);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        user.setIsActive(false);
        userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}