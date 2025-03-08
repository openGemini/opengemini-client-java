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
import io.opengemini.client.api.AuthConfig;
import io.opengemini.client.api.AuthType;
import io.opengemini.client.api.CompressMethod;
import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.ContentType;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Pong;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.common.BaseAsyncClient;
import io.opengemini.client.common.HeaderConst;
import io.opengemini.client.common.JacksonService;
import io.opengemini.client.common.compress.GzipCompressor;
import io.opengemini.client.common.compress.SnappyCompressor;
import io.opengemini.client.common.compress.ZstdCompressor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class OpenGeminiClient extends BaseAsyncClient {
    protected final Configuration conf;

    private final HttpClient client;
    private final Map<String, Object> compressorCache = new ConcurrentHashMap<>();

    public OpenGeminiClient(@NotNull Configuration conf) {
        super(conf);
        this.conf = conf;
        AuthConfig authConfig = conf.getAuthConfig();
        HttpClientConfig httpConfig = conf.getHttpConfig();
        if (authConfig != null && authConfig.getAuthType().equals(AuthType.PASSWORD)) {
            httpConfig.addRequestFilter(
                    new BasicAuthRequestFilter(authConfig.getUsername(), String.valueOf(authConfig.getPassword())));
        }
        this.client = HttpClientFactory.createHttpClient(httpConfig);
    }

    /**
     * Execute a GET query call with java HttpClient.
     *
     * @param query the query to execute.
     */
    @Override
    protected CompletableFuture<QueryResult> executeQuery(Query query) {
        String queryUrl = getQueryUrl(query);
        return get(queryUrl).thenCompose(response -> convertResponse(response, QueryResult.class));
    }

    /**
     * Execute a POST query call with java HttpClient.
     *
     * @param query the query to execute.
     */
    @Override
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
    @Override
    protected CompletableFuture<Void> executeWrite(String database, String retentionPolicy, String lineProtocol) {
        String writeUrl = getWriteUrl(database, retentionPolicy);
        return post(writeUrl, lineProtocol).thenCompose(response -> convertResponse(response, Void.class));
    }

    /**
     * Execute a ping call with java HttpClient.
     */
    @Override
    protected CompletableFuture<Pong> executePing() {
        String pingUrl = getPingUrl();
        return get(pingUrl).thenApply(response -> Optional.ofNullable(response.headers().get(HeaderConst.VERSION))
                .map(values -> values.get(0))
                .orElse(null)).thenApply(Pong::new);
    }

    private @NotNull <T> CompletableFuture<T> convertResponse(HttpResponse response, Class<T> type) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            try {
                T resp = processResponseBody(response, type);
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

    private <T> T processResponseBody(HttpResponse response, Class<T> type) throws IOException {
        String contentType = response.headers().get("Content-Type") != null
                ? response.headers().get("Content-Type").get(0) : null;
        String contentEncoding = response.headers().get("Content-Encoding") != null
                ? response.headers().get("Content-Encoding").get(0) : null;
        byte[] body = processCompression(contentEncoding, response.body(), type);

       return processContentType(contentType, body, type);
    }

    private <T> byte[] processCompression(String compressMethod, byte[] body, Class<T> type) throws IOException {
        byte[] decompressedBody = null;
        if (CompressMethod.GZIP.getValue().equals(compressMethod)) {
            GzipCompressor compressor = (GzipCompressor) compressorCache.computeIfAbsent(CompressMethod.GZIP.getValue(), k -> new GzipCompressor());
            decompressedBody = compressor.decompress(body);
        } else if (CompressMethod.SNAPPY.getValue().equals(compressMethod)) {
            SnappyCompressor compressor = (SnappyCompressor) compressorCache.computeIfAbsent(CompressMethod.SNAPPY.getValue(), k -> new SnappyCompressor());
            decompressedBody = compressor.decompress(body);
        } else if (CompressMethod.ZSTD.getValue().equals(compressMethod)) {
            ZstdCompressor compressor = (ZstdCompressor) compressorCache.computeIfAbsent(CompressMethod.ZSTD.getValue(), k -> new ZstdCompressor());
            decompressedBody = compressor.decompress(body);
        }

        return decompressedBody != null ? decompressedBody : body;
    }

    private <T> T processContentType(String contentType, byte[] body,  Class<T> type) throws IOException {
        if (ContentType.JSON.getValue().equals(contentType)) {
            return JacksonService.toObject(body, type);
        } else if (ContentType.MSGPACK.getValue().equals(contentType)) {
            throw new IOException("Unsupported content type: " + contentType);
        }
        return JacksonService.toObject(body, type);
    }

    public CompletableFuture<HttpResponse> get(String url) {
        return client.get(buildUriWithPrefix(url), headers);
    }

    public CompletableFuture<HttpResponse> post(String url, String body) {
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
}
