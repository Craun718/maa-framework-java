package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** FeatureMatch recognition parameters. */
public final class JFeatureMatch implements JRecognitionParam {

    public List<String> template;
    public Object roi = List.of(0, 0, 0, 0);
    @JsonProperty("roi_offset")
    public List<Integer> roiOffset = List.of(0, 0, 0, 0);
    public String detector = "SIFT";
    @JsonProperty("order_by")
    public String orderBy = "Horizontal";
    public int count = 4;
    public int index;
    @JsonProperty("green_mask")
    public boolean greenMask;
    public double ratio = 0.6;
}
