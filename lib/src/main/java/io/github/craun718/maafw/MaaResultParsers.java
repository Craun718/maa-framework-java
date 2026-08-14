package io.github.craun718.maafw;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Internal helpers for converting MaaFramework JSON detail values into Java values. */
public final class MaaResultParsers {

    private MaaResultParsers() {
    }

    static MaaRect rect(Object value) {
        if (value instanceof List<?> list && list.size() == 4) {
            return new MaaRect(integer(list.get(0)), integer(list.get(1)), integer(list.get(2)), integer(list.get(3)));
        }
        return null;
    }

    static MaaPoint point(Object value) {
        if (value instanceof List<?> list && list.size() == 2) {
            return new MaaPoint(integer(list.get(0)), integer(list.get(1)));
        }
        return null;
    }

    static List<MaaPoint> pointList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(MaaResultParsers::point).filter(java.util.Objects::nonNull).toList();
    }

    static List<Integer> integerList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(MaaResultParsers::integer).toList();
    }

    static Integer integer(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    static Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    static Double number(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return null;
    }

    static Boolean booleanValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return null;
    }

    static String string(Object value) {
        return value == null ? null : value.toString();
    }

    static Map<String, Object> unmodifiableMap(Map<String, Object> value) {
        if (value == null) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(value));
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> objectMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }
}
