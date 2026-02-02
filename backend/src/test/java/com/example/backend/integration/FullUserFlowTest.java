package com.example.backend.integration;

import com.example.backend.controller.AuthController;
import com.example.backend.controller.AuthController.JwtResponse;
import com.example.backend.controller.AuthController.LoginRequest;
import com.example.backend.model.Compromisso;
import com.example.backend.model.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FullUserFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("E2E: Register -> Login -> Create Compromisso -> List -> Logout")
    void criticalUserJourney() throws Exception {
        // 1. REGISTER
        String username = "e2e_user_" + System.currentTimeMillis();
        String password = "StrongPassword123!";
        
        Usuario newUser = new Usuario();
        newUser.setUsername(username);
        newUser.setPassword(password);
        
        mockMvc.perform(post("/api/auth/register")
                .with(csrf()) // Not needed for public endpoints usually, but safe to add
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated()); // 201 Created

        // 2. LOGIN
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(username);
        loginReq.setPassword(password);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        JwtResponse jwtResponse = objectMapper.readValue(responseBody, JwtResponse.class);
        String accessToken = jwtResponse.getAccessToken();
        
        assertThat(accessToken).isNotNull().isNotEmpty();

        // 3. CREATE COMPROMISSO
        // Use a simple map or DTO structure matching CompromissoRequestDTO
        java.util.Map<String, Object> compromissoMap = new java.util.HashMap<>();
        compromissoMap.put("titulo", "Reunião E2E");
        compromissoMap.put("descricao", "Validando fluxo completo");
        compromissoMap.put("dataHora", LocalDateTime.now().plusDays(1).toString()); // Jackson handles ISO string
        compromissoMap.put("tipo", "TRABALHO");
        compromissoMap.put("status", "PENDENTE");

        mockMvc.perform(post("/api/compromissos")
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compromissoMap)))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print()) // Print result for debugging
                .andExpect(status().isCreated());

        // 4. LIST COMPROMISSOS
        mockMvc.perform(get("/api/compromissos")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // 5. LOGOUT
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + accessToken)
                .with(csrf())) // CSRF usually required for state altering
                .andExpect(status().isNoContent()); // 204 No Content
        
        // 6. VERIFY TOKEN REVOCATION (Optional, depends on implementation)
        // If we implemented token blacklisting, this should fail. 
        // JWTs are stateless, so unless we check revocation, it might still work unless short-lived.
        // Assuming logout cleans up server-side refresh tokens.
    }
}
