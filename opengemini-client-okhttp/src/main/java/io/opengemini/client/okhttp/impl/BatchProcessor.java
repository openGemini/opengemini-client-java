package io.opengemini.client.okhttp.impl;

import io.opengemini.client.api.Point;
import io.opengemini.client.okhttp.OpenGeminiClient;
import io.opengemini.client.okhttp.OpenGeminiClient.ConsistencyLevel;
import io.opengemini.client.okhttp.dto.BatchPoints;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Janle
 * @date 2024/5/10 11:09
 */
public class BatchProcessor {
    private static final Logger LOG = Logger.getLogger(BatchProcessor.class.getName());
    protected final BlockingQueue<AbstractBatchEntry> queue;
    private final ScheduledExecutorService scheduler;
    private final BiConsumer<Iterable<Point>, Throwable> exceptionHandler;
    final OpenGeminiClient openGeminiClient;
    final int actions;
    private final TimeUnit flushIntervalUnit;
    private final int flushInterval;
    private final ConsistencyLevel consistencyLevel;
    private final int jitterInterval;
    private final TimeUnit precision;
    private final BatchWriter batchWriter;
    private boolean dropActionsOnQueueExhaustion;
    Consumer<Point> droppedActionHandler;
    Supplier<Double> randomSupplier;

    public static Builder builder(OpenGeminiClient influxDB) {
        return new Builder(influxDB);
    }

    BatchProcessor(OpenGeminiClient openGeminiClient, BatchWriter batchWriter, ThreadFactory threadFactory,
                   int actions, TimeUnit flushIntervalUnit, int flushInterval,
                   int jitterInterval, BiConsumer<Iterable<Point>, Throwable> exceptionHandler,
                   ConsistencyLevel consistencyLevel, TimeUnit precision,
                   boolean dropActionsOnQueueExhaustion, Consumer<Point> droppedActionHandler) {
        this.openGeminiClient = openGeminiClient;
        this.batchWriter = batchWriter;
        this.actions = actions;
        this.flushIntervalUnit = flushIntervalUnit;
        this.flushInterval = flushInterval;
        this.jitterInterval = jitterInterval;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(threadFactory);
        this.exceptionHandler = exceptionHandler;
        this.consistencyLevel = consistencyLevel;
        this.precision = precision;
        this.dropActionsOnQueueExhaustion = dropActionsOnQueueExhaustion;
        this.droppedActionHandler = droppedActionHandler;
        if (actions > 1 && actions < Integer.MAX_VALUE) {
            this.queue = new LinkedBlockingQueue(actions);
        } else {
            this.queue = new LinkedBlockingQueue();
        }

        this.randomSupplier = Math::random;
        Runnable flushRunnable = new Runnable() {
            public void run() {
                BatchProcessor.this.write();
                int jitterInterval = (int) ((Double) BatchProcessor.this.randomSupplier.get() * (double) BatchProcessor.this.jitterInterval);
                BatchProcessor.this.scheduler.schedule(this, (long) (BatchProcessor.this.flushInterval + jitterInterval), BatchProcessor.this.flushIntervalUnit);
            }
        };
        this.scheduler.schedule(flushRunnable, (long) (this.flushInterval + (int) ((Double) this.randomSupplier.get() * (double) this.jitterInterval)), this.flushIntervalUnit);
    }

    void write() {
        List<Point> currentBatch = null;

        try {
            if (this.queue.isEmpty()) {
                this.batchWriter.write(Collections.emptyList());
                return;
            }

            Map<String, BatchPoints> batchKeyToBatchPoints = new HashMap();
            Map<Integer, List<String>> udpPortToBatchPoints = new HashMap();
            List<AbstractBatchEntry> batchEntries = new ArrayList(this.queue.size());
            this.queue.drainTo(batchEntries);
            currentBatch = new ArrayList(batchEntries.size());
            Iterator var5 = batchEntries.iterator();

            while (var5.hasNext()) {
                AbstractBatchEntry batchEntry = (AbstractBatchEntry) var5.next();
                Point point = batchEntry.getPoint();
                currentBatch.add(point);
                if (batchEntry instanceof HttpBatchEntry) {
                    HttpBatchEntry httpBatchEntry = (HttpBatchEntry) HttpBatchEntry.class.cast(batchEntry);
                    String dbName = httpBatchEntry.getDb();
                    String rp = httpBatchEntry.getRp();
                    String batchKey = dbName + "_" + rp;
                    if (!batchKeyToBatchPoints.containsKey(batchKey)) {
                        BatchPoints batchPoints = BatchPoints.database(dbName).retentionPolicy(rp)
                                .consistency(this.getConsistencyLevel()).precision(this.getPrecision()).build();
                        batchKeyToBatchPoints.put(batchKey, batchPoints);
                    }

                    ((BatchPoints) batchKeyToBatchPoints.get(batchKey)).point(point);
                }
            }

            this.batchWriter.write(batchKeyToBatchPoints.values());
        } catch (Throwable var13) {
            this.exceptionHandler.accept(currentBatch, var13);
            LOG.log(Level.SEVERE, "Batch could not be sent. Data will be lost", var13);
        }

    }

