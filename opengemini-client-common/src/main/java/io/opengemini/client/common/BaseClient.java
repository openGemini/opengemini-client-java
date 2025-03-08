/*
 * Copyright 2024 openGemini Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opengemini.client.common;

import io.opengemini.client.api.Address;
import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.Endpoint;
import io.opengemini.client.api.Query;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseClient implements Closeable {
    protected final Map<String, List<String>> headers;

    private final List<Endpoint> serverUrls = new ArrayList<>();

    private final AtomicInteger prevIndex = new AtomicInteger(-1);

    private final Optional<ScheduledExecutorService> scheduler;

    public BaseClient(Configuration conf) {
        this.headers = new HashMap<>();
        if (conf.isGzipEnabled()) {
            ArrayList<String> contentEncodingHeader = new ArrayList<>();
            contentEncodingHeader.add("gzip");
            headers.put("Content-Encoding", contentEncodingHeader);
        }

        applyCodec(conf, headers);

        String httpPrefix;
        if (conf.getHttpConfig().tlsConfig() != null) {
            httpPrefix = "https://";
        } else {
            httpPrefix = "http://";
        }
        for (Address address : conf.getAddresses()) {
            String url = httpPrefix + address.getHost() + ":" + address.getPort();
            this.serverUrls.add(new Endpoint(url, new AtomicBoolean(false)));
        }
        if (this.serverUrls.size() > 1) {
            this.scheduler = Optional.of(Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("opengemini-client-health-checker");
                return t;
            }));
        } else {
            this.scheduler = Optional.empty();
        }
        scheduler.ifPresent(this::startHealthCheck);
    }

    private void applyCodec(Configuration config, Map<String, List<String>> headers) {
        if (config.getContentType() != null) {
            List<String> acceptHeader = new ArrayList<>();
            switch (config.getContentType()) {
                case MSGPACK:
                    acceptHeader.add("application/msgpack");
                    break;
                case JSON:
                    acceptHeader.add("application/json");
                    break;
            }
        headers.put("Accept", acceptHeader);
        }
        if (config.getCompressMethod() != null) {
            List<String> acceptEncodingHeader = new ArrayList<>();
            switch (config.getCompressMethod()) {
                case GZIP:
                    acceptEncodingHeader.add("gzip");
                    break;
                case ZSTD:
                    acceptEncodingHeader.add("zstd");
                    break;
                case SNAPPY:
                    acceptEncodingHeader.add("snappy");
                    break;
            }
            headers.put("Accept-Encoding", acceptEncodingHeader);
        }
    }

    /**
     * Health Check
     * Start schedule task(period 10s) to ping all server url
     */
    private void startHealthCheck(ScheduledExecutorService healthCheckSchedule) {
        healthCheckSchedule.scheduleWithFixedDelay(() -> {
            for (Endpoint url : serverUrls) {
                try {
                    URL urls = new URL(url.getUrl() + getPingUrl());
                    HttpURLConnection connection = (HttpURLConnection) urls.openConnection();
                    try {
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(2000);
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        url.getIsDown().set(responseCode < 200 || responseCode >= 300);
                    } finally {
                        connection.disconnect();
                    }
                } catch (IOException e) {
                    url.getIsDown().set(true);
                }
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    protected String nextUrlPrefix() {
        int idx = Math.abs(prevIndex.incrementAndGet() % serverUrls.size());
        if (serverUrls.size() > 1) {
            for (int i = 0; i < serverUrls.size(); i++) {
                if (!serverUrls.get(idx).getIsDown().get()) {
                    return serverUrls.get(idx).getUrl();
                } else {
                    idx = Math.abs(prevIndex.incrementAndGet() % serverUrls.size());
                }
            }
        }
        return serverUrls.get(idx).getUrl();
    }

    protected String buildUriWithPrefix(String url) {
        return nextUrlPrefix() + url;
    }

    protected String encode(String str) {
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new IllegalStateException("utf-8 should be supported by jvm", e);
        }
    }

    protected String getWriteUrl(String database, String retentionPolicy) {
        String writeUrl = UrlConst.WRITE + "?db=" + database;
        if (retentionPolicy != null) {
            writeUrl += "&rp=" + retentionPolicy;
        }
        return writeUrl;
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

    @Override
    public void close() throws IOException {
        scheduler.ifPresent(ExecutorService::shutdown);
    }
}
