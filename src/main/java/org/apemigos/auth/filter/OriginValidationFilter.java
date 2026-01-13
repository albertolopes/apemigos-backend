package org.apemigos.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class OriginValidationFilter extends OncePerRequestFilter {

    @Value("${frontend.require-origin-validation:true}")
    private boolean requireOriginValidation;

    @Value("${ALLOWED_SERVICE_TOKENS:}")
    private String allowedServiceTokensConfig;

    @Value("${REQUIRE_X_SERVICE_TOKEN_FOR_AUTH:true}")
    private boolean requireXServiceTokenForAuth;

    private final AtomicReference<List<String>> allowedServiceTokensRef = new AtomicReference<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!requireOriginValidation) {
            filterChain.doFilter(request, response);
            return;
        }

        if (allowedServiceTokensRef.get() == null) {
            initializeAllowedServiceTokens();
        }

        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");
        String requestURI = request.getRequestURI();

        // Allow documentation/public endpoints without token/origin validation
        if (isPublicEndpoint(requestURI)) {
            // handle preflight for docs
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                if (origin != null && !origin.isBlank()) {
                    setCorsHeadersForOrigin(response, origin);
                }
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }
            if (origin != null && !origin.isBlank()) {
                setCorsHeadersForOrigin(response, origin);
            }
            filterChain.doFilter(request, response);
            return;
        }

        // If Authorization header present and config requires X-Service-Token for auth, enforce token
        if (requireXServiceTokenForAuth && hasAuthorizationHeader(request)) {
            if (!isValidServiceToken(request)) {
                log.warn("Request with Authorization rejected: missing or invalid X-Service-Token (IP={})", getClientIp(request));
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Missing or invalid service token\"}");
                return;
            }
        }

        // Require valid service token for any non-public request
        if (isValidServiceToken(request)) {
            // echo CORS for requests coming from browsers (optional)
            if (origin != null && !origin.isBlank()) {
                setCorsHeadersForOrigin(response, origin);
            }
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("Requisição bloqueada - sem service token válido (IP: {})", getClientIp(request));
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Origem não permitida\"}");
    }

    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/api/auth/") ||
                requestURI.startsWith("/swagger-ui/") ||
                requestURI.startsWith("/swagger-ui.html") ||
                requestURI.startsWith("/swagger-ui/index.html") ||
                requestURI.startsWith("/v3/api-docs") ||
                requestURI.startsWith("/v3/api-docs/") ||
                requestURI.startsWith("/webjars/") ||
                requestURI.startsWith("/swagger-resources/") ||
                requestURI.startsWith("/swagger-resources");
    }

    private void initializeAllowedServiceTokens() {
        List<String> list = new ArrayList<>();
        if (allowedServiceTokensConfig != null && !allowedServiceTokensConfig.isBlank()) {
            list.addAll(Arrays.stream(allowedServiceTokensConfig.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList());
        }
        allowedServiceTokensRef.set(List.copyOf(list));
        log.info("Service tokens configurados: {}", allowedServiceTokensRef.get());
    }

    private boolean isValidServiceToken(HttpServletRequest request) {
        String svc = request.getHeader("X-Service-Token");
        if (svc == null || svc.isBlank()) return false;
        List<String> allowed = allowedServiceTokensRef.get();
        return allowed != null && allowed.contains(svc);
    }

    private boolean hasAuthorizationHeader(HttpServletRequest request) {
        String a = request.getHeader("Authorization");
        return a != null && !a.isBlank();
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void setCorsHeadersForOrigin(HttpServletResponse response, String origin) {
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,HEAD");
        response.setHeader("Access-Control-Allow-Headers", "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,X-Service-Token");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Vary", "Origin");
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return false;
    }
}
