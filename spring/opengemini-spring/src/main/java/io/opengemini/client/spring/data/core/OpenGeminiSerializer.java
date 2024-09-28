package io.opengemini.client.spring.data.core;

import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Point;
import io.opengemini.client.api.QueryResult;

import java.util.List;

/**
 * The serializer between POJO and Point
 *
 * @param <T> class type of POJO annotated by Measurement
 */
public interface OpenGeminiSerializer<T> {

    Point serialize(T t) throws OpenGeminiException;

    List<T> deserialize(QueryResult queryResult) throws OpenGeminiException;
}
