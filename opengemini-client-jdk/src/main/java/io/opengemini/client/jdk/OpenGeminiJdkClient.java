package io.opengemini.client.jdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.TlsConfig;
import io.opengemini.client.common.BaseClient;
import io.opengemini.client.common.JacksonService;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class OpenGeminiJdkClient extends BaseClient {

    private final Configuration conf;

    private final HttpClient client;

    public OpenGeminiJdkClient(Configuration conf) {
        super(conf);
        this.conf = conf;
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(conf.getConnectTimeout())
                .version(HttpClient.Version.HTTP_1_1);
        if (conf.isTlsEnabled()) {
            TlsConfig tlsConfig = conf.getTlsConfig();
            builder = builder.sslContext(SslContextUtil.buildSSLContextFromJks(
                    tlsConfig.getKeyStorePath(),
                    tlsConfig.getKeyStorePassword(),
                    tlsConfig.getTrustStorePath(),
                    tlsConfig.getTrustStorePassword(),
                    tlsConfig.isTlsVerificationDisabled()));
        }
        this.client = builder.build();
    }

    public CompletableFuture<QueryResult> query(Query query) {
        String queryUrl = getQueryUrl(query);
        return get(queryUrl, QueryResult.class);
    }

    public <T> CompletableFuture<T> get(String url, Class<T> type) {
        CompletableFuture<HttpResponse<String>> future = get(url);
        return future.thenCompose(response -> {
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                try {
                    T body = JacksonService.toObject(response.body(), type);
                    return CompletableFuture.completedFuture(body);
                } catch (JsonProcessingException e) {
                    return CompletableFuture.failedFuture(e);
                }
            } else {
                return CompletableFuture.failedFuture(new OpenGeminiException("http error: " + response.body(),
                        response.statusCode()));
            }
        });
    }

    public CompletableFuture<HttpResponse<String>> get(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri(url))
                .GET()
                .timeout(this.conf.getTimeout())
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI buildUri(String url) {
        return URI.create(nextUrlPrefix() + url);
    }

    @Override
    protected String encode(String str) {
        // jdk17 has a better way than jdk8
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }
}
