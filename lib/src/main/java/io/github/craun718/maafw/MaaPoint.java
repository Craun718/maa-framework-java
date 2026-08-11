package io.github.craun718.maafw;

import java.util.Objects;

/** Two-dimensional point data exchanged with MaaFramework. */
public final class MaaPoint {

    private final int x;
    private final int y;

    public MaaPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static MaaPoint of(int x, int y) {
        return new MaaPoint(x, y);
    }

    public static MaaPoint from(int[] values) {
        Objects.requireNonNull(values, "values");
        if (values.length != 2) {
            throw new IllegalArgumentException("Point array must contain 2 values");
        }
        return new MaaPoint(values[0], values[1]);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int[] toArray() {
        return new int[] {x, y};
    }

    @Override
    public String toString() {
        return "MaaPoint(" + x + ", " + y + ")";
    }
}
