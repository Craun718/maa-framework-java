package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** And recognition parameters. */
public final class JAnd implements JRecognitionParam {

    @JsonProperty("all_of")
    public List<Object> allOf = List.of();
    @JsonProperty("box_index")
    public int boxIndex;
    @JsonProperty("sub_name")
    public String subName;
}