    void put(AbstractBatchEntry batchEntry) {
        try {
            if (this.dropActionsOnQueueExhaustion) {
                if (!this.queue.offer(batchEntry)) {
                    this.droppedActionHandler.accept(batchEntry.getPoint());
                    return;
                }
            } else {
                this.queue.put(batchEntry);
            }
        } catch (InterruptedException var3) {
            throw new RuntimeException(var3);
        }

        if (this.queue.size() >= this.actions) {
            this.scheduler.submit(new Runnable() {
                public void run() {
                    BatchProcessor.this.write();
                }
            });
        }

    }

    void flushAndShutdown() {
        this.write();
        this.scheduler.shutdown();
        this.batchWriter.close();
    }

    void flush() {
        this.write();
    }

    public ConsistencyLevel getConsistencyLevel() {
        return this.consistencyLevel;
    }

    public TimeUnit getPrecision() {
        return this.precision;
    }

    BatchWriter getBatchWriter() {
        return this.batchWriter;
    }

    public boolean isDropActionsOnQueueExhaustion() {
        return this.dropActionsOnQueueExhaustion;
    }

    public Consumer<Point> getDroppedActionHandler() {
        return this.droppedActionHandler;
    }

    static class UdpBatchEntry extends AbstractBatchEntry {
        private final int udpPort;

        public UdpBatchEntry(Point point, int udpPort) {
            super(point);
            this.udpPort = udpPort;
        }

        public int getUdpPort() {
            return this.udpPort;
        }
    }

    static class HttpBatchEntry extends AbstractBatchEntry {
        private final String db;
        private final String rp;

        public HttpBatchEntry(Point point, String db, String rp) {
            super(point);
            this.db = db;
            this.rp = rp;
        }

        public String getDb() {
            return this.db;
        }

        public String getRp() {
            return this.rp;
        }
    }

    abstract static class AbstractBatchEntry {
        private final Point point;

        public AbstractBatchEntry(Point point) {
            this.point = point;
        }

        public Point getPoint() {
            return this.point;
        }
    }

    public static final class Builder {
        private final OpenGeminiClient openGeminiClient;
        private ThreadFactory threadFactory = Executors.defaultThreadFactory();
        private int actions;
        private TimeUnit flushIntervalUnit;
        private int flushInterval;
        private int jitterInterval;
        private int bufferLimit = 0;
        private TimeUnit precision;
        private BiConsumer<Iterable<Point>, Throwable> exceptionHandler = (entries, throwable) -> {
        };
        private ConsistencyLevel consistencyLevel;
        private boolean dropActionsOnQueueExhaustion;
        private Consumer<Point> droppedActionsHandler;

        public Builder threadFactory(ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
            return this;
        }

        public Builder(OpenGeminiClient openGeminiClient) {
            this.openGeminiClient = openGeminiClient;
        }

        public Builder actions(int maxActions) {
            this.actions = maxActions;
            return this;
        }

        public Builder interval(int interval, TimeUnit unit) {
            this.flushInterval = interval;
            this.flushIntervalUnit = unit;
            return this;
        }

        public Builder interval(int flushInterval, int jitterInterval, TimeUnit unit) {
            this.flushInterval = flushInterval;
            this.jitterInterval = jitterInterval;
            this.flushIntervalUnit = unit;
            return this;
        }

        public Builder bufferLimit(int bufferLimit) {
            this.bufferLimit = bufferLimit;
            return this;
        }

        public Builder exceptionHandler(BiConsumer<Iterable<Point>, Throwable> handler) {
            this.exceptionHandler = handler;
            return this;
        }

        public Builder dropActionsOnQueueExhaustion(boolean dropActionsOnQueueExhaustion) {
            this.dropActionsOnQueueExhaustion = dropActionsOnQueueExhaustion;
            return this;
        }

        public Builder droppedActionHandler(Consumer<Point> handler) {
            this.droppedActionsHandler = handler;
            return this;
        }

        public Builder consistencyLevel(ConsistencyLevel consistencyLevel) {
            this.consistencyLevel = consistencyLevel;
            return this;
        }

        public Builder precision(TimeUnit precision) {
            this.precision = precision;
            return this;
        }

        public BatchProcessor build() {
            Objects.requireNonNull(this.openGeminiClient, "influxDB");
            Preconditions.checkPositiveNumber(this.actions, "actions");
            Preconditions.checkPositiveNumber(this.flushInterval, "flushInterval");
            Preconditions.checkNotNegativeNumber(this.jitterInterval, "jitterInterval");
            Preconditions.checkNotNegativeNumber(this.bufferLimit, "bufferLimit");
            Objects.requireNonNull(this.flushIntervalUnit, "flushIntervalUnit");
            Objects.requireNonNull(this.threadFactory, "threadFactory");
            Objects.requireNonNull(this.exceptionHandler, "exceptionHandler");
            Object batchWriter;
            if (this.bufferLimit > this.actions) {
                batchWriter = new RetryCapableBatchWriter(this.openGeminiClient, this.exceptionHandler, this.bufferLimit, this.actions);
            } else {
                batchWriter = new OneShotBatchWriter(this.openGeminiClient);
            }

            return new BatchProcessor(this.openGeminiClient, (BatchWriter) batchWriter, this.threadFactory,
                    this.actions, this.flushIntervalUnit, this.flushInterval, this.jitterInterval, this.exceptionHandler,
                    this.consistencyLevel, this.precision, this.dropActionsOnQueueExhaustion, this.droppedActionsHandler);
        }
    }
}
