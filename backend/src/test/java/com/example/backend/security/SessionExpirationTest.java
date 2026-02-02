package com.example.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.backend.model.Usuario;
import com.example.backend.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.TestPropertySource(properties = "rate.limit.capacity=100")
public class SessionExpirationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_USERNAME = "sessionuser@example.com";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        // Garantir que o usuário existe
        if (!usuarioRepository.existsByUsername(TEST_USERNAME)) {
            Usuario user = new Usuario();
            user.setUsername(TEST_USERNAME);
            user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
            usuarioRepository.save(user);
        }
    }

    @Test
    @DisplayName("Deve invalidar a primeira sessão quando um segundo login ocorre (Max Sessions = 1)")
    void whenSecondLogin_FirstSessionShouldExpire() throws Exception {
        String loginBody = """
            {
                "username": "%s",
                "password": "%s"
            }
        """.formatted(TEST_USERNAME, TEST_PASSWORD);

        // 1. Primeiro Login
        MvcResult firstLoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        
        HttpSession firstSession = firstLoginResult.getRequest().getSession(false);
        assertThat(firstSession).isNotNull();
        String firstSessionId = firstSession.getId();

        // 2. Verificar que a primeira sessão está ativa (acessando recurso protegido)
        // Usamos um endpoint qualquer autenticado, ex: /api/compromissos (se existir) ou o próprio /api/auth/refresh (que requer autenticação no filtro customizado ou sessao valida)
        // Como o SecurityConfig protege anyRequest().authenticated(), qualquer endpoint protegido serve.
        // Vamos tentar acessar um endpoint que sabemos que existe ou deve responder 403/404 mas passar pela auth.
        // O endpoint /api/auth/logout requer autenticação.
        
        // NOTA: Para testar session concurrency no Spring Security com MockMvc, precisamos garantir que o SessionRegistry está sendo notificado.
        // O MockMvc não é um container servlet real, mas emulamos enviando sessões.

        // 3. Segundo Login (mesmo usuário)
        MvcResult secondLoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        HttpSession secondSession = secondLoginResult.getRequest().getSession(false);
        assertThat(secondSession).isNotNull();
        assertThat(secondSession.getId()).isNotEqualTo(firstSessionId); // Sessão deve ser diferente (Fixation protection)

        // 4. Tentar usar a PRIMEIRA sessão novamente
        // O comportamento esperado configurado é .expiredUrl("/api/auth/session-expired")
        // Isso normalmente retorna um redirect ou um request forward.
        
        mockMvc.perform(get("/api/auth/logout")
                .session((MockHttpSession) firstSession)
                .with(csrf()))
                .andExpect(redirectedUrl("/api/auth/session-expired"));
                
        // Vamos fazer assertions mais específicas observando o resultado.
        // Se a sessão expirou, o SessionManagementFilter deve detectar.
    }
}
