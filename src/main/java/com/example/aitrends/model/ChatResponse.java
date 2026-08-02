package com.example.aitrends.model;

public class ChatResponse {

    private final String sessionId;
    private final String reply;

    public ChatResponse(String sessionId, String reply) {
        this.sessionId = sessionId;
        this.reply = reply;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getReply() {
        return reply;
    }
}
