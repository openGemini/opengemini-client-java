package io.opengemini.client.okhttp;

import io.opengemini.client.api.AuthConfig;
import io.opengemini.client.api.AuthType;
import io.opengemini.client.api.BatchConfig;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.OpengeminiConst;
import io.opengemini.client.api.TlsConfig;
import org.jetbrains.annotations.NotNull;

public class OpenGeminiOkhttpClientFactory {
    public static OpenGeminiOkhttpClient create(@NotNull Configuration configuration) throws OpenGeminiException {
        if (configuration.getAddresses() == null || configuration.getAddresses().isEmpty()) {
            throw new OpenGeminiException("at least one address is required");
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
                if (authConfig.getPassword() == null || authConfig.getPassword().length == 0) {
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

        if (configuration.getTimeout() == null || configuration.getTimeout().isNegative()) {
            configuration.setTimeout(OpengeminiConst.DEFAULT_TIMEOUT);
        }

        if (configuration.getConnectTimeout() == null || configuration.getConnectTimeout().isNegative()) {
            configuration.setConnectTimeout(OpengeminiConst.DEFAULT_CONNECT_TIMEOUT);
        }

        TlsConfig tlsConfig = configuration.getTlsConfig();
        if (tlsConfig != null) {
            boolean enableTls = !tlsConfig.verifyDisabled;
            if (enableTls && (tlsConfig.trustStorePath == null || tlsConfig.trustStorePassword == null)) {
                throw new OpenGeminiException(
                        "tls verification enabled, trust store path and password must not be null");
            }
        }
        return new OpenGeminiOkhttpClient(configuration);
    }
}
