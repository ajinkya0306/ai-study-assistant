package com.example.aistudyassistant.controller;

import com.example.aistudyassistant.dto.ToolRequest;
import com.example.aistudyassistant.service.ToolService;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tools")
public class ToolController {

    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    @PostMapping("/ask")
    public Map<String, String> ask(@Valid @RequestBody ToolRequest request) {
        return Collections.singletonMap("answer", toolService.ask(request.getQuestion()));
    }
}
