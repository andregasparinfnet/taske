package com.example.backend.config;

import java.io.IOException;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * SEC-001: CSRF Protection Filter
 * 
 * Força a geração do CSRF token e o torna disponível para SPAs.
 * O token é enviado em um cookie não-httpOnly para que o JavaScript possa lê-lo.
 * 
 * Pattern: Double Submit Cookie
 * - Cookie: XSRF-TOKEN (readable by JS)
 * - Header: X-XSRF-TOKEN (enviado pelo frontend em cada request)
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain)
            throws ServletException, IOException {
        
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        
        // Force token generation
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        
        filterChain.doFilter(request, response);
    }
}
