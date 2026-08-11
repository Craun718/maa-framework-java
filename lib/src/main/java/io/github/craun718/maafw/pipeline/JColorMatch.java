package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** ColorMatch recognition parameters. */
public final class JColorMatch implements JRecognitionParam {

    public List<List<Integer>> lower;
    public List<List<Integer>> upper;
    public Object roi = List.of(0, 0, 0, 0);
    @JsonProperty("roi_offset")
    public List<Integer> roiOffset = List.of(0, 0, 0, 0);
    @JsonProperty("order_by")
    public String orderBy = "Horizontal";
    public int method = 4;
    public int count = 1;
    public int index;
    public boolean connected;
}
