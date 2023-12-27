package io.opengemini.client.jdk.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenGeminiCommon {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static <T> T converJson2Bean(String json, Class<T> cls) throws JsonProcessingException {
        return objectMapper.readValue(json, cls);
    }
}
