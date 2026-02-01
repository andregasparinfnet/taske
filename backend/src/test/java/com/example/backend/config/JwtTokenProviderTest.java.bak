package com.example.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
    }

    @Test
    public void deveGerarTokenValido() {
        // Cenário
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "usuario@teste.com",
                "senha",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Ação
        String token = tokenProvider.generateToken(authentication);

        // Verificação
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tem 3 partes separadas por ponto
    }

    @Test
    public void deveValidarTokenValido() {
        // Cenário
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "usuario@teste.com",
                "senha",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = tokenProvider.generateToken(authentication);

        // Ação
        boolean isValid = tokenProvider.validateToken(token);

        // Verificação
        assertTrue(isValid);
    }

    @Test
    public void deveRejeitarTokenInvalido() {
        // Ação
        boolean isValid = tokenProvider.validateToken("token.invalido.aqui");

        // Verificação
        assertFalse(isValid);
    }

    @Test
    public void deveRejeitarTokenMalformado() {
        // Ação
        boolean isValid = tokenProvider.validateToken("tokenmalformadosempontos");

        // Verificação
        assertFalse(isValid);
    }

    @Test
    public void deveRejeitarTokenNulo() {
        // Ação
        boolean isValid = tokenProvider.validateToken(null);

        // Verificação
        assertFalse(isValid);
    }

    @Test
    public void deveExtrairUsernameDoToken() {
        // Cenário
        String expectedUsername = "usuario@teste.com";
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                expectedUsername,
                "senha",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = tokenProvider.generateToken(authentication);

        // Ação
        String username = tokenProvider.getUsernameFromJWT(token);

        // Verificação
        assertEquals(expectedUsername, username);
    }

    @Test
    public void deveGerarTokensDiferentesParaUsuariosDiferentes() {
        // Cenário
        Authentication auth1 = new UsernamePasswordAuthenticationToken(
                "usuario1@teste.com", "senha",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication auth2 = new UsernamePasswordAuthenticationToken(
                "usuario2@teste.com", "senha",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Ação
        String token1 = tokenProvider.generateToken(auth1);
        String token2 = tokenProvider.generateToken(auth2);

        // Verificação
        assertNotEquals(token1, token2);
    }
}
