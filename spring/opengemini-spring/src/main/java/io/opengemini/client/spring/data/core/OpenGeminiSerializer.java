package io.opengemini.client.spring.data.core;

import io.opengemini.client.api.OpenGeminiException;
import io.opengemini.client.api.Point;

/**
 * The serializer between POJO and Point
 *
 * @param <T> class type of POJO annotated by Measurement
 */
public interface OpenGeminiSerializer<T> {
    Point serialize(T t) throws OpenGeminiException;
}
