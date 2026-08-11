package io.github.craun718.maafw;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import java.util.Objects;

/** Owned wrapper for a native MaaImageBuffer handle. */
public final class MaaImageBuffer implements AutoCloseable {

    private final Pointer handle;
    private final boolean owned;

    public MaaImageBuffer() {
        this(MaaLibrary.framework().MaaImageBufferCreate(), true);
        if (handle == null) {
            throw new IllegalStateException("Failed to create MaaImageBuffer");
        }
    }

    public MaaImageBuffer(Pointer handle) {
        this(handle, false);
    }

    private MaaImageBuffer(Pointer handle, boolean owned) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.owned = owned;
    }

    public Pointer handle() {
        return handle;
    }

    public MaaImage get() {
        Pointer raw = MaaLibrary.framework().MaaImageBufferGetRawData(handle);
        if (raw == null) {
            return MaaImage.empty();
        }
        int width = MaaLibrary.framework().MaaImageBufferWidth(handle);
        int height = MaaLibrary.framework().MaaImageBufferHeight(handle);
        int channels = MaaLibrary.framework().MaaImageBufferChannels(handle);
        int type = MaaLibrary.framework().MaaImageBufferType(handle);
        long bytes = (long) width * height * channels;
        byte[] data = raw.getByteArray(0, Math.toIntExact(Math.min(bytes, Integer.MAX_VALUE)));
        return new MaaImage(data, width, height, channels, type);
    }

    public byte[] getEncoded() {
        Pointer encoded = MaaLibrary.framework().MaaImageBufferGetEncoded(handle);
        long size = MaaLibrary.framework().MaaImageBufferGetEncodedSize(handle);
        if (encoded == null || size <= 0) {
            return new byte[0];
        }
        return encoded.getByteArray(0, Math.toIntExact(Math.min(size, Integer.MAX_VALUE)));
    }

    public void set(MaaImage image) {
        Objects.requireNonNull(image, "image");
        byte[] data = image.data();
        try (Memory memory = new Memory(Math.max(data.length, 1))) {
            memory.write(0, data, 0, data.length);
            MaaStringBuffer.requireOk(MaaLibrary.framework()
                    .MaaImageBufferSetRawData(handle, memory, image.width(), image.height(), image.type()));
        }
    }

    public void setEncoded(byte[] data) {
        Objects.requireNonNull(data, "data");
        try (Memory memory = new Memory(Math.max(data.length, 1))) {
            memory.write(0, data, 0, data.length);
            MaaStringBuffer.requireOk(MaaLibrary.framework().MaaImageBufferSetEncoded(handle, memory, data.length));
        }
    }

    public boolean empty() {
        return MaaStringBuffer.toBoolean(MaaLibrary.framework().MaaImageBufferIsEmpty(handle));
    }

    public void resize(int width, int height) {
        MaaStringBuffer.requireOk(MaaLibrary.framework().MaaImageBufferResize(handle, width, height));
    }

    public void clear() {
        MaaStringBuffer.requireOk(MaaLibrary.framework().MaaImageBufferClear(handle));
    }

    @Override
    public void close() {
        if (owned && handle != Pointer.NULL) {
            MaaLibrary.framework().MaaImageBufferDestroy(handle);
        }
    }
}
