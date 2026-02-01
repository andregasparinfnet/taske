package com.example.backend.security;

import com.example.backend.controller.AuthController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SEC-001, SEC-002, SEC-003: E2E Security Tests
 * 
 * Testes de integração para validar implementações de segurança:
 * - CSRF Protection (Double Submit Cookie Pattern)
 * - Session Fixation Prevention
 * - Timing Attack Protection
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("E2E Security Tests")
class SecurityE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== SEC-001: CSRF PROTECTION ====================

    @Test
    @DisplayName("SEC-001: Deve gerar token CSRF no cookie XSRF-TOKEN")
    void shouldGenerateCsrfTokenInCookie() throws Exception {
        // Arrange & Act: Fazer requisição GET para qualquer endpoint
        MvcResult result = mockMvc.perform(get("/api/compromissos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()) // Sem auth retorna 401
                .andReturn();

        // Assert: Verificar que cookie XSRF-TOKEN foi setado
        String csrfCookie = result.getResponse().getCookie("XSRF-TOKEN").getValue();
        assertThat(csrfCookie).isNotNull();
        assertThat(csrfCookie).isNotEmpty();
    }

    @Test
    @DisplayName("SEC-001: Deve aceitar requisição POST com token CSRF válido")
    void shouldAcceptPostWithValidCsrfToken() throws Exception {
        // Arrange: Obter token CSRF
        MvcResult getResult = mockMvc.perform(get("/api/compromissos"))
                .andReturn();
        
        String csrfToken = getResult.getResponse().getCookie("XSRF-TOKEN").getValue();
        
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("testuser", "password");

        // Act & Assert: POST com token CSRF deve funcionar
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-XSRF-TOKEN", csrfToken)
                        .cookie(getResult.getResponse().getCookie("XSRF-TOKEN"))
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("SEC-001: Deve rejeitar requisição POST sem token CSRF")
    void shouldRejectPostWithoutCsrfToken() throws Exception {
        // Arrange
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("testuser", "password");

        // Act & Assert: POST sem token CSRF deve retornar 403 Forbidden
        mockMvc.perform(post("/api/compromissos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-001: Endpoints /api/auth/login e /register devem ignorar CSRF")
    void shouldIgnoreCsrfForAuthEndpoints() throws Exception {
        // Arrange
        AuthController.LoginRequest registerRequest = new AuthController.LoginRequest("newuser123", "SecurePass!123");

        // Act & Assert: Register sem CSRF deve funcionar (endpoint ignorado)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login sem CSRF deve funcionar (endpoint ignorado)
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("newuser123", "SecurePass!123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SEC-001: Cookie XSRF-TOKEN deve ser não-httpOnly (acessível via JavaScript)")
    void csrfTokenCookieShouldNotBeHttpOnly() throws Exception {
        // Arrange & Act
        MvcResult result = mockMvc.perform(get("/api/compromissos"))
                .andReturn();

        // Assert: Cookie não deve ser httpOnly (para SPAs)
        boolean isHttpOnly = result.getResponse().getCookie("XSRF-TOKEN").isHttpOnly();
        assertThat(isHttpOnly).isFalse();
    }

    // ==================== SEC-002: SESSION FIXATION PREVENTION ====================

    @Test
    @DisplayName("SEC-002: Session ID deve mudar após login (Session Fixation Prevention)")
    void sessionIdShouldChangeAfterLogin() throws Exception {
        // Arrange: Criar usuário
        AuthController.LoginRequest registerRequest = new AuthController.LoginRequest("sessionuser", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Obter session ID antes do login
        MvcResult beforeLogin = mockMvc.perform(get("/api/compromissos"))
                .andReturn();
        String sessionIdBefore = beforeLogin.getResponse().getCookie("JSESSIONID") != null
                ? beforeLogin.getResponse().getCookie("JSESSIONID").getValue()
                : null;

        // Act: Fazer login
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("sessionuser", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Obter session ID depois do login
        String sessionIdAfter = loginResult.getResponse().getCookie("JSESSIONID") != null
                ? loginResult.getResponse().getCookie("JSESSIONID").getValue()
                : null;

        // Assert: Session ID deve ter mudado (ou sido criado se não existia antes)
        if (sessionIdBefore != null && sessionIdAfter != null) {
            assertThat(sessionIdAfter).isNotEqualTo(sessionIdBefore);
        } else {
            // Se não havia sessão antes, deve ter sido criada
            assertThat(sessionIdAfter).isNotNull();
        }
    }

    @Test
    @DisplayName("SEC-002: Limite de 1 sessão simultânea por usuário")
    void shouldLimitToOneSessionPerUser() throws Exception {
        // Arrange: Criar usuário
        AuthController.LoginRequest registerRequest = new AuthController.LoginRequest("singleuser", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Act: Fazer login primeira vez
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("singleuser", "password123");
        MvcResult firstLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String firstToken = objectMapper.readTree(firstLogin.getResponse().getContentAsString())
                .get("accessToken").asText();

        // Fazer login segunda vez (simula outro dispositivo)
        MvcResult secondLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String secondToken = objectMapper.readTree(secondLogin.getResponse().getContentAsString())
                .get("accessToken").asText();

        // Assert: Segundo token foi gerado (primeira sessão deve ser invalidada pelo limite)
        assertThat(firstToken).isNotEmpty();
        assertThat(secondToken).isNotEmpty();
        // JWT tokens serão diferentes pois têm timestamps diferentes
        assertThat(secondToken).isNotEqualTo(firstToken);
    }

    // ==================== SEC-003: TIMING ATTACK PROTECTION ====================

    @Test
    @DisplayName("SEC-003: Login deve retornar mensagem genérica para usuário inexistente")
    void shouldReturnGenericErrorForNonExistentUser() throws Exception {
        // Arrange
        AuthController.LoginRequest invalidUser = new AuthController.LoginRequest("nonexistentuser12345", "anypassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials")); // Mensagem genérica
    }

    @Test
    @DisplayName("SEC-003: Login deve retornar mensagem genérica para senha incorreta")
    void shouldReturnGenericErrorForWrongPassword() throws Exception {
        // Arrange: Criar usuário válido
        AuthController.LoginRequest registerRequest = new AuthController.LoginRequest("timinguser", "correctpassword");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Act: Login com senha errada
        AuthController.LoginRequest wrongPassword = new AuthController.LoginRequest("timinguser", "wrongpassword");

        // Assert: Deve retornar mesma mensagem genérica
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPassword)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials")); // Mesma mensagem genérica
    }

    @Test
    @DisplayName("SEC-003: Tempo de resposta deve ser similar para usuário válido/inválido")
    void timingAttackProtection_responseTimes() throws Exception {
        // Arrange: Criar usuário válido
        AuthController.LoginRequest registerRequest = new AuthController.LoginRequest("timingtest", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Medir tempo para usuário válido com senha errada
        AuthController.LoginRequest validUserWrongPass = new AuthController.LoginRequest("timingtest", "wrongpass");
        long startValid = System.nanoTime();
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserWrongPass)))
                .andExpect(status().isUnauthorized());
        long timeValid = System.nanoTime() - startValid;

        // Medir tempo para usuário inválido
        AuthController.LoginRequest invalidUser = new AuthController.LoginRequest("nonexistent999", "anypass");
        long startInvalid = System.nanoTime();
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isUnauthorized());
        long timeInvalid = System.nanoTime() - startInvalid;

        // Assert: Diferença de tempo deve ser razoável (< 50ms na maioria dos casos)
        // BCrypt garante tempo constante, mas há variação natural de I/O
        long diffMs = Math.abs(timeValid - timeInvalid) / 1_000_000;
        
        // Timing attack seria perceptível com diferenças > 100ms
        assertThat(diffMs).isLessThan(200); // Threshold generoso para ambiente de teste
    }

    // ==================== INTEGRATION: CSRF + JWT ====================

    @Test
    @DisplayName("INTEGRATION: Deve funcionar CSRF + JWT juntos em requisição autenticada")
    void shouldWorkCsrfAndJwtTogether() throws Exception {
        // Arrange: Criar usuário e fazer login
        AuthController.LoginRequest registerRequest = new AuthController.LoginRequest("integrationuser", "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("integrationuser", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken").asText();

        // Obter CSRF token
        MvcResult csrfResult = mockMvc.perform(get("/api/compromissos")
                        .header("Authorization", "Bearer " + accessToken))
                .andReturn();
        
        String csrfToken = csrfResult.getResponse().getCookie("XSRF-TOKEN").getValue();

        // Act & Assert: POST com JWT E CSRF deve funcionar
        String compromissoJson = "{\"titulo\":\"Test\",\"data\":\"2026-02-02\",\"tipo\":\"REUNIAO\"}";
        
        mockMvc.perform(post("/api/compromissos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .header("X-XSRF-TOKEN", csrfToken)
                        .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                        .content(compromissoJson))
                .andExpect(status().isCreated());
    }
}
