package io.opengemini.client.common;

import io.opengemini.client.api.BaseConfiguration;
import io.opengemini.client.api.OpenGeminiAsyncClient;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.Pong;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.RetentionPolicy;
import io.opengemini.client.api.RpConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

public abstract class BaseAsyncClient extends BaseClient implements OpenGeminiAsyncClient {

    public BaseAsyncClient(BaseConfiguration conf) {
        super(conf);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> createDatabase(String database) {
        String command = CommandFactory.createDatabase(database);
        Query query = new Query(command);
        return executePostQuery(query).thenApply(rsp -> null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> dropDatabase(String database) {
        String command = CommandFactory.dropDatabase(database);
        Query query = new Query(command);
        return executePostQuery(query).thenApply(rsp -> null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<String>> showDatabases() {
        String command = CommandFactory.showDatabases();
        Query query = new Query(command);
        return executeQuery(query).thenApply(ResultMapper::toDatabases);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> createRetentionPolicy(String database, RpConfig rpConfig, boolean isDefault) {
        String command = CommandFactory.createRetentionPolicy(database, rpConfig, isDefault);
        Query query = new Query(command);
        return executePostQuery(query).thenApply(rsp -> null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<List<RetentionPolicy>> showRetentionPolicies(String database) {
        if (StringUtils.isBlank(database)) {
            return null;
        }

        String command = CommandFactory.showRetentionPolicies(database);
        Query query = new Query(command);
        query.setDatabase(database);
        return executeQuery(query).thenApply(ResultMapper::toRetentionPolicies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> dropRetentionPolicy(String database, String retentionPolicy) {
        String command = CommandFactory.dropRetentionPolicy(database, retentionPolicy);
        Query query = new Query(command);
        return executePostQuery(query).thenApply(rsp -> null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<QueryResult> query(Query query) {
        return executeQuery(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> write(String database, Point point) {
        String body = point.lineProtocol();
        return executeWrite(database, body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> writeBatch(String database, List<Point> points) {
        StringJoiner sj = new StringJoiner("\n");
        points.forEach(point -> sj.add(point.lineProtocol()));
        return executeWrite(database, sj.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Pong> ping() {
        return executePing();
    }

    /**
     * The implementation class needs to implement this method to execute a GET query call.
     *
     * @param query the query to execute.
     */
    protected abstract CompletableFuture<QueryResult> executeQuery(Query query);

    /**
     * The implementation class needs to implement this method to execute a POST query call.
     *
     * @param query the query to execute.
     */
    protected abstract CompletableFuture<QueryResult> executePostQuery(Query query);

    /**
     * The implementation class needs to implement this method to execute a write call.
     *
     * @param database     the name of the database.
     * @param lineProtocol the line protocol string to write.
     */
    protected abstract CompletableFuture<Void> executeWrite(String database, String lineProtocol);

    /**
     * The implementation class needs to implement this method to execute a ping call.
     */
    protected abstract CompletableFuture<Pong> executePing();

}
