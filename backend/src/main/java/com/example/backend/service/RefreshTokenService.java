package com.example.backend.service;

import com.example.backend.config.JwtTokenProvider;
import com.example.backend.entity.RefreshToken;
import com.example.backend.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Cria um novo refresh token para o usuário
     * Revoga tokens anteriores do mesmo usuário
     */
    @Transactional
    public RefreshToken createRefreshToken(String username) {
        // Revogar todos os tokens ativos do usuário (one device policy)
        revokeAllUserTokens(username);

        // Criar novo token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUsername(username);
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setExpiryDate(Instant.now().plusMillis(tokenProvider.getRefreshTokenExpirationInMs()));

        refreshToken = refreshTokenRepository.save(refreshToken);
        
        logger.info("Refresh token criado para usuário: {}, expira em: {}", username, refreshToken.getExpiryDate());
        
        return refreshToken;
    }

    /**
     * Valida e retorna o refresh token
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Valida se o token ainda é válido
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (!token.isValid()) {
            logger.warn("Tentativa de uso de token inválido: usuário={}, motivo={}", 
                token.getUsername(), 
                token.isExpired() ? "expirado" : token.isRevoked() ? "revogado" : "já usado");
            
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token inválido ou expirado. Faça login novamente.");
        }
        return token;
    }

    /**
     * Marca o token como usado (one-time use)
     */
    @Transactional
    public void markAsUsed(RefreshToken token) {
        token.setUsed(true);
        refreshTokenRepository.save(token);
        logger.info("Refresh token marcado como usado: usuário={}", token.getUsername());
    }

    /**
     * Revoga todos os tokens de um usuário (útil para logout)
     */
    @Transactional
    public void revokeAllUserTokens(String username) {
        refreshTokenRepository.revokeAllByUsername(username, Instant.now());
        logger.info("Todos os refresh tokens do usuário {} foram revogados", username);
    }

    /**
     * Revoga um token específico
     */
    @Transactional
    public void revokeToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        if (refreshToken.isPresent()) {
            RefreshToken rt = refreshToken.get();
            rt.setRevokedAt(Instant.now());
            refreshTokenRepository.save(rt);
            logger.warn("Refresh token revogado: usuário={}", rt.getUsername());
        }
    }

    /**
     * Limpeza automática de tokens expirados (executa diariamente às 3AM)
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        refreshTokenRepository.deleteExpiredTokens(now);
        logger.info("Limpeza automática de refresh tokens expirados executada");
    }
}
