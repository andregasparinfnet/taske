package com.example.backend.integration;

import com.example.backend.model.Compromisso;
import com.example.backend.model.Usuario;
import com.example.backend.repository.CompromissoRepository;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Export Security Integration Tests")
class ExportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.example.backend.service.RateLimitService rateLimitService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompromissoRepository compromissoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario user1;
    private Usuario user2;
    private String user1Token;
    private String user2Token;

    @BeforeEach
    void setUp() throws Exception {
        org.mockito.Mockito.when(rateLimitService.tryConsume(org.mockito.ArgumentMatchers.anyString())).thenReturn(true);
        
        compromissoRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Setup Users
        user1 = new Usuario();
        user1.setUsername("user1");
        user1.setPassword(passwordEncoder.encode("pass1"));
        user1 = usuarioRepository.save(user1);

        user2 = new Usuario();
        user2.setUsername("user2");
        user2.setPassword(passwordEncoder.encode("pass2"));
        user2 = usuarioRepository.save(user2);

        // Get Tokens
        user1Token = login("user1", "pass1");
        user2Token = login("user2", "pass2");

        // Seed Data
        createCompromisso(user1, "User1 Secret Task", "PERICIA");
        createCompromisso(user2, "User2 Private Meeting", "TRABALHO");
    }

    private String login(String username, String password) throws Exception {
        String loginJson = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private void createCompromisso(Usuario user, String titulo, String tipo) {
        Compromisso c = new Compromisso();
        c.setTitulo(titulo);
        c.setDataHora(LocalDateTime.now());
        c.setTipo(tipo);
        c.setUsuario(user);
        compromissoRepository.save(c);
    }

    @Test
    @DisplayName("EXPORT: Should return CSV for authenticated user")
    void export_Success() throws Exception {
        mockMvc.perform(get("/api/compromissos/export")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv; charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(content().string(containsString("id,titulo,dataHora,tipo,status,valor,urgente,descricao")))
                .andExpect(content().string(containsString("User1 Secret Task")));
    }

    @Test
    @DisplayName("EXPORT: Should fail (403) without JWT")
    void export_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/compromissos/export"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EXPORT SECURITY: IDOR Prevention - User1 should NOT see User2 data in export")
    void export_IdorPrevention() throws Exception {
        mockMvc.perform(get("/api/compromissos/export")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User1 Secret Task")))
                .andExpect(content().string(not(containsString("User2 Private Meeting"))));
    }

    @Test
    @DisplayName("EXPORT SECURITY: CSV Injection Prevention - Formula should be escaped")
    void export_CsvInjectionEscaping() throws Exception {
        // Arrange: Create a dangerous record
        createCompromisso(user1, "=1+1", "OUTROS");

        // Act & Assert
        mockMvc.perform(get("/api/compromissos/export")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("'=1+1"))); // Should contain the escaped prefix
    }
}
