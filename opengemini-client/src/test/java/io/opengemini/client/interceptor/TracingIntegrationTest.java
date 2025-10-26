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

package io.opengemini.client.interceptor;

import io.github.openfacade.http.HttpClientConfig;
import io.opengemini.client.api.Address;
import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.Write;
import io.opengemini.client.impl.OpenGeminiClient;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Example demonstrating OpenGemini client usage with interceptors.
 */

public class TracingIntegrationTest {

    private OpenGeminiClient openGeminiClient;

    private String databaseName = "jaeger_storage";

    private String rpName = "trace";

    private String measurementName = "opengemini-client-java";

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException {
        HttpClientConfig httpConfig = new HttpClientConfig.Builder()
                .connectTimeout(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(3))
                .build();
        Configuration configuration = Configuration.builder()
                .addresses(Collections.singletonList(new Address("127.0.0.1", 8086)))
                .httpConfig(httpConfig)
                .gzipEnabled(false)
                .build();
        this.openGeminiClient = new OpenGeminiClient(configuration);

        OtelInterceptor otelInterceptor = new OtelInterceptor();

        otelInterceptor.setTracer(getTestTracer());
        openGeminiClient.addInterceptors(otelInterceptor);

        cleanTrace();
    }

    @AfterEach
    void setDown() throws InterruptedException {
        // for the last reporting record, otherwise the test case will not be detected
        Thread.sleep(500);
    }

    private Tracer getTestTracer() {
        OpenTelemetry openTelemetry;
        try {
            OtlpGrpcSpanExporter otlpGrpcSpanExporter = OtlpGrpcSpanExporter.builder()
                    .setEndpoint("http://127.0.0.1:18086")
                    .addHeader("Authentication", "")
                    .build();

            BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(otlpGrpcSpanExporter)
                    .setScheduleDelay(100, TimeUnit.MILLISECONDS)
                    .build();

            SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                    .addSpanProcessor(spanProcessor)
                    .setResource(Resource.create(
                            Attributes.of(ResourceAttributes.SERVICE_NAME, "opengemini-client-java")
                    ))
                    .build();

            openTelemetry = OpenTelemetrySdk.builder()
                    .setTracerProvider(tracerProvider)
                    .build();

            return openTelemetry.getTracer("opengemini-client-java");
        } catch (Exception e) {
            openTelemetry = OpenTelemetry.noop();
            return openTelemetry.getTracer("opengemini-client-java");
        }

    }

    private void cleanTrace() throws ExecutionException, InterruptedException {
        Query dropMeasurement = new Query("DROP MEASUREMENT \"%s\"".formatted(measurementName), databaseName, rpName);
        openGeminiClient.query(dropMeasurement).get();
    }

    private void checkLastTraceCommand(String command) throws ExecutionException, InterruptedException {
        String queryTraceCommand = "SELECT command FROM \"%s\"".formatted(measurementName);
        Query checkTraceQuery = new Query(queryTraceCommand, databaseName, rpName);
        QueryResult checkRst = openGeminiClient.query(checkTraceQuery).get();
        List<List<Object>> queryValues = checkRst.getResults().get(0).getSeries().get(0).getValues();
        int recordNum = queryValues.size();
        Assertions.assertEquals(command, queryValues.get(recordNum - 1).get(1));
    }

    @Test
    void testDatabaseCreation() throws ExecutionException, InterruptedException {
        String command = "CREATE DATABASE test_db";
        Assertions.assertDoesNotThrow(() -> {
            Query createDbQuery = new Query(command);
            openGeminiClient.query(createDbQuery).get(10, TimeUnit.SECONDS);
        }, "Database creation should not throw an exception");

        Thread.sleep(3000);
        checkLastTraceCommand(command);
    }

    @Test
    void testQueryOperation() throws InterruptedException, ExecutionException {
        Configuration config = new Configuration();
        config.setAddresses(Collections.singletonList(new Address("localhost", 8086)));
        if (config.getHttpConfig() == null) {
            config.setHttpConfig(new HttpClientConfig.Builder().build());
        }

        String command = "SHOW DATABASES";
        Assertions.assertDoesNotThrow(() -> {
            Query createDbQuery = new Query("CREATE DATABASE test_db");
            openGeminiClient.query(createDbQuery).get(10, TimeUnit.SECONDS);

            Query showDbQuery = new Query(command);
            QueryResult result = openGeminiClient.query(showDbQuery).get(10, TimeUnit.SECONDS);
            Assertions.assertNotNull(result, "Query result should not be null");
        }, "Query operation should not throw an exception");

        Thread.sleep(3000);
        checkLastTraceCommand(command);
    }

    @Test
    void testWriteOperation() throws InterruptedException, ExecutionException {
        Configuration config = new Configuration();
        config.setAddresses(Collections.singletonList(
                new Address("localhost", 8086)));

        if (config.getHttpConfig() == null) {
            config.setHttpConfig(new HttpClientConfig.Builder().build());
        }

        String lineProtocol = "temperature,location=room1 value=25.5";
        Assertions.assertDoesNotThrow(() -> {
            Query createDbQuery = new Query("CREATE DATABASE test_db");
            openGeminiClient.query(createDbQuery).get(10, TimeUnit.SECONDS);

            Write write = new Write(
                    "test_db",
                    "autogen",
                    lineProtocol,
                    "ns"
            );

            openGeminiClient.executeWrite(
                    write.getDatabase(),
                    write.getRetentionPolicy(),
                    write.getLineProtocol()
            ).get(10, TimeUnit.SECONDS);

        }, "Write operation should not throw an exception");

        Thread.sleep(3000);
        checkLastTraceCommand(lineProtocol);
    }

    @Test
    void testTracingIntegration() throws ExecutionException, InterruptedException {
        String databaseTestName = "tracing_test_db";
        CompletableFuture<Void> createdb = openGeminiClient.createDatabase(databaseTestName);
        createdb.get();

        String command = "SELECT * FROM tracing_measurement";
        Assertions.assertDoesNotThrow(() -> {

            Write write = new Write(
                    "tracing_test_db",
                    "autogen",
                    "tracing_measurement,tag=test value=8 " + System.currentTimeMillis(),
                    "ns"
            );

            openGeminiClient.executeWrite(
                    write.getDatabase(),
                    write.getRetentionPolicy(),
                    write.getLineProtocol()
            ).get(10, TimeUnit.SECONDS);

            Query query = new Query(command);
            openGeminiClient.query(query).get(10, TimeUnit.SECONDS);

        }, "Tracing integration should not throw an exception");

        Thread.sleep(3000);
        checkLastTraceCommand(command);
    }
}
