package io.github.craun718.maafw.pipeline;

import java.util.List;

/** LongPressKey action parameters. */
public final class JLongPressKey implements JActionParam {

    public List<Integer> key;
    public long duration = 1000;
}
