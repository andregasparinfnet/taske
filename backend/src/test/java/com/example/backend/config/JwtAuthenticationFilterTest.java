package com.example.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "tokenProvider", tokenProvider);
        ReflectionTestUtils.setField(filter, "userDetailsService", userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarComTokenValido() throws Exception {
        // Cenário
        String token = "valid.jwt.token";
        String username = "usuario@teste.com";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromJWT(token)).thenReturn(username);
        
        UserDetails userDetails = User.withUsername(username)
                .password("senha")
                .roles("USER")
                .build();
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // Ação
        filter.doFilterInternal(request, response, filterChain);

        // Verificação
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void deveContinuarSemAutenticacaoQuandoSemToken() throws Exception {
        // Cenário
        when(request.getHeader("Authorization")).thenReturn(null);

        // Ação
        filter.doFilterInternal(request, response, filterChain);

        // Verificação
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deveContinuarSemAutenticacaoQuandoTokenInvalido() throws Exception {
        // Cenário
        String token = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(false);

        // Ação
        filter.doFilterInternal(request, response, filterChain);

        // Verificação
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deveContinuarSemAutenticacaoQuandoAuthorizationSemBearer() throws Exception {
        // Cenário
        when(request.getHeader("Authorization")).thenReturn("Basic credentials");

        // Ação
        filter.doFilterInternal(request, response, filterChain);

        // Verificação
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deveContinuarQuandoOcorrerExcecao() throws Exception {
        // Cenário
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenThrow(new RuntimeException("Erro"));

        // Ação
        filter.doFilterInternal(request, response, filterChain);

        // Verificação
        verify(filterChain).doFilter(request, response);
    }
}
