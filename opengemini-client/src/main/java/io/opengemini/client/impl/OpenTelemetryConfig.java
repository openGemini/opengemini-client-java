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

package io.opengemini.client.impl;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

/**
 * Configuration for OpenTelemetry tracing.
 */
public class OpenTelemetryConfig {

    private static volatile OpenTelemetry openTelemetry;
    private static volatile Tracer tracer;

    public static synchronized void initialize() {
        if (openTelemetry != null) {
            return;
        }

        try {
            JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder()
                    .setEndpoint("http://localhost:14250")
                    .build();

            BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(jaegerExporter)
                    .setScheduleDelay(100, java.util.concurrent.TimeUnit.MILLISECONDS)
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

            tracer = openTelemetry.getTracer("opengemini-client-java");
        } catch (Exception e) {
            // Fallback to no-op implementation
            openTelemetry = OpenTelemetry.noop();
            tracer = openTelemetry.getTracer("opengemini-client-java");
        }
    }

    public static Tracer getTracer() {
        if (tracer == null) {
            initialize();
        }
        return tracer;
    }
}