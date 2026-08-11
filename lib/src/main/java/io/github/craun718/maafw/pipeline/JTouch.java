package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** TouchDown/TouchMove action parameters. */
public final class JTouch implements JActionParam {

    public long contact;
    public Object target = true;
    @JsonProperty("target_offset")
    public List<Integer> targetOffset = List.of(0, 0, 0, 0);
    public int pressure;
}
