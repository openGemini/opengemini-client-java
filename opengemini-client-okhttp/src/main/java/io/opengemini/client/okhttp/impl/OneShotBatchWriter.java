package io.opengemini.client.okhttp.impl;


import io.opengemini.client.okhttp.OpenGeminiClient;
import io.opengemini.client.okhttp.dto.BatchPoints;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Janle
 * @date 2024/5/10 11:36
 */
class OneShotBatchWriter implements BatchWriter {
    private OpenGeminiClient openGeminiClient;

    OneShotBatchWriter(OpenGeminiClient influxDB) {
        this.openGeminiClient = openGeminiClient;
    }

    public void write(Collection<BatchPoints> batchPointsCollection) {
        Iterator var2 = batchPointsCollection.iterator();

        while (var2.hasNext()) {
            BatchPoints batchPoints = (BatchPoints) var2.next();
            this.openGeminiClient.write(batchPoints);
        }

    }

    public void close() {
    }
}
