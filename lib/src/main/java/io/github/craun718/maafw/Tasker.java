package io.github.craun718.maafw;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import io.github.craun718.maafw.pipeline.JActionParam;
import io.github.craun718.maafw.pipeline.JActionType;
import io.github.craun718.maafw.pipeline.JRecognitionParam;
import io.github.craun718.maafw.pipeline.JRecognitionType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** High-level tasker wrapper equivalent to the Python binding's {@code Tasker}. */
public class Tasker implements AutoCloseable {

    private final Pointer handle;
    private final boolean owned;
    private final Map<Long, TaskerEventSink> taskerSinks = new HashMap<>();
    private final Map<Long, ContextEventSink> contextSinks = new HashMap<>();
    private Resource resourceHolder;
    private Controller controllerHolder;

    public Tasker() {
        this(MaaLibrary.framework().MaaTaskerCreate(), true);
        if (handle == null) {
            throw new IllegalStateException("Failed to create tasker");
        }
    }

    /** Wraps an existing native handle without taking ownership. */
    public Tasker(Pointer handle) {
        this(handle, false);
    }

    Tasker(Pointer handle, boolean owned) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.owned = owned;
    }

    public Pointer handle() {
        return handle;
    }

    public boolean bind(Resource resource, Controller controller) {
        resourceHolder = resource;
        controllerHolder = controller;
        return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaTaskerBindResource(handle, resource.handle()))
                && MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaTaskerBindController(handle, controller.handle()));
    }

    public Resource resource() {
        Pointer resourceHandle = MaaLibrary.framework().MaaTaskerGetResource(handle);
        if (resourceHandle == null || resourceHandle == Pointer.NULL) {
            throw new IllegalStateException("Failed to get resource");
        }
        return new Resource(resourceHandle, false);
    }

    public Controller controller() {
        Pointer controllerHandle = MaaLibrary.framework().MaaTaskerGetController(handle);
        if (controllerHandle == null || controllerHandle == Pointer.NULL) {
            throw new IllegalStateException("Failed to get controller");
        }
        return new Controller(controllerHandle, false);
    }

    public boolean inited() {
        return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaTaskerInited(handle));
    }

    public TaskJob postTask(String entry) {
        return postTask(entry, (String) null);
    }

    public TaskJob postTask(String entry, Map<String, Object> pipelineOverride) {
        return postTask(
                entry, MaaJson.write(pipelineOverride == null ? Map.of() : pipelineOverride));
    }

    public TaskJob postTask(String entry, String pipelineOverride) {
        long taskId = MaaLibrary.framework()
                .MaaTaskerPostTask(
                        handle, entry, MaaJson.objectJsonOrEmpty(pipelineOverride));
        return taskJob(taskId);
    }

    public TaskJob postRecognition(JRecognitionType recoType, JRecognitionParam recoParam, MaaImage image) {
        Objects.requireNonNull(recoType, "recoType");
        Objects.requireNonNull(recoParam, "recoParam");
        return postRecognition(
                recoType.nativeName(), MaaJson.parseObject(MaaJson.write(recoParam)), image);
    }

    public TaskJob postRecognition(String recoType, Map<String, Object> recoParam, MaaImage image) {
        try (MaaImageBuffer imageBuffer = new MaaImageBuffer()) {
            imageBuffer.set(image);
            long taskId = MaaLibrary.framework()
                    .MaaTaskerPostRecognition(
                            handle,
                            recoType,
                            MaaJson.write(recoParam == null ? Map.of() : recoParam),
                            imageBuffer.handle());
            return taskJob(taskId);
        }
    }

    public TaskJob postAction(JActionType actionType, JActionParam actionParam) {
        return postAction(actionType, actionParam, null, "");
    }

    public TaskJob postAction(JActionType actionType, JActionParam actionParam, MaaRect box) {
        return postAction(actionType, actionParam, box, "");
    }

    public TaskJob postAction(
            JActionType actionType, JActionParam actionParam, MaaRect box, String recoDetail) {
        Objects.requireNonNull(actionType, "actionType");
        Objects.requireNonNull(actionParam, "actionParam");
        return postAction(
                actionType.nativeName(),
                MaaJson.parseObject(MaaJson.write(actionParam)),
                box,
                recoDetail);
    }

    public TaskJob postAction(
            String actionType, Map<String, Object> actionParam, MaaRect box, String recoDetail) {
        try (MaaRectBuffer rectBuffer = new MaaRectBuffer()) {
            rectBuffer.set(box == null ? MaaRect.of(0, 0, 0, 0) : box);
            long taskId = MaaLibrary.framework()
                    .MaaTaskerPostAction(
                            handle,
                            actionType,
                            MaaJson.write(actionParam == null ? Map.of() : actionParam),
                            rectBuffer.handle(),
                            recoDetail == null ? "" : recoDetail);
            return taskJob(taskId);
        }
    }

    public boolean running() {
        return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaTaskerRunning(handle));
    }

    /** Posts a stop request and returns the asynchronous stop job. */
    public Job postStop() {
        long jobId = MaaLibrary.framework().MaaTaskerPostStop(handle);
        return new Job(jobId, this::status, this::waitFor);
    }

    public boolean stopping() {
        return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaTaskerStopping(handle));
    }

    public NodeDetail getLatestNode(String name) {
        LongByReference nodeId = new LongByReference();
        if (!MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaTaskerGetLatestNode(handle, name, nodeId))) {
            return null;
        }
        return getNodeDetail(nodeId.getValue());
    }

    public boolean clearCache() {
        return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaTaskerClearCache(handle));
    }

    public boolean overridePipeline(long taskId, String pipelineOverride) {
        return MaaStringBuffer.toBoolean(MaaLibrary.framework()
                .MaaTaskerOverridePipeline(
                        handle, taskId, MaaJson.objectJsonOrEmpty(pipelineOverride)));
    }

    public boolean overridePipeline(long taskId, Map<String, Object> pipelineOverride) {
        return overridePipeline(
                taskId, MaaJson.write(pipelineOverride == null ? Map.of() : pipelineOverride));
    }

    public RecognitionDetail getRecognitionDetail(long recoId) {
        try (MaaStringBuffer name = new MaaStringBuffer();
                MaaStringBuffer algorithm = new MaaStringBuffer();
                MaaRectBuffer box = new MaaRectBuffer();
                MaaStringBuffer detailJson = new MaaStringBuffer();
                MaaImageBuffer raw = new MaaImageBuffer();
                MaaImageListBuffer draws = new MaaImageListBuffer()) {
            ByteByReference hit = new ByteByReference();
            if (!MaaStringBuffer.toBoolean(MaaLibrary.framework()
                    .MaaTaskerGetRecognitionDetail(
                            handle,
                            recoId,
                            name.handle(),
                            algorithm.handle(),
                            hit,
                            box.handle(),
                            detailJson.handle(),
                            raw.handle(),
                            draws.handle()))) {
                return null;
            }

            String algorithmName = algorithm.getUtf8();
            Object rawDetailValue = MaaJson.parse(detailJson.get());
            ParsedRecognition parsed = parseRecognition(algorithmName, rawDetailValue);
            return new RecognitionDetail(
                    recoId,
                    name.getUtf8(),
                    algorithmName,
                    hit.getValue() != 0,
                    hit.getValue() != 0 ? box.get() : null,
                    parsed.allResults(),
                    parsed.filteredResults(),
                    parsed.bestResult(),
                    rawDetailValue,
                    raw.get(),
                    draws.get());
        }
    }

    public ActionDetail getActionDetail(long actionId) {
        try (MaaStringBuffer name = new MaaStringBuffer();
                MaaStringBuffer action = new MaaStringBuffer();
                MaaRectBuffer box = new MaaRectBuffer();
                MaaStringBuffer detailJson = new MaaStringBuffer()) {
            ByteByReference success = new ByteByReference();
            if (!MaaStringBuffer.toBoolean(MaaLibrary.framework()
                    .MaaTaskerGetActionDetail(
                            handle,
                            actionId,
                            name.handle(),
                            action.handle(),
                            box.handle(),
                            success,
                            detailJson.handle()))) {
                return null;
            }
            Map<String, Object> rawDetail = MaaJson.parseObjectOrEmpty(detailJson.get());
            return new ActionDetail(
                    actionId,
                    name.getUtf8(),
                    action.getUtf8(),
                    box.get(),
                    success.getValue() != 0,
                    rawDetail.isEmpty()
                            ? null
                            : new ActionResult(parseActionType(action.getUtf8()), rawDetail),
                    rawDetail);
        }
    }

    public WaitFreezesDetail getWaitFreezesDetail(long wfId) {
        try (MaaStringBuffer name = new MaaStringBuffer();
                MaaStringBuffer phase = new MaaStringBuffer();
                MaaRectBuffer roi = new MaaRectBuffer()) {
            ByteByReference success = new ByteByReference();
            LongByReference elapsedMs = new LongByReference();
            LongByReference size = new LongByReference();
            if (!MaaStringBuffer.toBoolean(MaaLibrary.framework()
                    .MaaTaskerGetWaitFreezesDetail(
                            handle,
                            wfId,
                            name.handle(),
                            phase.handle(),
                            success,
                            elapsedMs,
                            null,
                            size,
                            roi.handle()))) {
                return null;
            }

            long count = size.getValue();
            List<Long> recoIds = new ArrayList<>((int) Math.min(count, Integer.MAX_VALUE));
            if (count > 0) {
                try (Memory ids = new Memory(count * Long.BYTES)) {
                    if (!MaaStringBuffer.toBoolean(MaaLibrary.framework()
                            .MaaTaskerGetWaitFreezesDetail(
                                    handle,
                                    wfId,
                                    name.handle(),
                                    phase.handle(),
                                    success,
                                    elapsedMs,
                                    ids,
                                    size,
                                    roi.handle()))) {
                        return null;
                    }
                    long[] values = ids.getLongArray(0, Math.toIntExact(count));
                    for (long value : values) {
                        recoIds.add(value);
                    }
                }
            }

            return new WaitFreezesDetail(
                    wfId,
                    name.getUtf8(),
                    phase.getUtf8(),
                    success.getValue() != 0,
                    elapsedMs.getValue(),
                    recoIds,
                    roi.get());
        }
    }

    public NodeDetail getNodeDetail(long nodeId) {
        try (MaaStringBuffer name = new MaaStringBuffer()) {
            LongByReference recoId = new LongByReference();
            LongByReference actionId = new LongByReference();
            ByteByReference completed = new ByteByReference();
            if (!MaaStringBuffer.toBoolean(MaaLibrary.framework()
                    .MaaTaskerGetNodeDetail(
                            handle, nodeId, name.handle(), recoId, actionId, completed))) {
                return null;
            }
            return new NodeDetail(
                    nodeId,
                    name.getUtf8(),
                    recoId.getValue() == 0 ? null : getRecognitionDetail(recoId.getValue()),
                    actionId.getValue() == 0 ? null : getActionDetail(actionId.getValue()),
                    completed.getValue() != 0);
        }
    }

    public TaskDetail getTaskDetail(long taskId) {
        try (MaaStringBuffer entry = new MaaStringBuffer()) {
            LongByReference size = new LongByReference();
            IntByReference status = new IntByReference();
            if (!MaaStringBuffer.toBoolean(MaaLibrary.framework()
                    .MaaTaskerGetTaskDetail(handle, taskId, entry.handle(), null, size, status))) {
                return null;
            }

            long count = size.getValue();
            List<Long> nodeIds = new ArrayList<>((int) Math.min(count, Integer.MAX_VALUE));
            if (count > 0) {
                try (Memory ids = new Memory(count * Long.BYTES)) {
                    if (!MaaStringBuffer.toBoolean(MaaLibrary.framework()
                            .MaaTaskerGetTaskDetail(handle, taskId, entry.handle(), ids, size, status))) {
                        return null;
                    }
                    long[] values = ids.getLongArray(0, Math.toIntExact(count));
                    for (long value : values) {
                        nodeIds.add(value);
                    }
                }
            }

            return new TaskDetail(
                    taskId, entry.getUtf8(), nodeIds, MaaDef.Status.of(status.getValue()), this::getNodeDetail);
        }
    }

    public Long addSink(TaskerEventSink sink) {
        long sinkId = MaaLibrary.framework().MaaTaskerAddSink(handle, sink.callback(), null);
        if (sinkId == MaaDef.INVALID_ID) {
            return null;
        }
        taskerSinks.put(sinkId, sink);
        return sinkId;
    }

    public void removeSink(long sinkId) {
        MaaLibrary.framework().MaaTaskerRemoveSink(handle, sinkId);
        taskerSinks.remove(sinkId);
    }

    public void clearSinks() {
        MaaLibrary.framework().MaaTaskerClearSinks(handle);
        taskerSinks.clear();
    }

    public Long addContextSink(ContextEventSink sink) {
        long sinkId = MaaLibrary.framework().MaaTaskerAddContextSink(handle, sink.callback(), null);
        if (sinkId == MaaDef.INVALID_ID) {
            return null;
        }
        contextSinks.put(sinkId, sink);
        return sinkId;
    }

    public void removeContextSink(long sinkId) {
        MaaLibrary.framework().MaaTaskerRemoveContextSink(handle, sinkId);
        contextSinks.remove(sinkId);
    }

    public void clearContextSinks() {
        MaaLibrary.framework().MaaTaskerClearContextSinks(handle);
        contextSinks.clear();
    }

    public static boolean setLogDir(Path path) {
        return setLogDir(path.toString());
    }

    public static boolean setLogDir(String path) {
        return setGlobalStringOption(MaaDef.GlobalOption.LOG_DIR, path);
    }

    public static boolean setSaveDraw(boolean saveDraw) {
        return setGlobalBooleanOption(MaaDef.GlobalOption.SAVE_DRAW, saveDraw);
    }

    @Deprecated
    public static boolean setRecording(boolean recording) {
        return false;
    }

    public static boolean setStdoutLevel(MaaDef.LoggingLevel level) {
        return setGlobalIntOption(MaaDef.GlobalOption.STDOUT_LEVEL, level.code());
    }

    public static boolean setDebugMode(boolean debugMode) {
        return setGlobalBooleanOption(MaaDef.GlobalOption.DEBUG_MODE, debugMode);
    }

    public static boolean setSaveOnError(boolean saveOnError) {
        return setGlobalBooleanOption(MaaDef.GlobalOption.SAVE_ON_ERROR, saveOnError);
    }

    public static boolean setDrawQuality(int quality) {
        return setGlobalIntOption(MaaDef.GlobalOption.DRAW_QUALITY, quality);
    }

    public static boolean setRecoImageCacheLimit(long limit) {
        return setGlobalSizeTOption(MaaDef.GlobalOption.RECO_IMAGE_CACHE_LIMIT, limit);
    }

    public static boolean loadPlugin(String path) {
        return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaGlobalLoadPlugin(path));
    }

    public static boolean loadPlugin(Path path) {
        return loadPlugin(path.toString());
    }

    @Override
    public void close() {
        if (owned && handle != Pointer.NULL) {
            MaaLibrary.framework().MaaTaskerDestroy(handle);
        }
    }

    TaskJob taskJob(long taskId) {
        return new TaskJob(taskId, this::status, this::waitFor, this::getTaskDetail, this::overridePipelineJson);
    }

    private boolean overridePipelineJson(long taskId, String pipelineJson) {
        return MaaStringBuffer.toBoolean(
                MaaLibrary.framework().MaaTaskerOverridePipeline(handle, taskId, pipelineJson));
    }

    private MaaDef.Status status(long id) {
        return MaaDef.Status.of(MaaLibrary.framework().MaaTaskerStatus(handle, id));
    }

    private void waitFor(long id) {
        MaaLibrary.framework().MaaTaskerWait(handle, id);
    }

    private ParsedRecognition parseRecognition(String algorithmName, Object rawDetailValue) {
        if (rawDetailValue == null) {
            return new ParsedRecognition(List.of(), List.of(), null);
        }
        JRecognitionType type = parseRecognitionType(algorithmName);
        if (("And".equals(algorithmName) || "Or".equals(algorithmName)) && rawDetailValue instanceof List<?> list) {
            List<RecognitionDetail> subResults = new ArrayList<>(list.size());
            for (Object item : list) {
                if (!(item instanceof Map<?, ?> map)) {
                    continue;
                }
                Long recoId = MaaResultParsers.longValue(map.get("reco_id"));
                if (recoId == null || recoId == 0) {
                    continue;
                }
                RecognitionDetail subDetail = getRecognitionDetail(recoId);
                if (subDetail != null) {
                    subResults.add(subDetail);
                }
            }
            RecognitionResult result = new RecognitionResult(type, Map.of(), subResults);
            return new ParsedRecognition(List.of(result), List.of(result), result);
        }
        if (!(rawDetailValue instanceof Map<?, ?> rawMap)) {
            return new ParsedRecognition(List.of(), List.of(), null);
        }

        Map<String, Object> rawDetail = objectMap(rawMap);
        List<RecognitionResult> all = parseResultList(type, rawDetail.get("all"));
        List<RecognitionResult> filtered = parseResultList(type, rawDetail.get("filtered"));
        RecognitionResult best = rawDetail.get("best") instanceof Map<?, ?> bestMap
                ? new RecognitionResult(type, objectMap(bestMap))
                : null;
        return new ParsedRecognition(all, filtered, best);
    }

    private List<RecognitionResult> parseResultList(JRecognitionType type, Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<RecognitionResult> results = new ArrayList<>(list.size());
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                results.add(new RecognitionResult(type, objectMap(map)));
            }
        }
        return List.copyOf(results);
    }

    private static JActionType parseActionType(String name) {
        try {
            return JActionType.of(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static JRecognitionType parseRecognitionType(String name) {
        try {
            return JRecognitionType.of(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objectMap(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }

    private static boolean setGlobalStringOption(MaaDef.GlobalOption option, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        try (Memory memory = new Memory(bytes.length)) {
            memory.write(0, bytes, 0, bytes.length);
            return MaaStringBuffer.toBoolean(
                    MaaLibrary.framework().MaaGlobalSetOption(option.code(), memory, bytes.length));
        }
    }

    private static boolean setGlobalBooleanOption(MaaDef.GlobalOption option, boolean value) {
        try (Memory memory = new Memory(1)) {
            memory.setByte(0, (byte) (value ? 1 : 0));
            return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaGlobalSetOption(option.code(), memory, 1));
        }
    }

    private static boolean setGlobalIntOption(MaaDef.GlobalOption option, int value) {
        try (Memory memory = new Memory(Integer.BYTES)) {
            memory.setInt(0, value);
            return MaaStringBuffer.toBoolean(
                    MaaLibrary.framework().MaaGlobalSetOption(option.code(), memory, Integer.BYTES));
        }
    }

    private static boolean setGlobalSizeTOption(MaaDef.GlobalOption option, long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Native size_t values must be non-negative");
        }
        if (Native.POINTER_SIZE == Long.BYTES) {
            try (Memory memory = new Memory(Long.BYTES)) {
                memory.setLong(0, value);
                return MaaStringBuffer.toBoolean(
                        MaaLibrary.framework().MaaGlobalSetOption(option.code(), memory, Long.BYTES));
            }
        }
        if (value > 0xFFFF_FFFFL) {
            throw new IllegalArgumentException("Value does not fit in the native size_t width");
        }
        try (Memory memory = new Memory(Integer.BYTES)) {
            memory.setInt(0, (int) value);
            return MaaStringBuffer.toBoolean(
                    MaaLibrary.framework().MaaGlobalSetOption(option.code(), memory, Integer.BYTES));
        }
    }

    private record ParsedRecognition(
            List<RecognitionResult> allResults,
            List<RecognitionResult> filteredResults,
            RecognitionResult bestResult) {}
}
