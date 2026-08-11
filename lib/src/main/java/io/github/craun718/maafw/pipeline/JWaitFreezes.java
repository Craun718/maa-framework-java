package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Parameters for waiting until the screen stops changing. */
public final class JWaitFreezes {

    public long time = 1;
    public Object target = true;
    @JsonProperty("target_offset")
    public List<Integer> targetOffset = List.of(0, 0, 0, 0);
    public double threshold = 0.95;
    public int method = 5;
    @JsonProperty("rate_limit")
    public long rateLimit = 1000;
    public long timeout = 20000;
}
