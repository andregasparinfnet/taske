package com.example.backend.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.TestPropertySource(properties = "rate.limit.capacity=100")
public class SessionFixationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve mudar o ID da sessão após login (Session Fixation check)")
    void login_ShouldChangeSessionId() throws Exception {
        // Arrange - Criar uma sessão inicial
        MockHttpSession initialSession = new MockHttpSession();
        String initialSessionId = initialSession.getId();

        // Usuário válido para login
        // NOTA: Assumindo que o usuário 'user' existe no banco de dados de teste (ex: H2)
        // Se não existir, o teste falhará com 401. 
        // Em um teste de segurança isolado, deveríamos criar o usuário antes ou usar mocks.
        // Dado que AuthControllerTest já roda com sucesso, assumimos que o ambiente de teste tem dados ou usa banco em memória.
        // Vamos usar um usuário que sabemos que existe ou criar um, mas para este teste ser robusto, é ideal mockar ou garantir existência.
        // Como é @SpringBootTest, vai carregar o contexto real. Vamos mockar o login se possível ou usar credenciais de teste padrão.
        
        // Pelo AuthControllerTest, vimos que ele usa users/users repository.
        // Vamos tentar logar com um usuário que esperamos existir ou criar um mock se fosse WebMvcTest.
        // Como é SpringBootTest, vamos confiar que 'admin' ou 'test' existe ou falhar e arrumar.
        // O AuthControllerTest usa "validuser".
        
        String loginBody = """
            {
                "username": "validuser",
                "password": "validpassword123"
            }
        """;

        // Primeiro vamos REGISTRAR o usuário para garantir que ele existe
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody)); // Ignoramos o resultado pois pode já existir

        // Act - Fazer Login usando a sessão inicial
        HttpSession sessionAfterLogin = mockMvc.perform(post("/api/auth/login")
                .session(initialSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);

        // Assert
        assertThat(sessionAfterLogin).isNotNull();
        assertThat(sessionAfterLogin.getId()).isNotEqualTo(initialSessionId);
    }
}
