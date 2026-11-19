package com.example.aistudyassistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ToolRequest {

    @NotBlank
    @Size(max = 2000)
    private String question;

    public ToolRequest() {
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
