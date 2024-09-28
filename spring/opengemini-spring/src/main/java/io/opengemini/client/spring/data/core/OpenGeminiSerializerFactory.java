package io.opengemini.client.spring.data.core;

public interface OpenGeminiSerializerFactory {
    <T> OpenGeminiSerializer<T> getSerializer(Class<T> clazz);
}
