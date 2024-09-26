package io.opengemini.client.jdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.shoothzj.http.facade.client.HttpClient;
import io.github.shoothzj.http.facade.client.HttpClientConfig;
import io.github.shoothzj.http.facade.client.HttpClientEngine;
import io.github.shoothzj.http.facade.client.HttpClientFactory;
import io.github.shoothzj.http.facade.core.HttpResponse;
import io.opengemini.client.api.AuthConfig;
import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Pong;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.common.BaseAsyncClient;
import io.opengemini.client.common.HeaderConst;
import io.opengemini.client.common.JacksonService;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class OpenGeminiJdkClient extends BaseAsyncClient {

    private final Configuration conf;

    private final HttpClient client;

    public OpenGeminiJdkClient(@NotNull Configuration conf) {
        super(conf);
        this.conf = conf;
        HttpClientConfig.Builder builder = new HttpClientConfig.Builder();
        builder.engine(HttpClientEngine.JDK).connectTimeout(conf.getConnectTimeout())
                           .timeout(conf.getTimeout());

        if (conf.isTlsEnabled()) {
            builder.tlsConfig(conf.getTlsConfig());
        }

        AuthConfig authConfig = conf.getAuthConfig();
        if (authConfig != null) {
            configClientAuth(builder, authConfig);
        }
        this.client = HttpClientFactory.createHttpClient(builder.build());
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
     * @param database     the name of the database.
     * @param lineProtocol the line protocol string to write.
     */
    @Override
    protected CompletableFuture<Void> executeWrite(String database, String lineProtocol) {
        String writeUrl = getWriteUrl(database);
        return post(writeUrl, lineProtocol).thenCompose(response -> convertResponse(response, Void.class));
    }

    /**
     * Execute a ping call with java HttpClient.
     */
    @Override
    protected CompletableFuture<Pong> executePing() {
        String pingUrl = getPingUrl();
        return get(pingUrl).thenApply(
            response -> Optional.ofNullable(response.headers().get(HeaderConst.VERSION)).map(values -> values.get(0))
                                .orElse(null)).thenApply(Pong::new);
    }

    private <T> @NotNull CompletableFuture<T> convertResponse(HttpResponse response, Class<T> type) {
        String body = response.bodyAsString();
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            try {
                T resp = JacksonService.toObject(body, type);
                return CompletableFuture.completedFuture(resp);
            } catch (JsonProcessingException e) {
                CompletableFuture<T> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
        } else {
            String errorMsg = "http error: " + body;
            OpenGeminiException openGeminiException = new OpenGeminiException(errorMsg, response.statusCode());
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(openGeminiException);
            return future;
        }
    }

    public CompletableFuture<HttpResponse> get(String url) {
        return client.get(buildUriWithPrefix(url), headers);
    }

    public CompletableFuture<HttpResponse> post(String url, String body) {
        return client.post(buildUriWithPrefix(url), body == null ? null : body.getBytes(StandardCharsets.UTF_8),
                headers);
    }

    @Override
    public void close() {
        // no need to close
    }
}
