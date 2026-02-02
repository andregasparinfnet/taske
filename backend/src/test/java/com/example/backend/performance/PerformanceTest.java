package com.example.backend.performance;

import com.example.backend.model.Usuario;
import com.example.backend.repository.UsuarioRepository;
import com.example.backend.config.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.example.backend.repository.CompromissoRepository compromissoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;

    @BeforeEach
    void setUp() {
        // Clean DB in order
        compromissoRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Create User
        Usuario user = new Usuario();
        user.setUsername("perf_user");
        user.setPassword(passwordEncoder.encode("password123"));
        usuarioRepository.save(user);

        // Generate Token
        accessToken = jwtTokenProvider.generateAccessToken(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                user.getUsername(), null, java.util.Collections.emptyList()
            )
        );
    }

    @Test
    void createCompromisso_ShouldRespondWithin200ms() throws Exception {
        Map<String, Object> compromissoMap = new HashMap<>();
        compromissoMap.put("titulo", "Perf Test");
        compromissoMap.put("dataHora", LocalDateTime.now().plusDays(1).toString());
        compromissoMap.put("tipo", "TRABALHO");
        compromissoMap.put("status", "PENDENTE");

        String jsonInfo = objectMapper.writeValueAsString(compromissoMap);

        // Warmup
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/compromissos")
                    .with(csrf())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonInfo))
                    .andExpect(status().isCreated());
        }

        // Measure
        long totalTime = 0;
        int iterations = 20;

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            mockMvc.perform(post("/api/compromissos")
                    .with(csrf())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonInfo))
                    .andExpect(status().isCreated());
            totalTime += (System.nanoTime() - start);
        }

        double averageTimeMs = (totalTime / (double) iterations) / 1_000_000.0;
        System.out.println("Average Create Compromisso Time: " + averageTimeMs + " ms");
        
        assertThat(averageTimeMs).isLessThan(200.0);
    }

    @Test
    void listCompromissos_ShouldRespondWithin200ms() throws Exception {
        // Create 1 compromisso to have something to list
        Map<String, Object> compromissoMap = new HashMap<>();
        compromissoMap.put("titulo", "Perf Test List");
        compromissoMap.put("dataHora", LocalDateTime.now().plusDays(1).toString());
        compromissoMap.put("tipo", "TRABALHO");
        compromissoMap.put("status", "PENDENTE");
        
        mockMvc.perform(post("/api/compromissos")
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compromissoMap)))
                .andExpect(status().isCreated());

        // Warmup
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/compromissos")
                    .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk());
        }

        // Measure
        long totalTime = 0;
        int iterations = 20;

        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            mockMvc.perform(get("/api/compromissos")
                    .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk());
            totalTime += (System.nanoTime() - start);
        }

        double averageTimeMs = (totalTime / (double) iterations) / 1_000_000.0;
        System.out.println("Average List Compromissos Time: " + averageTimeMs + " ms");
        
        assertThat(averageTimeMs).isLessThan(200.0);
    }
}
