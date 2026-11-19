package com.example.aistudyassistant.security;

import com.example.aistudyassistant.repository.RevokedTokenRepository;
import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokenRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            RevokedTokenRepository revokedTokenRepository) {
        this.jwtService = jwtService;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                if (jwtService.isValid(token)) {
                    Claims claims = jwtService.getClaims(token);
                    String jti = claims.getId();

                    boolean revoked =
                            revokedTokenRepository.existsByJti(jti);

                    if (!revoked
                            && SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                        String email = claims.getSubject();

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        email,
                                        null,
                                        Collections.emptyList());

                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);
                    }
                }
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}