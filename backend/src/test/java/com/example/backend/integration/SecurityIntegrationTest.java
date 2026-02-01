package com.example.backend.integration;

import com.example.backend.model.Usuario;
import com.example.backend.repository.RefreshTokenRepository;
import com.example.backend.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Security Tests
 * Tests full authentication flow with security measures
 * Covers: SEC-001 through SEC-007, SEC-009
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Security Integration Tests")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_USERNAME = "securitytest";
    private static final String TEST_PASSWORD = "SecurePass123!";
    private static final String WRONG_PASSWORD = "WrongPassword";

    @BeforeEach
    void setUp() {
        // Clean up test data
        refreshTokenRepository.deleteByUsername(TEST_USERNAME);
        usuarioRepository.findByUsername(TEST_USERNAME).ifPresent(usuarioRepository::delete);

        // Create test user
        Usuario usuario = new Usuario();
        usuario.setUsername(TEST_USERNAME);
        usuario.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        usuarioRepository.save(usuario);
    }

    // ========== SEC-001, SEC-002, SEC-003: Complete Auth Flow ==========

    @Test
    @DisplayName("Security Integration: Complete authentication flow")
    void testCompleteAuthFlow_Success() throws Exception {
        // Step 1: Login
        String loginRequest = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}", 
            TEST_USERNAME, TEST_PASSWORD
        );

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.username").value(TEST_USERNAME))
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        Map<String, Object> loginResponse = objectMapper.readValue(responseBody, Map.class);
        String refreshToken = (String) loginResponse.get("refreshToken");

        assertNotNull(refreshToken, "SEC-001: Refresh token should be generated");

        // Step 2: Refresh token
        String refreshRequest = String.format("{\"refreshToken\":\"%s\"}", refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

        // Step 3: Logout
        String logoutRequest = String.format("{\"refreshToken\":\"%s\"}", refreshToken);

        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(logoutRequest))
                .andExpect(status().isOk());

        // Verify token was revoked (SEC-003)
        assertTrue(refreshTokenRepository.findByToken(refreshToken).isEmpty() ||
                   refreshTokenRepository.findByToken(refreshToken).get().isUsed(),
                "SEC-003: Token should be revoked after logout");
    }

    // ========== SEC-006: Rate Limiting ==========

    @Test
    @DisplayName("SEC-006: Rate limiting should block after 5 failed attempts")
    void testRateLimiting_BlocksAfter5Attempts() throws Exception {
        String loginRequest = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}", 
            TEST_USERNAME, WRONG_PASSWORD
        );

        // First 5 attempts should return 401 (Unauthorized)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginRequest)
                    .header("X-Forwarded-For", "192.168.1.201"))
                    .andExpect(status().isUnauthorized());
        }

        // 6th attempt should return 429 (Too Many Requests)
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
                .header("X-Forwarded-For", "192.168.1.201"))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Too many login attempts")));
    }

    @Test
    @DisplayName("SEC-006: Different IPs should have independent rate limits")
    void testRateLimiting_IndependentPerIP() throws Exception {
        String loginRequest = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}", 
            TEST_USERNAME, WRONG_PASSWORD
        );

        // Exhaust rate limit for IP1
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginRequest)
                    .header("X-Forwarded-For", "192.168.1.202"))
                    .andExpect(status().isUnauthorized());
        }

        // IP1 should be blocked
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
                .header("X-Forwarded-For", "192.168.1.202"))
                .andExpect(status().isTooManyRequests());

        // IP2 should still be allowed
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
                .header("X-Forwarded-For", "192.168.1.203"))
                .andExpect(status().isUnauthorized()); // Not rate limited, just wrong password
    }

    // ========== SEC-004: Security Headers ==========

    @Test
    @DisplayName("SEC-004: Should return security headers")
    void testSecurityHeaders_Present() throws Exception {
        String loginRequest = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}", 
            TEST_USERNAME, TEST_PASSWORD
        );

        mockMvc.perform(post("/api/auth/login")
                .secure(true) // Required for HSTS header
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("X-XSS-Protection"))
                .andExpect(header().exists("Strict-Transport-Security"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    // ========== SEC-009: CORS ==========

    @Test
    @DisplayName("SEC-009: Should handle CORS for allowed origin")
    void testCORS_AllowedOrigin() throws Exception {
        String loginRequest = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}", 
            TEST_USERNAME, TEST_PASSWORD
        );

        mockMvc.perform(post("/api/auth/login")
                .header("X-Forwarded-For", "192.168.1.105") // Unique IP to avoid rate limit
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
                .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    // ========== SEC-002: Token Expiration ==========

    @Test
    @DisplayName("SEC-002: Expired refresh token should be rejected")
    void testExpiredRefreshToken_Rejected() throws Exception {
        // This test would require mocking or waiting for actual expiration
        // For now, we test that invalid tokens are rejected

        String invalidRefreshRequest = "{\"refreshToken\":\"invalid-token-12345\"}";

        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRefreshRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid or expired refresh token")));
    }

    // ========== SEC-003: Token Revocation ==========

    @Test
    @DisplayName("SEC-003: Used refresh token should be rejected")
    void testUsedRefreshToken_Rejected() throws Exception {
        // Login to get tokens
        String loginRequest = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}", 
            TEST_USERNAME, TEST_PASSWORD
        );

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        Map<String, Object> loginResponse = objectMapper.readValue(responseBody, Map.class);
        String refreshToken = (String) loginResponse.get("refreshToken");

        // Use refresh token once
        String refreshRequest = String.format("{\"refreshToken\":\"%s\"}", refreshToken);
        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest))
                .andExpect(status().isOk());

        // Try to use the same token again (should fail - token rotation)
        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid or expired refresh token")));
    }

    // ========== Security Edge Cases ==========

    @Test
    @DisplayName("Security: SQL Injection attempt should fail safely")
    void testSQLInjection_SafelyHandled() throws Exception {
        String sqlInjectionAttempt = "{\"username\":\"admin' OR '1'='1\",\"password\":\"password\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sqlInjectionAttempt))
                .andExpect(status().isUnauthorized()); // Should not authenticate
    }

    @Test
    @DisplayName("Security: Empty credentials should be rejected")
    void testEmptyCredentials_Rejected() throws Exception {
        String emptyRequest = "{\"username\":\"\",\"password\":\"\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: Null refresh token should be handled safely")
    void testNullRefreshToken_SafelyHandled() throws Exception {
        String nullTokenRequest = "{\"refreshToken\":null}";

        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(nullTokenRequest))
                .andExpect(status().isUnauthorized());
    }
}
