package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Custom action parameters. */
public final class JCustomAction implements JActionParam {

    @JsonProperty("custom_action")
    public String customAction;
    public Object target = true;
    @JsonProperty("custom_action_param")
    public Object customActionParam;
    @JsonProperty("target_offset")
    public List<Integer> targetOffset = List.of(0, 0, 0, 0);
}
