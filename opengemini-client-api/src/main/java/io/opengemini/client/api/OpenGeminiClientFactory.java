package io.opengemini.client.api;

/**
 * Factory interface for creating OpenGeminiClient instances.
 */
public interface OpenGeminiClientFactory {
    /**
     * Creates a client.
     *
     * @param configuration the configuration for the client
     * @return the created client
     * @throws OpenGeminiException if there is an error during client creation
     */
    OpenGeminiClient create(BaseConfiguration configuration) throws OpenGeminiException;
}
