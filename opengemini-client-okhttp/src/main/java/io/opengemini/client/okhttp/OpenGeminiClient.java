package io.opengemini.client.okhttp;

import io.opengemini.client.api.Point;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.okhttp.dto.BatchPoints;
import io.opengemini.client.okhttp.dto.Pong;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 客户端声明
 *
 * @author Janle
 * @date 2024/5/9 16:56
 */
public interface OpenGeminiClient extends Cloneable {

    /**
     * 控制REST层的日志级别。
     */
    enum LogLevel {
        /**
         * 没有日志记录。
         */
        NONE,
        /**
         * 记录请求方法和URL，以及响应状态码和执行时间。
         */
        BASIC,
        /**
         * 记录请求方法和URL、响应状态码和执行时间，以及请求和响应的头部信息。
         */
        HEADERS,
        /**
         * 记录请求和响应的头部信息、请求体和响应体，以及元数据。
         * <p>
         * 注意：这需要将整个请求和响应体缓冲在内存中！
         */
        FULL;

        /**
         * 日志级别解析
         *
         * @param value a {@code String} containing the {@code LogLevel constant}
         *              representation to be parsed
         * @return the LogLevel constant representation of the param
         * or {@code NONE} for null or any invalid String representation.
         */
        public static LogLevel parseLogLevel(final String value) {
            LogLevel logLevel = NONE;
            if (value != null) {
                try {
                    logLevel = valueOf(value.toUpperCase());
                } catch (IllegalArgumentException e) {
                }
            }

            return logLevel;
        }
    }


    /**
     * ConsistencyLevel for write Operations.
     */
    enum ConsistencyLevel {
        /**
         * Write succeeds only if write reached all cluster members.
         */
        ALL("all"),
        /**
         * Write succeeds if write reached any cluster members.
         */
        ANY("any"),
        /**
         * Write succeeds if write reached at least one cluster members.
         */
        ONE("one"),
        /**
         * Write succeeds only if write reached a quorum of cluster members.
         */
        QUORUM("quorum");
        private final String value;

        private ConsistencyLevel(final String value) {
            this.value = value;
        }

        /**
         * Get the String value of the ConsistencyLevel.
         *
         * @return the lowercase String.
         */
        public String value() {
            return this.value;
        }
    }


    enum ResponseFormat {
        JSON, MSGPACK
    }

    interface Cancellable {

        /**
         * Cancel the streaming query call.
         */
        void cancel();

        /**
         * Return {@code true} if the {@link Cancellable#cancel()} was called.
         *
         * @return {@code true} if the {@link Cancellable#cancel()} was called
         */
        boolean isCanceled();
    }

    OpenGeminiClient enableGzip();

    OpenGeminiClient disableGzip();


    boolean isGzipEnabled();


    OpenGeminiClient enableBatch();

    OpenGeminiClient enableBatch(final BatchOptions batchOptions);

    boolean isBatchEnabled();

    OpenGeminiClient enableBatch(final int actions, final int flushDuration, final TimeUnit flushDurationTimeUnit);

    OpenGeminiClient enableBatch(final int actions, final int flushDuration, final TimeUnit flushDurationTimeUnit, final ThreadFactory threadFactory);

    OpenGeminiClient enableBatch(int actions, int flushDuration, TimeUnit flushDurationTimeUnit, ThreadFactory threadFactory, BiConsumer<Iterable<Point>, Throwable> exceptionHandler, ConsistencyLevel consistency);

    OpenGeminiClient enableBatch(final int actions, final int flushDuration, final TimeUnit flushDurationTimeUnit, final ThreadFactory threadFactory, final BiConsumer<Iterable<Point>, Throwable> exceptionHandler);

    void disableBatch();


    Pong ping();

    String version();

    void write(final Point point);

    void write(final String records);

    void write(final List<String> records);

    void write(final String database, final String retentionPolicy, final Point point);


    void write(final BatchPoints batchPoints);

    void writeWithRetry(final BatchPoints batchPoints);

    void write(final String database, final String retentionPolicy, final ConsistencyLevel consistency, final String records);

    /**
     * Write a set of Points to the influxdb database with the string records.
     *
     * @param database        the name of the database to write
     * @param retentionPolicy the retentionPolicy to use
     * @param consistency     the ConsistencyLevel to use
     * @param precision       the time precision to use
     * @param records         the points in the correct lineprotocol.
     * @see <a href="https://github.com/influxdb/influxdb/pull/2696">2696</a>
     */
    void write(final String database, final String retentionPolicy, final ConsistencyLevel consistency, final TimeUnit precision, final String records);

