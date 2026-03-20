package com.documentreader.ai;

import com.documentreader.config.DocumentReaderProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OpenAiChatService {

    private static final String SYSTEM_PROMPT =
            "You write clear prose for Word documents. Follow the user's instructions. "
                    + "Refuse requests for illegal or harmful content with a short explanation only.";

    private static final Duration HTTP_TIMEOUT = Duration.ofMinutes(5);

    private final DocumentReaderProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();

    public OpenAiChatService(DocumentReaderProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String generateFromPrompt(String userPrompt, String optionalPdfContext, boolean usePdfContext) {
        if (!properties.getAi().isConfigured()) {
            throw new AiUnavailableException("OPENAI_API_KEY is not configured");
        }
        DocumentReaderProperties.Ai ai = properties.getAi();
        String userContent = buildUserContent(userPrompt, optionalPdfContext, usePdfContext);
        Map<String, Object> requestBody =
                Map.of(
                        "model",
                        ai.getModel(),
                        "messages",
                        List.of(
                                Map.of("role", "system", "content", SYSTEM_PROMPT),
                                Map.of("role", "user", "content", userContent)));

        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            URI uri = URI.create(trimTrailingSlash(ai.getBaseUrl()) + "/v1/chat/completions");
            HttpRequest request =
                    HttpRequest.newBuilder(uri)
                            .timeout(HTTP_TIMEOUT)
                            .header("Authorization", "Bearer " + ai.getApiKey())
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AiUnavailableException("AI provider returned HTTP " + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choice = root.path("choices");
            if (!choice.isArray() || choice.isEmpty()) {
                throw new AiRefusalException("Model returned no choices");
            }
            String content = choice.get(0).path("message").path("content").asText("");
            if (content.isBlank()) {
                throw new AiRefusalException("Model returned empty content");
            }
            return content.trim();
        } catch (AiRefusalException | AiUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new AiUnavailableException("AI provider request failed: " + e.getMessage());
        }
    }

    private static String buildUserContent(String prompt, String pdfContext, boolean usePdfContext) {
        if (usePdfContext && pdfContext != null && !pdfContext.isBlank()) {
            return "Context from PDF (may be partial):\n---\n"
                    + pdfContext
                    + "\n---\n\nUser instructions:\n"
                    + prompt;
        }
        return prompt;
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isEmpty()) {
            return "https://api.openai.com";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
