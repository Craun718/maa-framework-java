package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** LongPress action parameters. */
public final class JLongPress implements JActionParam {

    public Object target = true;
    @JsonProperty("target_offset")
    public List<Integer> targetOffset = List.of(0, 0, 0, 0);
    public long duration = 1000;
    public long contact;
    public int pressure = 1;
}
