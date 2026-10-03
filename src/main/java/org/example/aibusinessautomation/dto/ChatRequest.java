package org.example.aibusinessautomation.dto;

public record ChatRequest(
        String sessionId,
        String message
) {
}