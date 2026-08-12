package io.github.craun718.maafw;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
import io.github.craun718.maafw.pipeline.JActionParam;
import io.github.craun718.maafw.pipeline.JActionType;
import io.github.craun718.maafw.pipeline.JPipelineData;
import io.github.craun718.maafw.pipeline.JPipelineParser;
import io.github.craun718.maafw.pipeline.JRecognitionParam;
import io.github.craun718.maafw.pipeline.JRecognitionType;
import io.github.craun718.maafw.pipeline.JWaitFreezes;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Context passed into custom recognizers/actions and context event sinks.
 *
 * <p>Native context handles are borrowed from the running tasker and do not need to be closed.
 */
public final class Context {

    private final Pointer handle;

    /** Wraps a borrowed native context handle. */
    public Context(Pointer handle) {
        this.handle = Objects.requireNonNull(handle, "handle");
    }

    public Pointer handle() {
        return handle;
    }

    public TaskDetail runTask(String entry) {
        return runTask(entry, (String) null);
    }

    public TaskDetail runTask(String entry, Map<String, Object> pipelineOverride) {
        return runTask(
                entry, MaaJson.write(pipelineOverride == null ? Map.of() : pipelineOverride));
    }

    public TaskDetail runTask(String entry, String pipelineOverride) {
        long taskId = MaaLibrary.framework()
                .MaaContextRunTask(
                        handle, entry, MaaJson.objectJsonOrEmpty(pipelineOverride));
        return taskId == 0 ? null : tasker().getTaskDetail(taskId);
    }

    public RecognitionDetail runRecognition(String entry, MaaImage image) {
        return runRecognition(entry, image, (String) null);
    }

    public RecognitionDetail runRecognition(
            String entry, MaaImage image, Map<String, Object> pipelineOverride) {
        return runRecognition(
                entry,
                image,
                MaaJson.write(pipelineOverride == null ? Map.of() : pipelineOverride));
    }

    public RecognitionDetail runRecognition(
            String entry, MaaImage image, String pipelineOverride) {
        try (MaaImageBuffer imageBuffer = new MaaImageBuffer()) {
            imageBuffer.set(image);
            long recoId = MaaLibrary.framework()
                    .MaaContextRunRecognition(
                            handle,
                            entry,
                            MaaJson.objectJsonOrEmpty(pipelineOverride),
                            imageBuffer.handle());
            return recoId == 0 ? null : tasker().getRecognitionDetail(recoId);
        }
    }

    public ActionDetail runAction(String entry) {
        return runAction(entry, null, "", (String) null);
    }

    public ActionDetail runAction(String entry, MaaRect box, String recoDetail) {
        return runAction(entry, box, recoDetail, (String) null);
    }

    public ActionDetail runAction(
            String entry, MaaRect box, String recoDetail, Map<String, Object> pipelineOverride) {
        return runAction(
                entry,
                box,
                recoDetail,
                MaaJson.write(pipelineOverride == null ? Map.of() : pipelineOverride));
    }

    public ActionDetail runAction(
            String entry, MaaRect box, String recoDetail, String pipelineOverride) {
        try (MaaRectBuffer rectBuffer = new MaaRectBuffer()) {
            rectBuffer.set(box == null ? MaaRect.of(0, 0, 0, 0) : box);
            long actionId = MaaLibrary.framework()
                    .MaaContextRunAction(
                            handle,
                            entry,
                            MaaJson.objectJsonOrEmpty(pipelineOverride),
                            rectBuffer.handle(),
                            recoDetail == null ? "" : recoDetail);
            return actionId == 0 ? null : tasker().getActionDetail(actionId);
        }
    }

    public RecognitionDetail runRecognitionDirect(
            JRecognitionType recoType, JRecognitionParam recoParam, MaaImage image) {
        Objects.requireNonNull(recoType, "recoType");
        Objects.requireNonNull(recoParam, "recoParam");
        return runRecognitionDirect(
                recoType.nativeName(), MaaJson.parseObject(MaaJson.write(recoParam)), image);
    }

