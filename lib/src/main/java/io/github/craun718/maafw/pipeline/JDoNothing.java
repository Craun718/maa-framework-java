package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;

/** DoNothing action parameters. */
public final class JDoNothing implements JActionParam {

    @JsonValue
    public Map<String, Object> jsonValue() {
        return Map.of();
    }
}
