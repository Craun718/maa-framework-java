package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** DirectHit recognition parameters. */
public final class JDirectHit implements JRecognitionParam {

    public Object roi = List.of(0, 0, 0, 0);
    @JsonProperty("roi_offset")
    public List<Integer> roiOffset = List.of(0, 0, 0, 0);
}
