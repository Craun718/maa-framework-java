package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Or recognition parameters. */
public final class JOr implements JRecognitionParam {

    @JsonProperty("any_of")
    public List<Object> anyOf = List.of();
}
