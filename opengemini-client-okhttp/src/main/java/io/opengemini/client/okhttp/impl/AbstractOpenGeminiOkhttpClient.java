package io.opengemini.client.okhttp.impl;

import com.squareup.moshi.JsonAdapter;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.Query;
import io.opengemini.client.api.QueryResult;
import io.opengemini.client.okhttp.BatchOptions;
import io.opengemini.client.okhttp.OpenGeminiClient;
import io.opengemini.client.okhttp.OpenGeminiException;
import io.opengemini.client.okhttp.dto.BatchPoints;
import io.opengemini.client.okhttp.dto.BoundParameterQuery;
import io.opengemini.client.okhttp.dto.Pong;
import io.opengemini.client.okhttp.msgpack.MessagePackTraverser;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSource;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 首相执行okhttp的客户端
 *
 * @author Janle
 * @date 2024/5/11 14:23
 */
public abstract class AbstractOpenGeminiOkhttpClient implements OpenGeminiClient {
    protected static final String APPLICATION_MSGPACK = "application/x-msgpack";

    static final MediaType MEDIA_TYPE_STRING = MediaType.parse("text/plain");

    protected static final String SHOW_DATABASE_COMMAND_ENCODED = Query.encode("SHOW DATABASES");
    protected String hostName;
    protected String version;
    protected Retrofit retrofit;
    protected OkHttpClient client;
    protected BatchProcessor batchProcessor;
    protected final AtomicBoolean batchEnabled = new AtomicBoolean(false);
    protected final LongAdder writeCount = new LongAdder();
    protected final LongAdder unBatchedCount = new LongAdder();
    protected final LongAdder batchedCount = new LongAdder();
    protected volatile DatagramSocket datagramSocket;
    protected HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    protected GzipRequestInterceptor gzipRequestInterceptor = new GzipRequestInterceptor();
    protected LogLevel logLevel = LogLevel.NONE;
    protected String database;
    protected String retentionPolicy = "autogen";
    protected ConsistencyLevel consistency = ConsistencyLevel.ONE;
    protected boolean messagePack;
    protected Boolean messagePackSupport;
    protected ChunkProccesor chunkProccesor;

