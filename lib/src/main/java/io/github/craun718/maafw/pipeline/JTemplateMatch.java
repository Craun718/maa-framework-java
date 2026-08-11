package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** TemplateMatch recognition parameters. */
public final class JTemplateMatch implements JRecognitionParam {

    public List<String> template;
    public Object roi = List.of(0, 0, 0, 0);
    @JsonProperty("roi_offset")
    public List<Integer> roiOffset = List.of(0, 0, 0, 0);
    public List<Double> threshold = List.of(0.7);
    @JsonProperty("order_by")
    public String orderBy = "Horizontal";
    public int index;
    public int method = 5;
    @JsonProperty("green_mask")
    public boolean greenMask;
}
