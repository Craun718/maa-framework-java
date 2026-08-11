package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Node reference with optional jump-back and anchor attributes. */
public final class JNodeAttr {

    public String name;
    @JsonProperty("jump_back")
    public boolean jumpBack;
    public boolean anchor;
}
