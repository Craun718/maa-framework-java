package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Scroll action parameters. */
public final class JScroll implements JActionParam {

    public Object target = true;
    @JsonProperty("target_offset")
    public List<Integer> targetOffset = List.of(0, 0, 0, 0);
    public int dx;
    public int dy;
}
