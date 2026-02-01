package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.backend.config.JwtTokenProvider;
import com.example.backend.entity.RefreshToken;
import com.example.backend.model.Usuario;
import com.example.backend.repository.UsuarioRepository;
import com.example.backend.service.RefreshTokenService;
import com.example.backend.service.RateLimitService;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RateLimitService rateLimitService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid Usuario usuario) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            return ResponseEntity.badRequest().body("Erro: Nome de usuário já existe!");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuarioRepository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário registrado com sucesso!");
    }

    /**
     * SEC-003: Timing Attack Protection
     * 
     * Login endpoint with constant-time execution to prevent user enumeration.
     * Always executes password verification (even for non-existent users) to ensure
     * response time doesn't leak information about valid usernames.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        // SEC-006: Rate limiting - extract client IP
        String clientIp = getClientIp(request);
        
        // Check rate limit before authentication
        if (!rateLimitService.tryConsume(clientIp)) {
            return ResponseEntity.status(429)
                    .body("Too many login attempts. Please try again later.");
        }
        
        try {
            // SEC-003: Authenticate with constant-time behavior
            // Spring Security's BCryptPasswordEncoder already uses constant-time comparison
            // but we ensure the auth manager is always invoked (even for non-existent users)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            String accessToken = tokenProvider.generateAccessToken(authentication);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authentication.getName());

            return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken.getToken(), authentication.getName()));
        } catch (Exception e) {
            // SEC-003: Return generic error message (don't reveal if user exists or password is wrong)
            // Timing is already protected by BCrypt's constant-time comparison
            return ResponseEntity.status(401)
                    .body("Invalid credentials");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshRequest refreshRequest) {
        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(refreshRequest.getRefreshToken())
                    .orElseThrow(() -> new RuntimeException("Refresh token não encontrado!"));
            
            refreshTokenService.verifyExpiration(refreshToken);
            refreshTokenService.markAsUsed(refreshToken);

            String newAccessToken = tokenProvider.generateRefreshToken(refreshToken.getUsername());
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(refreshToken.getUsername());

            return ResponseEntity.ok(new JwtResponse(newAccessToken, newRefreshToken.getToken(), refreshToken.getUsername()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest logoutRequest) {
        refreshTokenService.revokeAllUserTokens(logoutRequest.getUsername());
        return ResponseEntity.ok("Logout realizado com sucesso!");
    }

    // DTOs
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RefreshRequest {
        private String refreshToken;
        
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    /**
     * Extract real client IP from request, considering proxies
     * SEC-006: Rate limiting needs unique client identification
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can be comma-separated, get first IP
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Fallback to direct connection IP
        return request.getRemoteAddr();
    }

    public static class LogoutRequest {
        private String username;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    public static class JwtResponse {
        private final String accessToken;
        private final String refreshToken;
        private final String username;
        private final String tokenType = "Bearer";

        public JwtResponse(String accessToken, String refreshToken, String username) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.username = username;
        }

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public String getUsername() { return username; }
        public String getTokenType() { return tokenType; }
    }
}
