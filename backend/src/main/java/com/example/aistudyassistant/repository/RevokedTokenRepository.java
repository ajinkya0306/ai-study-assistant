package com.example.aistudyassistant.repository;

import com.example.aistudyassistant.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository
        extends JpaRepository<RevokedToken, String> {

    boolean existsByJti(String jti);
}