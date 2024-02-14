package io.opengemini.client.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

public class JacksonService {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String toJson(Object o) throws JsonProcessingException {
        return MAPPER.writeValueAsString(o);
    }

    public static <T> T toObject(String json, Class<T> type) throws JsonProcessingException {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return MAPPER.readValue(json, type);
    }

    public static <T> T toRefer(String json, TypeReference<T> ref) throws JsonProcessingException {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return MAPPER.readValue(json, ref);
    }

    public static <T> List<T> toList(String json, TypeReference<List<T>> typeRef) throws JsonProcessingException {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        return MAPPER.readValue(json, typeRef);
    }

    public static JsonNode toJsonNode(String json) throws JsonProcessingException {
        return MAPPER.readTree(json);
    }

    public static ObjectNode createObjectNode() {
        return MAPPER.createObjectNode();
    }

    public static ArrayNode createArrayNode() {
        return MAPPER.createArrayNode();
    }
}
