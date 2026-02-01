package com.example.backend.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for JwtTokenProvider
 * Tests SEC-008, SEC-010, SEC-011, SEC-012: JWT token generation and validation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    private static final String TEST_SECRET = "minha-chave-secreta-super-segura-com-pelo-menos-256-bits-para-hmac-sha256";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_ISSUER = "taske-api";
    private static final String TEST_AUDIENCE = "taske-client";
    private static final long ACCESS_TOKEN_EXPIRATION = 900000L; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 days
    private static final long CLOCK_SKEW = 60L; // 60 seconds

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(tokenProvider, "accessTokenExpirationInMs", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(tokenProvider, "refreshTokenExpirationInMs", REFRESH_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(tokenProvider, "jwtIssuer", TEST_ISSUER);
        ReflectionTestUtils.setField(tokenProvider, "jwtAudience", TEST_AUDIENCE);
        ReflectionTestUtils.setField(tokenProvider, "clockSkewInSeconds", CLOCK_SKEW);
    }

    // ========== Token Generation ==========

    @Test
    @DisplayName("Deve gerar access token válido")
    void generateAccessToken_ValidAuth_ReturnsToken() {
        // Arrange
        Authentication auth = createAuthentication(TEST_USERNAME);

        // Act
        String token = tokenProvider.generateAccessToken(auth);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT format: header.payload.signature
    }

    @Test
    @DisplayName("Deve gerar refresh token válido")
    void generateRefreshToken_ValidUsername_ReturnsToken() {
        // Act
        String token = tokenProvider.generateRefreshToken(TEST_USERNAME);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("SEC-008: Token deve conter issuer correto")
    void generateAccessToken_ContainsCorrectIssuer() {
        // Arrange
        Authentication auth = createAuthentication(TEST_USERNAME);

        // Act
        String token = tokenProvider.generateAccessToken(auth);

        // Assert - Validate by parsing token
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("SEC-010: Token deve conter audience correto")
    void generateAccessToken_ContainsCorrectAudience() {
        // Arrange
        Authentication auth = createAuthentication(TEST_USERNAME);

        // Act
        String token = tokenProvider.generateAccessToken(auth);

        // Assert
        assertTrue(tokenProvider.validateToken(token));
    }

    // ========== Token Validation ==========

    @Test
    @DisplayName("Deve validar token bem formado")
    void validateToken_WellFormedToken_ReturnsTrue() {
        // Arrange
        Authentication auth = createAuthentication(TEST_USERNAME);
        String token = tokenProvider.generateAccessToken(auth);

        // Act
        boolean result = tokenProvider.validateToken(token);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Deve rejeitar token mal formado")
    void validateToken_MalformedToken_ReturnsFalse() {
        // Act
        boolean result = tokenProvider.validateToken("not-a-valid-jwt-token");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Deve rejeitar token vazio")
    void validateToken_EmptyToken_ReturnsFalse() {
        // Act
        boolean result = tokenProvider.validateToken("");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("SEC-011: Deve rejeitar token expirado")
    void validateToken_ExpiredToken_ReturnsFalse() {
        // Arrange - Create an expired token manually
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Date expiredDate = new Date(System.currentTimeMillis() - 3600000); // 1 hour ago

        String expiredToken = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setIssuer(TEST_ISSUER)
                .setAudience(TEST_AUDIENCE)
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .setExpiration(expiredDate)
                .signWith(key)
                .compact();

        // Act
        boolean result = tokenProvider.validateToken(expiredToken);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("SEC-008: Deve rejeitar token com issuer inválido")
    void validateToken_WrongIssuer_ReturnsFalse() {
        // Arrange - Create token with wrong issuer
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

        String wrongIssuerToken = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setIssuer("wrong-issuer")
                .setAudience(TEST_AUDIENCE)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key)
                .compact();

        // Act
        boolean result = tokenProvider.validateToken(wrongIssuerToken);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("SEC-010: Deve rejeitar token com audience inválido")
    void validateToken_WrongAudience_ReturnsFalse() {
        // Arrange - Create token with wrong audience
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

        String wrongAudienceToken = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setIssuer(TEST_ISSUER)
                .setAudience("wrong-audience")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key)
                .compact();

        // Act
        boolean result = tokenProvider.validateToken(wrongAudienceToken);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Deve rejeitar token com assinatura inválida")
    void validateToken_InvalidSignature_ReturnsFalse() {
        // Arrange - Create token with different secret
        SecretKey differentKey = Keys.hmacShaKeyFor("different-secret-key-with-sufficient-length-for-hmac-sha256".getBytes(StandardCharsets.UTF_8));

        String invalidSignatureToken = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setIssuer(TEST_ISSUER)
                .setAudience(TEST_AUDIENCE)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(differentKey)
                .compact();

        // Act
        boolean result = tokenProvider.validateToken(invalidSignatureToken);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("SEC-012: Deve rejeitar token com not-before no futuro")
    void validateToken_FutureNotBefore_ReturnsFalse() {
        // Arrange - Create token with future not-before (beyond clock skew)
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

        String futureNbfToken = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setIssuer(TEST_ISSUER)
                .setAudience(TEST_AUDIENCE)
                .setIssuedAt(new Date())
                .setNotBefore(new Date(System.currentTimeMillis() + 300000)) // 5 minutes in future
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key)
                .compact();

        // Act
        boolean result = tokenProvider.validateToken(futureNbfToken);

        // Assert
        assertFalse(result);
    }

    // ========== Username Extraction ==========

    @Test
    @DisplayName("Deve extrair username do token")
    void getUsernameFromJWT_ValidToken_ReturnsUsername() {
        // Arrange
        Authentication auth = createAuthentication(TEST_USERNAME);
        String token = tokenProvider.generateAccessToken(auth);

        // Act
        String username = tokenProvider.getUsernameFromJWT(token);

        // Assert
        assertEquals(TEST_USERNAME, username);
    }

    @Test
    @DisplayName("Deve lançar exceção para token inválido ao extrair username")
    void getUsernameFromJWT_InvalidToken_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> tokenProvider.getUsernameFromJWT("invalid-token"));
    }

    // ========== Expiration Getters ==========

    @Test
    @DisplayName("Deve retornar tempo de expiração do access token")
    void getAccessTokenExpirationInMs_ReturnsCorrectValue() {
        // Act
        long result = tokenProvider.getAccessTokenExpirationInMs();

        // Assert
        assertEquals(ACCESS_TOKEN_EXPIRATION, result);
    }

    @Test
    @DisplayName("Deve retornar tempo de expiração do refresh token")
    void getRefreshTokenExpirationInMs_ReturnsCorrectValue() {
        // Act
        long result = tokenProvider.getRefreshTokenExpirationInMs();

        // Assert
        assertEquals(REFRESH_TOKEN_EXPIRATION, result);
    }

    // ========== Security Edge Cases ==========

    @Test
    @DisplayName("Tokens gerados devem ser diferentes para o mesmo usuário")
    void generateAccessToken_SameUser_DifferentTokens() {
        // Arrange
        Authentication auth = createAuthentication(TEST_USERNAME);

        // Act
        String token1 = tokenProvider.generateAccessToken(auth);
        // Small delay to ensure different timestamps
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        String token2 = tokenProvider.generateAccessToken(auth);

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Deve gerar tokens para diferentes usuários")
    void generateAccessToken_DifferentUsers_DifferentTokens() {
        // Arrange
        Authentication auth1 = createAuthentication("user1");
        Authentication auth2 = createAuthentication("user2");

        // Act
        String token1 = tokenProvider.generateAccessToken(auth1);
        String token2 = tokenProvider.generateAccessToken(auth2);

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Deve lidar com username nulo")
    void generateRefreshToken_NullUsername_ThrowsOrHandles() {
        // This test documents expected behavior - either exception or null handling
        // Depending on requirements, adjust the assertion
        assertDoesNotThrow(() -> {
            String token = tokenProvider.generateRefreshToken(null);
            // If it doesn't throw, token should still be generated (with null subject)
            assertNotNull(token);
        });
    }

    // ========== Helper Methods ==========

    private Authentication createAuthentication(String username) {
        return new UsernamePasswordAuthenticationToken(
                username,
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
