package io.opengemini.client.common;

import io.opengemini.client.api.Address;
import io.opengemini.client.api.BaseConfiguration;
import io.opengemini.client.api.Endpoint;
import io.opengemini.client.api.Query;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseClient {
    private final List<Endpoint> serverUrls = new ArrayList<>();

    private final AtomicInteger prevIndex = new AtomicInteger(-1);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private final HttpClient healthCheckClient = HttpClient.newHttpClient();

    public BaseClient(BaseConfiguration conf) {
        String httpPrefix;
        if (conf.isTlsEnabled()) {
            httpPrefix = "https://";
        } else {
            httpPrefix = "http://";
        }
        for (Address address : conf.getAddresses()) {
            String url = httpPrefix + address.getHost() + ":" + address.getPort();
            this.serverUrls.add(new Endpoint(url, new AtomicBoolean(false)));
        }
        startHealthCheck();
    }

    /**
     * Health Check
     * Start schedule task(period 10s) to ping all server url
     */
    private void startHealthCheck() {
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                for (Endpoint url : serverUrls) {
                    try {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(url.getUrl() + getPingUrl()))
                                .GET()
                                .timeout(Duration.ofSeconds(2))
                                .build();
                        HttpResponse<String> response = healthCheckClient
                                .sendAsync(request, HttpResponse.BodyHandlers.ofString()).get();
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            url.setIsDown(new AtomicBoolean(false));
                        } else {
                            url.setIsDown(new AtomicBoolean(true));
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        url.setIsDown(new AtomicBoolean(true));
                    }
                }
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    protected void stopHealthCheck() {
        scheduler.shutdown();
    }

    protected String nextUrlPrefix() {
        int idx = Math.abs(prevIndex.incrementAndGet() % serverUrls.size());
        for (int i = 0; i < serverUrls.size(); i++) {
            if (!serverUrls.get(idx).getIsDown().get()) {
                return serverUrls.get(idx).getUrl();
            } else {
                idx = Math.abs(prevIndex.incrementAndGet() % serverUrls.size());
            }
        }
        return serverUrls.get(idx).getUrl();
    }

    protected String encode(String str) {
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new IllegalStateException("utf-8 should be supported by jvm", e);
        }
    }

    protected String getWriteUrl(String database) {
        return UrlConst.WRITE + "?db=" + database;
    }

    protected String getPingUrl() {
        return UrlConst.PING;
    }

    protected String getQueryUrl(Query query) {
        String queryUrl = UrlConst.QUERY + "?q=" + encode(query.getCommand());

        if (query.getDatabase() != null) {
            queryUrl += "&db=" + query.getDatabase();
        }

        if (query.getRetentionPolicy() != null) {
            queryUrl += "&rp=" + query.getRetentionPolicy();
        }

        if (query.getPrecision() != null) {
            queryUrl += "&epoch=" + query.getPrecision().getEpoch();
        }
        return queryUrl;
    }
}
