package io.github.craun718.maafw;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

/** Small JSON helper shared by the binding. */
public final class MaaJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<Object> ANY_TYPE = new TypeReference<>() {
    };

    private MaaJson() {
    }

    public static String write(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize JSON value", e);
        }
    }

    public static Map<String, Object> parseObject(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return MAPPER.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JSON object", e);
        }
    }

    public static List<Map<String, Object>> parseObjectList(String json) {
        try {
            return MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JSON object list", e);
        }
    }

    /** Parses arbitrary JSON, returning {@code null} for blank input. */
    public static Object parse(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, ANY_TYPE);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JSON", e);
        }
    }

    /** Parses arbitrary JSON and returns an empty map on blank or invalid input. */
    public static Map<String, Object> parseObjectOrEmpty(String json) {
        Object value = parse(json);
        if (value instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> typed = (Map<String, Object>) map;
            return typed;
        }
        return Map.of();
    }

    static String objectJsonOrEmpty(String json) {
        return json == null || json.isBlank() ? "{}" : json;
    }
}
