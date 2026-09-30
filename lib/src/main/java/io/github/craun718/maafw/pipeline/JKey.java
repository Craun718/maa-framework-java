package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

/** KeyDown/KeyUp action parameters. */
public final class JKey implements JActionParam {

    public int key;
    @JsonProperty("auto_up")
    public boolean autoUp;
}
