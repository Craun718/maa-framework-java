package io.github.craun718.maafw;

import com.sun.jna.Pointer;
import java.util.Objects;

/** Desktop window information returned by {@link Toolkit#findDesktopWindows()}. */
public final class DesktopWindow {

    private final Pointer handle;
    private final String className;
    private final String windowName;

    public DesktopWindow(Pointer handle, String className, String windowName) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.className = className;
        this.windowName = windowName;
    }

    /** Native HWND/handle, usable directly by Win32 controller constructors. */
    public Pointer handle() {
        return handle;
    }

    public long handleValue() {
        return Pointer.nativeValue(handle);
    }

    public String className() {
        return className;
    }

    public String windowName() {
        return windowName;
    }

    @Override
    public String toString() {
        return "DesktopWindow(" + windowName + ", " + className + ")";
    }
}
