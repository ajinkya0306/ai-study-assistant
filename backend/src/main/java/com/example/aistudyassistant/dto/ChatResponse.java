package com.example.aistudyassistant.dto;

public class ChatResponse {

    private Long conversationId;
    private String reply;

    public ChatResponse(Long conversationId, String reply) {
        this.conversationId = conversationId;
        this.reply = reply;
    }

    public Long getConversationId() { return conversationId; }
    public String getReply() { return reply; }
}