package io.opengemini.client.impl;

import io.opengemini.client.api.Configuration;
import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.OpenGeminiSyncClient;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.Pong;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.RetentionPolicy;
import io.opengemini.client.api.RpConfig;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class OpenGeminiSyncClientImpl implements OpenGeminiSyncClient {
    protected Configuration conf;
    private OpenGeminiClient openGeminiAsyncClient;

    OpenGeminiSyncClientImpl(Configuration conf) {
        this.conf = conf;
        this.openGeminiAsyncClient = new OpenGeminiClient(conf);
    }

    @Override
    public void createDatabase(String database) throws OpenGeminiException {
        wrapFuture(openGeminiAsyncClient.createDatabase(database));
    }

    @Override
    public void dropDatabase(String database) throws OpenGeminiException {
        wrapFuture(openGeminiAsyncClient.dropDatabase(database));
    }

    @Override
    public List<String> showDatabases() throws OpenGeminiException {
        return wrapFuture(openGeminiAsyncClient.showDatabases());
    }

    @Override
    public void createRetentionPolicy(String database, RpConfig rpConfig, boolean isDefault)
        throws OpenGeminiException {
        wrapFuture(openGeminiAsyncClient.createRetentionPolicy(database, rpConfig, isDefault));
    }

    @Override
    public List<RetentionPolicy> showRetentionPolicies(String database) throws OpenGeminiException {
        return wrapFuture(openGeminiAsyncClient.showRetentionPolicies(database));
    }

    @Override
    public void dropRetentionPolicy(String database, String retentionPolicy) throws OpenGeminiException {
        wrapFuture(openGeminiAsyncClient.dropRetentionPolicy(database, retentionPolicy));
    }

    @Override
    public QueryResult query(Query query) throws OpenGeminiException {
        return wrapFuture(openGeminiAsyncClient.query(query));
    }

    @Override
    public void write(String database, Point point) throws OpenGeminiException {
        wrapFuture(openGeminiAsyncClient.write(database, point));
    }

    @Override
    public void write(String database, List<Point> points) throws OpenGeminiException {
        wrapFuture(openGeminiAsyncClient.write(database, points));
    }

    @Override
    public void write(String database, String retentionPolicy, Point point) throws OpenGeminiException {
        wrapFuture(openGeminiAsyncClient.write(database, retentionPolicy, point));
    }

    @Override
    public void write(String database, String retentionPolicy, List<Point> points) throws OpenGeminiException {
        wrapFuture(openGeminiAsyncClient.write(database, retentionPolicy, points));
    }

    @Override
    public Pong ping() throws OpenGeminiException {
        return wrapFuture(openGeminiAsyncClient.ping());
    }

    @Override
    public void close() throws IOException {
        openGeminiAsyncClient.close();
    }

    private <T> T wrapFuture(CompletableFuture<T> future) throws OpenGeminiException {
        try {
            return future.get(conf.getHttpConfig().timeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new OpenGeminiException(e);
        }
    }
}
