package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.core.env.Environment;

import com.example.backend.repository.UsuarioRepository;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;
    private final Environment environment;

    public SecurityConfig(UsuarioRepository usuarioRepository, Environment environment) {
        this.usuarioRepository = usuarioRepository;
        this.environment = environment;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // SEC-001: CSRF Protection (HIGH priority)
            // SEC-001: CSRF Protection
            // Disabled because we use stateless JWT in Headers (SEC-005).
            // Frontend cannot read CSRF cookie from a different subdomain (Render restriction).
            // Since we don't use session cookies for Auth, CSRF is not a threat.
            .csrf(csrf -> csrf.disable())
            
            // SEC-002: Session Fixation Prevention (HIGH priority)
            // Regenera session ID após autenticação para prevenir session fixation
            .sessionManagement(session -> session
                // JWT é stateless, mas mantemos mínimo de session para CSRF
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                // Prevenir session fixation - regenerar ID após login
                .sessionFixation().newSession()
                // Limite de sessões simultâneas por usuário
                .maximumSessions(1)
                    .expiredUrl("/api/auth/session-expired")
            )
            
            // Security Headers (SEC-004 - OWASP A02:2021)
            .headers(headers -> headers
                // Content Security Policy - Previne XSS e data injection attacks
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                                    "script-src 'self' 'unsafe-inline'; " +
                                    "style-src 'self' 'unsafe-inline'; " +
                                    "img-src 'self' data: https:; " +
                                    "font-src 'self' data:; " +
                                    "connect-src 'self'; " +
                                    "frame-ancestors 'none'")
                )
                
                // HTTP Strict Transport Security - Força HTTPS
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000) // 1 ano
                )
                
                // X-Frame-Options - Previne clickjacking
                .frameOptions(frame -> frame.deny())
                
                // X-Content-Type-Options - Previne MIME sniffing
                .contentTypeOptions(contentType -> {})
                
                // X-XSS-Protection - Proteção XSS para navegadores antigos
                .xssProtection(xss -> {})
            )
            
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                .anyRequest().authenticated()
            );

        // Add our custom JWT security filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        // Add CSRF cookie filter after BasicAuthentication to ensure token is set
        http.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> usuarioRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    
    @Bean
    public org.springframework.security.core.session.SessionRegistry sessionRegistry() {
        return new org.springframework.security.core.session.SessionRegistryImpl();
    }

    @Bean
    public org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy sessionAuthenticationStrategy() {
        org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy concurrentAuthenticationStrategy = 
            new org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy(sessionRegistry());
        concurrentAuthenticationStrategy.setMaximumSessions(1);
        concurrentAuthenticationStrategy.setExceptionIfMaximumExceeded(false);
        
        org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy sessionFixationProtectionStrategy = 
            new org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy();
        
        org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy registerSessionAuthenticationStrategy = 
            new org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy(sessionRegistry());
        
        return new org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy(java.util.Arrays.asList(
            concurrentAuthenticationStrategy,
            sessionFixationProtectionStrategy, 
            registerSessionAuthenticationStrategy
        ));
    }

    /**
     * SEC-009: CORS Hardening
     * 
     * - Allows only specific origins (no wildcard)
     * - Restricts methods to necessary operations
     * - Limits headers to essential ones
     * - Configurable per environment (dev/prod)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Read from application.properties
        String allowedOriginsStr = environment.getProperty("cors.allowed-origins", 
                "http://localhost:5173,http://localhost:3000");
        configuration.setAllowedOrigins(List.of(allowedOriginsStr.split(",")));
        
        String allowedMethodsStr = environment.getProperty("cors.allowed-methods", 
                "GET,POST,PUT,DELETE,PATCH");
        configuration.setAllowedMethods(List.of(allowedMethodsStr.split(",")));
        
        String allowedHeadersStr = environment.getProperty("cors.allowed-headers", 
                "Authorization,Content-Type,X-Requested-With,X-XSRF-TOKEN");
        configuration.setAllowedHeaders(List.of(allowedHeadersStr.split(",")));
        
        String exposedHeadersStr = environment.getProperty("cors.exposed-headers", 
                "Authorization,X-XSRF-TOKEN");
        configuration.setExposedHeaders(List.of(exposedHeadersStr.split(",")));
        
        boolean allowCredentials = environment.getProperty("cors.allow-credentials", 
                Boolean.class, true);
        configuration.setAllowCredentials(allowCredentials);
        
        long maxAge = environment.getProperty("cors.max-age", Long.class, 3600L);
        configuration.setMaxAge(maxAge);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
