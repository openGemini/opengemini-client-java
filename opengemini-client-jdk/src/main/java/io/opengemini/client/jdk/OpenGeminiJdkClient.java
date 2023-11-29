package io.opengemini.client.jdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.opengemini.client.api.Address;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.SslContextUtil;
import io.opengemini.client.api.TlsConfig;
import io.opengemini.client.jdk.common.OpenGeminiCommon;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class OpenGeminiJdkClient {

    private final Configuration conf;

    private final List<String> serverUrls = new ArrayList<>();

    private final HttpClient client;

    private final AtomicInteger prevIndex = new AtomicInteger(0);

    public OpenGeminiJdkClient(Configuration conf) {
        this.conf = conf;
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1);
        String httpPrefix;
        if (conf.isTlsEnabled()) {
            TlsConfig tlsConfig = conf.getTlsConfig();
            builder = builder.sslContext(SslContextUtil.buildSSLContextFromJks(
                    tlsConfig.getKeyStorePath(),
                    tlsConfig.getKeyStorePassword(),
                    tlsConfig.getTrustStorePath(),
                    tlsConfig.getTrustStorePassword(),
                    tlsConfig.isTlsVerificationDisabled()));
            httpPrefix = "https://";
        } else {
            httpPrefix = "http://";
        }
        for (Address address : conf.getAddresses()) {
            this.serverUrls.add(httpPrefix + address.getHost() + ":" + address.getPort());
        }
        this.client = builder.build();
    }

    public String encodeUtf8(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    private URI buildUri(String url, String command) throws URISyntaxException {
        // avoid multi-thread conflict, realize simple round-robin
        int idx = prevIndex.addAndGet(1);
        idx = idx % this.conf.getAddresses().size();

        return buildUri(idx, url, command);
    }

    private URI buildUri(int index, String url, String command) throws URISyntaxException {
        StringBuilder sb = new StringBuilder(this.serverUrls.get(index))
                .append(url)
                .append("?q=")
                .append(encodeUtf8(command));

        return new URI(sb.toString());
    }

    public CompletableFuture<QueryResult> query(Query query) throws URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(buildUri("/query", query.getCommand()))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenCompose(response ->{
            CompletableFuture<QueryResult> failedFuture = new CompletableFuture<>();
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                try {
                    QueryResult body = OpenGeminiCommon.converJson2Bean(response.body(), QueryResult.class);
                    failedFuture.complete(body);
                } catch (JsonProcessingException e) {
                    failedFuture.completeExceptionally(e);
                }
                return failedFuture;
            }

            failedFuture.completeExceptionally(new OpenGeminiException("http error: " + response.body(),
                        response.statusCode()));
            return failedFuture;
        });
    }
}