    /**
     * Write a set of Points to the influxdb database with the list of string records.
     *
     * @param database        the name of the database to write
     * @param retentionPolicy the retentionPolicy to use
     * @param consistency     the ConsistencyLevel to use
     * @param records         the List of points in the correct lineprotocol.
     * @see <a href="https://github.com/influxdb/influxdb/pull/2696">2696</a>
     */
    void write(final String database, final String retentionPolicy, final ConsistencyLevel consistency, final List<String> records);

    /**
     * Write a set of Points to the influxdb database with the list of string records.
     *
     * @param database        the name of the database to write
     * @param retentionPolicy the retentionPolicy to use
     * @param consistency     the ConsistencyLevel to use
     * @param precision       the time precision to use
     * @param records         the List of points in the correct lineprotocol.
     * @see <a href="https://github.com/influxdb/influxdb/pull/2696">2696</a>
     */
    void write(final String database, final String retentionPolicy, final ConsistencyLevel consistency, final TimeUnit precision, final List<String> records);

    QueryResult query(final Query query);

    /**
     * Execute a query against a database.
     * <p>
     * One of the consumers will be executed.
     *
     * @param query     the query to execute.
     * @param onSuccess the consumer to invoke when result is received
     * @param onFailure the consumer to invoke when error is thrown
     */
    void query(final Query query, final Consumer<QueryResult> onSuccess, final Consumer<Throwable> onFailure);

    /**
     * Execute a streaming query against a database.
     *
     * @param query     the query to execute.
     * @param chunkSize the number of QueryResults to process in one chunk.
     * @param onNext    the consumer to invoke for each received QueryResult
     */
    void query(Query query, int chunkSize, Consumer<QueryResult> onNext);

    /**
     * Execute a streaming query against a database.
     *
     * @param query     the query to execute.
     * @param chunkSize the number of QueryResults to process in one chunk.
     * @param onNext    the consumer to invoke for each received QueryResult; with capability to discontinue a streaming query
     */
    void query(Query query, int chunkSize, BiConsumer<Cancellable, QueryResult> onNext);

    /**
     * Execute a streaming query against a database.
     *
     * @param query      the query to execute.
     * @param chunkSize  the number of QueryResults to process in one chunk.
     * @param onNext     the consumer to invoke for each received QueryResult
     * @param onComplete the onComplete to invoke for successfully end of stream
     */
    void query(Query query, int chunkSize, Consumer<QueryResult> onNext, Runnable onComplete);

    /**
     * Execute a streaming query against a database.
     *
     * @param query      the query to execute.
     * @param chunkSize  the number of QueryResults to process in one chunk.
     * @param onNext     the consumer to invoke for each received QueryResult; with capability to discontinue a streaming query
     * @param onComplete the onComplete to invoke for successfully end of stream
     */
    void query(Query query, int chunkSize, BiConsumer<Cancellable, QueryResult> onNext, Runnable onComplete);

    /**
     * Execute a streaming query against a database.
     *
     * @param query      the query to execute.
     * @param chunkSize  the number of QueryResults to process in one chunk.
     * @param onNext     the consumer to invoke for each received QueryResult; with capability to discontinue a streaming query
     * @param onComplete the onComplete to invoke for successfully end of stream
     * @param onFailure  the consumer for error handling
     */
    void query(Query query, int chunkSize, BiConsumer<Cancellable, QueryResult> onNext, Runnable onComplete, Consumer<Throwable> onFailure);

    /**
     * Execute a query against a database.
     *
     * @param query    the query to execute.
     * @param timeUnit the time unit of the results.
     * @return a List of Series which matched the query.
     */
    QueryResult query(final Query query, TimeUnit timeUnit);


    /**
     * Create a new Database.
     *
     * @param name the name of the new database.
     * @deprecated (since 2.9, removed in 3.0) Use <code>org.influxdb.InfluxDB.query(Query)</code>
     * to execute a parameterized <strong>CREATE DATABASE</strong> query.
     */
    @Deprecated
    void createDatabase(final String name);

