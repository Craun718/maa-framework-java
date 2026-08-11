package io.github.craun718.maafw;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.LongFunction;

/** Detailed result of a task execution. Node details are loaded lazily. */
public final class TaskDetail {

    private final long taskId;
    private final String entry;
    private final List<Long> nodeIdList;
    private final MaaDef.Status status;
    private final LongFunction<NodeDetail> nodeDetailFunc;
    private List<NodeDetail> nodes;

    public TaskDetail(
            long taskId,
            String entry,
            List<Long> nodeIdList,
            MaaDef.Status status,
            LongFunction<NodeDetail> nodeDetailFunc) {
        this.taskId = taskId;
        this.entry = entry;
        this.nodeIdList = List.copyOf(nodeIdList == null ? List.of() : nodeIdList);
        this.status = status;
        this.nodeDetailFunc = nodeDetailFunc;
    }

    public long taskId() {
        return taskId;
    }

    public String entry() {
        return entry;
    }

    public List<Long> nodeIdList() {
        return nodeIdList;
    }

    public MaaDef.Status status() {
        return status;
    }

    public synchronized List<NodeDetail> nodes() {
        if (nodes == null) {
            nodes = new ArrayList<>(nodeIdList.size());
            if (nodeDetailFunc != null) {
                for (long nodeId : nodeIdList) {
                    NodeDetail detail = nodeDetailFunc.apply(nodeId);
                    if (detail == null) {
                        throw new IllegalStateException("Failed to get node detail: " + nodeId);
                    }
                    nodes.add(detail);
                }
            }
        }
        return List.copyOf(nodes);
    }

    @Override
    public String toString() {
        return "TaskDetail(id=" + taskId + ", entry=" + entry + ", status=" + status + ")";
    }
}
