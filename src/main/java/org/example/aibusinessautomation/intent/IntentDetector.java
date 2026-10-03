package org.example.aibusinessautomation.intent;

import org.example.aibusinessautomation.ai.OllamaService;
import org.example.aibusinessautomation.dto.ConversationMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IntentDetector {

    private final OllamaService ollamaService;

    public IntentDetector(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    public Intent detectIntent(
            String message,
            List<ConversationMessage> history) {

        String result = ollamaService.detectIntent(
                message,
                history
        );

        if (result == null || result.isBlank()) {
            return Intent.UNKNOWN;
        }

        String normalized = result
                .trim()
                .toUpperCase()
                .replace("-", "_")
                .replace(" ", "_");

        try {

            return Intent.valueOf(normalized);

        } catch (IllegalArgumentException e) {

            System.err.println(
                    "Invalid AI intent returned: " + result
            );

            return Intent.UNKNOWN;
        }
    }
}