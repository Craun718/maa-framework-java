package io.github.craun718.maafw;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/** MaaRect native structure. */
@Structure.FieldOrder({"x", "y", "width", "height"})
public class MaaRect extends Structure {

    public int x;
    public int y;
    public int width;
    public int height;

    public MaaRect() {}

    public MaaRect(Pointer pointer) {
        super(pointer);
        read();
    }

    public MaaRect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static MaaRect from(int[] values) {
        if (values == null || values.length != 4) {
            return new MaaRect();
        }
        return new MaaRect(values[0], values[1], values[2], values[3]);
    }

    public int[] toArray() {
        return new int[] {x, y, width, height};
    }
}
