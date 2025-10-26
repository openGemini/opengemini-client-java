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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Example demonstrating OpenGemini client usage with interceptors.
 */

public class TraceFailureToleranceTest {

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
                .gzipEnabled(false)
                .build();
        this.openGeminiClient = new OpenGeminiClient(configuration);

        OtelInterceptor otelInterceptor = new OtelInterceptor();

        otelInterceptor.setTracer(getErrTracer());
        openGeminiClient.addInterceptors(otelInterceptor);
    }

    private Tracer getErrTracer() {
        OpenTelemetry openTelemetry;
        OtlpGrpcSpanExporter otlpGrpcSpanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://127.0.0.1:38086")  // error endpoiont to test the failure tolerance
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
    }

    @Test
    void testTracingIntegration() throws ExecutionException, InterruptedException {
        String databaseTestName = "tracing_test_db";
        CompletableFuture<Void> createdb = openGeminiClient.createDatabase(databaseTestName);
        createdb.get();

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

            Query query = new Query("SELECT * FROM tracing_measurement");
            openGeminiClient.query(query).get(10, TimeUnit.SECONDS);

        }, "Tracing integration should not throw an exception");
    }
}
