//package org.apemigos.configuration.security;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//
//@Component
//@Order(1)
//public class IpWhitelistFilter implements Filter {
//
//    // Default to '*' (allow all) so deployments that don't set the property won't be blocked.
//    @Value("${security.allowed.ips:*}")
//    private String allowedIps;
//
//    private List<String> allowedIpList;
//
//    @PostConstruct
//    public void init() {
//        if (allowedIps == null || allowedIps.trim().isEmpty()) {
//            allowedIpList = List.of("*");
//        } else {
//            allowedIpList = Arrays.asList(allowedIps.split(","));
//        }
//        System.out.println("🔒 IPs permitidos: " + allowedIpList);
//    }
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws IOException, ServletException {
//
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        HttpServletResponse httpResponse = (HttpServletResponse) response;
//
//        // DEBUG: log forwarding headers to help diagnose issues behind proxies
//        String xfwd = httpRequest.getHeader("X-Forwarded-For");
//        String xproto = httpRequest.getHeader("X-Forwarded-Proto");
//        String xreal = httpRequest.getHeader("X-Real-IP");
//        if (xfwd != null || xproto != null || xreal != null) {
//            System.out.println("🔁 Forward headers: X-Forwarded-For=" + xfwd + ", X-Forwarded-Proto=" + xproto + ", X-Real-IP=" + xreal);
//        }
//
//        String clientIp = getClientIpAddress(httpRequest);
//
//        if (!isIpAllowed(clientIp)) {
//            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
//            httpResponse.setContentType("application/json");
//            httpResponse.getWriter().write("{\"error\": \"Acesso negado - IP não autorizado: " + clientIp + "\"}");
//            System.out.println("🚫 Acesso bloqueado para IP: " + clientIp);
//            return;
//        }
//
//        System.out.println("✅ Acesso permitido para IP: " + clientIp);
//        chain.doFilter(request, response);
//    }
//
//    private String getClientIpAddress(HttpServletRequest request) {
//        String[] headersToCheck = {
//                "X-Forwarded-For",
//                "X-Real-IP",
//                "Proxy-Client-IP",
//                "WL-Proxy-Client-IP",
//                "HTTP_X_FORWARDED_FOR",
//                "HTTP_X_FORWARDED",
//                "HTTP_X_CLUSTER_CLIENT_IP",
//                "HTTP_CLIENT_IP",
//                "HTTP_FORWARDED_FOR",
//                "HTTP_FORWARDED",
//                "HTTP_VIA"
//        };
//
//        for (String header : headersToCheck) {
//            String ip = request.getHeader(header);
//            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
//                // Pega o primeiro IP da lista se houver múltiplos
//                return ip.split(",")[0].trim();
//            }
//        }
//
//        return request.getRemoteAddr();
//    }
//
//    private boolean isIpAllowed(String ip) {
//        // If the list explicitly contains '*' treat it as allow-all
//        if (allowedIpList.contains("*")) return true;
//
//        return allowedIpList.stream()
//                .anyMatch(allowedIp ->
//                        allowedIp.equals(ip) ||
//                                allowedIp.equals("0.0.0.0") ||
//                                checkSubnet(ip, allowedIp)
//                );
//    }
//
//    private boolean checkSubnet(String ip, String subnet) {
//        if (!subnet.contains("/")) return false;
//
//        try {
//            String[] parts = subnet.split("/");
//            String network = parts[0];
//            int prefix = Integer.parseInt(parts[1]);
//
//            if (prefix >= 24 && ip.startsWith(network.substring(0, network.lastIndexOf('.')))) {
//                return true;
//            }
//            if (prefix >= 16 && ip.startsWith(network.substring(0, network.indexOf('.', network.indexOf('.') + 1)))) {
//                return true;
//            }
//        } catch (Exception ignore) {
//        }
//        return false;
//    }
//}
//
