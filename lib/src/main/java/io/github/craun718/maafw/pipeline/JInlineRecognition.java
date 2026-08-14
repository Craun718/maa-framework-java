package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/** Inline recognition object used inside And/Or sub-recognition lists. */
public final class JInlineRecognition {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("sub_name")
    public String subName;

    public JRecognitionType type;
    public JRecognitionParam param;

    public static JInlineRecognition of(String subName, JRecognition recognition) {
        Objects.requireNonNull(recognition, "recognition");
        JInlineRecognition inline = new JInlineRecognition();
        inline.subName = subName;
        inline.type = recognition.type;
        inline.param = recognition.param;
        return inline;
    }
}
