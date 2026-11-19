package com.example.aistudyassistant.controller;

import com.example.aistudyassistant.service.RagService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/rag")
public class DocumentController {

    private final RagService ragService;

    public DocumentController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "General") String topic) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Please select a file."));
            }

            String filename = file.getOriginalFilename();

            if (filename == null) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("error", "Invalid filename."));
            }

            String lowerName = filename.toLowerCase();
            String content;

            if (lowerName.endsWith(".pdf")) {
                try (PDDocument pdf = Loader.loadPDF(file.getBytes())) {
                    content = new PDFTextStripper().getText(pdf);
                }
            } else if (lowerName.endsWith(".txt")) {
                content = new String(file.getBytes(), StandardCharsets.UTF_8);
            } else {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap(
                                "error", "Only PDF and TXT files are supported."));
            }

            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap(
                                "error", "No readable text found in the file."));
            }

            ragService.addDocument(content, topic);

            return ResponseEntity.ok(Collections.singletonMap(
                    "message", "Document uploaded and indexed successfully: " + filename));

        } catch (IOException exception) {
            return ResponseEntity.badRequest().body(
                    Collections.singletonMap("error", "Unable to read the uploaded file."));
        }
    }
}