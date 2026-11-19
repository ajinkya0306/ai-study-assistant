package com.example.aistudyassistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RagRequest {

    @NotBlank
    @Size(max = 4000)
    private String question;

    public RagRequest() {
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
