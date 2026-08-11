package io.github.craun718.maafw.pipeline;

import java.util.List;

/** Command action parameters. */
public final class JCommand implements JActionParam {

    public String exec;
    public List<String> args = List.of();
    public boolean detach;
}
