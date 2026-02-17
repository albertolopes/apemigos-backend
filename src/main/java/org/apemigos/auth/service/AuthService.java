package org.apemigos.auth.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apemigos.auth.dto.JwtResponse;
import org.apemigos.auth.dto.LoginRequest;
import org.apemigos.configuration.JwtProperties;
import org.apemigos.usuarios.entity.Usuario;
import org.apemigos.usuarios.repository.UserRepository;
import org.apemigos.auth.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    private final Map<String, Instant> failedServiceAttempts = new HashMap<>();
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    public JwtResponse authenticate(LoginRequest request) {
        if (request.getServiceKey() != null && !request.getServiceKey().trim().isEmpty()) {
            return authenticateService(request.getServiceKey());
        }

        // Autenticação de usuário
        return authenticateUser(request);
    }

    public JwtResponse refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token inválido ou ausente");
        }

        String token = authHeader.substring(7);
        Claims claims = jwtUtil.extractClaimsFromExpiredToken(token);

        Boolean isService = claims.get("isService", Boolean.class);
        if (Boolean.TRUE.equals(isService)) {
            String newToken = jwtUtil.generateSecureServiceToken();
            return JwtResponse.forService(newToken, "service@apemigos.org", "Service Account");
        } else {
            Long userId = claims.get("id", Long.class);
            Usuario user = userRepository.findById(userId)
                    .orElseThrow(() -> new SecurityException("Usuário não encontrado"));

            if (!user.getIsActive()) {
                throw new SecurityException("Usuário inativo");
            }

            String newToken = jwtUtil.generateSecureUserToken(user);
            return new JwtResponse(newToken, user);
        }
    }

    private JwtResponse authenticateService(String serviceKey) {

        checkServiceRateLimit(serviceKey);

        try {
            // Verificação de timing constante para prevenir timing attacks
            if (!constantTimeEquals(serviceKey, jwtProperties.getServiceKey())) {
                recordFailedServiceAttempt(serviceKey);
                log.warn("Tentativa de autenticação de serviço com chave inválida");
                throw new SecurityException("Chave de serviço inválida");
            }

            String token = jwtUtil.generateSecureServiceToken();

            failedServiceAttempts.remove(serviceKey);

            log.info("Autenticação de serviço bem-sucedida");
            return JwtResponse.forService(
                    token,
                    "service@apemigos.org",
                    "Service Account"
            );

        } catch (Exception e) {
            log.error("Erro na autenticação de serviço: {}", e.getMessage());
            throw new SecurityException("Falha na autenticação de serviço");
        }
    }

    private JwtResponse authenticateUser(LoginRequest request) {
        // Validações de entrada
        if (request.getEmail() == null || request.getEmail().trim().isEmpty() ||
                request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Para autenticação de usuário, forneça email e senha válidos");
        }

        // Sanitização de entrada
        String email = request.getEmail().trim().toLowerCase();
        String password = request.getPassword();

        try {
            Usuario user = userRepository.findByEmailAndIsActiveTrue(email)
                    .orElseThrow(() -> {
                        log.warn("Tentativa de login com email não encontrado: {}", email);
                        return new SecurityException("Credenciais inválidas");
                    });

            // Verificação de senha com timing constante
            if (!passwordEncoder.matches(password, user.getSenhaHash())) {
                log.warn("Tentativa de login com senha inválida para usuário: {}", email);
                throw new SecurityException("Credenciais inválidas");
            }

            // Gera token seguro
            String token = jwtUtil.generateSecureUserToken(user);

            log.info("Autenticação de usuário bem-sucedida: {}", email);
            return new JwtResponse(token, user);

        } catch (SecurityException e) {
            throw e; // Re-lança exceções de segurança
        } catch (Exception e) {
            log.error("Erro inesperado na autenticação de usuário: {}", e.getMessage());
            throw new SecurityException("Falha na autenticação");
        }
    }

    /**
     * Comparação de tempo constante para prevenir timing attacks
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();

        if (aBytes.length != bBytes.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }

    /**
     * Verificação de rate limiting para serviços
     */
    private void checkServiceRateLimit(String serviceKey) {
        Instant lastFailedAttempt = failedServiceAttempts.get(serviceKey);
        if (lastFailedAttempt != null) {
            long minutesSinceLastAttempt = java.time.Duration.between(lastFailedAttempt, Instant.now()).toMinutes();

            // Conta tentativas falhas recentes
            long recentFailures = failedServiceAttempts.entrySet().stream()
                    .filter(entry -> entry.getKey().equals(serviceKey))
                    .filter(entry -> java.time.Duration.between(entry.getValue(), Instant.now()).toMinutes() < LOCKOUT_DURATION_MINUTES)
                    .count();

            if (recentFailures >= MAX_FAILED_ATTEMPTS) {
                log.warn("Bloqueio de autenticação de serviço devido a excesso de tentativas: {}", serviceKey);
                throw new SecurityException("Muitas tentativas falhas. Tente novamente em " + LOCKOUT_DURATION_MINUTES + " minutos.");
            }
        }
    }

    /**
     * Registra tentativa falha de serviço
     */
    private void recordFailedServiceAttempt(String serviceKey) {
        failedServiceAttempts.put(serviceKey, Instant.now());

        // Limpa entradas antigas periodicamente
        if (failedServiceAttempts.size() > 1000) {
            failedServiceAttempts.entrySet().removeIf(entry ->
                    java.time.Duration.between(entry.getValue(), Instant.now()).toMinutes() > LOCKOUT_DURATION_MINUTES * 2
            );
        }
    }

    /**
     * Método para limpar tentativas falhas (útil para testes)
     */
    public void clearFailedAttempts() {
        failedServiceAttempts.clear();
    }
}