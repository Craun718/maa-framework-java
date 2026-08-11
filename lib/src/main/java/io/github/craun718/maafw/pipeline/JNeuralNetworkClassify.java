package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** NeuralNetworkClassify recognition parameters. */
public final class JNeuralNetworkClassify implements JRecognitionParam {

    public String model;
    public List<Integer> expected = List.of();
    public Object roi = List.of(0, 0, 0, 0);
    @JsonProperty("roi_offset")
    public List<Integer> roiOffset = List.of(0, 0, 0, 0);
    public List<String> labels = List.of();
    @JsonProperty("order_by")
    public String orderBy = "Horizontal";
    public int index;
}
