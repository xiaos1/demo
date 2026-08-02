package com.example.aitrends.controller;

import com.example.aitrends.model.ChatRequest;
import com.example.aitrends.model.ChatResponse;
import com.example.aitrends.service.ElizaService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ElizaController {

    private final ElizaService elizaService;

    public ElizaController(ElizaService elizaService) {
        this.elizaService = elizaService;
    }

    @GetMapping("/api/eliza/greeting")
    public ChatResponse greeting() {
        return new ChatResponse(null, elizaService.greeting());
    }

    @PostMapping("/api/eliza/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String sessionId = (request.getSessionId() == null || request.getSessionId().isBlank())
                ? "default"
                : request.getSessionId();
        String reply = elizaService.respond(sessionId, request.getMessage());
        return new ChatResponse(sessionId, reply);
    }

    @DeleteMapping("/api/eliza/session/{sessionId}")
    public void reset(@PathVariable String sessionId) {
        elizaService.reset(sessionId);
    }
}
