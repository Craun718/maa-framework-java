package io.github.craun718.maafw;

import com.sun.jna.Pointer;
import java.util.Objects;

/** Owned wrapper for a native MaaRect buffer handle. */
public final class MaaRectBuffer implements AutoCloseable {

    private final Pointer handle;
    private final boolean owned;

    public MaaRectBuffer() {
        this(MaaLibrary.framework().MaaRectCreate(), true);
        if (handle == null) {
            throw new IllegalStateException("Failed to create MaaRectBuffer");
        }
    }

    public MaaRectBuffer(Pointer handle) {
        this(handle, false);
    }

    private MaaRectBuffer(Pointer handle, boolean owned) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.owned = owned;
    }

    public Pointer handle() {
        return handle;
    }

    public MaaRect get() {
        return new MaaRect(MaaLibrary.framework().MaaRectGetX(handle), MaaLibrary.framework().MaaRectGetY(handle),
            MaaLibrary.framework().MaaRectGetW(handle), MaaLibrary.framework().MaaRectGetH(handle));
    }

    public void set(MaaRect rect) {
        Objects.requireNonNull(rect, "rect");
        MaaStringBuffer.requireOk(MaaLibrary.framework().MaaRectSet(handle, rect.x(), rect.y(), rect.width(), rect.height()));
    }

    @Override
    public void close() {
        if (owned && handle != Pointer.NULL) {
            MaaLibrary.framework().MaaRectDestroy(handle);
        }
    }
}
