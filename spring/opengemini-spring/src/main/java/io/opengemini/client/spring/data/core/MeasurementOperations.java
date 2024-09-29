package io.opengemini.client.spring.data.core;

import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Query;

import java.util.List;

public interface MeasurementOperations<T> {

    void write(T t) throws OpenGeminiException;

    void write(List<T> list) throws OpenGeminiException;

    List<T> query(Query query);
}
