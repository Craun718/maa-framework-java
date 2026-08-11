package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Swipe action parameters. */
public final class JSwipe implements JActionParam {

    public long starting;
    public Object begin = true;
    @JsonProperty("begin_offset")
    public List<Integer> beginOffset = List.of(0, 0, 0, 0);
    public List<Object> end = List.of(true);
    @JsonProperty("end_offset")
    public List<List<Integer>> endOffset = List.of(List.of(0, 0, 0, 0));
    @JsonProperty("end_hold")
    public List<Long> endHold = List.of(0L);
    public List<Long> duration = List.of(200L);
    @JsonProperty("only_hover")
    public boolean onlyHover;
    public long contact;
    public int pressure = 1;
}
