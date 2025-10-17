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

import io.github.openfacade.http.HttpResponse;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.Write;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.CompletableFuture;

@Getter
@Setter
public class OtelInterceptor implements Interceptor {
    private io.opentelemetry.api.trace.Tracer tracer;

    @Override
    public CompletableFuture<Void> queryBefore(Query query) {
        return CompletableFuture.runAsync(() -> {
            Span querySpan = tracer.spanBuilder("query")
                    .setSpanKind(SpanKind.CLIENT)
                    .setAttribute("database", query.getDatabase() != null ? query.getDatabase() : "unknown")
                    .setAttribute("command", query.getCommand())
                    .startSpan();

            query.setAttribute("querySpan", querySpan);
        });
    }

    @Override
    public CompletableFuture<Void> queryAfter(Query query, HttpResponse response) {
        return CompletableFuture.runAsync(() -> {
            Span querySpan = (Span) query.getAttribute("querySpan");
            if (querySpan == null) {
                return;
            }

            int statusCode = response.statusCode();
            querySpan.setAttribute("status_code", statusCode);
            if (statusCode >= 400) {
                String errorBody = response.bodyAsString();
                querySpan.setStatus(StatusCode.ERROR, "HTTP error: " + statusCode);
                querySpan.setAttribute("error.message", errorBody);
            } else {
                querySpan.setStatus(StatusCode.OK);
            }
            querySpan.end();
        });
    }

    @Override
    public CompletableFuture<Void> writeBefore(Write write) {
        return CompletableFuture.runAsync(() -> {
            Span writeSpan = tracer.spanBuilder("write")
                    .setSpanKind(SpanKind.CLIENT)
                    .setAttribute("database", write.getDatabase())
                    .setAttribute("retention_policy", write.getRetentionPolicy())
                    .setAttribute("command", write.getLineProtocol())
                    .startSpan();
            write.setAttribute("writeSpan", writeSpan);
        });
    }

    @Override
    public CompletableFuture<Void> writeAfter(Write write, HttpResponse response) {
        return CompletableFuture.runAsync(() -> {
            Span writeSpan = (Span) write.getAttribute("writeSpan");
            if (writeSpan == null) {
                return;
            }
            int statusCode = response.statusCode();
            writeSpan.setAttribute("status_code", statusCode);

            if (statusCode >= 400) {
                String errorBody = response.bodyAsString();
                writeSpan.setStatus(StatusCode.ERROR, "HTTP error: " + statusCode);
                writeSpan.setAttribute("error.message", errorBody);
            } else {
                writeSpan.setStatus(StatusCode.OK);
            }
            writeSpan.end();
        });
    }
}
