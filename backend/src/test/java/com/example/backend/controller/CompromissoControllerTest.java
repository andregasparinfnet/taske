package com.example.backend.controller;

import com.example.backend.config.JwtTokenProvider;
import com.example.backend.config.SecurityConfig;
import com.example.backend.model.Compromisso;
import com.example.backend.model.Usuario;
import com.example.backend.repository.CompromissoRepository;
import com.example.backend.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompromissoController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
public class CompromissoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private CompromissoRepository compromissoRepository;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioLogado;
    private Usuario outroUsuario;
    private Compromisso compromissoExistente;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        usuarioLogado = new Usuario();
        usuarioLogado.setId(1L);
        usuarioLogado.setUsername("teste@user.com");
        usuarioLogado.setPassword("senha123");

        outroUsuario = new Usuario();
        outroUsuario.setId(2L);
        outroUsuario.setUsername("outro@user.com");
        outroUsuario.setPassword("senha456");

        compromissoExistente = new Compromisso();
        compromissoExistente.setId(1L);
        compromissoExistente.setTitulo("Compromisso Existente");
        compromissoExistente.setDataHora(LocalDateTime.now().plusDays(1));
        compromissoExistente.setTipo("PERICIA");
        compromissoExistente.setStatus("PENDENTE");
        compromissoExistente.setUsuario(usuarioLogado);
    }

    // ==================== TESTES DE LISTAGEM ====================

    @Test
    @WithMockUser(username = "teste@user.com")
    public void deveListarApenasCompromissosDoUsuarioLogado() throws Exception {
        List<Compromisso> meusCompromissos = Arrays.asList(compromissoExistente);

        Mockito.when(compromissoRepository.findByUsuarioUsername("teste@user.com"))
               .thenReturn(meusCompromissos);
        Mockito.when(usuarioRepository.findByUsername(anyString()))
               .thenReturn(Optional.of(usuarioLogado));

        mockMvc.perform(get("/api/compromissos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Compromisso Existente"));
    }

    // ==================== TESTES DE CRIAÇÃO ====================

    @Test
    @WithMockUser(username = "teste@user.com")
    public void deveCriarCompromissoComDadosValidos() throws Exception {
        Mockito.when(usuarioRepository.findByUsername("teste@user.com"))
               .thenReturn(Optional.of(usuarioLogado));
        Mockito.when(compromissoRepository.save(any(Compromisso.class)))
               .thenAnswer(invocation -> {
                   Compromisso c = invocation.getArgument(0);
                   c.setId(10L);
                   return c;
               });

        String requestBody = """
            {
                "titulo": "Novo Compromisso",
                "dataHora": "2026-02-15T14:00",
                "tipo": "TRABALHO"
            }
            """;

        mockMvc.perform(post("/api/compromissos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Novo Compromisso"))
                .andExpect(jsonPath("$.tipo").value("TRABALHO"));
    }

    @Test
    @WithMockUser(username = "teste@user.com")
    public void deveRetornarErroSeTituloVazio() throws Exception {
        String requestBody = """
            {
                "titulo": "",
                "dataHora": "2026-02-15T14:00",
                "tipo": "TRABALHO"
            }
            """;

        mockMvc.perform(post("/api/compromissos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "teste@user.com")
    public void deveRetornarErroSeDataNula() throws Exception {
        String requestBody = """
            {
                "titulo": "Compromisso Sem Data",
                "tipo": "TRABALHO"
            }
            """;

        mockMvc.perform(post("/api/compromissos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // ==================== TESTES DE ATUALIZAÇÃO ====================

    @Test
    @WithMockUser(username = "teste@user.com")
    public void deveAtualizarCompromissoDoProprioUsuario() throws Exception {
        Mockito.when(compromissoRepository.findById(1L))
               .thenReturn(Optional.of(compromissoExistente));
        Mockito.when(compromissoRepository.save(any(Compromisso.class)))
               .thenAnswer(invocation -> invocation.getArgument(0));

        String requestBody = """
            {
                "titulo": "Título Atualizado",
                "dataHora": "2026-02-20T10:00",
                "tipo": "FAMILIA",
                "status": "EM_ANDAMENTO"
            }
            """;

        mockMvc.perform(put("/api/compromissos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Título Atualizado"))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    @Test
    @WithMockUser(username = "teste@user.com")
    public void deveNegarAcessoAoCompromissoDeOutroUsuario() throws Exception {
        // Compromisso pertence a outro usuário
        compromissoExistente.setUsuario(outroUsuario);

        Mockito.when(compromissoRepository.findById(1L))
               .thenReturn(Optional.of(compromissoExistente));

        String requestBody = """
            {
                "titulo": "Tentativa de Hack",
                "dataHora": "2026-02-20T10:00",
                "tipo": "TRABALHO"
            }
            """;

        mockMvc.perform(put("/api/compromissos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teste@user.com")
    public void deveRetornar404SeCompromissoNaoExiste() throws Exception {
        Mockito.when(compromissoRepository.findById(999L))
               .thenReturn(Optional.empty());

        String requestBody = """
            {
                "titulo": "Qualquer Título",
                "dataHora": "2026-02-20T10:00",
                "tipo": "TRABALHO"
            }
            """;

        mockMvc.perform(put("/api/compromissos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    // ==================== TESTES DE DELEÇÃO ====================

    @Test
    @WithMockUser(username = "teste@user.com")
    public void deveDeletarCompromissoDoProprioUsuario() throws Exception {
        Mockito.when(compromissoRepository.findById(1L))
               .thenReturn(Optional.of(compromissoExistente));

        mockMvc.perform(delete("/api/compromissos/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(compromissoRepository).deleteById(1L);
    }

    @Test
    @WithMockUser(username = "teste@user.com")
    public void deveNegarDelecaoDeCompromissoDeOutroUsuario() throws Exception {
        // Compromisso pertence a outro usuário
        compromissoExistente.setUsuario(outroUsuario);

        Mockito.when(compromissoRepository.findById(1L))
               .thenReturn(Optional.of(compromissoExistente));

        mockMvc.perform(delete("/api/compromissos/1"))
                .andExpect(status().isForbidden());
    }
}
