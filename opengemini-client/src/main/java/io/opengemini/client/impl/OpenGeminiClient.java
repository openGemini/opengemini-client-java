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

import io.github.openfacade.http.BasicAuthRequestFilter;
import io.github.openfacade.http.HttpClient;
import io.github.openfacade.http.HttpClientConfig;
import io.github.openfacade.http.HttpClientFactory;
import io.github.openfacade.http.HttpResponse;
import io.opengemini.client.api.*;
import io.opengemini.client.common.BaseClient;
import io.opengemini.client.common.CommandFactory;
import io.opengemini.client.common.HeaderConst;
import io.opengemini.client.common.JacksonService;
import io.opengemini.client.common.ResultMapper;
import io.opengemini.client.interceptor.Interceptor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class OpenGeminiClient extends BaseClient implements OpenGeminiAsyncClient {
    private final List<Interceptor> interceptors = new ArrayList<>();
    protected final Configuration conf;
    private final HttpClient client;

    public OpenGeminiClient(@NotNull Configuration conf) {
        super(conf);
        this.conf = conf;
        AuthConfig authConfig = conf.getAuthConfig();
        HttpClientConfig httpConfig = conf.getHttpConfig();
        if (httpConfig == null) {
            httpConfig = new HttpClientConfig.Builder().build();
            conf.setHttpConfig(httpConfig);
        }
        if (authConfig != null && authConfig.getAuthType().equals(AuthType.PASSWORD)) {
            httpConfig.addRequestFilter(
                    new BasicAuthRequestFilter(authConfig.getUsername(), String.valueOf(authConfig.getPassword())));
        }
        this.client = HttpClientFactory.createHttpClient(httpConfig);
    }

    public void addInterceptors(Interceptor... interceptors) {
        Collections.addAll(this.interceptors, interceptors);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> createDatabase(String database) {
        String command = CommandFactory.createDatabase(database);
        Query query = new Query(command);
        return executePostQuery(query).thenApply(rsp -> null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> dropDatabase(String database) {
        String command = CommandFactory.dropDatabase(database);
        Query query = new Query(command);
        return executePostQuery(query).thenApply(rsp -> null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<String>> showDatabases() {
        String command = CommandFactory.showDatabases();
        Query query = new Query(command);
        return executeQuery(query).thenApply(ResultMapper::toDatabases);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> createRetentionPolicy(String database, RpConfig rpConfig, boolean isDefault) {
        String command = CommandFactory.createRetentionPolicy(database, rpConfig, isDefault);
        Query query = new Query(command);
        return executePostQuery(query).thenApply(rsp -> null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<RetentionPolicy>> showRetentionPolicies(String database) {
        if (StringUtils.isBlank(database)) {
            return null;
        }

        String command = CommandFactory.showRetentionPolicies(database);
        Query query = new Query(command);
        query.setDatabase(database);
        return executeQuery(query).thenApply(ResultMapper::toRetentionPolicies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> dropRetentionPolicy(String database, String retentionPolicy) {
        String command = CommandFactory.dropRetentionPolicy(database, retentionPolicy);
        Query query = new Query(command);
        return executePostQuery(query).thenApply(rsp -> null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<QueryResult> query(Query query) {
        return executeQuery(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> write(String database, Point point) {
        return write(database, null, point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> write(String database, List<Point> points) {
        return write(database, null, points);
    }

    @Override
    public CompletableFuture<Void> write(String database, String retentionPolicy, Point point) {
        String body = point.lineProtocol();
        if (StringUtils.isEmpty(body)) {
            return CompletableFuture.completedFuture(null);
        }
        return executeWrite(database, retentionPolicy, body);
    }

    @Override
    public CompletableFuture<Void> write(String database, String retentionPolicy, List<Point> points) {
        if (points.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        StringJoiner sj = new StringJoiner("\n");
        for (Point point : points) {
            String lineProtocol = point.lineProtocol();
            if (StringUtils.isEmpty(lineProtocol)) {
                continue;
            }
            sj.add(lineProtocol);
        }
        String body = sj.toString();
        if (StringUtils.isEmpty(body)) {
            return CompletableFuture.completedFuture(null);
        }
        return executeWrite(database, retentionPolicy, body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Pong> ping() {
        return executePing();
    }

    /**
     * Execute a GET query call with java HttpClient.
     *
     * @param query the query to execute.
     */
    public CompletableFuture<QueryResult> executeQuery(Query query) {
        String queryUrl = getQueryUrl(query);
        CompletableFuture<Void> beforeFutures = CompletableFuture.allOf(
                interceptors.stream()
                        .map(interceptor -> interceptor.queryBefore(query))
                        .toArray(CompletableFuture[]::new)
        );

        return beforeFutures.thenCompose(voidResult -> executeHttpQuery(query).thenCompose(response -> {
            CompletableFuture<Void> afterFutures = CompletableFuture.allOf(
                    interceptors.stream()
                            .map(interceptor -> interceptor.queryAfter(query, response))
                            .toArray(CompletableFuture[]::new)
            );
            return afterFutures.thenCompose(voidResult2 -> convertResponse(response, QueryResult.class));
        }));
    }

    /**
     * Execute a POST query call with java HttpClient.
     *
     * @param query the query to execute.
     */
    protected CompletableFuture<QueryResult> executePostQuery(Query query) {
        String queryUrl = getQueryUrl(query);
        return post(queryUrl, null).thenCompose(response -> convertResponse(response, QueryResult.class));
    }

    /**
     * Execute a write call with java HttpClient.
     *
     * @param database        the name of the database.
     * @param retentionPolicy the name of the retention policy.
     * @param lineProtocol    the line protocol string to write.
     */
    public CompletableFuture<Void> executeWrite(String database, String retentionPolicy, String lineProtocol) {
        String writeUrl = getWriteUrl(database, retentionPolicy);
        Write write = new Write(
                database,
                retentionPolicy,
                "default_measurement",
                lineProtocol,
                "ns"
        );

        CompletableFuture<Void> beforeFutures = CompletableFuture.allOf(
                interceptors.stream()
                        .map(interceptor -> interceptor.writeBefore(write))
                        .toArray(CompletableFuture[]::new)
        );

        return beforeFutures.thenCompose(voidResult ->
                executeHttpWrite(write).thenCompose(response -> {
                    CompletableFuture<Void> afterFutures = CompletableFuture.allOf(
                            interceptors.stream()
                                    .map(interceptor -> interceptor.writeAfter(write, response))
                                    .toArray(CompletableFuture[]::new)
                    );
                    return afterFutures.thenCompose(voidResult2 ->
                            convertResponse(response, Void.class));
                })
        );
    }

    /**
     * Execute a ping call with java HttpClient.
     */
    protected CompletableFuture<Pong> executePing() {
        String pingUrl = getPingUrl();
        return get(pingUrl).thenApply(response -> Optional.ofNullable(response.headers().get(HeaderConst.VERSION))
                .map(values -> values.get(0))
                .orElse(null)).thenApply(Pong::new);
    }

    private @NotNull <T> CompletableFuture<T> convertResponse(HttpResponse response, Class<T> type) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            try {
                T resp = JacksonService.toObject(response.body(), type);
                return CompletableFuture.completedFuture(resp);
            } catch (IOException e) {
                CompletableFuture<T> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
        } else {
            String body = response.bodyAsString();
            String errorMsg = "http error: " + body;
            OpenGeminiException openGeminiException = new OpenGeminiException(errorMsg, response.statusCode());
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(openGeminiException);
            return future;
        }
    }

    private CompletableFuture<HttpResponse> get(String url) {
        return client.get(buildUriWithPrefix(url), headers);
    }

    private CompletableFuture<HttpResponse> post(String url, String body) {
        return client.post(buildUriWithPrefix(url), body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8),
                headers);
    }

    @Override
    public void close() throws IOException {
        this.client.close();
    }

    @Override
    public String toString() {
        return "OpenGeminiClient{" + "httpEngine=" + conf.getHttpConfig().engine() + '}';
    }

    private CompletableFuture<HttpResponse> executeHttpQuery(Query query) {
        String queryUrl = getQueryUrl(query);
        return get(queryUrl);
    }

    private CompletableFuture<HttpResponse> executeHttpWrite(Write write) {
        String writeUrl = getWriteUrl(write.getDatabase(), write.getRetentionPolicy());
        return post(writeUrl, write.getLineProtocol());
    }
}
