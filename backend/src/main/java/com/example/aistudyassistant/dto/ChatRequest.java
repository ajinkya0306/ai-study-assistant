package com.example.aistudyassistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatRequest {

    private Long conversationId;

    @NotBlank
    @Size(max = 4000)
    private String message;

    public Long getConversationId() { return conversationId; }
    public String getMessage() { return message; }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}