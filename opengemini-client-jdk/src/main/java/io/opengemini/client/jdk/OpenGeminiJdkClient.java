package io.opengemini.client.jdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.TlsConfig;
import io.opengemini.client.common.BaseClient;
import io.opengemini.client.common.JacksonService;
import io.opengemini.client.common.UrlConst;

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

    public CompletableFuture<QueryResult> createDatabase(String database) {
        String command = "CREATE DATABASE \"%s\"".formatted(database);
        Query query = new Query(command);
        String queryUrl = getQueryUrl(query);
        return httpExcute(queryUrl, QueryResult.class, UrlConst.POST);
    }

    public CompletableFuture<QueryResult> dropDatabase(String database) {
        String command = "DROP DATABASE \"%s\"".formatted(database);
        Query query = new Query(command);
        String queryUrl = getQueryUrl(query);
        return httpExcute(queryUrl, QueryResult.class, UrlConst.POST);
    }

    public CompletableFuture<QueryResult> query(Query query) {
        String queryUrl = getQueryUrl(query);
        return httpExcute(queryUrl, QueryResult.class);
    }

    public CompletableFuture<QueryResult> queryPost(Query query) {
        String queryUrl = getQueryUrl(query);
        return httpExcute(queryUrl, QueryResult.class, UrlConst.POST);
    }

    public <T> CompletableFuture<T> httpExcute(String url, Class<T> type) {
        return httpExcute(url, type, UrlConst.GET);
    }

    public <T> CompletableFuture<T> httpExcute(String url, Class<T> type, String method) {
        CompletableFuture<HttpResponse<String>> future;
        if (UrlConst.GET.equals(method)) {
            future = get(url);
        } else if (UrlConst.POST.equals(method)) {
            future = post(url);
        } else {
            Exception e = new RuntimeException("not support method:" + method);
            return CompletableFuture.failedFuture(e);
        }

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

    public CompletableFuture<HttpResponse<String>> post(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri(url))
                .POST(HttpRequest.BodyPublishers.noBody())
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
