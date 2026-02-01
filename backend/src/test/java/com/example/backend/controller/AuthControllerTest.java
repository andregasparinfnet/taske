package com.example.backend.controller;

import com.example.backend.config.JwtTokenProvider;
import com.example.backend.config.SecurityConfig;
import com.example.backend.entity.RefreshToken;
import com.example.backend.model.Usuario;
import com.example.backend.repository.UsuarioRepository;
import com.example.backend.service.RateLimitService;
import com.example.backend.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Tests for AuthController
 * Tests authentication endpoints: register, login, refresh, logout
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private RateLimitService rateLimitService;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_ACCESS_TOKEN = "mock.access.token";
    private static final String TEST_REFRESH_TOKEN = "mock-refresh-token-uuid";

    private Usuario testUsuario;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        testUsuario = new Usuario();
        testUsuario.setId(1L);
        testUsuario.setUsername(TEST_USERNAME);
        testUsuario.setPassword("encoded-password");

        testRefreshToken = new RefreshToken();
        testRefreshToken.setId(1L);
        testRefreshToken.setToken(TEST_REFRESH_TOKEN);
        testRefreshToken.setUsername(TEST_USERNAME);
        testRefreshToken.setExpiryDate(Instant.now().plusSeconds(604800));
        testRefreshToken.setUsed(false);
    }

    // ========== Registration Tests ==========

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso")
    void register_ValidUser_ReturnsCreated() throws Exception {
        // Arrange
        when(usuarioRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn("encoded-password");

        String requestBody = """
            {
                "username": "%s",
                "password": "%s"
            }
            """.formatted(TEST_USERNAME, TEST_PASSWORD);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sucesso")));

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve rejeitar registro de usuário duplicado")
    void register_DuplicateUsername_ReturnsBadRequest() throws Exception {
        // Arrange
        when(usuarioRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        String requestBody = """
            {
                "username": "%s",
                "password": "%s"
            }
            """.formatted(TEST_USERNAME, TEST_PASSWORD);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("já existe")));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ========== Login Tests ==========

    @Test
    @DisplayName("Deve fazer login com credenciais válidas")
    void login_ValidCredentials_ReturnsTokens() throws Exception {
        // Arrange
        when(rateLimitService.tryConsume(anyString())).thenReturn(true);
        
        Authentication auth = new UsernamePasswordAuthenticationToken(TEST_USERNAME, TEST_PASSWORD);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateAccessToken(auth)).thenReturn(TEST_ACCESS_TOKEN);
        when(refreshTokenService.createRefreshToken(TEST_USERNAME)).thenReturn(testRefreshToken);

        String requestBody = """
            {
                "username": "%s",
                "password": "%s"
            }
            """.formatted(TEST_USERNAME, TEST_PASSWORD);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(TEST_ACCESS_TOKEN))
                .andExpect(jsonPath("$.refreshToken").value(TEST_REFRESH_TOKEN))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("Deve rejeitar login com credenciais inválidas")
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Arrange
        when(rateLimitService.tryConsume(anyString())).thenReturn(true);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        String requestBody = """
            {
                "username": "%s",
                "password": "wrongpassword"
            }
            """.formatted(TEST_USERNAME);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEC-006: Deve bloquear após muitas tentativas (rate limit)")
    void login_RateLimitExceeded_ReturnsTooManyRequests() throws Exception {
        // Arrange
        when(rateLimitService.tryConsume(anyString())).thenReturn(false);

        String requestBody = """
            {
                "username": "%s",
                "password": "%s"
            }
            """.formatted(TEST_USERNAME, TEST_PASSWORD);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Too many")));

        // Verify authentication was NOT attempted
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("SEC-006: Deve extrair IP do header X-Forwarded-For")
    void login_WithXForwardedFor_ExtractsCorrectIP() throws Exception {
        // Arrange
        when(rateLimitService.tryConsume("192.168.1.100")).thenReturn(true);
        
        Authentication auth = new UsernamePasswordAuthenticationToken(TEST_USERNAME, TEST_PASSWORD);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateAccessToken(auth)).thenReturn(TEST_ACCESS_TOKEN);
        when(refreshTokenService.createRefreshToken(TEST_USERNAME)).thenReturn(testRefreshToken);

        String requestBody = """
            {
                "username": "%s",
                "password": "%s"
            }
            """.formatted(TEST_USERNAME, TEST_PASSWORD);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("X-Forwarded-For", "192.168.1.100, 10.0.0.1"))
                .andExpect(status().isOk());

        verify(rateLimitService).tryConsume("192.168.1.100");
    }

    // ========== Refresh Token Tests ==========

    @Test
    @DisplayName("Deve renovar tokens com refresh token válido")
    void refreshToken_ValidToken_ReturnsNewTokens() throws Exception {
        // Arrange
        when(refreshTokenService.findByToken(TEST_REFRESH_TOKEN))
                .thenReturn(Optional.of(testRefreshToken));
        when(refreshTokenService.verifyExpiration(testRefreshToken))
                .thenReturn(testRefreshToken);
        when(tokenProvider.generateRefreshToken(TEST_USERNAME))
                .thenReturn(TEST_ACCESS_TOKEN);
        
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken("new-refresh-token");
        when(refreshTokenService.createRefreshToken(TEST_USERNAME))
                .thenReturn(newRefreshToken);

        String requestBody = """
            {
                "refreshToken": "%s"
            }
            """.formatted(TEST_REFRESH_TOKEN);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));

        // Verify token was marked as used (one-time use)
        verify(refreshTokenService).markAsUsed(testRefreshToken);
    }

    @Test
    @DisplayName("Deve rejeitar refresh token inválido")
    void refreshToken_InvalidToken_ReturnsUnauthorized() throws Exception {
        // Arrange
        when(refreshTokenService.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        String requestBody = """
            {
                "refreshToken": "invalid-token"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve rejeitar refresh token expirado")
    void refreshToken_ExpiredToken_ReturnsUnauthorized() throws Exception {
        // Arrange
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired-token");
        expiredToken.setExpiryDate(Instant.now().minusSeconds(3600)); // Expired

        when(refreshTokenService.findByToken("expired-token"))
                .thenReturn(Optional.of(expiredToken));
        when(refreshTokenService.verifyExpiration(expiredToken))
                .thenThrow(new RuntimeException("Token expirado"));

        String requestBody = """
            {
                "refreshToken": "expired-token"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    // ========== Logout Tests ==========

    @Test
    @DisplayName("Deve fazer logout revogando todos os tokens")
    void logout_ValidRequest_RevokesTokens() throws Exception {
        // Arrange
        String requestBody = """
            {
                "username": "%s"
            }
            """.formatted(TEST_USERNAME);

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sucesso")));

        verify(refreshTokenService).revokeAllUserTokens(TEST_USERNAME);
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("Deve lidar com request body vazio no login")
    void login_EmptyBody_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Deve lidar com username inválido no registro")
    void register_InvalidUsername_ReturnsBadRequest() throws Exception {
        // Arrange - username too short
        String requestBody = """
            {
                "username": "ab",
                "password": "validpassword123"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve lidar com senha inválida no registro")
    void register_InvalidPassword_ReturnsBadRequest() throws Exception {
        // Arrange - password too short
        String requestBody = """
            {
                "username": "validuser",
                "password": "12345"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
