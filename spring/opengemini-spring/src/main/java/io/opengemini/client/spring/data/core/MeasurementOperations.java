package io.opengemini.client.spring.data.core;

import io.opengemini.client.api.Query;

import java.util.List;

public interface MeasurementOperations<T> {

    void write(T t);

    void write(List<T> list);

    List<T> query(Query query);
}
