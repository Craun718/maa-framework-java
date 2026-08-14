package io.github.craun718.maafw;

import java.util.Objects;

/** Rectangle data exchanged with MaaFramework. */
public final class MaaRect {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public MaaRect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static MaaRect of(int x, int y, int width, int height) {
        return new MaaRect(x, y, width, height);
    }

    public static MaaRect from(int[] values) {
        Objects.requireNonNull(values, "values");
        if (values.length != 4) {
            throw new IllegalArgumentException("Rect array must contain 4 values");
        }
        return new MaaRect(values[0], values[1], values[2], values[3]);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int[] toArray() {
        return new int[] { x, y, width, height };
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MaaRect rect)) {
            return false;
        }
        return x == rect.x && y == rect.y && width == rect.width && height == rect.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }

    @Override
    public String toString() {
        return "MaaRect(" + x + ", " + y + ", " + width + ", " + height + ")";
    }
}
