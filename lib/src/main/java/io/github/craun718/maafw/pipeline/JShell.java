package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Shell action parameters. */
public final class JShell implements JActionParam {

    public String cmd;
    @JsonProperty("shell_timeout")
    public long shellTimeout = 20000;
}
