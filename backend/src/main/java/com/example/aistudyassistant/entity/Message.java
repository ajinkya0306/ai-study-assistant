package com.example.aistudyassistant.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    private LocalDateTime createdAt;

    public Message() {}

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Conversation getConversation() { return conversation; }
    public String getRole() { return role; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public void setRole(String role) { this.role = role; }
    public void setContent(String content) { this.content = content; }
}