/*
 * Copyright 2024 openGemini Authors
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

package io.opengemini.client.interceptor;

import io.github.openfacade.http.HttpClientConfig;
import io.opengemini.client.api.*;
import io.opengemini.client.impl.OpenGeminiClient;
import io.opengemini.client.impl.OpenGeminiClientFactory;
import io.opengemini.client.impl.OpenTelemetryConfig;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Example demonstrating OpenGemini client usage with interceptors.
 */

@Getter
public class TracingIntegrationTest {
    @Setter
    private String database;
    @Setter
    private String retentionPolicy;
    @Setter
    private String lineProtocol;
    private static final Logger LOG = LoggerFactory.getLogger(TracingIntegrationTest.class);

    private OpenGeminiClient openGeminiClient;

    @BeforeEach
    void setUp() {
        HttpClientConfig httpConfig = new HttpClientConfig.Builder()
                .connectTimeout(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(3))
                .build();
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                .httpConfig(httpConfig)
                .authConfig(new AuthConfig(AuthType.PASSWORD, "test", "testPwd123@".toCharArray(), null))
                .gzipEnabled(false)
                .build();
        this.openGeminiClient = new OpenGeminiClient(configuration);
    }

    @Test
    void testClientCreation() {
        Configuration config = new Configuration();
        config.setAddresses(java.util.Collections.singletonList(new io.opengemini.client.api.Address("localhost", 8086)));
        if (config.getHttpConfig() == null) {
            config.setHttpConfig(new HttpClientConfig.Builder().build());
        }

        assertDoesNotThrow(() -> {
            OpenGeminiClient client = OpenGeminiClientFactory.createClientWithInterceptors(config);
            assertNotNull(client, "OpenGeminiClient should be created successfully");
            client.close();
        }, "Client creation should not throw an exception");
    }

    @Test
    void testDatabaseCreation() {
        Configuration config = new Configuration();
        config.setAddresses(java.util.Collections.singletonList(new io.opengemini.client.api.Address("localhost", 8086)));
        if (config.getHttpConfig() == null) {
            config.setHttpConfig(new HttpClientConfig.Builder().build());
        }

        assertDoesNotThrow(() -> {
            try (OpenGeminiClient client = OpenGeminiClientFactory.createClientWithInterceptors(config)) {
                Query createDbQuery = new Query("CREATE DATABASE test_db");
                client.query(createDbQuery).get(10, TimeUnit.SECONDS);
            }
        }, "Database creation should not throw an exception");
    }

    @Test
    void testQueryOperation() {
        Configuration config = new Configuration();
        config.setAddresses(java.util.Collections.singletonList(new Address("localhost", 8086)));
        if (config.getHttpConfig() == null) {
            config.setHttpConfig(new HttpClientConfig.Builder().build());
        }

        assertDoesNotThrow(() -> {
            try (OpenGeminiClient client = OpenGeminiClientFactory.createClientWithInterceptors(config)) {
                Query createDbQuery = new Query("CREATE DATABASE test_db");
                client.query(createDbQuery).get(10, TimeUnit.SECONDS);

                Query showDbQuery = new Query("SHOW DATABASES");
                QueryResult result = client.query(showDbQuery).get(10, TimeUnit.SECONDS);
                assertNotNull(result, "Query result should not be null");
            }
        }, "Query operation should not throw an exception");
    }

    @BeforeAll
    static void initializeTracing() {
        OpenTelemetryConfig.initialize();
    }

    @Test
    void testWriteOperation() {
        Configuration config = new Configuration();
        config.setAddresses(java.util.Collections.singletonList(
                new Address("localhost", 8086)));

        if (config.getHttpConfig() == null) {
            config.setHttpConfig(new HttpClientConfig.Builder().build());
        }

        assertDoesNotThrow(() -> {
            try (OpenGeminiClient client = OpenGeminiClientFactory.createClientWithInterceptors(config)) {
                Query createDbQuery = new Query("CREATE DATABASE test_db");
                client.query(createDbQuery).get(10, TimeUnit.SECONDS);

                Thread.sleep(1000);

                Write write = new Write(
                        "test_db",
                        "autogen",
                        "temperature",
                        "temperature,location=room1 value=25.5 " + System.currentTimeMillis(),
                        "ns"
                );

                client.executeWrite(
                        write.getDatabase(),
                        write.getRetentionPolicy(),
                        write.getLineProtocol()
                ).get(10, TimeUnit.SECONDS);
            }
        }, "Write operation should not throw an exception");
    }

    @Test
    void testTracingIntegration() throws ExecutionException, InterruptedException {
        String databaseTestName = "tracing_test_db";
        CompletableFuture<Void> createdb = openGeminiClient.createDatabase(databaseTestName);
        createdb.get();

        assertDoesNotThrow(() -> {

            Write write = new Write(
                    "tracing_test_db",
                    "autogen",
                    "tracing_measurement",
                    "tracing_measurement,tag=test value=8 " + System.currentTimeMillis(),
                    "ns"
            );

            openGeminiClient.executeWrite(
                    write.getDatabase(),
                    write.getRetentionPolicy(),
                    write.getLineProtocol()
            ).get(10, TimeUnit.SECONDS);

            Query query = new Query("SELECT * FROM tracing_measurement");
            openGeminiClient.query(query).get(10, TimeUnit.SECONDS);

        }, "Tracing integration should not throw an exception");
    }
}
