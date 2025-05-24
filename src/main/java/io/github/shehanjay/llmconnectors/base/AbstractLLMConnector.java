package io.github.shehanjay.llmconnectors.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.shehanjay.llmconnectors.api.LLMConnector;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for LLM connectors that provides common functionality.
 */
public abstract class AbstractLLMConnector implements LLMConnector {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final OkHttpClient httpClient;
    protected final ObjectMapper objectMapper;
    protected String apiKey;
    protected String baseUrl;
    protected boolean initialized;

    protected AbstractLLMConnector() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
        this.initialized = false;
    }

    /**
     * Initialize the connector with the required configuration.
     *
     * @param apiKey The API key for the AI service
     * @param baseUrl The base URL for the AI service
     */
    public void initialize(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.initialized = true;
        logger.info("Initialized {} connector", getClass().getSimpleName());
    }

    @Override
    public boolean isReady() {
        return initialized && apiKey != null && !apiKey.isEmpty() && baseUrl != null && !baseUrl.isEmpty();
    }

    @Override
    public CompletableFuture<String> sendPrompt(String prompt) {
        return sendPrompt(prompt, Map.of());
    }

    /**
     * Creates the authentication header for API requests.
     *
     * @return The authentication header value
     */
    protected String getAuthHeader() {
        return "Bearer " + apiKey;
    }

    /**
     * Validates that the connector is properly initialized before making requests.
     *
     * @throws IllegalStateException if the connector is not properly initialized
     */
    protected void validateInitialization() {
        if (!isReady()) {
            throw new IllegalStateException("Connector not properly initialized. Call initialize() first.");
        }
    }
} 