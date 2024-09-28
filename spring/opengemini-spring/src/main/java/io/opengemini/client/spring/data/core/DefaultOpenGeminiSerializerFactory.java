package io.opengemini.client.spring.data.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultOpenGeminiSerializerFactory implements OpenGeminiSerializerFactory {

    private final Map<Class<?>, OpenGeminiSerializer<?>> serializerMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> OpenGeminiSerializer<T> getSerializer(Class<T> clazz) {
        return (OpenGeminiSerializer<T>) serializerMap.computeIfAbsent(clazz, DefaultOpenGeminiSerializer::of);
    }
}
