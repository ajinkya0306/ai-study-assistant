package com.example.aistudyassistant.service;

import com.example.aistudyassistant.dto.*;
import com.example.aistudyassistant.entity.RevokedToken;
import com.example.aistudyassistant.entity.User;
import com.example.aistudyassistant.repository.RevokedTokenRepository;
import com.example.aistudyassistant.repository.UserRepository;
import com.example.aistudyassistant.security.JwtService;

import io.jsonwebtoken.Claims;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RevokedTokenRepository revokedTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists.");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(request.getPassword()));

        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail());
    }

    public AuthResponse login(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid email or password."));

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException(
                    "Invalid email or password.");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail());
    }

    @Transactional
    public void logout(String token) {

        Claims claims = jwtService.getClaims(token);
        String jti = claims.getId();

        if (!revokedTokenRepository.existsByJti(jti)) {
            Date expiration = claims.getExpiration();

            LocalDateTime expiresAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(expiration.getTime()),
                    ZoneOffset.UTC);

            revokedTokenRepository.save(
                    new RevokedToken(jti, expiresAt));
        }
    }

    public User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found."));
    }
}