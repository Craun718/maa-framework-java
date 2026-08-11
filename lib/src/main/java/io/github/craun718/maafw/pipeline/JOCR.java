package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** OCR recognition parameters. */
public final class JOCR implements JRecognitionParam {

    public List<String> expected = List.of();
    public Object roi = List.of(0, 0, 0, 0);
    @JsonProperty("roi_offset")
    public List<Integer> roiOffset = List.of(0, 0, 0, 0);
    public double threshold = 0.3;
    public List<List<String>> replace = List.of();
    @JsonProperty("order_by")
    public String orderBy = "Horizontal";
    public int index;
    @JsonProperty("only_rec")
    public boolean onlyRec;
    public String model = "";
    @JsonProperty("color_filter")
    public String colorFilter = "";
}
