package com.example.aistudyassistant.controller;

import com.example.aistudyassistant.dto.ConversationResponse;
import com.example.aistudyassistant.entity.Conversation;
import com.example.aistudyassistant.entity.Message;
import com.example.aistudyassistant.entity.User;
import com.example.aistudyassistant.repository.ConversationRepository;
import com.example.aistudyassistant.repository.MessageRepository;
import com.example.aistudyassistant.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ConversationController(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            UserRepository userRepository) {

        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    // Get all conversations for the logged-in user.
    @GetMapping
    public List<ConversationResponse> getConversations(
            Authentication authentication) {

        User user = getUser(authentication.getName());

        return conversationRepository
                .findByUserIdOrderByUpdatedAtDesc(user.getId())
                .stream()
                .map(conversation -> new ConversationResponse(
                        conversation.getId(),
                        conversation.getTitle(),
                        conversation.getCreatedAt(),
                        conversation.getUpdatedAt()
                ))
                .toList();
    }

    // Get one conversation and its messages.
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getConversation(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getUser(authentication.getName());

        Conversation conversation = conversationRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Conversation not found."));

        List<Map<String, Object>> messages =
                messageRepository
                        .findByConversationIdOrderByCreatedAtAsc(id)
                        .stream()
                        .map(this::toMessageMap)
                        .toList();

        return ResponseEntity.ok(Map.of(
                "id", conversation.getId(),
                "title", conversation.getTitle(),
                "messages", messages
        ));
    }

    // Delete a conversation owned by the logged-in user.
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteConversation(
            @PathVariable Long id,
            Authentication authentication) {

        User user = getUser(authentication.getName());

        Conversation conversation = conversationRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Conversation not found."));

        messageRepository.deleteAll(
                messageRepository.findByConversationIdOrderByCreatedAtAsc(id));

        conversationRepository.delete(conversation);

        return ResponseEntity.ok(
                Map.of("message", "Conversation deleted successfully."));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found."));
    }

    private Map<String, Object> toMessageMap(Message message) {
        return Map.of(
                "id", message.getId(),
                "role", message.getRole(),
                "content", message.getContent(),
                "createdAt", message.getCreatedAt()
        );
    }
}