    public RecognitionDetail runRecognitionDirect(
            String recoType, Map<String, Object> recoParam, MaaImage image) {
        try (MaaImageBuffer imageBuffer = new MaaImageBuffer()) {
            imageBuffer.set(image);
            long recoId = MaaLibrary.framework()
                    .MaaContextRunRecognitionDirect(
                            handle,
                            recoType,
                            MaaJson.write(recoParam == null ? Map.of() : recoParam),
                            imageBuffer.handle());
            return recoId == 0 ? null : tasker().getRecognitionDetail(recoId);
        }
    }

    public ActionDetail runActionDirect(JActionType actionType, JActionParam actionParam) {
        return runActionDirect(actionType, actionParam, null, "");
    }

    public ActionDetail runActionDirect(JActionType actionType, JActionParam actionParam, MaaRect box) {
        return runActionDirect(actionType, actionParam, box, "");
    }

    public ActionDetail runActionDirect(
            JActionType actionType, JActionParam actionParam, MaaRect box, String recoDetail) {
        Objects.requireNonNull(actionType, "actionType");
        Objects.requireNonNull(actionParam, "actionParam");
        return runActionDirect(
                actionType.nativeName(),
                MaaJson.parseObject(MaaJson.write(actionParam)),
                box,
                recoDetail);
    }

    public ActionDetail runActionDirect(
            String actionType, Map<String, Object> actionParam, MaaRect box, String recoDetail) {
        try (MaaRectBuffer rectBuffer = new MaaRectBuffer()) {
            rectBuffer.set(box == null ? MaaRect.of(0, 0, 0, 0) : box);
            long actionId = MaaLibrary.framework()
                    .MaaContextRunActionDirect(
                            handle,
                            actionType,
                            MaaJson.write(actionParam == null ? Map.of() : actionParam),
                            rectBuffer.handle(),
                            recoDetail == null ? "" : recoDetail);
            return actionId == 0 ? null : tasker().getActionDetail(actionId);
        }
    }

    public boolean waitFreezes(long time) {
        return waitFreezes(time, null, Map.of());
    }

    public boolean waitFreezes(long time, MaaRect box) {
        return waitFreezes(time, box, Map.of());
    }

    public boolean waitFreezes(long time, Map<String, Object> waitFreezesParam) {
        return waitFreezes(time, null, waitFreezesParam);
    }

    /** Uses the typed {@link JWaitFreezes} parameters; the top-level time remains zero. */
    public boolean waitFreezes(JWaitFreezes waitFreezesParam) {
        return waitFreezes(0, null, waitFreezesParam);
    }

    /** Uses the typed {@link JWaitFreezes} parameters with an explicit top-level time. */
    public boolean waitFreezes(long time, JWaitFreezes waitFreezesParam) {
        return waitFreezes(time, null, waitFreezesParam);
    }

    /** Uses the typed {@link JWaitFreezes} parameters with a box for {@code target=Self}. */
    public boolean waitFreezes(long time, MaaRect box, JWaitFreezes waitFreezesParam) {
        Objects.requireNonNull(waitFreezesParam, "waitFreezesParam");
        return waitFreezes(time, box, MaaJson.parseObject(MaaJson.write(waitFreezesParam)));
    }

    public boolean waitFreezes(long time, MaaRect box, Map<String, Object> waitFreezesParam) {
        String paramJson = MaaJson.write(waitFreezesParam == null ? Map.of() : waitFreezesParam);
        if (box == null) {
            return MaaStringBuffer.toBoolean(
                    MaaLibrary.framework().MaaContextWaitFreezes(handle, time, null, paramJson));
        }
        try (MaaRectBuffer rectBuffer = new MaaRectBuffer()) {
            rectBuffer.set(box);
            return MaaStringBuffer.toBoolean(
                    MaaLibrary.framework().MaaContextWaitFreezes(handle, time, rectBuffer.handle(), paramJson));
        }
    }

    public boolean overridePipeline(String pipelineOverride) {
        return MaaStringBuffer.toBoolean(MaaLibrary.framework()
                .MaaContextOverridePipeline(
                        handle, MaaJson.objectJsonOrEmpty(pipelineOverride)));
    }

    public boolean overridePipeline(Map<String, Object> pipelineOverride) {
        return overridePipeline(
                MaaJson.write(pipelineOverride == null ? Map.of() : pipelineOverride));
    }

