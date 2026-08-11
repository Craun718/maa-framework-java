package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;

/** StopTask action parameters. */
public final class JStopTask implements JActionParam {

    @JsonValue
    public Map<String, Object> jsonValue() {
        return Map.of();
    }
}
