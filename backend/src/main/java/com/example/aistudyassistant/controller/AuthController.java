package com.example.aistudyassistant.controller;

import com.example.aistudyassistant.dto.*;
import com.example.aistudyassistant.entity.User;
import com.example.aistudyassistant.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authorization) {

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Bearer token is required."));
        }

        authService.logout(authorization.substring(7));

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully."));
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {

        User user = authService.getUser(authentication.getName());

        return Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail()
        );
    }
}
