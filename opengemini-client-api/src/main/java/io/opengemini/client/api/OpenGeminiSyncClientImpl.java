package io.opengemini.client.api;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class OpenGeminiSyncClientImpl implements OpenGeminiSyncClient {
    private OpenGeminiAsyncClient openGeminiAsyncClient;
    private Configuration conf;

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
    public void close() throws Exception {
        openGeminiAsyncClient.close();
    }

    private <T> T wrapFuture(CompletableFuture<T> future) throws OpenGeminiException {
        try {
            return future.get(conf.httpConfig.timeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new OpenGeminiException(e);
        }
    }
}
