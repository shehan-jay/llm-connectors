# LLM Connectors

<div align="center">
  <img src="docs/images/logo.png" alt="LLM Connectors Logo" width="200"/>
  <br/>
  <em>Connect to multiple LLM platforms with ease</em>
</div>

A Java library providing connectors for popular AI platforms including ChatGPT, Copilot, Gemini, and DeepSeek.

## Features

- Unified interface for multiple AI platforms
- Asynchronous API support
- Conversation management
- Configurable parameters
- Comprehensive error handling
- Thread-safe implementation

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.llm.connectors</groupId>
    <artifactId>llm-connectors</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Usage

### ChatGPT Example

```java
// Create and initialize the connector
ChatGPTConnector connector = new ChatGPTConnector();
connector.initialize("your-api-key", "https://api.openai.com");

// Send a simple prompt
CompletableFuture<String> response = connector.sendPrompt("What is the capital of France?");
response.thenAccept(result -> System.out.println(result));

// Send a prompt with parameters
Map<String, Object> parameters = Map.of(
    "model", "gpt-4",
    "temperature", 0.8,
    "max_tokens", 2000
);
CompletableFuture<String> responseWithParams = connector.sendPrompt("Explain quantum computing", parameters);

// Send a conversation
List<LLMConnector.Message> messages = List.of(
    new LLMConnector.Message("system", "You are a helpful assistant."),
    new LLMConnector.Message("user", "What is the weather like?"),
    new LLMConnector.Message("assistant", "I don't have access to real-time weather data."),
    new LLMConnector.Message("user", "Then what can you help me with?")
);
CompletableFuture<String> conversationResponse = connector.sendConversation(messages);
```

## Supported Platforms

- ChatGPT (OpenAI)
- DeepSeek
- Gemini (Google)
- Microsoft Copilot

## Configuration

Each connector requires an API key and base URL for initialization. The specific requirements for each platform are:

- ChatGPT: OpenAI API key and base URL (https://api.openai.com)
- DeepSeek: DeepSeek API key and base URL
- Gemini: Google API key and base URL
- Microsoft Copilot: Microsoft API key and base URL

## Error Handling

The library provides comprehensive error handling:

- Invalid API keys
- Network issues
- Rate limiting
- Invalid parameters
- Service unavailability

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
