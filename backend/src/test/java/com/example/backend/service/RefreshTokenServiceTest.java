package com.example.backend.service;

import com.example.backend.config.JwtTokenProvider;
import com.example.backend.entity.RefreshToken;
import com.example.backend.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for RefreshTokenService
 * Tests token lifecycle: creation, validation, rotation, and revocation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Unit Tests")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_TOKEN = "test-refresh-token-uuid";
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 days

    private RefreshToken validToken;
    private RefreshToken expiredToken;
    private RefreshToken usedToken;
    private RefreshToken revokedToken;

    @BeforeEach
    void setUp() {
        // Valid token
        validToken = new RefreshToken();
        validToken.setId(1L);
        validToken.setToken(TEST_TOKEN);
        validToken.setUsername(TEST_USERNAME);
        validToken.setCreatedAt(Instant.now());
        validToken.setExpiryDate(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION));
        validToken.setUsed(false);
        validToken.setRevokedAt(null);

        // Expired token
        expiredToken = new RefreshToken();
        expiredToken.setId(2L);
        expiredToken.setToken("expired-token");
        expiredToken.setUsername(TEST_USERNAME);
        expiredToken.setCreatedAt(Instant.now().minusSeconds(86400));
        expiredToken.setExpiryDate(Instant.now().minusSeconds(3600)); // Expired 1 hour ago
        expiredToken.setUsed(false);
        expiredToken.setRevokedAt(null);

        // Used token
        usedToken = new RefreshToken();
        usedToken.setId(3L);
        usedToken.setToken("used-token");
        usedToken.setUsername(TEST_USERNAME);
        usedToken.setCreatedAt(Instant.now());
        usedToken.setExpiryDate(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION));
        usedToken.setUsed(true);
        usedToken.setRevokedAt(null);

        // Revoked token
        revokedToken = new RefreshToken();
        revokedToken.setId(4L);
        revokedToken.setToken("revoked-token");
        revokedToken.setUsername(TEST_USERNAME);
        revokedToken.setCreatedAt(Instant.now());
        revokedToken.setExpiryDate(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION));
        revokedToken.setUsed(false);
        revokedToken.setRevokedAt(Instant.now().minusSeconds(60));
    }

    // ========== Token Creation ==========

    @Test
    @DisplayName("Deve criar refresh token para usuário válido")
    void createRefreshToken_ValidUser_Success() {
        // Arrange
        when(tokenProvider.getRefreshTokenExpirationInMs()).thenReturn(REFRESH_TOKEN_EXPIRATION);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            token.setId(1L);
            return token;
        });

        // Act
        RefreshToken result = refreshTokenService.createRefreshToken(TEST_USERNAME);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_USERNAME, result.getUsername());
        assertNotNull(result.getToken());
        assertFalse(result.isUsed());
        assertNull(result.getRevokedAt());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));

        // Verify old tokens were revoked
        verify(refreshTokenRepository).revokeAllByUsername(eq(TEST_USERNAME), any(Instant.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Deve gerar tokens UUID únicos")
    void createRefreshToken_GeneratesUniqueTokens() {
        // Arrange
        when(tokenProvider.getRefreshTokenExpirationInMs()).thenReturn(REFRESH_TOKEN_EXPIRATION);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RefreshToken token1 = refreshTokenService.createRefreshToken(TEST_USERNAME);
        RefreshToken token2 = refreshTokenService.createRefreshToken(TEST_USERNAME);

        // Assert
        assertNotEquals(token1.getToken(), token2.getToken());
    }

    // ========== Token Lookup ==========

    @Test
    @DisplayName("Deve encontrar token existente")
    void findByToken_Exists_ReturnsToken() {
        // Arrange
        when(refreshTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(validToken));

        // Act
        Optional<RefreshToken> result = refreshTokenService.findByToken(TEST_TOKEN);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validToken.getToken(), result.get().getToken());
    }

    @Test
    @DisplayName("Deve retornar empty para token inexistente")
    void findByToken_NotExists_ReturnsEmpty() {
        // Arrange
        when(refreshTokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<RefreshToken> result = refreshTokenService.findByToken("nonexistent");

        // Assert
        assertTrue(result.isEmpty());
    }

    // ========== Token Verification ==========

    @Test
    @DisplayName("Deve validar token válido")
    void verifyExpiration_ValidToken_ReturnsToken() {
        // Act
        RefreshToken result = refreshTokenService.verifyExpiration(validToken);

        // Assert
        assertNotNull(result);
        assertEquals(validToken, result);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve rejeitar token expirado")
    void verifyExpiration_ExpiredToken_ThrowsException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> refreshTokenService.verifyExpiration(expiredToken));

        assertTrue(exception.getMessage().contains("inválido") || 
                   exception.getMessage().contains("expirado"));
        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    @DisplayName("Deve rejeitar token já usado (one-time use)")
    void verifyExpiration_UsedToken_ThrowsException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> refreshTokenService.verifyExpiration(usedToken));

        verify(refreshTokenRepository).delete(usedToken);
    }

    @Test
    @DisplayName("Deve rejeitar token revogado")
    void verifyExpiration_RevokedToken_ThrowsException() {
        // Act & Assert
        assertThrows(RuntimeException.class, 
            () -> refreshTokenService.verifyExpiration(revokedToken));

        verify(refreshTokenRepository).delete(revokedToken);
    }

    // ========== Token Usage ==========

    @Test
    @DisplayName("Deve marcar token como usado")
    void markAsUsed_ValidToken_UpdatesFlag() {
        // Act
        refreshTokenService.markAsUsed(validToken);

        // Assert
        assertTrue(validToken.isUsed());
        verify(refreshTokenRepository).save(validToken);
    }

    // ========== Token Revocation ==========

    @Test
    @DisplayName("Deve revogar todos os tokens do usuário")
    void revokeAllUserTokens_CallsRepository() {
        // Act
        refreshTokenService.revokeAllUserTokens(TEST_USERNAME);

        // Assert
        verify(refreshTokenRepository).revokeAllByUsername(eq(TEST_USERNAME), any(Instant.class));
    }

    @Test
    @DisplayName("Deve revogar token específico")
    void revokeToken_ExistingToken_Revokes() {
        // Arrange
        when(refreshTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(validToken));

        // Act
        refreshTokenService.revokeToken(TEST_TOKEN);

        // Assert
        assertNotNull(validToken.getRevokedAt());
        verify(refreshTokenRepository).save(validToken);
    }

    @Test
    @DisplayName("Deve lidar com revogação de token inexistente")
    void revokeToken_NonExistingToken_NoException() {
        // Arrange
        when(refreshTokenRepository.findByToken("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert - não deve lançar exceção
        assertDoesNotThrow(() -> refreshTokenService.revokeToken("nonexistent"));
        verify(refreshTokenRepository, never()).save(any());
    }

    // ========== Token Cleanup ==========

    @Test
    @DisplayName("Deve limpar tokens expirados no cleanup")
    void cleanupExpiredTokens_DeletesExpired() {
        // Act
        refreshTokenService.cleanupExpiredTokens();

        // Assert
        verify(refreshTokenRepository).deleteExpiredTokens(any(Instant.class));
    }

    // ========== Security Edge Cases ==========

    @Test
    @DisplayName("Deve configurar expiração correta do token")
    void createRefreshToken_CorrectExpiration() {
        // Arrange
        when(tokenProvider.getRefreshTokenExpirationInMs()).thenReturn(REFRESH_TOKEN_EXPIRATION);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RefreshToken result = refreshTokenService.createRefreshToken(TEST_USERNAME);

        // Assert - expiry should be ~7 days from now (with 1 second tolerance)
        Instant expectedExpiry = Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION);
        assertTrue(result.getExpiryDate().isAfter(expectedExpiry.minusSeconds(1)));
        assertTrue(result.getExpiryDate().isBefore(expectedExpiry.plusSeconds(1)));
    }

    @Test
    @DisplayName("Deve revogar tokens antigos ao criar novo")
    void createRefreshToken_RevokesOldTokens() {
        // Arrange
        when(tokenProvider.getRefreshTokenExpirationInMs()).thenReturn(REFRESH_TOKEN_EXPIRATION);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        refreshTokenService.createRefreshToken(TEST_USERNAME);

        // Assert - verify old tokens were revoked BEFORE new one was created
        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        verify(refreshTokenRepository).revokeAllByUsername(usernameCaptor.capture(), any(Instant.class));
        assertEquals(TEST_USERNAME, usernameCaptor.getValue());
    }
}
