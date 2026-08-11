package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Custom recognition parameters. */
public final class JCustomRecognition implements JRecognitionParam {

    @JsonProperty("custom_recognition")
    public String customRecognition;
    public Object roi = List.of(0, 0, 0, 0);
    @JsonProperty("roi_offset")
    public List<Integer> roiOffset = List.of(0, 0, 0, 0);
    @JsonProperty("custom_recognition_param")
    public Object customRecognitionParam;
}
