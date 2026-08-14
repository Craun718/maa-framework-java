package io.github.craun718.maafw;

import java.lang.reflect.Constructor;
import java.util.Objects;

/** Internal helpers for class-based custom registration overloads. */
final class CustomRegistrations {

    private CustomRegistrations() {
    }

    static <T> T newInstance(Class<? extends T> type, String description) {
        Objects.requireNonNull(type, description);
        try {
            Constructor<? extends T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IllegalArgumentException(description + " class must expose a no-arg constructor", e);
        }
    }
}