    /**
     * Overrides a node's {@code next} list for the current task execution. Items may be raw
     * strings with {@code [JumpBack]}/{@code [Anchor]} prefixes or typed
     * {@link io.github.craun718.maafw.pipeline.JNodeAttr} values.
     *
     * @return {@code true} when the node already exists and the override was accepted
     */
    public boolean overrideNext(String name, List<?> nextList) {
        try (MaaStringListBuffer listBuffer = new MaaStringListBuffer()) {
            listBuffer.set(MaaNextItems.names(nextList));
            return MaaStringBuffer.toBoolean(
                    MaaLibrary.framework().MaaContextOverrideNext(handle, name, listBuffer.handle()));
        }
    }

    public boolean overrideImage(String imageName, MaaImage image) {
        try (MaaImageBuffer imageBuffer = new MaaImageBuffer()) {
            imageBuffer.set(image);
            return MaaStringBuffer.toBoolean(
                    MaaLibrary.framework().MaaContextOverrideImage(handle, imageName, imageBuffer.handle()));
        }
    }

    /** Returns the current pipeline node definition, or {@code null} if the node does not exist. */
    public Map<String, Object> getNodeData(String name) {
        try (MaaStringBuffer buffer = new MaaStringBuffer()) {
            if (!MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaContextGetNodeData(handle, name, buffer.handle()))) {
                return null;
            }
            String json = buffer.get();
            if (json == null || json.isBlank()) {
                return null;
            }
            try {
                return MaaJson.parseObject(json);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    /** Returns the current pipeline node definition as raw JSON, or {@code null} if the node does not exist. */
    public String getNodeJson(String name) {
        try (MaaStringBuffer buffer = new MaaStringBuffer()) {
            if (!MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaContextGetNodeData(handle, name, buffer.handle()))) {
                return null;
            }
            String json = buffer.get();
            return json == null || json.isBlank() ? null : json;
        }
    }

    /** Returns the current pipeline node as a typed object, or {@code null} if the node does not exist. */
    public JPipelineData getNodeObject(String name) {
        Map<String, Object> data = getNodeData(name);
        return data == null ? null : JPipelineParser.parse(data);
    }

    public Tasker tasker() {
        Pointer taskerHandle = MaaLibrary.framework().MaaContextGetTasker(handle);
        if (taskerHandle == null || taskerHandle == Pointer.NULL) {
            throw new IllegalStateException("Failed to get tasker");
        }
        return new Tasker(taskerHandle, false);
    }

    public TaskJob getTaskJob() {
        long taskId = MaaLibrary.framework().MaaContextGetTaskId(handle);
        if (taskId == 0) {
            throw new IllegalStateException("Context has no task id");
        }
        return tasker().taskJob(taskId);
    }

    /**
     * Clones this context. The returned handle is owned by the original native context and shares
     * its task state, so it does not need to be closed.
     */
    @Override
    public Context clone() {
        Pointer cloned = MaaLibrary.framework().MaaContextClone(handle);
        if (cloned == null || cloned == Pointer.NULL) {
            throw new IllegalStateException("Failed to clone context");
        }
        return new Context(cloned);
    }

    public boolean setAnchor(String anchorName, String nodeName) {
        return MaaStringBuffer.toBoolean(
                MaaLibrary.framework().MaaContextSetAnchor(handle, anchorName, nodeName));
    }

    /** Returns the anchor's node name, or {@code null} when the anchor does not exist. */
    public String getAnchor(String anchorName) {
        try (MaaStringBuffer buffer = new MaaStringBuffer()) {
            if (!MaaStringBuffer.toBoolean(
                    MaaLibrary.framework().MaaContextGetAnchor(handle, anchorName, buffer.handle()))) {
                return null;
            }
            return buffer.getUtf8();
        }
    }

    public long getHitCount(String nodeName) {
        LongByReference count = new LongByReference();
        if (!MaaStringBuffer.toBoolean(
                MaaLibrary.framework().MaaContextGetHitCount(handle, nodeName, count))) {
            return 0;
        }
        return count.getValue();
    }

    public boolean clearHitCount(String nodeName) {
        return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaContextClearHitCount(handle, nodeName));
    }
}
