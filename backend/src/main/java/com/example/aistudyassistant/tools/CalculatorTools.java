package com.example.aistudyassistant.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTools {

    @Tool(description = "Add two numbers and return the sum.")
    public double add(double first, double second) {
        return first + second;
    }

    @Tool(description = "Subtract the second number from the first number.")
    public double subtract(double first, double second) {
        return first - second;
    }

    @Tool(description = "Multiply two numbers and return the product.")
    public double multiply(double first, double second) {
        return first * second;
    }

    @Tool(description = "Divide the first number by the second number. Do not call this when the second number is zero.")
    public double divide(double first, double second) {
        if (second == 0.0) {
            throw new IllegalArgumentException("Cannot divide by zero.");
        }
        return first / second;
    }
}
