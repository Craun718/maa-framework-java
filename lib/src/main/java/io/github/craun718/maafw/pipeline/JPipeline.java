package io.github.craun718.maafw.pipeline;

import io.github.craun718.maafw.MaaJson;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Mutable collection of named pipeline v2 nodes. */
public final class JPipeline {

    private final Map<String, JPipelineData> nodes = new LinkedHashMap<>();

    public JPipeline add(JPipelineData node) {
        Objects.requireNonNull(node, "node");
        String name = node.name;
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Node name is required");
        }
        nodes.put(name, node);
        return this;
    }

    public JPipeline add(String name, JPipelineData node) {
        Objects.requireNonNull(node, "node");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Node name is required");
        }
        node.name = name;
        nodes.put(name, node);
        return this;
    }

    public JPipeline remove(String name) {
        nodes.remove(name);
        return this;
    }

    public JPipeline clear() {
        nodes.clear();
        return this;
    }

    public JPipelineData get(String name) {
        return nodes.get(name);
    }

    public boolean has(String name) {
        return nodes.containsKey(name);
    }

    public int size() {
        return nodes.size();
    }

    public Map<String, JPipelineData> nodes() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(nodes));
    }

    public String toJson() {
        return MaaJson.write(nodes);
    }

    public static JPipeline fromJson(String json) {
        JPipeline pipeline = new JPipeline();
        JPipelineParser.parseAll(json).forEach(pipeline::add);
        return pipeline;
    }
}
