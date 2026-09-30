package top.natsuu.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Or recognition parameters. */
public final class JOr implements JRecognitionParam {

    @JsonProperty("any_of")
    public List<JSubRecognitionItem> anyOf = List.of();
}