    protected abstract OpenGeminiService getOpenGeminiService();

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenGeminiClient enableGzip() {
        this.gzipRequestInterceptor.enable();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenGeminiClient disableGzip() {
        this.gzipRequestInterceptor.disable();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGzipEnabled() {
        return this.gzipRequestInterceptor.isEnabled();
    }

    @Override
    public OpenGeminiClient enableBatch() {
        enableBatch(BatchOptions.DEFAULTS);
        return this;
    }

    @Override
    public OpenGeminiClient enableBatch(final BatchOptions batchOptions) {

        if (this.batchEnabled.get()) {
            throw new IllegalStateException("BatchProcessing is already enabled.");
        }
        this.batchProcessor = BatchProcessor
                .builder(this)
                .actions(batchOptions.getActions())
                .exceptionHandler(batchOptions.getExceptionHandler())
                .interval(batchOptions.getFlushDuration(), batchOptions.getJitterDuration(), TimeUnit.MILLISECONDS)
                .threadFactory(batchOptions.getThreadFactory())
                .bufferLimit(batchOptions.getBufferLimit())
                .consistencyLevel(batchOptions.getConsistency())
                .precision(batchOptions.getPrecision())
                .dropActionsOnQueueExhaustion(batchOptions.isDropActionsOnQueueExhaustion())
                .droppedActionHandler(batchOptions.getDroppedActionHandler())
                .build();
        this.batchEnabled.set(true);
        return this;
    }

    @Override
    public OpenGeminiClient enableBatch(final int actions, final int flushDuration,
                                        final TimeUnit flushDurationTimeUnit) {
        enableBatch(actions, flushDuration, flushDurationTimeUnit, Executors.defaultThreadFactory());
        return this;
    }

    @Override
    public OpenGeminiClient enableBatch(final int actions, final int flushDuration,
                                        final TimeUnit flushDurationTimeUnit, final ThreadFactory threadFactory) {
        enableBatch(actions, flushDuration, flushDurationTimeUnit, threadFactory, (points, throwable) -> {
        });
        return this;
    }

    @Override
    public OpenGeminiClient enableBatch(final int actions, final int flushDuration, final TimeUnit flushDurationTimeUnit,
                                        final ThreadFactory threadFactory,
                                        final BiConsumer<Iterable<Point>, Throwable> exceptionHandler,
                                        final ConsistencyLevel consistency) {
        enableBatch(actions, flushDuration, flushDurationTimeUnit, threadFactory, exceptionHandler)
                .setConsistency(consistency);
        return this;
    }

    @Override
    public OpenGeminiClient enableBatch(final int actions, final int flushDuration, final TimeUnit flushDurationTimeUnit,
                                        final ThreadFactory threadFactory,
                                        final BiConsumer<Iterable<Point>, Throwable> exceptionHandler) {
        enableBatch(actions, flushDuration, 0, flushDurationTimeUnit, threadFactory, exceptionHandler, false, null);
        return this;
    }

    private OpenGeminiClient enableBatch(final int actions, final int flushDuration, final int jitterDuration,
                                         final TimeUnit durationTimeUnit, final ThreadFactory threadFactory,
                                         final BiConsumer<Iterable<Point>, Throwable> exceptionHandler,
                                         final boolean dropActionsOnQueueExhaustion, final Consumer<Point> droppedActionHandler) {
        if (this.batchEnabled.get()) {
            throw new IllegalStateException("BatchProcessing is already enabled.");
        }
        this.batchProcessor = BatchProcessor
                .builder(this)
                .actions(actions)
                .exceptionHandler(exceptionHandler)
                .interval(flushDuration, jitterDuration, durationTimeUnit)
                .threadFactory(threadFactory)
                .consistencyLevel(consistency)
                .dropActionsOnQueueExhaustion(dropActionsOnQueueExhaustion)
                .droppedActionHandler(droppedActionHandler)
                .build();
        this.batchEnabled.set(true);
        return this;
    }

    @Override
    public void disableBatch() {
        this.batchEnabled.set(false);
        if (this.batchProcessor != null) {
            this.batchProcessor.flushAndShutdown();
        }
    }

    @Override
    public boolean isBatchEnabled() {
        return this.batchEnabled.get();
    }

    @Override
    public Pong ping() {
        final long started = System.currentTimeMillis();
        retrofit2.Call<ResponseBody> call = getOpenGeminiService().ping();
        try {
            Response<ResponseBody> response = call.execute();
            Headers headers = response.headers();
            String version = "unknown";
            for (String name : headers.toMultimap().keySet()) {
                if (null != name && "X-Influxdb-Version".equalsIgnoreCase(name)) {
                    version = headers.get(name);
                    break;
                }
            }
            Pong pong = new Pong();
            pong.setVersion(version);
            pong.setResponseTime(System.currentTimeMillis() - started);
            return pong;
        } catch (IOException e) {
            throw new OpenGeminiException(e);
        }
    }

    @Override
    public String version() {
        if (version == null) {
            this.version = ping().getVersion();
        }
        return this.version;
    }

    @Override
    public void write(final Point point) {
        write(database, retentionPolicy, point);
    }

    @Override
    public void write(final String records) {
        write(database, retentionPolicy, consistency, records);
    }

    @Override
    public void write(final List<String> records) {
        write(database, retentionPolicy, consistency, records);
    }

    @Override
    public void write(final String database, final String retentionPolicy, final Point point) {
        if (this.batchEnabled.get()) {
            BatchProcessor.HttpBatchEntry batchEntry = new BatchProcessor.HttpBatchEntry(point, database, retentionPolicy);
            this.batchProcessor.put(batchEntry);
        } else {
            BatchPoints batchPoints = BatchPoints.database(database)
                    .retentionPolicy(retentionPolicy).build();
            batchPoints.point(point);
            this.write(batchPoints);
            this.unBatchedCount.increment();
        }
        this.writeCount.increment();
    }


    @Override
    public void write(final BatchPoints batchPoints) {
        this.batchedCount.add(batchPoints.getPoints().size());
        RequestBody lineProtocol = RequestBody.create(MEDIA_TYPE_STRING, batchPoints.lineProtocol());
        String db = batchPoints.getDatabase();
        if (db == null) {
            db = this.database;
        }
        execute(getOpenGeminiService().writePoints(
                db,
                batchPoints.getRetentionPolicy(),
                TimeUtil.toTimePrecision(batchPoints.getPrecision()),
                batchPoints.getConsistency().value(),
                lineProtocol));
    }

    @Override
    public void writeWithRetry(final BatchPoints batchPoints) {
        if (isBatchEnabled()) {
            batchProcessor.getBatchWriter().write(Collections.singleton(batchPoints));
        } else {
            write(batchPoints);
        }
    }

    @Override
    public void write(final String database, final String retentionPolicy, final ConsistencyLevel consistency,
                      final TimeUnit precision, final String records) {
        execute(getOpenGeminiService().writePoints(
                database,
                retentionPolicy,
                TimeUtil.toTimePrecision(precision),
                consistency.value(),
                RequestBody.create(MEDIA_TYPE_STRING, records)));
    }

    @Override
    public void write(final String database, final String retentionPolicy, final ConsistencyLevel consistency,
                      final String records) {
        write(database, retentionPolicy, consistency, TimeUnit.NANOSECONDS, records);
    }

    @Override
    public void write(final String database, final String retentionPolicy, final ConsistencyLevel consistency,
                      final List<String> records) {
        write(database, retentionPolicy, consistency, TimeUnit.NANOSECONDS, records);
    }


    @Override
    public void write(final String database, final String retentionPolicy, final ConsistencyLevel consistency,
                      final TimeUnit precision, final List<String> records) {
        write(database, retentionPolicy, consistency, precision, String.join("\n", records));
    }


    private void initialDatagramSocket() {
        if (datagramSocket == null) {
            synchronized (OpenGeminiOkhttpClient.class) {
                if (datagramSocket == null) {
                    try {
                        datagramSocket = new DatagramSocket();
                    } catch (SocketException e) {
                        throw new OpenGeminiException(e);
                    }
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public QueryResult query(final Query query) {
        return executeQuery(callQuery(query));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void query(final Query query, final Consumer<QueryResult> onSuccess, final Consumer<Throwable> onFailure) {
        final retrofit2.Call<QueryResult> call = callQuery(query);
        call.enqueue(new retrofit2.Callback<QueryResult>() {
            @Override
            public void onResponse(final retrofit2.Call<QueryResult> call, final Response<QueryResult> response) {
                if (response.isSuccessful()) {
                    onSuccess.accept(response.body());
                } else {
                    Throwable t = null;
                    String errorBody = null;

                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        t = e;
                    }

                    if (t != null) {
                        onFailure.accept(new OpenGeminiException(response.message(), t));
                    } else if (errorBody != null) {
                        onFailure.accept(new OpenGeminiException(response.message() + " - " + errorBody));
                    } else {
                        onFailure.accept(new OpenGeminiException(response.message()));
                    }
                }
            }

            @Override
            public void onFailure(final retrofit2.Call<QueryResult> call, final Throwable throwable) {
                onFailure.accept(throwable);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void query(final Query query, final int chunkSize, final Consumer<QueryResult> onNext) {
        query(query, chunkSize, onNext, () -> {
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void query(final Query query, final int chunkSize, final BiConsumer<Cancellable, QueryResult> onNext) {
        query(query, chunkSize, onNext, () -> {
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void query(final Query query, final int chunkSize, final Consumer<QueryResult> onNext,
                      final Runnable onComplete) {
        query(query, chunkSize, (cancellable, queryResult) -> onNext.accept(queryResult), onComplete);
    }

    @Override
    public void query(final Query query, final int chunkSize, final BiConsumer<Cancellable, QueryResult> onNext,
                      final Runnable onComplete) {
        query(query, chunkSize, onNext, onComplete, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void query(final Query query, final int chunkSize, final BiConsumer<Cancellable, QueryResult> onNext,
                      final Runnable onComplete, final Consumer<Throwable> onFailure) {
        retrofit2.Call<ResponseBody> call;
        if (query instanceof BoundParameterQuery) {
            BoundParameterQuery boundParameterQuery = (BoundParameterQuery) query;
            call = getOpenGeminiService().query(getDatabase(query),
                    query.getRetentionPolicy(), query.getCommandWithUrlEncoded(), chunkSize,
                    boundParameterQuery.getParameterJsonWithUrlEncoded());
        } else {
            call = getOpenGeminiService().query(getDatabase(query), query.getRetentionPolicy(),
                    query.getCommandWithUrlEncoded(), chunkSize, null);
        }

        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(final retrofit2.Call<ResponseBody> call, final Response<ResponseBody> response) {

                Cancellable cancellable = new Cancellable() {
                    @Override
                    public void cancel() {
                        call.cancel();
                    }

                    @Override
                    public boolean isCanceled() {
                        return call.isCanceled();
                    }
                };

                try {
                    if (response.isSuccessful()) {
                        ResponseBody chunkedBody = response.body();
                        chunkProccesor.process(chunkedBody, cancellable, onNext, onComplete);
                    } else {
                        // REVIEW: must be handled consistently with IOException.
                        ResponseBody errorBody = response.errorBody();
                        if (errorBody != null) {
                            OpenGeminiException OpenGeminiException = new OpenGeminiException(errorBody.string());
                            if (onFailure == null) {
                                throw OpenGeminiException;
                            } else {
                                onFailure.accept(OpenGeminiException);
                            }
                        }
                    }
                } catch (IOException e) {
                    QueryResult queryResult = new QueryResult();
                    queryResult.setError(e.toString());
                    onNext.accept(cancellable, queryResult);
                    //passing null onFailure consumer is here for backward compatibility
                    //where the empty queryResult containing error is propagating into onNext consumer
                    if (onFailure != null) {
                        onFailure.accept(e);
                    }
                } catch (Exception e) {
                    call.cancel();
                    if (onFailure != null) {
                        onFailure.accept(e);
                    }
                }

            }

            @Override
            public void onFailure(final retrofit2.Call<ResponseBody> call, final Throwable t) {
                if (onFailure == null) {
                    throw new OpenGeminiException(t);
                } else {
                    onFailure.accept(t);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryResult query(final Query query, final TimeUnit timeUnit) {
        retrofit2.Call<QueryResult> call;
        if (query instanceof BoundParameterQuery) {
            BoundParameterQuery boundParameterQuery = (BoundParameterQuery) query;
            call = getOpenGeminiService().query(getDatabase(query),
                    TimeUtil.toTimePrecision(timeUnit), query.getCommandWithUrlEncoded(),
                    boundParameterQuery.getParameterJsonWithUrlEncoded());
        } else {
            call = getOpenGeminiService().query(getDatabase(query),
                    TimeUtil.toTimePrecision(timeUnit), query.getCommandWithUrlEncoded(), null);
        }
        return executeQuery(call);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createDatabase(final String name) {
        Preconditions.checkNonEmptyString(name, "name");
        String createDatabaseQueryString = String.format("CREATE DATABASE \"%s\"", name);
        executeQuery(getOpenGeminiService().postQuery(Query.encode(createDatabaseQueryString)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteDatabase(final String name) {
        executeQuery(getOpenGeminiService().postQuery(Query.encode("DROP DATABASE \"" + name + "\"")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> describeDatabases() {
        QueryResult result = executeQuery(getOpenGeminiService().postQuery(SHOW_DATABASE_COMMAND_ENCODED));
        // {"results":[{"series":[{"name":"databases","columns":["name"],"values":[["mydb"]]}]}]}
        // Series [name=databases, columns=[name], values=[[mydb], [unittest_1433605300968]]]
        List<List<Object>> databaseNames = result.getResults().get(0).getSeries().get(0).getValues();
        List<String> databases = new ArrayList<>();
        if (databaseNames != null) {
            for (List<Object> database : databaseNames) {
                databases.add(database.get(0).toString());
            }
        }
        return databases;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean databaseExists(final String name) {
        List<String> databases = this.describeDatabases();
        for (String databaseName : databases) {
            if (databaseName.trim().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calls the openGeminiService for the query.
     */
    private retrofit2.Call<QueryResult> callQuery(final Query query) {
        retrofit2.Call<QueryResult> call;
        if (query instanceof BoundParameterQuery) {
            BoundParameterQuery boundParameterQuery = (BoundParameterQuery) query;
            call = getOpenGeminiService().postQuery(getDatabase(query), query.getCommandWithUrlEncoded(),
                    boundParameterQuery.getParameterJsonWithUrlEncoded());
        } else {
            call = getOpenGeminiService().postQuery(getDatabase(query), query.getCommandWithUrlEncoded());
        }
        return call;
    }

    static class ErrorMessage {
        public String error;
    }

    private boolean checkMessagePackSupport() {
        Matcher matcher = Pattern.compile("(\\d+\\.*)+").matcher(version());
        if (!matcher.find()) {
            return false;
        }
        String s = matcher.group();
        String[] versionNumbers = s.split("\\.");
        final int major = Integer.parseInt(versionNumbers[0]);
        final int minor = Integer.parseInt(versionNumbers[1]);
        final int fromMinor = 4;
        return (major >= 2) || ((major == 1) && (minor >= fromMinor));
    }

    private QueryResult executeQuery(final retrofit2.Call<QueryResult> call) {
        if (messagePack) {
            if (messagePackSupport == null) {
                messagePackSupport = checkMessagePackSupport();
            }

            if (!messagePackSupport) {
                throw new UnsupportedOperationException(
                        "MessagePack format is only supported from InfluxDB version 1.4 and later");
            }
        }
        return execute(call);
    }

    private <T> T execute(final Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            }
            try (ResponseBody errorBody = response.errorBody()) {
                if (messagePack) {
                    throw OpenGeminiException.buildExceptionForErrorState(errorBody.byteStream());
                } else {
                    throw OpenGeminiException.buildExceptionForErrorState(errorBody.string());
                }
            }
        } catch (IOException e) {
            throw new OpenGeminiException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() {
        if (!batchEnabled.get()) {
            throw new IllegalStateException("BatchProcessing is not enabled.");
        }
        batchProcessor.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            this.disableBatch();
        } finally {
            if (datagramSocket != null && !datagramSocket.isClosed()) {
                datagramSocket.close();
            }
        }
        this.client.dispatcher().executorService().shutdown();
        this.client.connectionPool().evictAll();
    }

    @Override
    public OpenGeminiClient setConsistency(ConsistencyLevel consistency) {
        this.consistency = consistency;
        return this;
    }

    @Override
    public OpenGeminiClient setDatabase(final String database) {
        this.database = database;
        return this;
    }

    @Override
    public OpenGeminiClient setRetentionPolicy(final String retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createRetentionPolicy(final String rpName, final String database, final String duration,
                                      final String shardDuration, final int replicationFactor, final boolean isDefault) {
        Preconditions.checkNonEmptyString(rpName, "retentionPolicyName");
        Preconditions.checkNonEmptyString(database, "database");
        Preconditions.checkNonEmptyString(duration, "retentionDuration");
        Preconditions.checkDuration(duration, "retentionDuration");
        if (shardDuration != null && !shardDuration.isEmpty()) {
            Preconditions.checkDuration(shardDuration, "shardDuration");
        }
        Preconditions.checkPositiveNumber(replicationFactor, "replicationFactor");

        StringBuilder queryBuilder = new StringBuilder("CREATE RETENTION POLICY \"");
        queryBuilder.append(rpName)
                .append("\" ON \"")
                .append(database)
                .append("\" DURATION ")
                .append(duration)
                .append(" REPLICATION ")
                .append(replicationFactor);
        if (shardDuration != null && !shardDuration.isEmpty()) {
            queryBuilder.append(" SHARD DURATION ");
            queryBuilder.append(shardDuration);
        }
        if (isDefault) {
            queryBuilder.append(" DEFAULT");
        }
        executeQuery(getOpenGeminiService().postQuery(Query.encode(queryBuilder.toString())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createRetentionPolicy(final String rpName, final String database, final String duration,
                                      final int replicationFactor, final boolean isDefault) {
        createRetentionPolicy(rpName, database, duration, null, replicationFactor, isDefault);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createRetentionPolicy(final String rpName, final String database, final String duration,
                                      final String shardDuration, final int replicationFactor) {
        createRetentionPolicy(rpName, database, duration, null, replicationFactor, false);
    }

    /**
     * {@inheritDoc}
     *
     * @param rpName   the name of the retentionPolicy
     * @param database the name of the database
     */
    @Override
    public void dropRetentionPolicy(final String rpName, final String database) {
        Preconditions.checkNonEmptyString(rpName, "retentionPolicyName");
        Preconditions.checkNonEmptyString(database, "database");
        StringBuilder queryBuilder = new StringBuilder("DROP RETENTION POLICY \"");
        queryBuilder.append(rpName)
                .append("\" ON \"")
                .append(database)
                .append("\"");
        executeQuery(getOpenGeminiService().postQuery(Query.encode(queryBuilder.toString())));
    }


    protected String getDatabase(final Query query) {
        String db = query.getDatabase();
        if (db == null) {
            return this.database;
        }
        return db;
    }


    public OpenGeminiClient setLogLevel(final LogLevel logLevel) {
        switch (logLevel) {
            case NONE:
                this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
                break;
            case BASIC:
                this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
                break;
            case HEADERS:
                this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
                break;
            case FULL:
                this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                break;
            default:
                break;
        }
        this.logLevel = logLevel;
        return this;
    }

    protected String parseHost(final String url) {
        String hostName;
        try {
            URI uri = new URI(url);
            hostName = uri.getHost();
        } catch (URISyntaxException e1) {
            throw new IllegalArgumentException("Unable to parse url: " + url, e1);
        }

        if (hostName == null) {
            throw new IllegalArgumentException("Unable to parse url: " + url);
        }

        try {
            InetAddress.getByName(hostName);
        } catch (UnknownHostException e) {
            throw new OpenGeminiException(e);
        }
        return hostName;
    }

    protected interface ChunkProccesor {
        void process(ResponseBody chunkedBody, Cancellable cancellable,
                     BiConsumer<Cancellable, QueryResult> consumer, Runnable onComplete) throws IOException;
    }

    protected class MessagePackChunkProccesor implements ChunkProccesor {
        @Override
        public void process(final ResponseBody chunkedBody, final Cancellable cancellable,
                            final BiConsumer<Cancellable, QueryResult> consumer, final Runnable onComplete)
                throws IOException {
            MessagePackTraverser traverser = new MessagePackTraverser();
            try (InputStream is = chunkedBody.byteStream()) {
                for (Iterator<QueryResult> it = traverser.traverse(is).iterator(); it.hasNext() && !cancellable.isCanceled(); ) {
                    QueryResult result = it.next();
                    consumer.accept(cancellable, result);
                }
            }
            if (!cancellable.isCanceled()) {
                onComplete.run();
            }
        }
    }

    protected class JSONChunkProccesor implements ChunkProccesor {
        private JsonAdapter<QueryResult> adapter;

        public JSONChunkProccesor(final JsonAdapter<QueryResult> adapter) {
            this.adapter = adapter;
        }

        @Override
        public void process(final ResponseBody chunkedBody, final Cancellable cancellable,
                            final BiConsumer<Cancellable, QueryResult> consumer, final Runnable onComplete)
                throws IOException {
            try {
                BufferedSource source = chunkedBody.source();
                while (!cancellable.isCanceled()) {
                    QueryResult result = adapter.fromJson(source);
                    if (result != null) {
                        consumer.accept(cancellable, result);
                    }
                }
            } catch (EOFException e) {
                QueryResult queryResult = new QueryResult();
                queryResult.setError("DONE");
                consumer.accept(cancellable, queryResult);
                if (!cancellable.isCanceled()) {
                    onComplete.run();
                }
            } finally {
                chunkedBody.close();
            }
        }
    }
}
