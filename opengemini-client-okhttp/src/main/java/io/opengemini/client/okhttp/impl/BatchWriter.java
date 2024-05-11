package io.opengemini.client.okhttp.impl;


import io.opengemini.client.okhttp.dto.BatchPoints;

import java.util.Collection;

/**
 * @author Janle
 * @date 2024/5/10 11:36
 */
public interface BatchWriter {
    /**
     * Write the given batch into InfluxDB.
     *
     * @param batchPointsCollection to write
     */
    void write(Collection<BatchPoints> batchPointsCollection);

    /**
     * FLush all cached writes into InfluxDB. The application is about to exit.
     */
    void close();
}
