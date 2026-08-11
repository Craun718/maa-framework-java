package io.github.craun718.maafw;

import com.sun.jna.Pointer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** Owned wrapper for a native MaaStringBuffer handle. */
public final class MaaStringBuffer implements AutoCloseable {

    private final Pointer handle;
    private final boolean owned;

    public MaaStringBuffer() {
        this(MaaLibrary.framework().MaaStringBufferCreate(), true);
        if (handle == null) {
            throw new IllegalStateException("Failed to create MaaStringBuffer");
        }
    }

    public MaaStringBuffer(Pointer handle) {
        this(handle, false);
    }

    private MaaStringBuffer(Pointer handle, boolean owned) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.owned = owned;
    }

    public Pointer handle() {
        return handle;
    }

    public String get() {
        return MaaLibrary.framework().MaaStringBufferGet(handle);
    }

    public String getUtf8() {
        String value = get();
        return value == null ? "" : value;
    }

    public void set(String value) {
        setBytes(value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8));
    }

    public void setBytes(byte[] value) {
        Objects.requireNonNull(value, "value");
        requireOk(MaaLibrary.framework().MaaStringBufferSetEx(handle, value, value.length));
    }

    public boolean empty() {
        return toBoolean(MaaLibrary.framework().MaaStringBufferIsEmpty(handle));
    }

    public long size() {
        return MaaLibrary.framework().MaaStringBufferSize(handle);
    }

    public void clear() {
        requireOk(MaaLibrary.framework().MaaStringBufferClear(handle));
    }

    @Override
    public void close() {
        if (owned && handle != Pointer.NULL) {
            MaaLibrary.framework().MaaStringBufferDestroy(handle);
        }
    }

    static boolean toBoolean(byte value) {
        return value != 0;
    }

    static void requireOk(byte result) {
        if (result == 0) {
            throw new IllegalStateException("MaaFramework native call returned false");
        }
    }
}
