package io.github.shehanjay.llmconnectors.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.shehanjay.llmconnectors.api.LLMConnector;
import io.github.shehanjay.llmconnectors.base.AbstractLLMConnector;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Connector implementation for Microsoft Copilot API.
 */
public class CopilotConnector extends AbstractLLMConnector {
    private static final String API_VERSION = "v1";
    private static final String CHAT_ENDPOINT = "/chat/completions";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public CopilotConnector() {
        super();
    }

    @Override
    public CompletableFuture<String> sendPrompt(String prompt, Map<String, Object> parameters) {
        validateInitialization();
        
        ObjectNode requestBody = objectMapper.createObjectNode()
                .put("model", parameters.getOrDefault("model", "copilot-gpt-4").toString())
                .put("temperature", ((Number) parameters.getOrDefault("temperature", 0.7)).doubleValue())
                .put("max_tokens", ((Number) parameters.getOrDefault("max_tokens", 1000)).intValue())
                .put("top_p", ((Number) parameters.getOrDefault("top_p", 1.0)).doubleValue());

        ArrayNode messages = requestBody.putArray("messages");
        messages.addObject()
                .put("role", "user")
                .put("content", prompt);

        return executeRequest(requestBody);
    }

    @Override
    public CompletableFuture<String> sendConversation(List<LLMConnector.Message> messages) {
        validateInitialization();
        
        ObjectNode requestBody = objectMapper.createObjectNode()
                .put("model", "copilot-gpt-4")
                .put("temperature", 0.7)
                .put("max_tokens", 1000)
                .put("top_p", 1.0);

        ArrayNode messagesArray = requestBody.putArray("messages");
        for (LLMConnector.Message message : messages) {
            messagesArray.addObject()
                    .put("role", message.getRole())
                    .put("content", message.getContent());
        }

        return executeRequest(requestBody);
    }

    private CompletableFuture<String> executeRequest(ObjectNode requestBody) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        Request request = new Request.Builder()
                .url(baseUrl + "/" + API_VERSION + CHAT_ENDPOINT)
                .addHeader("Authorization", getAuthHeader())
                .addHeader("Content-Type", "application/json")
                .addHeader("api-version", API_VERSION)
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
                    String content = jsonResponse.path("choices")
                            .get(0)
                            .path("message")
                            .path("content")
                            .asText();
                    
                    future.complete(content);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }
} 