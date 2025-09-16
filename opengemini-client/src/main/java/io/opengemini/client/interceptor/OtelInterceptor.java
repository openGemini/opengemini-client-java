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

import io.github.openfacade.http.HttpResponse;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.Write;
import io.opengemini.client.impl.OpenTelemetryConfig;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;

import java.util.concurrent.CompletableFuture;

public class OtelInterceptor implements Interceptor {
    private static final io.opentelemetry.api.trace.Tracer tracer = OpenTelemetryConfig.getTracer();

    @Override
    public CompletableFuture<Void> queryBefore(Query query) {
        return CompletableFuture.runAsync(() -> {
            Span querySpan = tracer.spanBuilder("query")
                    .setSpanKind(SpanKind.CLIENT)
                    .setAttribute("database", query.getDatabase() != null ? query.getDatabase() : "unknown")
                    .setAttribute("command", query.getCommand())
                    .startSpan();

            Span queryBeforeSpan = tracer.spanBuilder("queryBefore")
                    .setParent(io.opentelemetry.context.Context.current().with(querySpan))
                    .setSpanKind(SpanKind.INTERNAL)
                    .startSpan();

            try (Scope scope = queryBeforeSpan.makeCurrent()) {
                query.setAttribute("querySpan", querySpan);
                query.setAttribute("queryBeforeSpan", queryBeforeSpan);
            } finally {
                queryBeforeSpan.end();
            }
        });
    }

    @Override
    public CompletableFuture<Void> queryAfter(Query query, HttpResponse response) {
        return CompletableFuture.runAsync(() -> {
            Span querySpan = (Span) query.getAttribute("querySpan");
            if (querySpan != null) {
                Span queryAfterSpan = tracer.spanBuilder("queryAfter")
                        .setParent(io.opentelemetry.context.Context.current().with(querySpan))
                        .setSpanKind(SpanKind.INTERNAL)
                        .startSpan();

                try (Scope scope = queryAfterSpan.makeCurrent()) {
                    int statusCode = response.statusCode();
                    queryAfterSpan.setAttribute("status_code", statusCode);
                    if (statusCode >= 400) {
                        String errorBody = response.bodyAsString();
                        queryAfterSpan.setStatus(StatusCode.ERROR, "HTTP error: " + statusCode);
                        queryAfterSpan.setAttribute("error.message", errorBody);
                        querySpan.setStatus(StatusCode.ERROR, "Query failed: " + errorBody);
                    } else {
                        queryAfterSpan.setStatus(StatusCode.OK);
                        querySpan.setStatus(StatusCode.OK);
                    }
                } finally {
                    queryAfterSpan.end();
                    querySpan.end();
                }
            }
        });
    }

    @Override
    public CompletableFuture<Void> writeBefore(Write write) {
        return CompletableFuture.runAsync(() -> {
            Span writeSpan = tracer.spanBuilder("write")
                    .setSpanKind(SpanKind.CLIENT)
                    .setAttribute("database", write.getDatabase())
                    .setAttribute("retention_policy", write.getRetentionPolicy())
                    .setAttribute("measurement", write.getMeasurement())
                    .startSpan();

            Span writeBeforeSpan = tracer.spanBuilder("writeBefore")
                    .setParent(io.opentelemetry.context.Context.current().with(writeSpan))
                    .setSpanKind(SpanKind.INTERNAL)
                    .startSpan();

            try (Scope scope = writeBeforeSpan.makeCurrent()) {
                write.setAttribute("writeSpan", writeSpan);
                write.setAttribute("writeBeforeSpan", writeBeforeSpan);
            } finally {
                writeBeforeSpan.end();
            }
        });
    }

    @Override
    public CompletableFuture<Void> writeAfter(Write write, HttpResponse response) {
        return CompletableFuture.runAsync(() -> {
            Span writeSpan = (Span) write.getAttribute("writeSpan");
            if (writeSpan != null) {
                Span writeAfterSpan = tracer.spanBuilder("writeAfter")
                        .setParent(io.opentelemetry.context.Context.current().with(writeSpan))
                        .setSpanKind(SpanKind.INTERNAL)
                        .startSpan();

                try (Scope scope = writeAfterSpan.makeCurrent()) {
                    int statusCode = response.statusCode();
                    writeAfterSpan.setAttribute("status_code", statusCode);

                    if (statusCode >= 400) {
                        String errorBody = response.bodyAsString();
                        writeAfterSpan.setStatus(StatusCode.ERROR, "HTTP error: " + statusCode);
                        writeAfterSpan.setAttribute("error.message", errorBody);
                        writeSpan.setStatus(StatusCode.ERROR, "Write failed: " + errorBody);
                    } else {
                        writeAfterSpan.setStatus(StatusCode.OK);
                        writeSpan.setStatus(StatusCode.OK);
                    }
                } finally {
                    writeAfterSpan.end();
                    writeSpan.end();
                }
            }
        });
    }
}