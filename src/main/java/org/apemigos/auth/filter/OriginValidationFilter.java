package org.apemigos.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
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

    @Value("${frontend.urls}")
    private String[] primaryFrontendUrls;

    @Value("${frontend.urls}")
    private String allowedFrontendsConfig;

    @Value("${frontend.require-origin-validation}")
    private boolean requireOriginValidation;

    @Value("${frontend.allowed-user-agents}")
    private String allowedUserAgentsConfig;

    @Value("${frontend.allow-if-auth-header}")
    private boolean allowIfAuthHeader;

    private final AtomicReference<List<String>> allowedOriginsRef = new AtomicReference<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!requireOriginValidation) {
            String origin = request.getHeader("Origin");
            if (origin != null && !origin.isBlank()) {
                setCorsHeadersWhenPossible(request, response, origin, true);
            }
            filterChain.doFilter(request, response);
            return;
        }

        if (allowedOriginsRef.get() == null)
            initializeAllowedOrigins();

        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");

        if (origin == null || origin.isBlank()) {
            if (isBypassAllowed(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            log.warn("Requisição bloqueada - sem Origin e sem bypass (UA: {}, IP: {})", request.getHeader("User-Agent"), getClientIp(request));
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Origem não permitida\"}");
            return;
        }

        boolean isOriginAllowed = isAllowedOrigin(origin, referer);

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            if (isOriginAllowed || isBypassAllowed(request)) {
                setCorsHeadersWhenPossible(request, response, origin, true);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                log.warn("Preflight bloqueado - Origin não permitida: {} (Referer: {}, IP: {})", origin, referer, getClientIp(request));
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Origem não permitida\"}");
            }
            return;
        }

        if (!isOriginAllowed) {
            if (isBypassAllowed(request)) {
                setCorsHeadersWhenPossible(request, response, origin, false);
                filterChain.doFilter(request, response);
                return;
            }

            log.warn("Requisição bloqueada - Origin não permitida: {} (Referer: {}, IP: {})", origin, referer, getClientIp(request));
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Origem não permitida\"}");
            return;
        }

        setCorsHeadersWhenPossible(request, response, origin, false);
        filterChain.doFilter(request, response);
    }

    private boolean isBypassAllowed(HttpServletRequest request) {
        if (isAllowedUserAgent(request)) return true;
        if (allowIfAuthHeader && hasAuthorizationHeader(request)) return true;
        String svc = request.getHeader("X-Service-Token");
        if (svc != null && !svc.isBlank()) return true;
        String internal = request.getHeader("X-Internal-Request");
        return internal != null && !internal.isBlank();
    }

    private boolean isAllowedUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null || ua.isBlank()) return false;
        String lower = ua.toLowerCase();
        if (allowedUserAgentsConfig == null || allowedUserAgentsConfig.isBlank()) return false;
        String[] tokens = allowedUserAgentsConfig.split(",");
        for (String t : tokens) {
            if (t == null) continue;
            String token = t.trim().toLowerCase();
            if (token.isEmpty()) continue;
            if (lower.contains(token)) return true;
        }
        return false;
    }

    private boolean hasAuthorizationHeader(HttpServletRequest request) {
        String a = request.getHeader("Authorization");
        return a != null && !a.isBlank();
    }

    private void initializeAllowedOrigins() {
        List<String> list = new ArrayList<>();

        if (allowedFrontendsConfig != null && !allowedFrontendsConfig.isBlank()) {
            list.addAll(Arrays.stream(allowedFrontendsConfig.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(this::normalizeOrigin)
                    .toList());
        }

        if (primaryFrontendUrls != null) {
            for (String pf : primaryFrontendUrls) {
                if (pf != null && !pf.isBlank()) {
                    String norm = normalizeOrigin(pf);
                    if (!list.contains(norm)) {
                        list.add(norm);
                    }
                }
            }
        }

        allowedOriginsRef.set(List.copyOf(list));
        log.info("Origens permitidas configuradas: {}", allowedOriginsRef.get());
    }

    private boolean isAllowedOrigin(String origin, String referer) {
        List<String> allowed = allowedOriginsRef.get();
        if (allowed == null) return false;

        if (origin != null && !origin.isBlank()) {
            String normOrigin = normalizeOrigin(origin);
            for (String allowedOrigin : allowed) {
                if (normOrigin.equalsIgnoreCase(allowedOrigin) || normOrigin.startsWith(allowedOrigin)) {
                    return true;
                }
            }
        }

        if (referer != null && !referer.isBlank()) {
            try {
                String refererOrigin = extractOriginFromReferer(referer);
                String normReferer = normalizeOrigin(refererOrigin);
                for (String allowedOrigin : allowed) {
                    if (normReferer.equalsIgnoreCase(allowedOrigin) || normReferer.startsWith(allowedOrigin)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                log.debug("Erro ao extrair origem do referer: {}", referer);
            }
        }

        return false;
    }

    private String normalizeOrigin(String origin) {
        if (origin == null) return "";
        String o = origin.trim();
        if (o.endsWith("/")) o = o.substring(0, o.length() - 1);
        return o.toLowerCase();
    }

    private String extractOriginFromReferer(String referer) {
        try {
            URI uri = new URI(referer);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();
            if (scheme == null || host == null) return referer;
            if (port == -1) {
                return scheme + "://" + host;
            }
            return scheme + "://" + host + ":" + port;
        } catch (URISyntaxException e) {
            int index = referer.indexOf("/", 8); // Pós "https://"
            if (index > 0) {
                return referer.substring(0, index);
            }
            return referer;
        }
    }


    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Define os headers CORS padrão quando possível. Se originForHeader for nulo ou vazio,
     * o método tentará usar o header Origin presente na requisição. Quando allowAllForDebug=true,
     * será mais permissivo para ambientes de desenvolvimento/public endpoints.
     */
    private void setCorsHeadersWhenPossible(HttpServletRequest request, HttpServletResponse response, String originForHeader, boolean allowAllForDebug) {
        String origin = originForHeader;
        if ((origin == null || origin.isBlank())) {
            origin = request.getHeader("Origin");
            if ((origin == null || origin.isBlank())) {
                String referer = request.getHeader("Referer");
                if (referer != null && !referer.isBlank()) {
                    origin = extractOriginFromReferer(referer);
                }
            }
        }

        if (origin == null || origin.isBlank())
            return;
        boolean allowed = allowAllForDebug || isAllowedOrigin(origin, null) || isAllowedOrigin(null, origin);

        if (!allowed)
            return;


        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,HEAD");
        response.setHeader("Access-Control-Allow-Headers", "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Vary", "Origin");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return false;
    }
}
