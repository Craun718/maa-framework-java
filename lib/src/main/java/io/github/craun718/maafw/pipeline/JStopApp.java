package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

/** StopApp action parameters. */
public final class JStopApp implements JActionParam {

    @JsonProperty("package")
    public String packageName;
}
