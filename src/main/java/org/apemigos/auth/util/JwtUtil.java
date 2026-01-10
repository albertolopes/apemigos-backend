package org.apemigos.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apemigos.configuration.JwtProperties;
import org.apemigos.usuarios.entity.Usuario;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    /**
     * Gera uma chave de assinatura segura
     */
    private SecretKey getSecureSigningKey() {
        try {
            byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);

            // Garante que a chave tenha pelo menos 64 bytes para HS512
            if (keyBytes.length < 64) {
                throw new SecurityException("Chave JWT muito curta. Mínimo 64 caracteres requeridos.");
            }

            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("Erro ao gerar chave de assinatura: {}", e.getMessage());
            throw new SecurityException("Falha na configuração de segurança");
        }
    }

    /**
     * Gera token de serviço seguro
     */
    public String generateSecureServiceToken() {
        try {
            Instant now = Instant.now();
            Instant expiration = now.plusMillis(jwtProperties.getServiceExpirationMs());

            return Jwts.builder()
                    .setId(UUID.randomUUID().toString()) // JTI único
                    .setSubject("SERVICE")
                    .claim("type", "service")
                    .claim("isService", true)
                    .claim("email", "service@apemigos.org")
                    .claim("name", "Service Account")
                    .claim("role", Usuario.UserRole.SERVICE.name())
                    .claim("iss", jwtProperties.getIssuer()) // Issuer
                    .claim("aud", "apemigos-api") // Audience
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiration))
                    .signWith(getSecureSigningKey(), SignatureAlgorithm.HS512)
                    .compact();

        } catch (Exception e) {
            log.error("Erro ao gerar token de serviço: {}", e.getMessage());
            throw new SecurityException("Falha na geração do token");
        }
    }

    /**
     * Gera token de usuário seguro
     */
    public String generateSecureUserToken(Usuario user) {
        try {
            Instant now = Instant.now();
            Instant expiration = now.plusMillis(jwtProperties.getExpirationMs());

            return Jwts.builder()
                    .setId(UUID.randomUUID().toString()) // JTI único
                    .setSubject(user.getEmail())
                    .claim("id", user.getId())
                    .claim("name", user.getNome())
                    .claim("role", user.getRole().name())
                    .claim("type", "user")
                    .claim("isService", false)
                    .claim("iss", jwtProperties.getIssuer())
                    .claim("aud", "apemigos-api")
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiration))
                    .signWith(getSecureSigningKey(), SignatureAlgorithm.HS512)
                    .compact();

        } catch (Exception e) {
            log.error("Erro ao gerar token de usuário: {}", e.getMessage());
            throw new SecurityException("Falha na geração do token");
        }
    }

    /**
     * Valida token de forma segura
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecureSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Assinatura do token inválida: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token malformado: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("Violação de segurança no token: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Token inválido: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Verifica se o token está expirado
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("Erro ao verificar expiração do token: {}", e.getMessage());
            return true; // Considera como expirado em caso de erro
        }
    }

    /**
     * Extrai o username/email do token
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.warn("Erro ao extrair username do token: {}", e.getMessage());
            throw new SecurityException("Token inválido");
        }
    }

    /**
     * Extrai a role do token
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            log.warn("Erro ao extrair role do token: {}", e.getMessage());
            throw new SecurityException("Token inválido");
        }
    }

    /**
     * Extrai o ID do usuário do token
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.get("id", Long.class);
        } catch (Exception e) {
            log.warn("Erro ao extrair user ID do token: {}", e.getMessage());
            return null; // Para tokens de serviço, pode ser null
        }
    }

    /**
     * Verifica se é um token de serviço
     */
    public boolean isServiceToken(String token) {
        try {
            Claims claims = extractClaims(token);
            Boolean isService = claims.get("isService", Boolean.class);
            return isService != null && isService;
        } catch (Exception e) {
            log.warn("Erro ao verificar tipo do token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrai todos os claims do token de forma segura
     */
    public Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecureSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token expirado ao extrair claims: {}", e.getMessage());
            throw new SecurityException("Token expirado");
        } catch (SignatureException e) {
            log.warn("Assinatura inválida ao extrair claims: {}", e.getMessage());
            throw new SecurityException("Token inválido");
        } catch (MalformedJwtException e) {
            log.warn("Token malformado ao extrair claims: {}", e.getMessage());
            throw new SecurityException("Token malformado");
        } catch (Exception e) {
            log.warn("Erro ao extrair claims do token: {}", e.getMessage());
            throw new SecurityException("Falha ao processar token");
        }
    }

    /**
     * Extrai o email do token
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = extractClaims(token);
            String email = claims.get("email", String.class);
            return email != null ? email : claims.getSubject(); // Fallback para subject
        } catch (Exception e) {
            log.warn("Erro ao extrair email do token: {}", e.getMessage());
            throw new SecurityException("Token inválido");
        }
    }

    /**
     * Extrai o nome do token
     */
    public String getNameFromToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.get("name", String.class);
        } catch (Exception e) {
            log.warn("Erro ao extrair nome do token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validação completa e segura do token
     */
    public boolean validateTokenSecure(String token) {
        return validateToken(token) && !isTokenExpired(token);
    }

    /**
     * Obtém data de expiração do token
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.warn("Erro ao obter data de expiração: {}", e.getMessage());
            return new Date(); // Retorna data atual em caso de erro
        }
    }
}