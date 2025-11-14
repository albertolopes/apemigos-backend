package org.apemigos.auth.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apemigos.auth.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Verifica se o header Authorization está presente e no formato correto
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // Validação adicional: token não pode estar vazio
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token JWT vazio ou em branco");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Validação segura do token
            if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                // Validações adicionais de segurança
                if (username == null || username.trim().isEmpty() || role == null || role.trim().isEmpty()) {
                    log.warn("Token JWT com claims inválidos: username={}, role={}", username, role);
                    filterChain.doFilter(request, response);
                    return;
                }

                // Cria a autenticação com as roles
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                // Define no contexto de segurança
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Usuário autenticado via JWT: {} com role: {}", username, role);

            } else {
                log.warn("Token JWT inválido ou expirado");
            }

        } catch (SecurityException e) {
            log.warn("Falha de segurança na autenticação JWT: {}", e.getMessage());
            // Limpa o contexto de segurança em caso de erro
            SecurityContextHolder.clearContext();

        } catch (Exception e) {
            log.error("Erro inesperado durante a autenticação JWT: {}", e.getMessage());
            // Limpa o contexto de segurança
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Define URLs que não precisam de autenticação JWT
        String path = request.getServletPath();
        return path.startsWith("/api/auth/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/webjars/") ||
                path.equals("/swagger-ui.html");
    }
}