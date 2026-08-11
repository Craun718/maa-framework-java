package io.github.craun718.maafw;

/** Detailed result of a pipeline node execution. */
public final class NodeDetail {

    private final long nodeId;
    private final String name;
    private final RecognitionDetail recognition;
    private final ActionDetail action;
    private final boolean completed;

    public NodeDetail(long nodeId, String name, RecognitionDetail recognition, ActionDetail action, boolean completed) {
        this.nodeId = nodeId;
        this.name = name;
        this.recognition = recognition;
        this.action = action;
        this.completed = completed;
    }

    public long nodeId() {
        return nodeId;
    }

    public String name() {
        return name;
    }

    public RecognitionDetail recognition() {
        return recognition;
    }

    public ActionDetail action() {
        return action;
    }

    public boolean completed() {
        return completed;
    }

    @Override
    public String toString() {
        return "NodeDetail(id=" + nodeId + ", name=" + name + ", completed=" + completed + ")";
    }
}
