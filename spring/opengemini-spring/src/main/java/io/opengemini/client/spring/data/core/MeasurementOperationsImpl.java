package io.opengemini.client.spring.data.core;

import io.opengemini.client.api.OpenGeminiAsyncClient;
import io.opengemini.client.api.Query;
import lombok.SneakyThrows;

import java.util.List;

public class MeasurementOperationsImpl<T> implements MeasurementOperations<T> {

    private final OpenGeminiAsyncClient asyncClient;
    private final OpenGeminiSerializer<T> serializer;
    private final String databaseName;
    private final String retentionPolicyName;
    private final String measurementName;

    public MeasurementOperationsImpl(OpenGeminiAsyncClient asyncClient,
                                     OpenGeminiSerializer<T> serializer,
                                     String databaseName,
                                     String retentionPolicyName,
                                     String measurementName) {
        this.asyncClient = asyncClient;
        this.serializer = serializer;
        this.databaseName = databaseName;
        this.retentionPolicyName = retentionPolicyName;
        this.measurementName = measurementName;
    }

    @SneakyThrows
    @Override
    public void write(T t) {
        asyncClient.write(databaseName, retentionPolicyName, serializer.serialize(measurementName, t)).get();
    }

    @SneakyThrows
    @Override
    public void write(List<T> list) {
        asyncClient.write(databaseName, retentionPolicyName, serializer.serialize(measurementName, list)).get();
    }

    @SneakyThrows
    @Override
    public List<T> query(Query query) {
        return serializer.deserialize(measurementName, asyncClient.query(query).get());
    }
}
