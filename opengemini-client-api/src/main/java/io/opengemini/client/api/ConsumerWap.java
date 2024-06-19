package io.opengemini.client.api;

import java.util.function.Consumer;

/**
 * Consumer query data wrapper class.
 */
public class ConsumerWap {
    /**
     * The number of query results to process in one chunk.
     * Default value is 1000.
     */
    private int chunkSize = 1000;

    Consumer<QueryResult> onNext;
    Consumer<QueryResult> onSuccess;
    Consumer<QueryResult> onFailure;
}