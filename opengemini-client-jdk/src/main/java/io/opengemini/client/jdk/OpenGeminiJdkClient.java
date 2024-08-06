package io.opengemini.client.jdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.opengemini.client.api.AuthConfig;
import io.opengemini.client.api.AuthType;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Pong;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.TlsConfig;
import io.opengemini.client.common.BaseAsyncClient;
import io.opengemini.client.common.HeaderConst;
import io.opengemini.client.common.JacksonService;
import io.opengemini.client.common.UrlConst;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class OpenGeminiJdkClient extends BaseAsyncClient {

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
                    tlsConfig.keyStorePath,
                    tlsConfig.keyStorePassword,
                    tlsConfig.trustStorePath,
                    tlsConfig.trustStorePassword,
                    tlsConfig.verifyDisabled));
        }

        AuthConfig authConfig = conf.getAuthConfig();
        if (authConfig != null) {
            AuthType authType = authConfig.getAuthType();
            if (AuthType.PASSWORD.equals(authType)) {
                builder.authenticator(getAuthenticator(authConfig.getUsername(), authConfig.getPassword()));
            }
        }
        this.client = builder.build();
    }

    private Authenticator getAuthenticator(String username, String password) {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password.toCharArray());
            }
        };
    }

    /**
     * Execute a GET query call with java HttpClient.
     *
     * @param query the query to execute.
     */
    @Override
    protected CompletableFuture<QueryResult> executeQuery(Query query) {
        String queryUrl = getQueryUrl(query);
        return httpExecute(queryUrl, QueryResult.class);
    }

    /**
     * Execute a POST query call with java HttpClient.
     *
     * @param query the query to execute.
     */
    @Override
    protected CompletableFuture<QueryResult> executePostQuery(Query query) {
        String queryUrl = getQueryUrl(query);
        return httpExecute(queryUrl, QueryResult.class, UrlConst.POST);
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
        return httpExecute(writeUrl, Void.class, UrlConst.POST, HttpRequest.BodyPublishers.ofString(lineProtocol));
    }

    /**
     * Execute a ping call with java HttpClient.
     */
    @Override
    protected CompletableFuture<Pong> executePing() {
        String pingUrl = getPingUrl();
        return get(pingUrl).thenApply(response -> response.headers().firstValue(HeaderConst.VERSION).orElse(null))
                .thenApply(Pong::new);
    }

    private <T> CompletableFuture<T> httpExecute(String url, Class<T> type) {
        return httpExecute(url, type, UrlConst.GET);
    }

    private <T> CompletableFuture<T> httpExecute(String url, Class<T> type, String method) {
        return httpExecute(url, type, method, HttpRequest.BodyPublishers.noBody());
    }

    private <T> CompletableFuture<T> httpExecute(String url, Class<T> type,
                                                 String method, HttpRequest.BodyPublisher bodyPublisher) {
        CompletableFuture<HttpResponse<String>> future;
        if (UrlConst.GET.equals(method)) {
            future = get(url);
        } else if (UrlConst.POST.equals(method)) {
            future = post(url, bodyPublisher);
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

    public CompletableFuture<HttpResponse<String>> post(String url, HttpRequest.BodyPublisher bodyPublisher) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri(url))
                .POST(bodyPublisher)
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

    @Override
    public void close() {
        // no need to close
    }
}
