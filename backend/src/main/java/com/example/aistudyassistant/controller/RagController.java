package com.example.aistudyassistant.controller;

import com.example.aistudyassistant.dto.AddDocumentRequest;
import com.example.aistudyassistant.dto.RagRequest;
import com.example.aistudyassistant.service.RagService;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ask")
    public Map<String, String> ask(@Valid @RequestBody RagRequest request) {
        return Collections.singletonMap(
                "answer", ragService.ask(request.getQuestion()));
    }

    @PostMapping("/documents")
    public Map<String, String> addDocument(
            @Valid @RequestBody AddDocumentRequest request) {
        ragService.addDocument(request.getContent(), request.getTopic());
        return Collections.singletonMap("message", "Document added to the in-memory vector store.");
    }
}
