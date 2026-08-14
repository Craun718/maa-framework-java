package io.github.craun718.maafw;

import java.util.List;

/** Detailed result of a wait-freezes operation. */
public final class WaitFreezesDetail {

    private final long wfId;
    private final String name;
    private final String phase;
    private final boolean success;
    private final long elapsedMs;
    private final List<Long> recoIdList;
    private final MaaRect roi;

    public WaitFreezesDetail(long wfId, String name, String phase, boolean success, long elapsedMs, List<Long> recoIdList, MaaRect roi) {
        this.wfId = wfId;
        this.name = name;
        this.phase = phase;
        this.success = success;
        this.elapsedMs = elapsedMs;
        this.recoIdList = List.copyOf(recoIdList == null ? List.of() : recoIdList);
        this.roi = roi;
    }

    public long wfId() {
        return wfId;
    }

    public String name() {
        return name;
    }

    public String phase() {
        return phase;
    }

    public boolean success() {
        return success;
    }

    public long elapsedMs() {
        return elapsedMs;
    }

    public List<Long> recoIdList() {
        return recoIdList;
    }

    public MaaRect roi() {
        return roi;
    }

    @Override
    public String toString() {
        return "WaitFreezesDetail(id=" + wfId + ", name=" + name + ", success=" + success + ")";
    }
}
