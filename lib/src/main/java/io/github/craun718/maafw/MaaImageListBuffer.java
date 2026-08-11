package io.github.craun718.maafw;

import com.sun.jna.Pointer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Owned wrapper for a native MaaImageListBuffer handle. */
public final class MaaImageListBuffer implements AutoCloseable {

    private final Pointer handle;
    private final boolean owned;

    public MaaImageListBuffer() {
        this(MaaLibrary.framework().MaaImageListBufferCreate(), true);
        if (handle == null) {
            throw new IllegalStateException("Failed to create MaaImageListBuffer");
        }
    }

    public MaaImageListBuffer(Pointer handle) {
        this(handle, false);
    }

    private MaaImageListBuffer(Pointer handle, boolean owned) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.owned = owned;
    }

    public Pointer handle() {
        return handle;
    }

    public List<MaaImage> get() {
        long size = MaaLibrary.framework().MaaImageListBufferSize(handle);
        List<MaaImage> images = new ArrayList<>((int) Math.min(size, Integer.MAX_VALUE));
        for (long i = 0; i < size; i++) {
            try (MaaImageBuffer item = new MaaImageBuffer(MaaLibrary.framework().MaaImageListBufferAt(handle, i))) {
                images.add(item.get());
            }
        }
        return List.copyOf(images);
    }

    public void set(List<MaaImage> images) {
        Objects.requireNonNull(images, "images");
        clear();
        for (MaaImage image : images) {
            append(image);
        }
    }

    public void append(MaaImage image) {
        try (MaaImageBuffer buffer = new MaaImageBuffer()) {
            buffer.set(image);
            MaaStringBuffer.requireOk(MaaLibrary.framework().MaaImageListBufferAppend(handle, buffer.handle()));
        }
    }

    public void remove(long index) {
        MaaStringBuffer.requireOk(MaaLibrary.framework().MaaImageListBufferRemove(handle, index));
    }

    public void clear() {
        MaaStringBuffer.requireOk(MaaLibrary.framework().MaaImageListBufferClear(handle));
    }

    @Override
    public void close() {
        if (owned && handle != Pointer.NULL) {
            MaaLibrary.framework().MaaImageListBufferDestroy(handle);
        }
    }
}
