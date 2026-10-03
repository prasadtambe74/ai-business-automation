package org.example.aibusinessautomation.ai;

import org.example.aibusinessautomation.dto.ConversationMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OllamaService {

    private static final String MODEL = "qwen2.5:1.5b";

    private final RestClient restClient;

    public OllamaService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    // =========================================================
    // DAY 1 - AI RESPONSE GENERATION
    // =========================================================

    public String generateResponse(
            String message,
            List<ConversationMessage> history,
            String intent,
            String flow) {

        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of(
                "role", "system",
                "content",
                """
                You are a helpful AI business customer support assistant.

                Give short, clear and useful answers.
                Prefer 1 to 3 sentences.
                Do not repeat the user's question.
                Do not invent business information.
                If information is unavailable, say so clearly.

                The application has already classified the user's intent.

                Use the intent only as supporting context.
                Do not change the intent yourself.

                The current application intent is:
                %s

                The current application flow is:
                %s

                Use conversation history when the current message
                depends on previous conversation context.

                If the user provides personal information such as
                their name, remember it during this conversation.
                """.formatted(intent, flow)
        ));

        addHistory(messages, history);

        messages.add(Map.of(
                "role", "user",
                "content", message
        ));

        Map<String, Object> request = Map.of(
                "model", MODEL,
                "messages", messages,
                "stream", false,
                "options", Map.of(
                        "temperature", 0,
                        "seed", 42,
                        "num_predict", 80
                )
        );

        try {

            Map<?, ?> response = restClient.post()
                    .uri("/api/chat")
                    .body(request)
                    .retrieve()
                    .body(Map.class);

            return extractResponseContent(response);

        } catch (Exception e) {

            System.err.println(
                    "Ollama response generation failed: "
                            + e.getMessage()
            );

            return "The AI service is temporarily unavailable. Please try again.";
        }
    }


    // =========================================================
    // DAY 2 - AI INTENT DETECTION
    // =========================================================

    public String detectIntent(
            String message,
            List<ConversationMessage> history) {

        List<Map<String, String>> messages = new ArrayList<>();

        String systemPrompt = """
                You are an AI intent classifier for a business chatbot.

                Your task is to understand the USER'S GOAL.

                Classify the CURRENT USER MESSAGE into exactly ONE
                of the following intents:

                GREETING
                FAQ
                LEAD
                PRODUCT_SEARCH
                ORDER_STATUS
                SUPPORT
                APPOINTMENT
                HUMAN_HANDOFF
                GOODBYE
                UNKNOWN


                INTENT DEFINITIONS:


                GREETING:
                The user is starting a conversation or greeting.

                Examples:
                "Hi"
                "Hello"
                "Good morning"


                FAQ:
                The user wants general information about the company,
                services, industries, location, policies, hours,
                or capabilities.

                Examples:
                "What services do you provide?"
                "Which industries do you work with?"
                "Where are you located?"


                PRODUCT_SEARCH:
                The user wants to find, search, show, filter,
                select, or compare a specific product, property,
                package, or service offering.

                Examples:
                "Find a 2BHK in Pune."
                "Show me properties under 70 lakhs."
                "Show me restaurant packages."


                LEAD:
                The user shows buying/business interest,
                requests a quote, wants to proceed,
                or wants to be contacted.

                Examples:
                "I want to get a quote."
                "Please contact me."
                "I am interested in your service."


                ORDER_STATUS:
                The user wants information about the CURRENT STATE
                of an existing order, booking, shipment, delivery,
                or previously created transaction.

                Examples:
                "Where is my order?"
                "What is the status of my booking?"
                "My booking isn't showing in my account."

                IMPORTANT:
                ORDER_STATUS is about checking an existing item.


                SUPPORT:
                The user reports a PROBLEM, ERROR, FAILURE,
                ISSUE, or difficulty and wants assistance.

                Examples:
                "My booking has a problem."
                "There is an issue with my payment."
                "The website is not working."

                IMPORTANT:
                If the user is reporting a problem,
                choose SUPPORT even if words such as
                booking, order, appointment, or payment appear.


                APPOINTMENT:
                The user wants to CREATE, SCHEDULE, RESCHEDULE,
                or CANCEL an appointment, consultation,
                meeting, property visit, or other future activity.

                Examples:
                "I want to book a property visit."
                "Can I schedule an appointment tomorrow?"
                "I want to reschedule my consultation."

                IMPORTANT:
                APPOINTMENT means the user wants to take
                an appointment-related action.

                It does NOT mean the user is reporting a
                problem with an existing booking.


                HUMAN_HANDOFF:
                The user wants to speak with, connect with,
                or be transferred to a human, agent,
                representative, employee, or staff member.

                Examples:
                "Can I speak to someone?"
                "Connect me with an agent."
                "I want to talk to a human."


                GOODBYE:
                The user is ending the conversation.

                Examples:
                "Bye"
                "Thanks, goodbye"
                "That's all."


                UNKNOWN:
                The user's intent cannot be determined clearly.


                IMPORTANT CLASSIFICATION RULES:

                1. The CURRENT USER MESSAGE is the primary source.

                2. Conversation history is supporting context.

                3. Use history when the current message depends
                   on previous conversation.

                4. If the current message contains a clear new
                   request, prioritize the new request.

                5. Do not classify based only on a single keyword.

                6. Understand the user's goal and situation.

                7. Return ONLY one intent name.

                8. Do not explain your answer.

                9. Do not return multiple intents.


                CONTEXT EXAMPLES:

                Previous:
                "I want to schedule a property visit."

                Current:
                "Tomorrow afternoon."

                Result:
                APPOINTMENT


                Previous:
                "My order isn't showing."

                Current:
                "12345"

                Result:
                ORDER_STATUS


                Previous:
                "I have a payment problem."

                Current:
                "Can I speak to someone?"

                Result:
                HUMAN_HANDOFF


                SEMANTIC DISTINCTIONS:

                "I want to book a property visit."
                → APPOINTMENT

                "I want to schedule a property visit."
                → APPOINTMENT

                "My property booking has a problem."
                → SUPPORT

                "There is a problem with my booking."
                → SUPPORT

                "My booking isn't showing."
                → ORDER_STATUS

                "What is the status of my booking?"
                → ORDER_STATUS

                "Find properties in Pune."
                → PRODUCT_SEARCH

                "Which industries do you work with?"
                → FAQ

                "What services do you provide?"
                → FAQ

                "I want a quote."
                → LEAD

                "Can someone contact me?"
                → LEAD

                "I want to speak with an agent."
                → HUMAN_HANDOFF
                """;

        StringBuilder fullSystemPrompt = new StringBuilder(systemPrompt);

        addHistoryToPrompt(fullSystemPrompt, history);

        messages.add(Map.of(
                "role", "system",
                "content", fullSystemPrompt.toString()
        ));

        messages.add(Map.of(
                "role", "user",
                "content", message
        ));

        Map<String, Object> request = Map.of(
                "model", MODEL,
                "messages", messages,
                "stream", false,
                "options", Map.of(
                        "temperature", 0,
                        "seed", 42,
                        "num_predict", 10
                )
        );

        try {

            Map<?, ?> response = restClient.post()
                    .uri("/api/chat")
                    .body(request)
                    .retrieve()
                    .body(Map.class);

            String result = extractResponseContent(response);

            if (result == null || result.isBlank()) {
                return "UNKNOWN";
            }

            return result.trim();

        } catch (Exception e) {

            System.err.println(
                    "Ollama intent detection failed: "
                            + e.getMessage()
            );

            return "UNKNOWN";
        }
    }


    // =========================================================
    // HELPER METHODS
    // =========================================================

    private void addHistory(
            List<Map<String, String>> messages,
            List<ConversationMessage> history) {

        if (history == null || history.isEmpty()) {
            return;
        }

        for (ConversationMessage msg : history) {

            if (msg == null ||
                    msg.role() == null ||
                    msg.content() == null) {
                continue;
            }

            String role = msg.role().trim().toLowerCase();

            if (role.equals("user") ||
                    role.equals("assistant")) {

                messages.add(Map.of(
                        "role", role,
                        "content", msg.content()
                ));
            }
        }
    }


    private void addHistoryToPrompt(
            StringBuilder prompt,
            List<ConversationMessage> history) {

        if (history == null || history.isEmpty()) {
            return;
        }

        prompt.append("\n\nCONVERSATION HISTORY:\n");

        for (ConversationMessage msg : history) {

            if (msg == null ||
                    msg.role() == null ||
                    msg.content() == null) {
                continue;
            }

            String role = msg.role().trim().toUpperCase();

            if (role.equals("USER") ||
                    role.equals("ASSISTANT")) {

                prompt.append(role)
                        .append(": ")
                        .append(msg.content())
                        .append("\n");
            }
        }

        prompt.append(
                "\nUse the conversation history only when the "
                        + "current message depends on previous context.\n"
        );
    }


    private String extractResponseContent(Map<?, ?> response) {

        if (response == null) {
            return null;
        }

        Object messageObject = response.get("message");

        if (!(messageObject instanceof Map<?, ?> responseMessage)) {
            return null;
        }

        Object content = responseMessage.get("content");

        if (content == null) {
            return null;
        }

        return content.toString().trim();
    }
}