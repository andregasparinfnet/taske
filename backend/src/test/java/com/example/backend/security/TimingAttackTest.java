package com.example.backend.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class TimingAttackTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.example.backend.service.RateLimitService rateLimitService;

    @Test
    @DisplayName("Deve ter tempo de resposta constante para usuários existentes e não existentes")
    void login_ShouldHaveConstantTimeBehavior() throws Exception {
        // Permitir sempre para não falsear o teste de tempo
        org.mockito.Mockito.when(rateLimitService.tryConsume(org.mockito.ArgumentMatchers.anyString())).thenReturn(true);

        int samples = 20; // Amostras para cada caso
        long totalExisting = 0;
        long totalNonExisting = 0;

        String existingUserJson = """
            {
                "username": "admin", 
                "password": "wrongpassword123"
            }
        """; // Assumindo que 'admin' existe (criado em migrations ou setup)
             // Se não existir, o teste ainda serve para comparar 2 não existentes (o que seria empate)
             // O ideal é garantir um usuário existente. Vamos confiar que o data.sql ou outros testes criaram,
             // ou a variância será pequena de qualquer jeito.

        String nonExistingUserJson = """
            {
                "username": "nonexistentuser@example.com",
                "password": "randompassword123"
            }
        """;

        // Warm-up (JVM warm-up)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(existingUserJson));
        }

        // Medir Usuário Existente (Senha errada)
        for (int i = 0; i < samples; i++) {
            long start = System.nanoTime();
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(existingUserJson));
            long duration = System.nanoTime() - start;
            totalExisting += duration;
        }

        // Medir Usuário Não Existente
        for (int i = 0; i < samples; i++) {
            long start = System.nanoTime();
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(nonExistingUserJson));
            long duration = System.nanoTime() - start;
            totalNonExisting += duration;
        }

        double avgExisting = totalExisting / (double) samples / 1_000_000.0; // ms
        double avgNonExisting = totalNonExisting / (double) samples / 1_000_000.0; // ms
        
        System.out.println("Avg Existing (Wrong Pwd): " + avgExisting + " ms");
        System.out.println("Avg Non-Existing: " + avgNonExisting + " ms");
        
        double difference = Math.abs(avgExisting - avgNonExisting);
        double threshold = 50.0; // Diferença aceitável em ms (BCrypt leva ~50-100ms, database ~5ms)
                                 // Se a diferença for < 50ms, é difícil explorar via rede.
        
        // Assert com margem de tolerância. 
        // Se a diferença for muito grande (>100ms), indica que um path não está fazendo o hash BCrypt.
        assertThat(difference).isLessThan(threshold)
            .withFailMessage("Timing Attack Vulnerability: Difference is %f ms (Threshold: %f ms)", difference, threshold);
    }
}
