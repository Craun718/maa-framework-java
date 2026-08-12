package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Node reference with optional jump-back and anchor attributes. */
public final class JNodeAttr {

    public String name;
    @JsonProperty("jump_back")
    public boolean jumpBack;
    public boolean anchor;

    public static JNodeAttr of(String name) {
        return new JNodeAttr().name(name);
    }

    public static JNodeAttr of(String name, boolean jumpBack, boolean anchor) {
        return new JNodeAttr().name(name).jumpBack(jumpBack).anchor(anchor);
    }

    public JNodeAttr name(String name) {
        this.name = name;
        return this;
    }

    public JNodeAttr jumpBack(boolean jumpBack) {
        this.jumpBack = jumpBack;
        return this;
    }

    public JNodeAttr anchor(boolean anchor) {
        this.anchor = anchor;
        return this;
    }

    /** Returns the native node reference string, e.g. {@code [JumpBack][Anchor]NodeA}. */
    public String formatName() {
        String prefix = "";
        if (jumpBack) {
            prefix += "[JumpBack]";
        }
        if (anchor) {
            prefix += "[Anchor]";
        }
        return prefix + (name == null ? "" : name);
    }
}
