package com.example.aistudyassistant.service;

import com.example.aistudyassistant.tools.CalculatorTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ToolService {

    private final ChatClient chatClient;
    private final CalculatorTools calculatorTools;

    public ToolService(ChatClient.Builder builder, CalculatorTools calculatorTools) {
        this.chatClient = builder
                .defaultSystem("You are a helpful assistant. Use the calculator tools for arithmetic. "
                        + "Explain the result briefly.")
                .build();
        this.calculatorTools = calculatorTools;
    }

    public String ask(String question) {
        return chatClient.prompt()
                .user(question)
                .tools(calculatorTools)
                .call()
                .content();
    }
}
