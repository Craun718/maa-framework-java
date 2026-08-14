package io.github.craun718.maafw;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** High-level controller wrapper equivalent to the Python binding's {@code Controller}. */
public class Controller implements AutoCloseable {

    protected Pointer handle;
    private final boolean owned;
    private final Map<Long, ControllerEventSink> sinks = new HashMap<>();

    protected Controller() {
        this.handle = null;
        this.owned = true;
    }

    /** Wraps an existing native handle without taking ownership. */
    public Controller(Pointer handle) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.owned = false;
    }

    Controller(Pointer handle, boolean owned) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.owned = owned;
    }

    public Pointer handle() {
        return handle;
    }

    public Job postConnection() {
        return job(MaaLibrary.framework().MaaControllerPostConnection(handle));
    }

    public Job postClick(int x, int y) {
        return postClick(x, y, 0, 1);
    }

    public Job postClick(int x, int y, int contact, int pressure) {
        return job(MaaLibrary.framework().MaaControllerPostClickV2(handle, x, y, contact, pressure));
    }

    public Job postSwipe(int x1, int y1, int x2, int y2, int duration) {
        return postSwipe(x1, y1, x2, y2, duration, 0, 1);
    }

    public Job postSwipe(int x1, int y1, int x2, int y2, int duration, int contact, int pressure) {
        return job(MaaLibrary.framework().MaaControllerPostSwipeV2(handle, x1, y1, x2, y2, duration, contact, pressure));
    }

    @Deprecated
    public Job postPressKey(int key) {
        return postClickKey(key);
    }

    public Job postClickKey(int key) {
        return job(MaaLibrary.framework().MaaControllerPostClickKey(handle, key));
    }

    public Job postKeyDown(int key) {
        return job(MaaLibrary.framework().MaaControllerPostKeyDown(handle, key));
    }

    public Job postKeyUp(int key) {
        return job(MaaLibrary.framework().MaaControllerPostKeyUp(handle, key));
    }

    public Job postInputText(String text) {
        return job(MaaLibrary.framework().MaaControllerPostInputText(handle, text));
    }

    public Job postStartApp(String intent) {
        return job(MaaLibrary.framework().MaaControllerPostStartApp(handle, intent));
    }

    public Job postStopApp(String intent) {
        return job(MaaLibrary.framework().MaaControllerPostStopApp(handle, intent));
    }

    public Job postTouchDown(int x, int y) {
        return postTouchDown(x, y, 0, 1);
    }

    public Job postTouchDown(int x, int y, int contact, int pressure) {
        return job(MaaLibrary.framework().MaaControllerPostTouchDown(handle, contact, x, y, pressure));
    }

    public Job postTouchMove(int x, int y) {
        return postTouchMove(x, y, 0, 1);
    }

    public Job postTouchMove(int x, int y, int contact, int pressure) {
        return job(MaaLibrary.framework().MaaControllerPostTouchMove(handle, contact, x, y, pressure));
    }

    public Job postTouchUp() {
        return postTouchUp(0);
    }

    public Job postTouchUp(int contact) {
        return job(MaaLibrary.framework().MaaControllerPostTouchUp(handle, contact));
    }

    public Job postRelativeMove(int dx, int dy) {
        return job(MaaLibrary.framework().MaaControllerPostRelativeMove(handle, dx, dy));
    }

    public Job postScroll(int dx, int dy) {
        return job(MaaLibrary.framework().MaaControllerPostScroll(handle, dx, dy));
    }

    public JobWithResult<MaaImage> postScreencap() {
        long id = MaaLibrary.framework().MaaControllerPostScreencap(handle);
        return new JobWithResult<>(id, this::status, this::waitFor, ignored -> cachedImage());
    }

    public JobWithResult<String> postShell(String command) {
        return postShell(command, 20_000L);
    }

    public JobWithResult<String> postShell(String command, long timeout) {
        long id = MaaLibrary.framework().MaaControllerPostShell(handle, command, timeout);
        return new JobWithResult<>(id, this::status, this::waitFor, ignored -> shellOutput());
    }

    public Job postInactive() {
        return job(MaaLibrary.framework().MaaControllerPostInactive(handle));
    }

    public MaaImage cachedImage() {
        try (MaaImageBuffer buffer = new MaaImageBuffer()) {
            MaaStringBuffer.requireOk(MaaLibrary.framework().MaaControllerCachedImage(handle, buffer.handle()));
            return buffer.get();
        }
    }

    public String shellOutput() {
        try (MaaStringBuffer buffer = new MaaStringBuffer()) {
            MaaStringBuffer.requireOk(MaaLibrary.framework().MaaControllerGetShellOutput(handle, buffer.handle()));
            return buffer.getUtf8();
        }
    }

    public boolean connected() {
        return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaControllerConnected(handle));
    }

    public String uuid() {
        try (MaaStringBuffer buffer = new MaaStringBuffer()) {
            MaaStringBuffer.requireOk(MaaLibrary.framework().MaaControllerGetUuid(handle, buffer.handle()));
            return buffer.getUtf8();
        }
    }

    public Map<String, Object> info() {
        try (MaaStringBuffer buffer = new MaaStringBuffer()) {
            MaaStringBuffer.requireOk(MaaLibrary.framework().MaaControllerGetInfo(handle, buffer.handle()));
            return MaaJson.parseObject(buffer.getUtf8());
        }
    }

    public MaaPoint resolution() {
        IntByReference width = new IntByReference();
        IntByReference height = new IntByReference();
        if (!MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaControllerGetResolution(handle, width, height))) {
            return new MaaPoint(0, 0);
        }
        return new MaaPoint(width.getValue(), height.getValue());
    }

    public boolean setMouseLockFollow(boolean enabled) {
        return setBooleanOption(MaaDef.CtrlOption.MOUSE_LOCK_FOLLOW, enabled);
    }

    public boolean setBackgroundManagedKeys(List<Integer> keys) {
        try (Memory memory = new Memory(Math.max(keys.size() * Integer.BYTES, 1))) {
            for (int i = 0; i < keys.size(); i++) {
                memory.setInt((long) i * Integer.BYTES, keys.get(i));
            }
            return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaControllerSetOption(handle,
                    MaaDef.CtrlOption.BACKGROUND_MANAGED_KEYS.code(), memory, (long) keys.size() * Integer.BYTES));
        }
    }

    public boolean setScreenshotTargetLongSide(int longSide) {
        return setIntOption(MaaDef.CtrlOption.SCREENSHOT_TARGET_LONG_SIDE, longSide);
    }

    public boolean setScreenshotTargetShortSide(int shortSide) {
        return setIntOption(MaaDef.CtrlOption.SCREENSHOT_TARGET_SHORT_SIDE, shortSide);
    }

    public boolean setScreenshotUseRawSize(boolean enable) {
        return setBooleanOption(MaaDef.CtrlOption.SCREENSHOT_USE_RAW_SIZE, enable);
    }

    public boolean setScreenshotResizeMethod(int method) {
        return setIntOption(MaaDef.CtrlOption.SCREENSHOT_RESIZE_METHOD, method);
    }

    public Long addSink(ControllerEventSink sink) {
        long sinkId = MaaLibrary.framework().MaaControllerAddSink(handle, sink.callback(), null);
        if (sinkId == MaaDef.INVALID_ID) {
            return null;
        }
        sinks.put(sinkId, sink);
        return sinkId;
    }

    public void removeSink(long sinkId) {
        MaaLibrary.framework().MaaControllerRemoveSink(handle, sinkId);
        sinks.remove(sinkId);
    }

    public void clearSinks() {
        MaaLibrary.framework().MaaControllerClearSinks(handle);
        sinks.clear();
    }

    @Override
    public void close() {
        if (owned && handle != null && handle != Pointer.NULL) {
            MaaLibrary.framework().MaaControllerDestroy(handle);
        }
    }

    protected void checkHandle() {
        if (handle == null || handle == Pointer.NULL) {
            throw new IllegalStateException("Controller is not initialized");
        }
    }

    protected void setHandle(Pointer createdHandle) {
        if (createdHandle == null || createdHandle == Pointer.NULL) {
            throw new IllegalStateException("Failed to create controller");
        }
        this.handle = createdHandle;
    }

    private Job job(long id) {
        return new Job(id, this::status, this::waitFor);
    }

    private MaaDef.Status status(long id) {
        return MaaDef.Status.of(MaaLibrary.framework().MaaControllerStatus(handle, id));
    }

    private void waitFor(long id) {
        MaaLibrary.framework().MaaControllerWait(handle, id);
    }

    private boolean setIntOption(MaaDef.CtrlOption option, int value) {
        try (Memory memory = new Memory(Integer.BYTES)) {
            memory.setInt(0, value);
            return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaControllerSetOption(handle, option.code(), memory, Integer.BYTES));
        }
    }

    private boolean setBooleanOption(MaaDef.CtrlOption option, boolean value) {
        try (Memory memory = new Memory(1)) {
            memory.setByte(0, (byte) (value ? 1 : 0));
            return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaControllerSetOption(handle, option.code(), memory, 1));
        }
    }
}
