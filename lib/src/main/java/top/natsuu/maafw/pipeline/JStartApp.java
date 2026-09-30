package top.natsuu.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

/** StartApp action parameters. */
public final class JStartApp implements JActionParam {

    @JsonProperty("package")
    public String packageName;
}
