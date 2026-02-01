package com.example.backend.config;

import com.example.backend.model.Usuario;
import com.example.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityConfigTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    @Test
    void deveRetornarPasswordEncoderBCrypt() {
        // Cenário
        SecurityConfig securityConfig = new SecurityConfig(usuarioRepository);

        // Ação
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // Verificação
        assertNotNull(encoder);
        String encoded = encoder.encode("password");
        assertTrue(encoder.matches("password", encoded));
    }

    @Test
    void deveCriarJwtAuthenticationFilter() {
        // Cenário
        SecurityConfig securityConfig = new SecurityConfig(usuarioRepository);

        // Ação
        JwtAuthenticationFilter filter = securityConfig.jwtAuthenticationFilter();

        // Verificação
        assertNotNull(filter);
    }

    @Test
    void deveRetornarCorsConfigurationSource() {
        // Cenário
        SecurityConfig securityConfig = new SecurityConfig(usuarioRepository);

        // Ação
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        // Verificação
        assertNotNull(source);
    }

    @Test
    void deveCarregarUserDetailsQuandoUsuarioExiste() {
        // Cenário
        SecurityConfig securityConfig = new SecurityConfig(usuarioRepository);
        Usuario usuario = new Usuario();
        usuario.setUsername("teste@user.com");
        usuario.setPassword("senhaEncoded");

        when(usuarioRepository.findByUsername("teste@user.com")).thenReturn(Optional.of(usuario));

        // Ação
        UserDetailsService userDetailsService = securityConfig.userDetailsService();
        UserDetails userDetails = userDetailsService.loadUserByUsername("teste@user.com");

        // Verificação
        assertNotNull(userDetails);
        assertEquals("teste@user.com", userDetails.getUsername());
        assertEquals("senhaEncoded", userDetails.getPassword());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        // Cenário
        SecurityConfig securityConfig = new SecurityConfig(usuarioRepository);
        when(usuarioRepository.findByUsername("naoexiste@user.com")).thenReturn(Optional.empty());

        // Ação
        UserDetailsService userDetailsService = securityConfig.userDetailsService();

        // Verificação
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("naoexiste@user.com");
        });
    }

    @Test
    void deveRetornarAuthenticationManager() throws Exception {
        // Cenário
        SecurityConfig securityConfig = new SecurityConfig(usuarioRepository);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        // Ação
        AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);

        // Verificação
        assertNotNull(result);
        assertEquals(authenticationManager, result);
    }
}
