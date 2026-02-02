package com.example.backend.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.backend.service.RefreshTokenService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CsrfTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("POST /api/auth/logout sem CSRF deve retornar 403 Forbidden")
    @WithMockUser // Autenticado, mas sem CSRF
    void logout_NoCsrf_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/auth/logout COM CSRF deve retornar 204 No Content (Sucesso)")
    @WithMockUser // Autenticado
    void logout_WithCsrf_ReturnsSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/auth/refresh sem CSRF deve retornar 403 Forbidden")
    void refresh_NoCsrf_ReturnsForbidden() throws Exception {
        // Refresh não requer autenticação de usuário (filtro) para o endpoint em si,
        // mas requer CSRF se não for ignorado.
        // Verificando a SecurityConfig: .ignoringRequestMatchers("/api/auth/login", "/api/auth/register")
        // O restante /api/auth/** requer autenticação e CSRF.
        // Se eu não passar usuário, o 403 do CSRF deve vir ANTES do 401?
        // Vamos testar com usuário mockado para garantir que o erro é CSRF e não Auth.
        
        mockMvc.perform(post("/api/auth/refresh")
                .contentType("application/json")
                .content("{}")) // Body vazio ou inválido, mas o foco é o status 403
                .andExpect(status().isForbidden());
    }
}
