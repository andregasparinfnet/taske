package com.example.backend.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpirationInMs;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationInMs;

    // SEC-008: Issuer validation
    @Value("${jwt.issuer}")
    private String jwtIssuer;

    // SEC-010: Audience validation
    @Value("${jwt.audience}")
    private String jwtAudience;

    // SEC-011, SEC-012: Clock skew tolerance (seconds)
    @Value("${jwt.clock-skew}")
    private long clockSkewInSeconds;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationInMs);

        logger.info("Gerando access token para usuário: {}, expira em: {}", username, expiryDate);

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(jwtIssuer)  // SEC-008
                .setAudience(jwtAudience)  // SEC-010
                .setIssuedAt(now)
                .setNotBefore(now)  // SEC-012: Token válido imediatamente
                .setExpiration(expiryDate)  // SEC-011
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationInMs);

        logger.info("Gerando refresh token para usuário: {}, expira em: {}", username, expiryDate);

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(jwtIssuer)  // SEC-008
                .setAudience(jwtAudience)  // SEC-010
                .setIssuedAt(now)
                .setNotBefore(now)  // SEC-012
                .setExpiration(expiryDate)  // SEC-011
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (JwtException ex) {
            logger.error("Erro ao extrair username do JWT: {}", ex.getMessage());
            throw ex;
        }
    }

    /**
     * SEC-008, SEC-010, SEC-011, SEC-012: Advanced JWT validation
     * 
     * Validates:
     * - Signature
     * - Issuer (SEC-008)
     * - Audience (SEC-010)
     * - Expiration with clock skew (SEC-011)
     * - Not-before with clock skew (SEC-012)
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .requireIssuer(jwtIssuer)  // SEC-008: Validates issuer
                    .requireAudience(jwtAudience)  // SEC-010: Validates audience
                    .setAllowedClockSkewSeconds(clockSkewInSeconds)  // SEC-011, SEC-012: Clock skew tolerance
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            logger.error("Assinatura JWT inválida: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("JWT mal formado: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.warn("JWT expirado para usuário: {}", ex.getClaims().getSubject());
        } catch (UnsupportedJwtException ex) {
            logger.error("JWT não suportado: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string vazia: {}", ex.getMessage());
        } catch (IncorrectClaimException ex) {
            // SEC-008, SEC-010: Invalid issuer or audience
            logger.error("JWT claim inválido (issuer/audience): {}", ex.getMessage());
        }
        return false;
    }

    public long getAccessTokenExpirationInMs() {
        return accessTokenExpirationInMs;
    }

    public long getRefreshTokenExpirationInMs() {
        return refreshTokenExpirationInMs;
    }
}
