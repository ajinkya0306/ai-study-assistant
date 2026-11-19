package com.example.aistudyassistant.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultSystem("Answer using the supplied study-note context. "
                        + "If the context does not contain the answer, clearly say "
                        + "that the notes do not provide that information. Do not invent facts.")
                .build();
        this.vectorStore = vectorStore;
    }

    public void addDocument(String content, String topic) {

        List<Document> chunks = new ArrayList<>();

        int chunkSize = 1000;
        int overlap = 150;

        int start = 0;

        while (start < content.length()) {

            int end = Math.min(start + chunkSize, content.length());

            String chunkText = content.substring(start, end);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("topic", topic);

            Document document = new Document(chunkText, metadata);

            chunks.add(document);

            if (end == content.length()) {
                break;
            }

            start = end - overlap;
        }

        vectorStore.add(chunks);
    }

    public String ask(String question) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(4)
                .build();

        List<Document> matches = vectorStore.similaritySearch(searchRequest);

        if (matches == null || matches.isEmpty()) {
            return "I could not find relevant information in the study notes. Add a note first or ask a different question.";
        }

        StringBuilder context = new StringBuilder();
        for (Document document : matches) {
            context.append("\n---\n");
            context.append(document.getText());
        }

        return chatClient.prompt()
                .system("Use only this retrieved context to answer:\n" + context)
                .user(question)
                .call()
                .content();
    }

    public void addStarterDocuments() {
        addDocument("HashMap stores data as key-value pairs. Keys are unique. "
                + "HashMap is part of the Java Collections Framework.", "Java Collections");
        addDocument("ArrayList is a resizable-array implementation of the List interface. "
                + "It maintains insertion order and allows duplicate elements.", "Java Collections");
        addDocument("Inheritance is an object-oriented programming concept. "
                + "A child class can inherit accessible fields and methods from a parent class.", "Java OOP");
        addDocument("Spring Boot simplifies Spring application development. "
                + "It provides auto-configuration and embedded-server support.", "Spring Boot");
    }
}
