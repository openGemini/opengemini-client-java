package io.opengemini.client.api;

import java.util.List;

/**
 *
 */
public interface OpenGeminiClient {

    /**
     * Enable Gzip compression interceptor.
     * config gzip setting open
     *
     * @return
     */
    OpenGeminiClient enableGzip();

    /**
     * Disable Gzip compression interceptor.
     *
     * @return
     */
    OpenGeminiClient disableGzip();

    /**
     * Check if Gzip compression is enabled.
     *
     * @return
     */
    boolean isGzipEnabled();

    /**
     * Enable batch writing, which will activate the default batch writing configuration.
     *
     * @return
     */
    OpenGeminiClient enableBatch();

    /**
     * Enable batch writing. This will activate the default configuration for batch writing.
     *
     * @param batchOptions
     * @return
     */
    OpenGeminiClient enableBatch(final BatchOptions batchOptions);

    /**
     * Check if batch writing is enabled.
     *
     * @return
     */
    boolean isBatchEnabled();

    /**
     * Enable batch writing. This will activate custom batch writing configurations,
     * allowing you to set up parameters for batch writing, such as the number of threads used and the batch size for each write operation.
     *
     * @param config
     * @return
     */
    OpenGeminiClient enableBatch(BatchConfig config);

    /**
     * Disable batch writing.
     *
     * @return
     */
    void disableBatch();

    /**
     * Ping check.
     *
     * @return
     */
    Pong ping();

    /**
     * Get version information.
     *
     * @return
     */
    String version();

    /**
     * Write data.
     *
     * @param point
     */
    void write(final Point point);

    /**
     * Write data.
     *
     * @param records
     */
    void write(final String records);

    /**
     * Write data.
     *
     * @param records
     */
    void write(final List<String> records);

    /**
     * Write data.
     *
     * @param database        Database
     * @param retentionPolicy Retention policy
     * @param point           Data point
     */
    void write(final String database, final String retentionPolicy, final Point point);

    /**
     * Write data.
     *
     * @param batchPoints Batch data
     */
    void write(final BatchPoints batchPoints);

    /**
     * Write data.
     *
     * @param database        Database
     * @param retentionPolicy Storage policy
     * @param records         Records
     */
    void write(final String database, final String retentionPolicy, final String records);

    /**
     * Write a set of points.
     *
     * @param database        Database name to write to
     * @param retentionPolicy Retention policy
     * @param records         Line records
     */
    void write(final String database, final String retentionPolicy, final List<String> records);

    /**
     * Query data.
     *
     * @param query
     * @return
     */
    QueryResult query(final Query query);

    /**
     * Execute a query against a database.
     * <p>
     * One of the consumers will be executed.
     *
     * @param query       Query to execute.
     * @param consumerWap Stream processing execution result
     */
    void query(final Query query, ConsumerWap consumerWap);

    /**
     * Wait for all data to be written.
     *
     * @throws IllegalStateException if batching is not enabled.
     */
    void flush();

    void close();

    /**
     * Set the database for remote operations.
     */
    OpenGeminiClient setDatabase(final String database);

    /**
     * Set the retention policy for operations.
     */
    OpenGeminiClient setRetentionPolicy(final String retentionPolicy);


}
