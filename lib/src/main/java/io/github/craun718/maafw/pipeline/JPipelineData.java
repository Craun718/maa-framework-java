package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Typed pipeline v2 node definition. */
public final class JPipelineData {

    @JsonIgnore
    public String name;
    public JRecognition recognition;
    public JAction action;
    public List<JNodeAttr> next = List.of();
    @JsonProperty("rate_limit")
    public long rateLimit = 1000;
    public long timeout = 20000;
    @JsonProperty("on_error")
    public List<JNodeAttr> onError = List.of();
    public Map<String, String> anchor = Map.of();
    public boolean inverse;
    public boolean enabled = true;
    @JsonProperty("pre_delay")
    public long preDelay = 200;
    @JsonProperty("post_delay")
    public long postDelay = 200;
    @JsonProperty("pre_wait_freezes")
    public JWaitFreezes preWaitFreezes;
    @JsonProperty("post_wait_freezes")
    public JWaitFreezes postWaitFreezes;
    public long repeat = 1;
    @JsonProperty("repeat_delay")
    public long repeatDelay;
    @JsonProperty("repeat_wait_freezes")
    public JWaitFreezes repeatWaitFreezes;
    @JsonProperty("max_hit")
    public long maxHit = 4294967295L;
    public Object focus;
    public Map<String, Object> attach = Map.of();

    public JPipelineData name(String name) {
        this.name = name;
        return this;
    }

    public JPipelineData recognition(JRecognition recognition) {
        this.recognition = recognition;
        return this;
    }

    public JPipelineData action(JAction action) {
        this.action = action;
        return this;
    }

    public JPipelineData next(List<JNodeAttr> next) {
        this.next = next == null ? List.of() : List.copyOf(next);
        return this;
    }

    public JPipelineData rateLimit(long rateLimit) {
        this.rateLimit = rateLimit;
        return this;
    }

    public JPipelineData timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public JPipelineData onError(List<JNodeAttr> onError) {
        this.onError = onError == null ? List.of() : List.copyOf(onError);
        return this;
    }

    public JPipelineData anchor(Map<String, String> anchor) {
        this.anchor = anchor == null ? Map.of() : Map.copyOf(anchor);
        return this;
    }

    public JPipelineData inverse(boolean inverse) {
        this.inverse = inverse;
        return this;
    }

    public JPipelineData enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public JPipelineData preDelay(long preDelay) {
        this.preDelay = preDelay;
        return this;
    }

    public JPipelineData postDelay(long postDelay) {
        this.postDelay = postDelay;
        return this;
    }

    public JPipelineData preWaitFreezes(JWaitFreezes preWaitFreezes) {
        this.preWaitFreezes = preWaitFreezes;
        return this;
    }

    public JPipelineData postWaitFreezes(JWaitFreezes postWaitFreezes) {
        this.postWaitFreezes = postWaitFreezes;
        return this;
    }

    public JPipelineData repeat(long repeat) {
        this.repeat = repeat;
        return this;
    }

    public JPipelineData repeatDelay(long repeatDelay) {
        this.repeatDelay = repeatDelay;
        return this;
    }

    public JPipelineData repeatWaitFreezes(JWaitFreezes repeatWaitFreezes) {
        this.repeatWaitFreezes = repeatWaitFreezes;
        return this;
    }

    public JPipelineData maxHit(long maxHit) {
        this.maxHit = maxHit;
        return this;
    }

    public JPipelineData focus(Object focus) {
        this.focus = focus;
        return this;
    }

    public JPipelineData attach(Map<String, Object> attach) {
        this.attach = attach == null ? Map.of() : Map.copyOf(attach);
        return this;
    }

    public JPipelineData addNext(String nodeName) {
        if (nodeName == null || nodeName.isBlank()) {
            return this;
        }
        return addNext(JNodeAttr.of(nodeName));
    }

    public JPipelineData addNext(JNodeAttr attr) {
        Objects.requireNonNull(attr, "attr");
        List<JNodeAttr> updated = new ArrayList<>(next);
        updated.removeIf(item -> Objects.equals(item.name, attr.name));
        updated.add(attr);
        next = List.copyOf(updated);
        return this;
    }

    public JPipelineData removeNext(String nodeName) {
        next = removeNodeAttr(next, nodeName);
        return this;
    }

    public JPipelineData addOnError(String nodeName) {
        if (nodeName == null || nodeName.isBlank()) {
            return this;
        }
        return addOnError(JNodeAttr.of(nodeName));
    }

    public JPipelineData addOnError(JNodeAttr attr) {
        Objects.requireNonNull(attr, "attr");
        List<JNodeAttr> updated = new ArrayList<>(onError);
        updated.removeIf(item -> Objects.equals(item.name, attr.name));
        updated.add(attr);
        onError = List.copyOf(updated);
        return this;
    }

    public JPipelineData removeOnError(String nodeName) {
        onError = removeNodeAttr(onError, nodeName);
        return this;
    }

    public JPipelineData addAnchor(String anchorName) {
        return setAnchorTarget(anchorName, name);
    }

    public JPipelineData setAnchorTarget(String anchorName, String target) {
        if (anchorName == null || anchorName.isBlank()) {
            return this;
        }
        Map<String, String> updated = new LinkedHashMap<>(anchor);
        updated.put(anchorName, target == null ? "" : target);
        anchor = Map.copyOf(updated);
        return this;
    }

    public JPipelineData clearAnchor(String anchorName) {
        return setAnchorTarget(anchorName, "");
    }

    public JPipelineData removeAnchor(String anchorName) {
        if (anchorName == null || anchorName.isBlank()) {
            return this;
        }
        Map<String, String> updated = new LinkedHashMap<>(anchor);
        updated.remove(anchorName);
        anchor = Map.copyOf(updated);
        return this;
    }

    public String toJson() {
        return JPipelineParser.toJson(this);
    }

    private static List<JNodeAttr> removeNodeAttr(List<JNodeAttr> current, String nodeName) {
        if (nodeName == null || nodeName.isBlank()) {
            return current;
        }
        List<JNodeAttr> updated = new ArrayList<>(current);
        updated.removeIf(item -> Objects.equals(item.name, nodeName));
        return List.copyOf(updated);
    }
}
