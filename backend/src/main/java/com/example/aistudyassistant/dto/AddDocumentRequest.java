package com.example.aistudyassistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddDocumentRequest {

    @NotBlank
    @Size(max = 12000)
    private String content;

    @NotBlank
    @Size(max = 100)
    private String topic;

    public AddDocumentRequest() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
