package io.github.shehanjay.llmconnectors.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for LLM (Large Language Model) connectors.
 * This interface defines the contract that all AI platform connectors must implement.
 */
public interface LLMConnector {
    
    /**
     * Sends a text prompt to the AI model and returns the response.
     *
     * @param prompt The text prompt to send to the AI model
     * @return A CompletableFuture containing the model's response
     */
    CompletableFuture<String> sendPrompt(String prompt);

    /**
     * Sends a text prompt with additional parameters to the AI model.
     *
     * @param prompt The text prompt to send
     * @param parameters Additional parameters to control the model's behavior
     * @return A CompletableFuture containing the model's response
     */
    CompletableFuture<String> sendPrompt(String prompt, Map<String, Object> parameters);

    /**
     * Sends multiple prompts in a conversation format.
     *
     * @param messages List of messages in the conversation
     * @return A CompletableFuture containing the model's response
     */
    CompletableFuture<String> sendConversation(List<Message> messages);

    /**
     * Checks if the connector is properly configured and can connect to the AI service.
     *
     * @return true if the connector is ready to use, false otherwise
     */
    boolean isReady();

    /**
     * Represents a message in a conversation.
     */
    class Message {
        private final String role;
        private final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
} 