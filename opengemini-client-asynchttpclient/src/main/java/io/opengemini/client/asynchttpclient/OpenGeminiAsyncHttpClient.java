package io.opengemini.client.asynchttpclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.shoothzj.http.facade.client.HttpClient;
import io.github.shoothzj.http.facade.client.HttpClientConfig;
import io.github.shoothzj.http.facade.client.HttpClientEngine;
import io.github.shoothzj.http.facade.client.HttpClientFactory;
import io.github.shoothzj.http.facade.core.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpStatusClass;
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
import java.util.concurrent.CompletableFuture;

public class OpenGeminiAsyncHttpClient extends BaseAsyncClient {

    private final HttpClient asyncHttpClient;

    public OpenGeminiAsyncHttpClient(@NotNull Configuration conf) {
        super(conf);
        this.asyncHttpClient = buildClient(conf);
    }

    private HttpClient buildClient(Configuration conf) {
        HttpClientConfig.Builder builder = new HttpClientConfig.Builder();
        HttpClientConfig.Builder basicConfig =
            builder.engine(HttpClientEngine.AsyncHttpClient).timeout(conf.getTimeout())
                   .connectTimeout(conf.getConnectTimeout());
        AuthConfig authConfig = conf.getAuthConfig();

        if (authConfig != null) {
            configClientAuth(builder, authConfig);
        }
        return HttpClientFactory.createHttpClient(basicConfig.build());
    }


    /**
     * Execute a GET query call with AsyncHttpClient.
     *
     * @param query the query to execute.
     */
    @Override
    protected CompletableFuture<QueryResult> executeQuery(Query query) {
        String queryUrl = getQueryUrl(query);
        CompletableFuture<HttpResponse> resFuture = asyncHttpClient.get(buildUriWithPrefix(queryUrl));
        return compose(resFuture, QueryResult.class);
    }

    /**
     * Execute a POST query call with AsyncHttpClient.
     *
     * @param query the query to execute.
     */
    @Override
    protected CompletableFuture<QueryResult> executePostQuery(Query query) {
        String queryUrl = getQueryUrl(query);
        CompletableFuture<HttpResponse> responseFuture = asyncHttpClient.post(buildUriWithPrefix(queryUrl), null);
        return compose(responseFuture, QueryResult.class);
    }

    /**
     * Execute a write call with AsyncHttpClient.
     *
     * @param database     the name of the database.
     * @param lineProtocol the line protocol string to write.
     */
    @Override
    protected CompletableFuture<Void> executeWrite(String database, String lineProtocol) {
        String writeUrl = getWriteUrl(database);
        CompletableFuture<HttpResponse> responseFuture = asyncHttpClient.post(buildUriWithPrefix(writeUrl),
                lineProtocol.getBytes(StandardCharsets.UTF_8));
        return compose(responseFuture, Void.class);
    }

    /**
     * Execute a ping call with AsyncHttpClient.
     */
    @Override
    protected CompletableFuture<Pong> executePing() {
        String pingUrl = getPingUrl();
        return asyncHttpClient.get(buildUriWithPrefix(pingUrl))
                .thenApply(response -> new Pong(response.headers().get(HeaderConst.VERSION).get(0)));
    }

    private <T> CompletableFuture<T> compose(CompletableFuture<HttpResponse> responseFuture, Class<T> type) {
        return responseFuture.thenCompose(response -> {
            HttpResponseStatus responseStatus = HttpResponseStatus.valueOf(response.statusCode());
            String responseBody = response.bodyAsString();

            if (HttpStatusClass.SUCCESS == responseStatus.codeClass()) {
                try {
                    T body = JacksonService.toObject(responseBody, type);
                    return CompletableFuture.completedFuture(body);
                } catch (JsonProcessingException e) {
                    return CompletableFuture.failedFuture(e);
                }
            } else {
                OpenGeminiException exp = new OpenGeminiException("http error: " + responseBody, responseStatus.code());
                return CompletableFuture.failedFuture(exp);
            }
        });
    }

    @Override
    public void close() throws IOException {
        asyncHttpClient.close();
    }
}
