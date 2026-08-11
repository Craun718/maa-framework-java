package io.github.craun718.maafw;

import com.sun.jna.Pointer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Owned wrapper for a native MaaStringListBuffer handle. */
public final class MaaStringListBuffer implements AutoCloseable {

    private final Pointer handle;
    private final boolean owned;

    public MaaStringListBuffer() {
        this(MaaLibrary.framework().MaaStringListBufferCreate(), true);
        if (handle == null) {
            throw new IllegalStateException("Failed to create MaaStringListBuffer");
        }
    }

    public MaaStringListBuffer(Pointer handle) {
        this(handle, false);
    }

    private MaaStringListBuffer(Pointer handle, boolean owned) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.owned = owned;
    }

    public Pointer handle() {
        return handle;
    }

    public List<String> get() {
        long size = MaaLibrary.framework().MaaStringListBufferSize(handle);
        List<String> values = new ArrayList<>((int) Math.min(size, Integer.MAX_VALUE));
        for (long i = 0; i < size; i++) {
            try (MaaStringBuffer item = new MaaStringBuffer(MaaLibrary.framework().MaaStringListBufferAt(handle, i))) {
                values.add(item.getUtf8());
            }
        }
        return List.copyOf(values);
    }

    public void set(List<String> values) {
        Objects.requireNonNull(values, "values");
        clear();
        for (String value : values) {
            append(value);
        }
    }

    public void append(String value) {
        try (MaaStringBuffer buffer = new MaaStringBuffer()) {
            buffer.set(value);
            MaaStringBuffer.requireOk(MaaLibrary.framework().MaaStringListBufferAppend(handle, buffer.handle()));
        }
    }

    public void remove(long index) {
        MaaStringBuffer.requireOk(MaaLibrary.framework().MaaStringListBufferRemove(handle, index));
    }

    public void clear() {
        MaaStringBuffer.requireOk(MaaLibrary.framework().MaaStringListBufferClear(handle));
    }

    @Override
    public void close() {
        if (owned && handle != Pointer.NULL) {
            MaaLibrary.framework().MaaStringListBufferDestroy(handle);
        }
    }
}