    /**
     * Delete a database.
     *
     * @param name the name of the database to delete.
     * @deprecated (since 2.9, removed in 3.0) Use <code>org.influxdb.InfluxDB.query(Query)</code>
     * to execute a <strong>DROP DATABASE</strong> query.
     */
    @Deprecated
    void deleteDatabase(final String name);

    /**
     * Describe all available databases.
     *
     * @return a List of all Database names.
     * @deprecated (since 2.9, removed in 3.0) Use <code>org.influxdb.InfluxDB.query(Query)</code>
     * to execute a <strong>SHOW DATABASES</strong> query.
     */
    @Deprecated
    List<String> describeDatabases();

    /**
     * Check if a database exists.
     *
     * @param name the name of the database to search.
     * @return true if the database exists or false if it doesn't exist
     * @deprecated (since 2.9, removed in 3.0) Use <code>org.influxdb.InfluxDB.query(Query)</code>
     * to execute a <strong>SHOW DATABASES</strong> query and inspect the result.
     */
    @Deprecated
    boolean databaseExists(final String name);

    /**
     * Send any buffered points to InfluxDB. This method is synchronous and will block while all pending points are
     * written.
     *
     * @throws IllegalStateException if batching is not enabled.
     */
    void flush();

    /**
     * close thread for asynchronous batch write and UDP socket to release resources if need.
     */
    void close();

    OpenGeminiClient setConsistency(final ConsistencyLevel consistency);

    /**
     * Set the database which is used for writing points.
     *
     * @param database the database to set.
     * @return the InfluxDB instance to be able to use it in a fluent manner.
     */
    OpenGeminiClient setDatabase(final String database);

    /**
     * Set the retention policy which is used for writing points.
     *
     * @param retentionPolicy the retention policy to set.
     * @return the InfluxDB instance to be able to use it in a fluent manner.
     */
    OpenGeminiClient setRetentionPolicy(final String retentionPolicy);

    /**
     * Creates a retentionPolicy.
     *
     * @param rpName            the name of the retentionPolicy(rp)
     * @param database          the name of the database
     * @param duration          the duration of the rp
     * @param shardDuration     the shardDuration
     * @param replicationFactor the replicationFactor of the rp
     * @param isDefault         if the rp is the default rp for the database or not
     * @deprecated (since 2.9, removed in 3.0) Use <code>org.influxdb.InfluxDB.query(Query)</code>
     * to execute a parameterized <strong>CREATE RETENTION POLICY</strong> query.
     */
    @Deprecated
    void createRetentionPolicy(final String rpName, final String database, final String duration, final String shardDuration, final int replicationFactor, final boolean isDefault);

    /**
     * Creates a retentionPolicy. (optional shardDuration)
     *
     * @param rpName            the name of the retentionPolicy(rp)
     * @param database          the name of the database
     * @param duration          the duration of the rp
     * @param replicationFactor the replicationFactor of the rp
     * @param isDefault         if the rp is the default rp for the database or not
     * @deprecated (since 2.9, removed in 3.0) Use <code>org.influxdb.InfluxDB.query(Query)</code>
     * to execute a parameterized <strong>CREATE RETENTION POLICY</strong> query.
     */
    @Deprecated
    void createRetentionPolicy(final String rpName, final String database, final String duration, final int replicationFactor, final boolean isDefault);

    /**
     * Creates a retentionPolicy. (optional shardDuration and isDefault)
     *
     * @param rpName            the name of the retentionPolicy(rp)
     * @param database          the name of the database
     * @param duration          the duration of the rp
     * @param shardDuration     the shardDuration
     * @param replicationFactor the replicationFactor of the rp
     * @deprecated (since 2.9, removed in 3.0) Use <code>org.influxdb.InfluxDB.query(Query)</code>
     * to execute a parameterized <strong>CREATE RETENTION POLICY</strong> query.
     */
    @Deprecated
    void createRetentionPolicy(final String rpName, final String database, final String duration, final String shardDuration, final int replicationFactor);

    /**
     * Drops a retentionPolicy in a database.
     *
     * @param rpName   the name of the retentionPolicy
     * @param database the name of the database
     * @deprecated (since 2.9, removed in 3.0) Use <code>org.influxdb.InfluxDB.query(Query)</code>
     * to execute a <strong>DROP RETENTION POLICY</strong> query.
     */
    @Deprecated
    void dropRetentionPolicy(final String rpName, final String database);
}
