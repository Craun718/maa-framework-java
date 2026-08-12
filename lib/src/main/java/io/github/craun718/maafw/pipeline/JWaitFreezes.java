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

    public JWaitFreezes time(long time) {
        this.time = time;
        return this;
    }

    public JWaitFreezes target(Object target) {
        this.target = target;
        return this;
    }

    public JWaitFreezes targetOffset(List<Integer> targetOffset) {
        this.targetOffset = targetOffset == null ? List.of(0, 0, 0, 0) : List.copyOf(targetOffset);
        return this;
    }

    public JWaitFreezes threshold(double threshold) {
        this.threshold = threshold;
        return this;
    }

    public JWaitFreezes method(int method) {
        this.method = method;
        return this;
    }

    public JWaitFreezes rateLimit(long rateLimit) {
        this.rateLimit = rateLimit;
        return this;
    }

    public JWaitFreezes timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }
}
