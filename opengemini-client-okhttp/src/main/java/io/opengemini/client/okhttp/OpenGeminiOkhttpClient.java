package io.opengemini.client.okhttp;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.shoothzj.http.facade.client.HttpClient;
import io.github.shoothzj.http.facade.client.HttpClientConfig;
import io.github.shoothzj.http.facade.client.HttpClientEngine;
import io.github.shoothzj.http.facade.client.HttpClientFactory;
import io.github.shoothzj.http.facade.core.HttpResponse;
import io.opengemini.client.api.AuthConfig;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Pong;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.common.BaseAsyncClient;
import io.opengemini.client.common.HeaderConst;
import io.opengemini.client.common.JacksonService;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class OpenGeminiOkhttpClient extends BaseAsyncClient {

    private final HttpClient okHttpClient;

    public OpenGeminiOkhttpClient(@NotNull Configuration conf) {
        super(conf);
        HttpClientConfig.Builder builder =
            new HttpClientConfig.Builder().engine(HttpClientEngine.OkHttp)
                                          .connectTimeout(conf.getConnectTimeout())
                                          .timeout(conf.getTimeout());


        if (conf.isTlsEnabled()) {
            builder.tlsConfig(conf.getTlsConfig());
        }

        AuthConfig authConfig = conf.getAuthConfig();
        if (authConfig != null) {
            configClientAuth(builder, authConfig);
        }

        ConnectionPoolConfig poolConfig = conf.getConnectionPoolConfig();
        if (poolConfig != null) {
            configClientConnectionPool(poolConfig, builder);
        }

        this.okHttpClient = HttpClientFactory.createHttpClient(builder.build());
    }

    private static void configClientConnectionPool(ConnectionPoolConfig poolConfig, HttpClientConfig.Builder builder) {
        HttpClientConfig.OkHttpConfig.ConnectionPoolConfig connectionPoolConfig =
            new HttpClientConfig.OkHttpConfig.ConnectionPoolConfig();
        connectionPoolConfig.setMaxIdleConnections(poolConfig.getMaxIdleConnections());

        long nanos = poolConfig.getKeepAliveTimeUnit().toNanos(poolConfig.getKeepAliveDuration());
        connectionPoolConfig.setKeepAliveDuration(Duration.ofNanos(nanos));

        HttpClientConfig.OkHttpConfig okHttpConfig = new HttpClientConfig.OkHttpConfig();
        okHttpConfig.setConnectionPoolConfig(connectionPoolConfig);
        builder.okHttpConfig(okHttpConfig);
    }

    /**
     * Execute a GET query call with OkHttpClient.
     *
     * @param query the query to execute.
     */
    @Override
    protected CompletableFuture<QueryResult> executeQuery(Query query) {
        String queryUrl = getQueryUrl(query);
        return composeExtractBody(get(queryUrl), QueryResult.class);
    }

    /**
     * Execute a POST query call with OkHttpClient.
     *
     * @param query the query to execute.
     */
    @Override
    protected CompletableFuture<QueryResult> executePostQuery(Query query) {
        String queryUrl = getQueryUrl(query);
        return composeExtractBody(post(queryUrl, null), QueryResult.class);
    }

    /**
     * Execute a write call with OkHttpClient.
     *
     * @param database     the name of the database.
     * @param lineProtocol the line protocol string to write.
     */
    @Override
    protected CompletableFuture<Void> executeWrite(String database, String lineProtocol) {
        String writeUrl = getWriteUrl(database);
        return composeExtractBody(post(writeUrl, lineProtocol), Void.class);
    }

    /**
     * Execute a ping call with OkHttpClient.
     */
    @Override
    protected CompletableFuture<Pong> executePing() {
        String pingUrl = getPingUrl();
        CompletableFuture<HttpResponse> respFuture = get(pingUrl);
        return composeExtractHeader(respFuture, HeaderConst.VERSION).thenApply(Pong::new);
    }

    private CompletableFuture<HttpResponse> get(String url) {
        return okHttpClient.get(buildUriWithPrefix(url), headers);
    }

    private CompletableFuture<HttpResponse> post(String url, String body) {
        byte[] requestBody = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        return okHttpClient.post(buildUriWithPrefix(url), requestBody, headers);
    }

    private static <T> CompletableFuture<T> composeExtractBody(CompletableFuture<HttpResponse> responseFuture,
                                                               Class<T> type) {
        return responseFuture.thenCompose(response -> {
            CompletableFuture<T> future = new CompletableFuture<>();

            int statusCode = response.statusCode();
            String responseBodyString = response.bodyAsString();

            if (statusCode >= 200 && statusCode < 300) {
                try {
                    T result = JacksonService.toObject(responseBodyString, type);
                    future.complete(result);
                } catch (JsonProcessingException e) {
                    future.completeExceptionally(e);
                }
            } else {
                completeUnsuccessfulResponse(future, responseBodyString, statusCode);
            }
            return future;
        });
    }

    private static CompletableFuture<String> composeExtractHeader(CompletableFuture<HttpResponse> responseFuture,
                                                                  String headerName) {
        return responseFuture.thenCompose(response -> {
            CompletableFuture<String> future = new CompletableFuture<>();

            int statusCode = response.statusCode();
            String responseBodyString = response.bodyAsString();

            if (statusCode >= 200 && statusCode < 300) {
                future.complete(response.headers().get(headerName).get(0));
            } else {
                completeUnsuccessfulResponse(future, responseBodyString, statusCode);
            }
            return future;
        });
    }

    private static <T> void completeUnsuccessfulResponse(CompletableFuture<T> future, String responseBodyString,
                                                         int statusCode) {
        String httpErrorMsg = responseBodyString == null ? "empty body" : responseBodyString;
        future.completeExceptionally(new OpenGeminiException("http error: " + httpErrorMsg, statusCode));
    }

    @Override
    public void close() throws IOException {
        okHttpClient.close();
    }
}
