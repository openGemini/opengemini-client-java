package io.opengemini.client.asynchttpclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpStatusClass;
import io.opengemini.client.api.AuthConfig;
import io.opengemini.client.api.AuthType;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Pong;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.common.BaseAsyncClient;
import io.opengemini.client.common.HeaderConst;
import io.opengemini.client.common.JacksonService;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class OpenGeminiAsyncHttpClient extends BaseAsyncClient {

    private final AsyncHttpClient asyncHttpClient;

    public OpenGeminiAsyncHttpClient(@NotNull Configuration conf) {
        super(conf);
        this.asyncHttpClient = buildClient(conf);
    }

    private AsyncHttpClient buildClient(Configuration conf) {
        DefaultAsyncHttpClientConfig.Builder builder = Dsl.config();
        builder.setConnectTimeout(conf.getConnectTimeout())
                .setReadTimeout(conf.getTimeout())
                .setRequestTimeout(conf.getTimeout());

        AuthConfig authConfig = conf.getAuthConfig();
        if (authConfig != null) {
            configClientAuth(authConfig, builder);
        }
        return Dsl.asyncHttpClient(builder);
    }

    private static void configClientAuth(AuthConfig authConfig, DefaultAsyncHttpClientConfig.Builder builder) {
        if (AuthType.PASSWORD == authConfig.getAuthType()) {
            builder.addRequestFilter(new BasicAuthRequestFilter(authConfig.getUsername(), authConfig.getPassword()));
        }
    }

    /**
     * Execute a GET query call with AsyncHttpClient.
     *
     * @param query the query to execute.
     */
    @Override
    protected CompletableFuture<QueryResult> executeQuery(Query query) {
        String queryUrl = getQueryUrl(query);
        CompletableFuture<Response> responseFuture = asyncHttpClient.prepareGet(nextUrlPrefix() + queryUrl)
                .execute()
                .toCompletableFuture();
        return compose(responseFuture, QueryResult.class);
    }

    /**
     * Execute a POST query call with AsyncHttpClient.
     *
     * @param query the query to execute.
     */
    @Override
    protected CompletableFuture<QueryResult> executePostQuery(Query query) {
        String queryUrl = getQueryUrl(query);
        CompletableFuture<Response> responseFuture = asyncHttpClient.preparePost(nextUrlPrefix() + queryUrl)
                .execute()
                .toCompletableFuture();
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
        CompletableFuture<Response> responseFuture = asyncHttpClient.preparePost(nextUrlPrefix() + writeUrl)
                .setBody(lineProtocol)
                .execute()
                .toCompletableFuture();
        return compose(responseFuture, Void.class);
    }

    /**
     * Execute a ping call with AsyncHttpClient.
     */
    @Override
    protected CompletableFuture<Pong> executePing() {
        String pingUrl = getPingUrl();
        return asyncHttpClient.prepareGet(nextUrlPrefix() + pingUrl)
                .execute()
                .toCompletableFuture()
                .thenApply(response -> new Pong(response.getHeader(HeaderConst.VERSION)));
    }

    private <T> CompletableFuture<T> compose(CompletableFuture<Response> responseFuture, Class<T> type) {
        return responseFuture.thenCompose(response -> {
            HttpResponseStatus responseStatus = HttpResponseStatus.valueOf(response.getStatusCode());
            String responseBody = response.getResponseBody();

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
    public void close() throws Exception {
        asyncHttpClient.close();
    }
}
