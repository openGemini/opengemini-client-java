package io.opengemini.client.jdk;


import io.opengemini.client.api.AuthConfig;
import io.opengemini.client.api.AuthType;
import io.opengemini.client.api.BatchConfig;
import io.opengemini.client.api.OpenGeminiClient;
import io.opengemini.client.api.OpenGeminiException;

public class OpenGeminiJdkClientFactory {
    public static OpenGeminiClient createClient(Configuration configuration) throws OpenGeminiException {
        if (configuration.getAddresses() == null || configuration.getAddresses().isEmpty()) {
            throw new OpenGeminiException("must have at least one address");
        }
        AuthConfig authConfig = configuration.getAuthConfig();
        if (authConfig != null) {
            if (authConfig.getAuthType() == AuthType.TOKEN && (authConfig.getToken() == null
                    || authConfig.getToken().isEmpty())) {
                throw new OpenGeminiException("invalid auth config due to empty token");
            }
            if (authConfig.getAuthType() == AuthType.PASSWORD) {
                if (authConfig.getUsername() == null || authConfig.getUsername().isEmpty()) {
                    throw new OpenGeminiException("invalid auth config due to empty username");
                }
                if (authConfig.getPassword() == null || authConfig.getPassword().isEmpty()) {
                    throw new OpenGeminiException("invalid auth config due to empty password");
                }
            }
        }
        BatchConfig batchConfig = configuration.getBatchConfig();
        if (batchConfig != null) {
            if (batchConfig.getBatchInterval() <= 0) {
                throw new OpenGeminiException("batch enabled, batch interval must be great than 0");
            }
            if (batchConfig.getBatchSize() <= 0) {
                throw new OpenGeminiException("batch enabled, batch size must be great than 0");
            }
        }
        return new OpenGeminiJdkClient(configuration);
    }
}
