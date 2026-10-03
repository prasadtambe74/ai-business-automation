package org.example.aibusinessautomation.service;

import org.example.aibusinessautomation.dto.ConversationMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationMemoryService {

    private final Map<String, List<ConversationMessage>> conversations =
            new ConcurrentHashMap<>();


    public List<ConversationMessage> getHistory(
            String sessionId) {

        if (sessionId == null || sessionId.isBlank()) {
            return new ArrayList<>();
        }

        List<ConversationMessage> history =
                conversations.get(sessionId.trim());

        if (history == null) {
            return new ArrayList<>();
        }

        synchronized (history) {
            return new ArrayList<>(history);
        }
    }


    public void addMessage(
            String sessionId,
            String role,
            String content) {

        if (sessionId == null ||
                sessionId.isBlank() ||
                role == null ||
                role.isBlank() ||
                content == null) {

            return;
        }

        String normalizedSessionId = sessionId.trim();

        List<ConversationMessage> history =
                conversations.computeIfAbsent(
                        normalizedSessionId,
                        key -> new ArrayList<>()
                );

        synchronized (history) {

            history.add(
                    new ConversationMessage(
                            role.trim().toLowerCase(),
                            content
                    )
            );
        }
    }


    public void clearHistory(String sessionId) {

        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        conversations.remove(sessionId.trim());
    }
}