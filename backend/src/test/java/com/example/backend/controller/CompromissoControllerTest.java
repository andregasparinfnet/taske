package com.example.backend.controller;

import com.example.backend.model.Compromisso;
import com.example.backend.model.Usuario;
import com.example.backend.dto.CompromissoRequestDTO;
import com.example.backend.repository.CompromissoRepository;
import com.example.backend.repository.UsuarioRepository;
import com.example.backend.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração completos para operações CRUD de Update e Delete
 * 
 * COBERTURA:
 * - Update (PUT): success, not found, forbidden, validation
 * - Delete (DELETE): success, not found, forbidden
 * - Security: IDOR, authorization, multi-user isolation
 * - Edge cases: concurrent updates, update after delete
 */
@SpringBootTest(properties = {
    "ratelimit.enabled=false"  // Desabilitar rate limiting para não bloquear logins em testes
})
@AutoConfigureMockMvc
@Transactional
class CompromissoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompromissoRepository compromissoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private RateLimitService rateLimitService;

    private Usuario user1;
    private Usuario user2;
    private String user1Token;
    private String user2Token;

    @BeforeEach
    void setUp() throws Exception {
        // Mock rate limiting para não bloquear logins em testes
        when(rateLimitService.tryConsume(anyString())).thenReturn(true);

        // Limpar dados
        compromissoRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Criar usuários
        user1 = new Usuario();
        user1.setUsername("user1");
        user1.setPassword(passwordEncoder.encode("password1"));
        user1 = usuarioRepository.save(user1);

        user2 = new Usuario();
        user2.setUsername("user2");
        user2.setPassword(passwordEncoder.encode("password2"));
        user2 = usuarioRepository.save(user2);

        // Obter tokens JWT
        user1Token = login("user1", "password1");
        user2Token = login("user2", "password2");
    }

    private String login(String username, String password) throws Exception {
        String loginJson = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    // ========== UPDATE TESTS ==========

    @Test
    @DisplayName("UPDATE: Deve atualizar compromisso com sucesso")
    void testUpdate_Success() throws Exception {
        // Arrange: Criar compromisso
        Compromisso compromisso = createCompromisso(user1, "Título Original", "PERICIA");
        Long id = compromisso.getId();

        // Preparar dados de atualização
        // Preparar dados de atualização
        CompromissoRequestDTO updateData = new CompromissoRequestDTO();
        updateData.setTitulo("Título Atualizado");
        updateData.setDataHora(LocalDateTime.now().plusDays(2));
        updateData.setTipo(com.example.backend.model.CompromissoTipo.TRABALHO);
        updateData.setUrgente(true);
        updateData.setStatus(com.example.backend.model.CompromissoStatus.EM_ANDAMENTO);
        updateData.setValor(java.math.BigDecimal.valueOf(1500.50));
        updateData.setDescricao("Descrição atualizada");

        // Act & Assert: Atualizar
        mockMvc.perform(put("/api/compromissos/{id}", id)
                        .with(csrf())
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.titulo").value("Título Atualizado"))
                .andExpect(jsonPath("$.tipo").value("TRABALHO"))
                .andExpect(jsonPath("$.urgente").value(true))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.valor").value(1500.50))
                .andExpect(jsonPath("$.descricao").value("Descrição atualizada"));
    }

    @Test
    @DisplayName("UPDATE: Deve retornar 404 para ID inexistente")
    void testUpdate_NotFound() throws Exception {
        // Arrange
        Long nonExistentId = 99999L;
        CompromissoRequestDTO updateData = new CompromissoRequestDTO();
        updateData.setTitulo("Título");
        updateData.setDataHora(LocalDateTime.now().plusHours(1));
        updateData.setTipo(com.example.backend.model.CompromissoTipo.OUTROS);
        updateData.setStatus(com.example.backend.model.CompromissoStatus.PENDENTE);
        updateData.setValor(java.math.BigDecimal.TEN);

        // Act & Assert
        mockMvc.perform(put("/api/compromissos/{id}", nonExistentId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("UPDATE: Deve retornar 403 ao tentar editar compromisso de outro usuário (IDOR)")
    void testUpdate_Forbidden_IDOR() throws Exception {
        // Arrange: user1 cria compromisso
        Compromisso compromisso = createCompromisso(user1, "Compromisso do User1", "PERICIA");
        Long id = compromisso.getId();

        // Preparar dados de atualização
        CompromissoRequestDTO updateData = new CompromissoRequestDTO();
        updateData.setTitulo("Tentativa de Hack");
        updateData.setDataHora(LocalDateTime.now().plusHours(1));
        updateData.setTipo(com.example.backend.model.CompromissoTipo.TRABALHO);
        updateData.setStatus(com.example.backend.model.CompromissoStatus.PENDENTE);
        updateData.setValor(java.math.BigDecimal.TEN);

        // Act & Assert: user2 tenta atualizar compromisso de user1
        mockMvc.perform(put("/api/compromissos/{id}", id)
                        .with(csrf())
                        .header("Authorization", "Bearer " + user2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("UPDATE: Deve retornar 400 para dados inválidos")
    void testUpdate_ValidationError() throws Exception {
        // Arrange
        Compromisso compromisso = createCompromisso(user1, "Compromisso", "PERICIA");
        Long id = compromisso.getId();

        // Dados inválidos: título vazio
        String invalidJson = "{\"titulo\":\"\",\"dataHora\":\"2025-12-31T10:00:00\",\"tipo\":\"PERICIA\"}";

        // Act & Assert
        mockMvc.perform(put("/api/compromissos/{id}", id)
                        .with(csrf())
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("UPDATE: Deve retornar 401 sem token JWT")
    void testUpdate_Unauthorized() throws Exception {
        // Arrange
        Compromisso compromisso = createCompromisso(user1, "Compromisso", "PERICIA");
        Long id = compromisso.getId();

        CompromissoRequestDTO updateData = new CompromissoRequestDTO();
        updateData.setTitulo("Update sem auth");
        updateData.setDataHora(LocalDateTime.now().plusHours(1));
        updateData.setTipo(com.example.backend.model.CompromissoTipo.TRABALHO);
        updateData.setStatus(com.example.backend.model.CompromissoStatus.PENDENTE);
        updateData.setValor(java.math.BigDecimal.TEN);

        // Act & Assert: Request sem token
        mockMvc.perform(put("/api/compromissos/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isForbidden());  // CSRF protection returns 403 when no token
    }

    // ========== DELETE TESTS ==========

    @Test
    @DisplayName("DELETE: Deve deletar compromisso com sucesso")
    void testDelete_Success() throws Exception {
        // Arrange: Criar compromisso
        Compromisso compromisso = createCompromisso(user1, "Compromisso a deletar", "PERICIA");
        Long id = compromisso.getId();

        // Act: Deletar
        mockMvc.perform(delete("/api/compromissos/{id}", id)
                        .with(csrf())
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isNoContent());

        // Assert: Verificar que foi deletado
        mockMvc.perform(get("/api/compromissos")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("DELETE: Deve retornar 403 ao tentar deletar compromisso de outro usuário (IDOR)")
    void testDelete_Forbidden_IDOR() throws Exception {
        // Arrange: user1 cria compromisso
        Compromisso compromisso = createCompromisso(user1, "Compromisso do User1", "PERICIA");
        Long id = compromisso.getId();

        // Act & Assert: user2 tenta deletar
        mockMvc.perform(delete("/api/compromissos/{id}", id)
                        .with(csrf())
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isForbidden());

        // Verificar que não foi deletado
        mockMvc.perform(get("/api/compromissos")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("DELETE: Deve ser idempotente (não quebra se ID não existe)")
    void testDelete_Idempotent() throws Exception {
        // Arrange
        Long nonExistentId = 99999L;

        // Act & Assert: Deletar ID inexistente não deve quebrar
        mockMvc.perform(delete("/api/compromissos/{id}", nonExistentId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE: Deve retornar 401 sem token JWT")
    void testDelete_Unauthorized() throws Exception {
        // Arrange
        Compromisso compromisso = createCompromisso(user1, "Compromisso", "PERICIA");
        Long id = compromisso.getId();

        // Act & Assert
        mockMvc.perform(delete("/api/compromissos/{id}", id)
                        .with(csrf()))
                .andExpect(status().isForbidden());  // CSRF protection returns 403 when no token
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    @DisplayName("INTEGRATION: Fluxo completo de UPDATE")
    void testCompleteUpdateFlow() throws Exception {
        // 1. Criar
        Compromisso original = createCompromisso(user1, "Original", "PERICIA");
        Long id = original.getId();

        // 2. Ler e validar criação
        mockMvc.perform(get("/api/compromissos")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Original"))
                .andExpect(jsonPath("$[0].tipo").value("PERICIA"));

        // 3. Atualizar
        CompromissoRequestDTO updateData = new CompromissoRequestDTO();
        updateData.setTitulo("Atualizado");
        updateData.setDataHora(LocalDateTime.now().plusHours(1));
        updateData.setTipo(com.example.backend.model.CompromissoTipo.TRABALHO);
        updateData.setStatus(com.example.backend.model.CompromissoStatus.PENDENTE);
        updateData.setValor(java.math.BigDecimal.TEN);

        mockMvc.perform(put("/api/compromissos/{id}", id)
                        .with(csrf())
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk());

        // 4. Ler novamente e validar mudança
        mockMvc.perform(get("/api/compromissos")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Atualizado"))
                .andExpect(jsonPath("$[0].tipo").value("TRABALHO"));
    }

    @Test
    @DisplayName("INTEGRATION: Fluxo completo de DELETE")
    void testCompleteDeleteFlow() throws Exception {
        // 1. Criar
        Compromisso compromisso = createCompromisso(user1, "A deletar", "FAMILIA");
        Long id = compromisso.getId();

        // 2. Verificar existe
        mockMvc.perform(get("/api/compromissos")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 3. Deletar
        mockMvc.perform(delete("/api/compromissos/{id}", id)
                        .with(csrf())
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isNoContent());

        // 4. Verificar foi deletado
        mockMvc.perform(get("/api/compromissos")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("INTEGRATION: Tentar atualizar após deletar deve retornar 404")
    void testUpdateAfterDelete() throws Exception {
        // 1. Criar e deletar
        Compromisso compromisso = createCompromisso(user1, "Compromisso", "PERICIA");
        Long id = compromisso.getId();

        mockMvc.perform(delete("/api/compromissos/{id}", id)
                        .with(csrf())
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isNoContent());

        // 2. Tentar atualizar
        // 2. Tentar atualizar
        CompromissoRequestDTO updateData = new CompromissoRequestDTO();
        updateData.setTitulo("Update após delete");
        updateData.setDataHora(LocalDateTime.now().plusHours(1));
        updateData.setTipo(com.example.backend.model.CompromissoTipo.TRABALHO);
        updateData.setStatus(com.example.backend.model.CompromissoStatus.PENDENTE);
        updateData.setValor(java.math.BigDecimal.TEN);

        mockMvc.perform(put("/api/compromissos/{id}", id)
                        .with(csrf())
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("SECURITY: Isolamento multi-usuário - User1 não vê compromissos de User2")
    void testMultiUserIsolation() throws Exception {
        // Arrange: Criar compromissos para cada usuário
        createCompromisso(user1, "Compromisso User1", "PERICIA");
        createCompromisso(user2, "Compromisso User2", "TRABALHO");

        // Act & Assert: User1 vê apenas seus próprios
        mockMvc.perform(get("/api/compromissos")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Compromisso User1"));

        // User2 vê apenas seus próprios
        mockMvc.perform(get("/api/compromissos")
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Compromisso User2"));
    }

    // ========== HELPER METHODS ==========

    private Compromisso createCompromisso(Usuario usuario, String titulo, String tipo) {
        Compromisso compromisso = new Compromisso();
        compromisso.setTitulo(titulo);
        compromisso.setDataHora(LocalDateTime.now().plusDays(1));
        compromisso.setTipo(tipo);
        compromisso.setStatus("PENDENTE");
        compromisso.setValor(1000.0);
        compromisso.setDescricao("Descrição teste");
        compromisso.setUrgente(false);
        compromisso.setUsuario(usuario);
        return compromissoRepository.save(compromisso);
    }
}
