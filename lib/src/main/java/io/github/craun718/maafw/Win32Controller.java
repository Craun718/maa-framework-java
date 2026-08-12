package io.github.craun718.maafw;

import com.sun.jna.Pointer;

/** Win32 controller. */
public class Win32Controller extends Controller {

    public Win32Controller(long windowHandle) {
        this(
                windowHandle == 0 ? null : new Pointer(windowHandle),
                MaaDef.WIN32_SCREENCAP_BACKGROUND,
                MaaDef.WIN32_INPUT_SEIZE,
                MaaDef.WIN32_INPUT_SEIZE);
    }

    public Win32Controller(long windowHandle, long screencapMethod, long mouseMethod, long keyboardMethod) {
        this(
                windowHandle == 0 ? null : new Pointer(windowHandle),
                screencapMethod,
                mouseMethod,
                keyboardMethod);
    }

    public Win32Controller(Pointer windowHandle) {
        this(
                windowHandle,
                MaaDef.WIN32_SCREENCAP_BACKGROUND,
                MaaDef.WIN32_INPUT_SEIZE,
                MaaDef.WIN32_INPUT_SEIZE);
    }

    public Win32Controller(Pointer windowHandle, long screencapMethod, long mouseMethod, long keyboardMethod) {
        super();
        setHandle(MaaLibrary.framework()
                .MaaWin32ControllerCreate(windowHandle, screencapMethod, mouseMethod, keyboardMethod));
    }
}
