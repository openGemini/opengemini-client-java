package io.opengemini.client.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface to access a OpenGemini database provides a set of non-blocking methods that return CompletableFuture.
 */
public interface OpenGeminiAsyncClient extends AutoCloseable {

    /**
     * Create a new database.
     *
     * @param database the name of the new database.
     */
    CompletableFuture<Void> createDatabase(String database);

    /**
     * Drop a database.
     *
     * @param database the name of the database to drop.
     */
    CompletableFuture<Void> dropDatabase(String database);

    /**
     * Show all available databases.
     */
    CompletableFuture<List<String>> showDatabases();

    /**
     * Create a retention policy.
     *
     * @param database  the name of the database.
     * @param rpConfig  the config of the retention policy
     * @param isDefault if the retention policy is the default retention policy for the database or not
     */
    CompletableFuture<Void> createRetentionPolicy(String database, RpConfig rpConfig, boolean isDefault);

    /**
     * Show all available retention policies.
     *
     * @param database the name of the database.
     */
    CompletableFuture<List<RetentionPolicy>> showRetentionPolicies(String database);

    /**
     * Drop a retention policy.
     *
     * @param database        the name of the database.
     * @param retentionPolicy the name of the retention policy to drop.
     */
    CompletableFuture<Void> dropRetentionPolicy(String database, String retentionPolicy);

    /**
     * Execute a query against a database.
     *
     * @param query the query to execute.
     */
    CompletableFuture<QueryResult> query(Query query);

    /**
     * Write a single point to the database.
     *
     * @param database the name of the database.
     * @param point    the point to write.
     */
    CompletableFuture<Void> write(String database, Point point);

    /**
     * Write points to the database.
     *
     * @param database the name of the database.
     * @param points   the points to write.
     */
    CompletableFuture<Void> writeBatch(String database, List<Point> points);
}
