package io.opengemini.client.api;

import io.opengemini.client.jdk.Configuration;
import io.opengemini.client.jdk.OpenGeminiJdkClientFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class OpenGeminiJdkClientFactoryTest {

    private static Configuration configuration;
    private static AuthConfig authConfig;
    private static BatchConfig batchConfig;

    @BeforeAll
    public static void setUp() {
        configuration = new Configuration();
        authConfig = new AuthConfig();
        batchConfig = new BatchConfig();
    }

    @Test
    public void testGetClientWithNullAddresses() {
        configuration.setAddresses(null);
        Throwable actualException = Assertions.assertThrows(OpenGeminiException.class, () -> {
            OpenGeminiJdkClientFactory.create(configuration);
        });
        Assertions.assertEquals("must have at least one address", actualException.getMessage());
    }

    @Test
    public void testGetClientWithEmptyAddresses() {
        configuration.setAddresses(new ArrayList<>());

        Throwable actualException = Assertions.assertThrows(OpenGeminiException.class, () -> {
            OpenGeminiJdkClientFactory.create(configuration);
        });
        Assertions.assertEquals("must have at least one address", actualException.getMessage());
    }

    @Test
    public void testGetClientWithEmptyToken() {
        configuration.setAddresses(List.of(new Address()));
        authConfig.setAuthType(AuthType.TOKEN);
        authConfig.setToken("");
        configuration.setAuthConfig(authConfig);

        Throwable actualException = Assertions.assertThrows(OpenGeminiException.class, () -> {
            OpenGeminiJdkClientFactory.create(configuration);
        });
        Assertions.assertEquals("invalid auth config due to empty token", actualException.getMessage());
    }

    @Test
    public void testGetClientWithEmptyUserName() {
        configuration.setAddresses(List.of(new Address()));
        authConfig.setAuthType(AuthType.PASSWORD);
        authConfig.setPassword("pass");
        authConfig.setUsername("");
        configuration.setAuthConfig(authConfig);

        Throwable actualException = Assertions.assertThrows(OpenGeminiException.class, () -> {
            OpenGeminiJdkClientFactory.create(configuration);
        });
        Assertions.assertEquals("invalid auth config due to empty username", actualException.getMessage());
    }

    @Test
    public void testGetClientWithNullPassword() {
        configuration.setAddresses(List.of(new Address()));
        authConfig.setAuthType(AuthType.PASSWORD);
        authConfig.setPassword(null);
        authConfig.setUsername("user");
        configuration.setAuthConfig(authConfig);

        Throwable actualException = Assertions.assertThrows(OpenGeminiException.class, () -> {
            OpenGeminiJdkClientFactory.create(configuration);
        });
        Assertions.assertEquals("invalid auth config due to empty password", actualException.getMessage());
    }

    @Test
    public void testGetClientWithInvalidBatchInterval() {
        configuration.setAddresses(List.of(new Address()));
        authConfig.setAuthType(null);
        batchConfig.setBatchInterval(-1);
        configuration.setBatchConfig(batchConfig);

        Throwable actualException = Assertions.assertThrows(OpenGeminiException.class, () -> {
            OpenGeminiJdkClientFactory.create(configuration);
        });
        Assertions.assertEquals("batch enabled, batch interval must be great than 0", actualException.getMessage());
    }

    @Test
    public void testGetClientWithInvalidBatchSize() {
        configuration.setAddresses(List.of(new Address()));
        authConfig.setAuthType(null);
        batchConfig.setBatchInterval(1);
        batchConfig.setBatchSize(-1);
        configuration.setBatchConfig(batchConfig);

        Throwable actualException = Assertions.assertThrows(OpenGeminiException.class, () -> {
            OpenGeminiJdkClientFactory.create(configuration);
        });
        Assertions.assertEquals("batch enabled, batch size must be great than 0", actualException.getMessage());
    }
}
