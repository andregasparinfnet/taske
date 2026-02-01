package com.example.backend.controller;

import com.example.backend.config.JwtTokenProvider;
import com.example.backend.config.SecurityConfig;
import com.example.backend.model.Usuario;
import com.example.backend.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
public class AuthControllerTest {

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

    // ==================== TESTES DE REGISTRO ====================

    @Test
    public void deveRegistrarNovoUsuarioComSucesso() throws Exception {
        // Cenário
        Mockito.when(usuarioRepository.existsByUsername("novousuario")).thenReturn(false);
        Mockito.when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptografada");

        String requestBody = """
            {
                "username": "novousuario",
                "password": "senha123"
            }
            """;

        // Ação e Verificação
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().string("Usuário registrado com sucesso!"));
    }

    @Test
    public void deveRetornarErroSeUsuarioJaExiste() throws Exception {
        // Cenário
        Mockito.when(usuarioRepository.existsByUsername("usuarioexistente")).thenReturn(true);

        String requestBody = """
            {
                "username": "usuarioexistente",
                "password": "senha123"
            }
            """;

        // Ação e Verificação
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Erro: Nome de usuário já existe!"));
    }

    // ==================== TESTES DE LOGIN ====================

    @Test
    public void deveFazerLoginComCredenciaisValidas() throws Exception {
        // Cenário
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        Mockito.when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token-valido");

        String requestBody = """
            {
                "username": "usuario",
                "password": "senha123"
            }
            """;

        // Ação e Verificação
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token-valido"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    public void deveRetornarErroComCredenciaisInvalidas() throws Exception {
        // Cenário
        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        String requestBody = """
            {
                "username": "usuario",
                "password": "senhaerrada"
            }
            """;

        // Ação e Verificação
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    // ==================== TESTES DE VALIDAÇÃO ====================

    @Test
    public void deveRetornarErroSeUsernameVazio() throws Exception {
        String requestBody = """
            {
                "username": "",
                "password": "senha123"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deveRetornarErroSeSenhaMuitoCurta() throws Exception {
        String requestBody = """
            {
                "username": "usuario",
                "password": "123"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
