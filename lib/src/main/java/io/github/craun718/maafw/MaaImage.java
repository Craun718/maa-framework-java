package io.github.craun718.maafw;

import java.util.Arrays;
import java.util.Objects;

/** BGR image data compatible with MaaFramework's OpenCV raw image buffers. */
public final class MaaImage {

    public static final int TYPE_8UC3 = 16;

    private final byte[] data;
    private final int width;
    private final int height;
    private final int channels;
    private final int type;

    public MaaImage(byte[] data, int width, int height, int channels, int type) {
        this.data = Objects.requireNonNull(data, "data").clone();
        this.width = width;
        this.height = height;
        this.channels = channels;
        this.type = type;
    }

    public static MaaImage empty() {
        return new MaaImage(new byte[0], 0, 0, 0, 0);
    }

    public byte[] data() {
        return data.clone();
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int channels() {
        return channels;
    }

    public int type() {
        return type;
    }

    public boolean isEmpty() {
        return data.length == 0 || width == 0 || height == 0;
    }

    @Override
    public String toString() {
        return "MaaImage(" + width + "x" + height + ", channels=" + channels + ", type=" + type + ", bytes="
                + data.length + ")";
    }
}
