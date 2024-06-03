package io.opengemini.client.okhttp;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.opengemini.client.api.AuthConfig;
import io.opengemini.client.api.AuthType;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.TlsConfig;
import io.opengemini.client.common.BaseAsyncClient;
import io.opengemini.client.common.JacksonService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class OpenGeminiOkhttpClient extends BaseAsyncClient {

    private static final okhttp3.MediaType MEDIA_TYPE_STRING = MediaType.parse("text/plain");
    private final OkHttpClient okHttpClient;

    public OpenGeminiOkhttpClient(Configuration conf) {
        super(conf);
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().connectTimeout(conf.getConnectTimeout())
                .readTimeout(conf.getTimeout())
                .writeTimeout(conf.getTimeout());

        if (conf.isTlsEnabled()) {
            TlsConfig tlsConfig = conf.getTlsConfig();

            // set tls version and cipher suits
            ConnectionSpec connectionSpec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).tlsVersions(
                    tlsConfig.tlsVersions).cipherSuites(tlsConfig.tlsCipherSuites).build();
            okHttpClientBuilder.connectionSpecs(Collections.singletonList(connectionSpec));

            // create ssl context from keystore and truststore
            OkHttpSslContextFactory.OkHttpSslContext sslContext = OkHttpSslContextFactory.createOkHttpSslContext(
                    tlsConfig);
            okHttpClientBuilder.sslSocketFactory(sslContext.sslSocketFactory, sslContext.x509TrustManager);

            // override hostnameVerifier to make it always success when hostname verification has been disabled
            if (tlsConfig.tlsHostnameVerifyDisabled) {
                okHttpClientBuilder.hostnameVerifier((s, sslSession) -> true);
            }
        }

        AuthConfig authConfig = conf.getAuthConfig();
        if (authConfig != null) {
            configClientAuth(authConfig, okHttpClientBuilder);
        }

        ConnectionPoolConfig poolConfig = conf.getConnectionPoolConfig();
        if (poolConfig != null) {
            configClientConnectionPool(poolConfig, okHttpClientBuilder);
        }

        this.okHttpClient = okHttpClientBuilder.build();
    }

    private static void configClientAuth(AuthConfig authConfig, OkHttpClient.Builder okHttpClientBuilder) {
        if (AuthType.PASSWORD == authConfig.getAuthType()) {
            okHttpClientBuilder.addInterceptor(
                    new BasicAuthInterceptor(authConfig.getUsername(), authConfig.getPassword()));
        }
    }

    private void configClientConnectionPool(ConnectionPoolConfig poolConfig, OkHttpClient.Builder okHttpClientBuilder) {
        int maxIdleConnections = poolConfig.getMaxIdleConnections();
        long keepAliveDuration = poolConfig.getKeepAliveDuration();
        TimeUnit timeUnit = poolConfig.getKeepAliveTimeUnit();
        if (maxIdleConnections > 0 && keepAliveDuration > 0 && timeUnit != null) {
            okHttpClientBuilder.connectionPool(new ConnectionPool(maxIdleConnections, keepAliveDuration, timeUnit));
        }
    }

    /**
     * Execute a GET query call with OkHttpClient.
     *
     * @param query the query to execute.
     */
    @Override
    protected CompletableFuture<QueryResult> executeQuery(Query query) {
        String queryUrl = getQueryUrl(query);
        Request request = new Request.Builder().url(nextUrlPrefix() + queryUrl).get().build();
        return execute(request, QueryResult.class);
    }

    /**
     * Execute a POST query call with OkHttpClient.
     *
     * @param query the query to execute.
     */
    @Override
    protected CompletableFuture<QueryResult> executePostQuery(Query query) {
        String queryUrl = getQueryUrl(query);
        Request request = new Request.Builder().url(nextUrlPrefix() + queryUrl)
                .post(RequestBody.create(new byte[0]))
                .build();
        return execute(request, QueryResult.class);
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
        Request request = new Request.Builder().url(nextUrlPrefix() + writeUrl)
                .post(RequestBody.create(lineProtocol, MEDIA_TYPE_STRING))
                .build();
        return execute(request, Void.class);
    }

    private <T> CompletableFuture<T> execute(Request request, Class<T> type) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                int statusCode = response.code();
                ResponseBody responseBody = response.body();

                String responseBodyString;
                if (responseBody != null) {
                    responseBodyString = responseBody.string();
                } else {
                    responseBodyString = null;
                }

                if (response.isSuccessful()) {
                    try {
                        T result = JacksonService.toObject(responseBodyString, type);
                        future.complete(result);
                    } catch (JsonProcessingException e) {
                        future.completeExceptionally(e);
                    }
                } else {
                    String httpErrorMsg = responseBodyString == null ? "empty body" : responseBodyString;
                    future.completeExceptionally(new OpenGeminiException("http error: " + httpErrorMsg, statusCode));
                }
            }
        });
        return future;
    }

    @Override
    public void close() {
        okHttpClient.dispatcher().executorService().shutdown();
        okHttpClient.connectionPool().evictAll();
    }
}
