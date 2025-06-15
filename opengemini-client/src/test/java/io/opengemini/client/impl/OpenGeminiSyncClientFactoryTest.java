/*
 * Copyright 2025 openGemini Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opengemini.client.impl;

import io.opengemini.client.api.Address;
import io.opengemini.client.api.AuthConfig;
import io.opengemini.client.api.AuthType;
import io.opengemini.client.api.BatchConfig;
import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.OpenGeminiException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class OpenGeminiSyncClientFactoryTest implements FactoryTestCase {

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
            OpenGeminiClientFactory.createSyncClient(configuration);
        });
        Assertions.assertEquals("must have at least one address", actualException.getMessage());
    }

    @Test
    public void testGetClientWithEmptyAddresses() {
        configuration.setAddresses(new ArrayList<>());

        Throwable actualException = Assertions.assertThrows(OpenGeminiException.class, () -> {
            OpenGeminiClientFactory.createSyncClient(configuration);
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
            OpenGeminiClientFactory.createSyncClient(configuration);
        });
        Assertions.assertEquals("invalid auth config due to empty token", actualException.getMessage());
    }

    @Test
    public void testGetClientWithEmptyUserName() {
        configuration.setAddresses(List.of(new Address()));
        authConfig.setAuthType(AuthType.PASSWORD);
        authConfig.setPassword("pass".toCharArray());
        authConfig.setUsername("");
        configuration.setAuthConfig(authConfig);

        Throwable actualException = Assertions.assertThrows(OpenGeminiException.class, () -> {
            OpenGeminiClientFactory.createSyncClient(configuration);
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
            OpenGeminiClientFactory.createSyncClient(configuration);
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
            OpenGeminiClientFactory.createSyncClient(configuration);
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
            OpenGeminiClientFactory.createSyncClient(configuration);
        });
        Assertions.assertEquals("batch enabled, batch size must be great than 0", actualException.getMessage());
    }
}
