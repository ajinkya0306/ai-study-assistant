package com.example.aistudyassistant.controller;

import com.example.aistudyassistant.dto.ChatRequest;
import com.example.aistudyassistant.dto.ChatResponse;
import com.example.aistudyassistant.service.ChatService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {

        return chatService.chat(
                authentication.getName(),
                request.getConversationId(),
                request.getMessage()
        );
    }
}