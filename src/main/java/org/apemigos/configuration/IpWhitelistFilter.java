package org.apemigos.configuration.security;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Order(1)
public class IpWhitelistFilter implements Filter {

    @Value("${security.allowed.ips:127.0.0.1,0:0:0:0:0:0:0:1,::1,172.0.0.0/8,10.0.0.0/8}")
    private String allowedIps;

    private List<String> allowedIpList;

    @PostConstruct
    public void init() {
        allowedIpList = Arrays.asList(allowedIps.split(","));
        System.out.println("🔒 IPs permitidos: " + allowedIpList);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIpAddress(httpRequest);

        if (!isIpAllowed(clientIp)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\": \"Acesso negado - IP não autorizado: " + clientIp + "\"}");
            System.out.println("🚫 Acesso bloqueado para IP: " + clientIp);
            return;
        }

        System.out.println("✅ Acesso permitido para IP: " + clientIp);
        chain.doFilter(request, response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headersToCheck = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA"
        };

        for (String header : headersToCheck) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Pega o primeiro IP da lista se houver múltiplos
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    private boolean isIpAllowed(String ip) {
        return allowedIpList.stream()
                .anyMatch(allowedIp ->
                        allowedIp.equals(ip) ||
                                allowedIp.equals("0.0.0.0") ||
                                checkSubnet(ip, allowedIp)
                );
    }

    private boolean checkSubnet(String ip, String subnet) {
        if (!subnet.contains("/")) return false;

        try {
            String[] parts = subnet.split("/");
            String network = parts[0];
            int prefix = Integer.parseInt(parts[1]);

            if (prefix >= 24 && ip.startsWith(network.substring(0, network.lastIndexOf('.')))) {
                return true;
            }
            if (prefix >= 16 && ip.startsWith(network.substring(0, network.indexOf('.', network.indexOf('.') + 1)))) {
                return true;
            }
        } catch (Exception ignore) {
        }
        return false;
    }
}