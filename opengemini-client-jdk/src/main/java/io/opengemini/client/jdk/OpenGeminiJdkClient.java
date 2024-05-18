package io.opengemini.client.jdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.RetentionPolicy;
import io.opengemini.client.api.RpConfig;
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
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
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
                    tlsConfig.isTlsVerifyDisabled()));
        }
        this.client = builder.build();
    }

    public CompletableFuture<Void> createDatabase(String database) {
        String command = "CREATE DATABASE \"%s\"".formatted(database);
        Query query = new Query(command);
        String queryUrl = getQueryUrl(query);
        return httpExecute(queryUrl, QueryResult.class, UrlConst.POST).thenApply(rsp -> null);
    }

    public CompletableFuture<Void> dropDatabase(String database) {
        String command = "DROP DATABASE \"%s\"".formatted(database);
        Query query = new Query(command);
        String queryUrl = getQueryUrl(query);
        return httpExecute(queryUrl, QueryResult.class, UrlConst.POST).thenApply(rsp -> null);
    }

    public CompletableFuture<List<String>> showDatabases() {
        String command = "SHOW DATABASES";
        Query query = new Query(command);
        String queryUrl = getQueryUrl(query);
        return httpExecute(queryUrl, QueryResult.class)
                .thenApply(rsp -> rsp.getResults().get(0).getSeries().get(0).getValues().stream()
                        .map(x -> String.valueOf(x.get(0))).toList());
    }

    public CompletableFuture<Void> createRetentionPolicy(String database, RpConfig rpConfig, boolean isDefault) {
        StringBuilder command = new StringBuilder("CREATE RETENTION POLICY ");
        command.append(rpConfig.getName());
        command.append(" ON ");
        command.append(database);
        command.append(" DURATION ");
        command.append(rpConfig.getDuration());
        command.append(" REPLICATION 1");

        if (rpConfig.getShardGroupDuration() != null && !rpConfig.getShardGroupDuration().isBlank()) {
            command.append(" SHARD DURATION ");
            command.append(rpConfig.getShardGroupDuration());
        }

        if (rpConfig.getIndexDuration() != null && !rpConfig.getIndexDuration().isBlank()) {
            command.append(" INDEX DURATION ");
            command.append(rpConfig.getIndexDuration());
        }

        if (isDefault) {
            command.append(" DEFAULT ");
        }
        Query query = new Query(command.toString());
        String queryUrl = getQueryUrl(query);
        return httpExecute(queryUrl, QueryResult.class, UrlConst.POST).thenApply(rsp -> null);
    }

    private List<RetentionPolicy> converseRps(List<List<Object>> queryRpValues) {
        ArrayList<RetentionPolicy> retentionPolicies = new ArrayList<>();
        queryRpValues.forEach(x -> retentionPolicies.add(converseRp(x)));
        return retentionPolicies;
    }

    private RetentionPolicy converseRp(List<Object> queryRpValue) {
        RetentionPolicy rst = new RetentionPolicy();
        rst.setName((String) queryRpValue.get(0));
        rst.setDuration((String) queryRpValue.get(1));
        rst.setShardGroupDuration((String) queryRpValue.get(2));
        rst.setHotDuration((String) queryRpValue.get(3));
        rst.setWarmDuration((String) queryRpValue.get(4));
        rst.setIndexDuration((String) queryRpValue.get(5));
        rst.setReplicaNum((Integer) queryRpValue.get(6));
        rst.setDefault((Boolean) queryRpValue.get(7));
        return rst;
    }

    public CompletableFuture<List<RetentionPolicy>> showRetentionPolicies(String database) {
        String command = "SHOW RETENTION POLICIES";
        if (database == null || database.isBlank()) {
            return null;
        }

        Query query = new Query(command);
        query.setDatabase(database);

        String queryUrl = getQueryUrl(query);
        return httpExecute(queryUrl, QueryResult.class)
                .thenApply(rsp -> converseRps(rsp.getResults().get(0).getSeries().get(0).getValues()));
    }

    public CompletableFuture<Void> dropRetentionPolicy(String database, String retentionPolicy) {
        String command = "DROP RETENTION POLICY %s ON \"%s\"".formatted(retentionPolicy, database);
        Query query = new Query(command);
        String queryUrl = getQueryUrl(query);
        return httpExecute(queryUrl, QueryResult.class, UrlConst.POST).thenApply(rsp -> null);
    }

    public CompletableFuture<QueryResult> query(Query query) {
        String queryUrl = getQueryUrl(query);
        return httpExecute(queryUrl, QueryResult.class);
    }

    public CompletableFuture<Void> write(String database, Point point) {
        String writeUrl = getWriteUrl(database);
        String body = point.toString();

        return httpExecute(writeUrl, Void.class, UrlConst.POST, HttpRequest.BodyPublishers.ofString(body));
    }

    public CompletableFuture<Void> writeBatch(String database, List<Point> points) {
        String writeUrl = getWriteUrl(database);
        StringJoiner sj = new StringJoiner("\n");
        points.forEach(point -> sj.add(point.toString()));

        return httpExecute(writeUrl, Void.class, UrlConst.POST, HttpRequest.BodyPublishers.ofString(sj.toString()));
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
}
