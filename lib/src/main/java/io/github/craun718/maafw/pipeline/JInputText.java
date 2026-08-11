package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

/** InputText action parameters. */
public final class JInputText implements JActionParam {

    @JsonProperty("input_text")
    public String inputText;
}
