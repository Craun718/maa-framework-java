package io.github.craun718.maafw;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import io.github.craun718.maafw.pipeline.JActionParam;
import io.github.craun718.maafw.pipeline.JActionType;
import io.github.craun718.maafw.pipeline.JPipelineData;
import io.github.craun718.maafw.pipeline.JPipelineParser;
import io.github.craun718.maafw.pipeline.JRecognitionParam;
import io.github.craun718.maafw.pipeline.JRecognitionType;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** High-level resource wrapper equivalent to the Python binding's {@code Resource}. */
public class Resource implements AutoCloseable {

    private final Pointer handle;
    private final boolean owned;
    private final Map<Long, ResourceEventSink> sinks = new HashMap<>();
    private final Map<String, CustomRecognition> customRecognitions = new HashMap<>();
    private final Map<String, CustomAction> customActions = new HashMap<>();

    public Resource() {
        this(MaaLibrary.framework().MaaResourceCreate(), true);
        if (handle == null) {
            throw new IllegalStateException("Failed to create resource");
        }
    }

    /** Wraps an existing native handle without taking ownership. */
    public Resource(Pointer handle) {
        this(handle, false);
    }

    Resource(Pointer handle, boolean owned) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.owned = owned;
    }

    public Pointer handle() {
        return handle;
    }

    public Job postBundle(Path path) {
        return postBundle(path.toString());
    }

    public Job postBundle(String path) {
        return new Job(MaaLibrary.framework().MaaResourcePostBundle(handle, path), this::status, this::waitFor);
    }

    public Job postOcrModel(Path path) {
        return postOcrModel(path.toString());
    }

    public Job postOcrModel(String path) {
        return new Job(MaaLibrary.framework().MaaResourcePostOcrModel(handle, path), this::status, this::waitFor);
    }

    public Job postPipeline(Path path) {
        return postPipeline(path.toString());
    }

    public Job postPipeline(String path) {
        return new Job(MaaLibrary.framework().MaaResourcePostPipeline(handle, path), this::status, this::waitFor);
    }

    public Job postImage(Path path) {
        return postImage(path.toString());
    }

    public Job postImage(String path) {
        return new Job(MaaLibrary.framework().MaaResourcePostImage(handle, path), this::status, this::waitFor);
    }

    /** Overrides the pipeline using a raw JSON object string. */
    public boolean overridePipeline(String pipelineOverride) {
        return MaaStringBuffer.toBoolean(
                MaaLibrary.framework()
                        .MaaResourceOverridePipeline(
                                handle, MaaJson.objectJsonOrEmpty(pipelineOverride)));
    }

    public boolean overridePipeline(Map<String, Object> pipelineOverride) {
        return overridePipeline(
                MaaJson.write(pipelineOverride == null ? Map.of() : pipelineOverride));
    }

    /**
     * Overrides a node's {@code next} list, creating the node when it does not already exist.
     * Items may be raw strings with {@code [JumpBack]}/{@code [Anchor]} prefixes or typed
     * {@link io.github.craun718.maafw.pipeline.JNodeAttr} values.
     *
     * @return {@code true} when the override was accepted
     */
    public boolean overrideNext(String name, List<?> nextList) {
        try (MaaStringListBuffer buffer = new MaaStringListBuffer()) {
            buffer.set(MaaNextItems.names(nextList));
            return MaaStringBuffer.toBoolean(
                    MaaLibrary.framework().MaaResourceOverrideNext(handle, name, buffer.handle()));
        }
    }

    public boolean overrideImage(String imageName, MaaImage image) {
        try (MaaImageBuffer buffer = new MaaImageBuffer()) {
            buffer.set(image);
            return MaaStringBuffer.toBoolean(
                    MaaLibrary.framework().MaaResourceOverrideImage(handle, imageName, buffer.handle()));
        }
    }

    /** Returns the current pipeline node definition, or {@code null} if the node does not exist. */
    public Map<String, Object> getNodeData(String name) {
        try (MaaStringBuffer buffer = new MaaStringBuffer()) {
            if (!MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaResourceGetNodeData(handle, name, buffer.handle()))) {
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
            if (!MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaResourceGetNodeData(handle, name, buffer.handle()))) {
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

    /** Returns the default recognition parameter JSON for a native algorithm name. */
    public Map<String, Object> getDefaultRecognitionParam(String recoType) {
        try (MaaStringBuffer buffer = new MaaStringBuffer()) {
            if (!MaaStringBuffer.toBoolean(
                    MaaLibrary.framework().MaaResourceGetDefaultRecognitionParam(handle, recoType, buffer.handle()))) {
                return Map.of();
            }
            String json = buffer.get();
            return json == null || json.isBlank() ? Map.of() : MaaJson.parseObject(json);
        }
    }

    /** Returns the default action parameter JSON for a native action name. */
    public Map<String, Object> getDefaultActionParam(String actionType) {
        try (MaaStringBuffer buffer = new MaaStringBuffer()) {
            if (!MaaStringBuffer.toBoolean(
                    MaaLibrary.framework().MaaResourceGetDefaultActionParam(handle, actionType, buffer.handle()))) {
                return Map.of();
            }
            String json = buffer.get();
            return json == null || json.isBlank() ? Map.of() : MaaJson.parseObject(json);
        }
    }

    /** Returns typed default parameters for a recognition type, or {@code null} if unavailable. */
    public JRecognitionParam getDefaultRecognitionParam(JRecognitionType recoType) {
        Objects.requireNonNull(recoType, "recoType");
        Map<String, Object> data = getDefaultRecognitionParam(recoType.nativeName());
        if (data.isEmpty()) {
            return null;
        }
        return JPipelineParser.parseRecognitionParam(recoType, data);
    }

    /** Returns typed default parameters for an action type, or {@code null} if unavailable. */
    public JActionParam getDefaultActionParam(JActionType actionType) {
        Objects.requireNonNull(actionType, "actionType");
        Map<String, Object> data = getDefaultActionParam(actionType.nativeName());
        if (data.isEmpty()) {
            return null;
        }
        return JPipelineParser.parseActionParam(actionType, data);
    }

    public boolean loaded() {
        return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaResourceLoaded(handle));
    }

    public boolean clear() {
        return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaResourceClear(handle));
    }

    public boolean useCpu() {
        return setInference(MaaDef.INFERENCE_EXECUTION_PROVIDER_CPU, MaaDef.INFERENCE_DEVICE_CPU);
    }

    public boolean useDirectml() {
        return useDirectml(MaaDef.INFERENCE_DEVICE_AUTO);
    }

    public boolean useDirectml(int deviceId) {
        return setInference(MaaDef.INFERENCE_EXECUTION_PROVIDER_DIRECT_ML, deviceId);
    }

    public boolean useCoreml() {
        return useCoreml(MaaDef.INFERENCE_DEVICE_AUTO);
    }

    public boolean useCoreml(int coremlFlag) {
        return setInference(MaaDef.INFERENCE_EXECUTION_PROVIDER_CORE_ML, coremlFlag);
    }

    public boolean useAutoEp() {
        return setInference(MaaDef.INFERENCE_EXECUTION_PROVIDER_AUTO, MaaDef.INFERENCE_DEVICE_AUTO);
    }

    /** Deprecated alias for {@link #useDirectml()}; returns false for negative GPU ids. */
    @Deprecated
    public boolean setGpu(int gpuId) {
        return gpuId >= 0 && useDirectml(gpuId);
    }

    @Deprecated
    public boolean setCpu() {
        return useCpu();
    }

    @Deprecated
    public boolean setAutoDevice() {
        return useAutoEp();
    }

    public List<String> nodeList() {
        try (MaaStringListBuffer buffer = new MaaStringListBuffer()) {
            MaaStringBuffer.requireOk(MaaLibrary.framework().MaaResourceGetNodeList(handle, buffer.handle()));
            return buffer.get();
        }
    }

    public List<String> customRecognitionList() {
        try (MaaStringListBuffer buffer = new MaaStringListBuffer()) {
            MaaStringBuffer.requireOk(MaaLibrary.framework().MaaResourceGetCustomRecognitionList(handle, buffer.handle()));
            return buffer.get();
        }
    }

    public List<String> customActionList() {
        try (MaaStringListBuffer buffer = new MaaStringListBuffer()) {
            MaaStringBuffer.requireOk(MaaLibrary.framework().MaaResourceGetCustomActionList(handle, buffer.handle()));
            return buffer.get();
        }
    }

    public boolean registerCustomRecognition(String name, CustomRecognition recognition) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(recognition, "recognition");
        customRecognitions.put(name, recognition);
        return MaaStringBuffer.toBoolean(MaaLibrary.framework()
                .MaaResourceRegisterCustomRecognition(handle, name, recognition.callback(), null));
    }

    /** Registers a recognizer created by {@code factory}, matching the factory pattern used by bindings with registration decorators. */
    public boolean registerCustomRecognition(String name, Supplier<? extends CustomRecognition> factory) {
        Objects.requireNonNull(factory, "factory");
        return registerCustomRecognition(name, factory.get());
    }

    /** Registers a recognizer instantiated from {@code recognitionClass}, matching class-based decorator bindings. */
    public boolean registerCustomRecognition(
            String name, Class<? extends CustomRecognition> recognitionClass) {
        Objects.requireNonNull(recognitionClass, "recognitionClass");
        return registerCustomRecognition(
                name, CustomRegistrations.newInstance(recognitionClass, "custom recognition"));
    }

    public boolean unregisterCustomRecognition(String name) {
        customRecognitions.remove(name);
        return MaaStringBuffer.toBoolean(
                MaaLibrary.framework().MaaResourceUnregisterCustomRecognition(handle, name));
    }

    public boolean clearCustomRecognition() {
        customRecognitions.clear();
        return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaResourceClearCustomRecognition(handle));
    }

    public boolean registerCustomAction(String name, CustomAction action) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(action, "action");
        customActions.put(name, action);
        return MaaStringBuffer.toBoolean(
                MaaLibrary.framework().MaaResourceRegisterCustomAction(handle, name, action.callback(), null));
    }

    /** Registers an action created by {@code factory}, matching the factory pattern used by bindings with registration decorators. */
    public boolean registerCustomAction(String name, Supplier<? extends CustomAction> factory) {
        Objects.requireNonNull(factory, "factory");
        return registerCustomAction(name, factory.get());
    }

    /** Registers an action instantiated from {@code actionClass}, matching class-based decorator bindings. */
    public boolean registerCustomAction(String name, Class<? extends CustomAction> actionClass) {
        Objects.requireNonNull(actionClass, "actionClass");
        return registerCustomAction(
                name, CustomRegistrations.newInstance(actionClass, "custom action"));
    }

    public boolean unregisterCustomAction(String name) {
        customActions.remove(name);
        return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaResourceUnregisterCustomAction(handle, name));
    }

    public boolean clearCustomAction() {
        customActions.clear();
        return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaResourceClearCustomAction(handle));
    }

    public String hash() {
        try (MaaStringBuffer buffer = new MaaStringBuffer()) {
            MaaStringBuffer.requireOk(MaaLibrary.framework().MaaResourceGetHash(handle, buffer.handle()));
            return buffer.getUtf8();
        }
    }

    public Long addSink(ResourceEventSink sink) {
        long sinkId = MaaLibrary.framework().MaaResourceAddSink(handle, sink.callback(), null);
        if (sinkId == MaaDef.INVALID_ID) {
            return null;
        }
        sinks.put(sinkId, sink);
        return sinkId;
    }

    public void removeSink(long sinkId) {
        MaaLibrary.framework().MaaResourceRemoveSink(handle, sinkId);
        sinks.remove(sinkId);
    }

    public void clearSinks() {
        MaaLibrary.framework().MaaResourceClearSinks(handle);
        sinks.clear();
    }

    @Override
    public void close() {
        if (owned && handle != Pointer.NULL) {
            MaaLibrary.framework().MaaResourceDestroy(handle);
        }
    }

    private MaaDef.Status status(long id) {
        return MaaDef.Status.of(MaaLibrary.framework().MaaResourceStatus(handle, id));
    }

    private void waitFor(long id) {
        MaaLibrary.framework().MaaResourceWait(handle, id);
    }

    public boolean setInference(int executionProvider, int deviceId) {
        try (Memory executionProviderValue = intMemory(executionProvider);
                Memory deviceValue = intMemory(deviceId)) {
            boolean providerOk = MaaStringBuffer.toBoolean(MaaLibrary.framework()
                    .MaaResourceSetOption(
                            handle,
                            MaaDef.ResOption.INFERENCE_EXECUTION_PROVIDER.code(),
                            executionProviderValue,
                            Integer.BYTES));
            boolean deviceOk = MaaStringBuffer.toBoolean(MaaLibrary.framework()
                    .MaaResourceSetOption(
                            handle,
                            MaaDef.ResOption.INFERENCE_DEVICE.code(),
                            deviceValue,
                            Integer.BYTES));
            return providerOk && deviceOk;
        }
    }

    private static Memory intMemory(int value) {
        Memory memory = new Memory(Integer.BYTES);
        memory.setInt(0, value);
        return memory;
    }
}
