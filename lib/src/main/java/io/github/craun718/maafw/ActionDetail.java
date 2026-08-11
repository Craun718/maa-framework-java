package io.github.craun718.maafw;

import java.util.Map;

/** Detailed result of an action operation. */
public final class ActionDetail {

    private final long actionId;
    private final String name;
    private final String action;
    private final MaaRect box;
    private final boolean success;
    private final ActionResult result;
    private final Map<String, Object> rawDetail;

    public ActionDetail(
            long actionId,
            String name,
            String action,
            MaaRect box,
            boolean success,
            ActionResult result,
            Map<String, Object> rawDetail) {
        this.actionId = actionId;
        this.name = name;
        this.action = action;
        this.box = box;
        this.success = success;
        this.result = result;
        this.rawDetail = rawDetail == null ? Map.of() : Map.copyOf(rawDetail);
    }

    public long actionId() {
        return actionId;
    }

    public String name() {
        return name;
    }

    public String action() {
        return action;
    }

    public MaaRect box() {
        return box;
    }

    public boolean success() {
        return success;
    }

    public ActionResult result() {
        return result;
    }

    public Map<String, Object> rawDetail() {
        return rawDetail;
    }

    @Override
    public String toString() {
        return "ActionDetail(id=" + actionId + ", name=" + name + ", action=" + action + ", success=" + success + ")";
    }
}
