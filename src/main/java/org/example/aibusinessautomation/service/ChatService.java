package org.example.aibusinessautomation.service;

import org.example.aibusinessautomation.ai.OllamaService;
import org.example.aibusinessautomation.dto.ConversationMessage;
import org.example.aibusinessautomation.intent.Intent;
import org.example.aibusinessautomation.intent.IntentDetector;
import org.example.aibusinessautomation.intent.IntentRouter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final OllamaService ollamaService;
    private final ConversationMemoryService memoryService;
    private final IntentDetector intentDetector;
    private final IntentRouter intentRouter;

    public ChatService(
            OllamaService ollamaService,
            ConversationMemoryService memoryService,
            IntentDetector intentDetector,
            IntentRouter intentRouter) {

        this.ollamaService = ollamaService;
        this.memoryService = memoryService;
        this.intentDetector = intentDetector;
        this.intentRouter = intentRouter;
    }


    public String processMessage(
            String sessionId,
            String message) {

        // =====================================================
        // 1. BASIC INPUT VALIDATION
        // =====================================================

        if (sessionId == null || sessionId.isBlank()) {
            return "Session ID is required.";
        }

        if (message == null || message.isBlank()) {
            return "Please enter a message.";
        }

        sessionId = sessionId.trim();
        message = message.trim();


        // =====================================================
        // 2. GET PREVIOUS CONVERSATION
        // =====================================================

        List<ConversationMessage> history =
                memoryService.getHistory(sessionId);


        // =====================================================
        // 3. DETECT INTENT USING AI
        // =====================================================

        Intent intent =
                intentDetector.detectIntent(
                        message,
                        history
                );


        // =====================================================
        // 4. ROUTE INTENT
        // =====================================================

        String flow =
                intentRouter.route(intent);


        System.out.println(
                "Detected Intent: " + intent
        );

        System.out.println(
                "Selected Flow: " + flow
        );


        // =====================================================
        // 5. GENERATE RESPONSE
        // =====================================================

        String response =
                ollamaService.generateResponse(
                        message,
                        history,
                        intent.name(),
                        flow
                );


        // =====================================================
        // 6. SAVE USER MESSAGE
        // =====================================================

        memoryService.addMessage(
                sessionId,
                "user",
                message
        );


        // =====================================================
        // 7. SAVE AI RESPONSE
        // =====================================================

        memoryService.addMessage(
                sessionId,
                "assistant",
                response
        );


        // =====================================================
        // 8. RETURN RESPONSE
        // =====================================================

        return response;
    }


    public List<ConversationMessage> getConversationHistory(
            String sessionId) {

        return memoryService.getHistory(sessionId);
    }
}