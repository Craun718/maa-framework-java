package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/** Typed pipeline v2 node definition. */
public final class JPipelineData {

    public JRecognition recognition;
    public JAction action;
    public List<JNodeAttr> next = List.of();
    @JsonProperty("rate_limit")
    public long rateLimit = 1000;
    public long timeout = 20000;
    @JsonProperty("on_error")
    public List<JNodeAttr> onError = List.of();
    public Map<String, String> anchor = Map.of();
    public boolean inverse;
    public boolean enabled = true;
    @JsonProperty("pre_delay")
    public long preDelay = 200;
    @JsonProperty("post_delay")
    public long postDelay = 200;
    @JsonProperty("pre_wait_freezes")
    public JWaitFreezes preWaitFreezes;
    @JsonProperty("post_wait_freezes")
    public JWaitFreezes postWaitFreezes;
    public long repeat = 1;
    @JsonProperty("repeat_delay")
    public long repeatDelay;
    @JsonProperty("repeat_wait_freezes")
    public JWaitFreezes repeatWaitFreezes;
    @JsonProperty("max_hit")
    public long maxHit = 4294967295L;
    public Object focus;
    public Map<String, Object> attach = Map.of();
}
