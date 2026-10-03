package org.example.aibusinessautomation.controller;

import org.example.aibusinessautomation.dto.ChatRequest;
import org.example.aibusinessautomation.dto.ChatResponse;
import org.example.aibusinessautomation.dto.ConversationMessage;
import org.example.aibusinessautomation.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }


    @PostMapping("/chat")
    public ChatResponse chat(
            @RequestBody ChatRequest request) {

        String response =
                chatService.processMessage(
                        request.sessionId(),
                        request.message()
                );

        return new ChatResponse(response);
    }


    @GetMapping("/chat/memory/{sessionId}")
    public List<ConversationMessage> getMemory(
            @PathVariable String sessionId) {

        return chatService.getConversationHistory(
                sessionId
        );
    }
}