package com.llm.connectors.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.llm.connectors.api.LLMConnector;
import com.llm.connectors.base.AbstractLLMConnector;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Connector implementation for Google's Gemini API.
 */
public class GeminiConnector extends AbstractLLMConnector {
    private static final String API_VERSION = "v1";
    private static final String GENERATE_ENDPOINT = "/models/%s:generateContent";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public GeminiConnector() {
        super();
    }

    @Override
    public CompletableFuture<String> sendPrompt(String prompt, Map<String, Object> parameters) {
        validateInitialization();
        
        String model = parameters.getOrDefault("model", "gemini-pro").toString();
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        // Create contents array
        ArrayNode contents = requestBody.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        // Add generation config
        ObjectNode generationConfig = requestBody.putObject("generationConfig");
        generationConfig.put("temperature", ((Number) parameters.getOrDefault("temperature", 0.7)).doubleValue());
        generationConfig.put("maxOutputTokens", ((Number) parameters.getOrDefault("max_tokens", 1000)).intValue());
        generationConfig.put("topP", ((Number) parameters.getOrDefault("top_p", 1.0)).doubleValue());
        generationConfig.put("topK", ((Number) parameters.getOrDefault("top_k", 40)).intValue());

        return executeRequest(model, requestBody);
    }

    @Override
    public CompletableFuture<String> sendConversation(List<LLMConnector.Message> messages) {
        validateInitialization();
        
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contents = requestBody.putArray("contents");
        
        for (LLMConnector.Message message : messages) {
            ObjectNode content = contents.addObject();
            content.put("role", message.getRole());
            ArrayNode parts = content.putArray("parts");
            parts.addObject().put("text", message.getContent());
        }

        // Add generation config
        ObjectNode generationConfig = requestBody.putObject("generationConfig");
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 1000);
        generationConfig.put("topP", 1.0);
        generationConfig.put("topK", 40);

        return executeRequest("gemini-pro", requestBody);
    }

    private CompletableFuture<String> executeRequest(String model, ObjectNode requestBody) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        String endpoint = String.format(GENERATE_ENDPOINT, model);
        Request request = new Request.Builder()
                .url(baseUrl + "/" + API_VERSION + endpoint)
                .addHeader("x-goog-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (response) {
                    if (!response.isSuccessful()) {
                        future.completeExceptionally(new IOException("Unexpected response code: " + response.code()));
                        return;
                    }

                    String responseBody = response.body().string();
                    JsonNode jsonResponse = objectMapper.readTree(responseBody);
                    String content = jsonResponse.path("candidates")
                            .get(0)
                            .path("content")
                            .path("parts")
                            .get(0)
                            .path("text")
                            .asText();
                    
                    future.complete(content);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }

    @Override
    protected String getAuthHeader() {
        // Gemini uses x-goog-api-key header instead of Bearer token
        return null;
    }
} 