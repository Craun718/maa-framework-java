package top.natsuu.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Custom recognition parameters. */
public final class JCustomRecognition implements JRecognitionParam {

    @JsonProperty("custom_recognition")
    public String customRecognition;
    /** Index of the result to select; negative indices count from the end. */
    public int index;
    public Object roi = List.of(0, 0, 0, 0);
    @JsonProperty("roi_offset")
    public List<Integer> roiOffset = List.of(0, 0, 0, 0);
    @JsonProperty("custom_recognition_param")
    public Object customRecognitionParam;
}
