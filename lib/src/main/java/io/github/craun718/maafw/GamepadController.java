package io.github.craun718.maafw;

import com.sun.jna.Pointer;

/** Virtual gamepad controller for Windows. */
public class GamepadController extends Controller {

    public GamepadController(long windowHandle) {
        this(windowHandle, MaaDef.GAMEPAD_XBOX360, MaaDef.WIN32_SCREENCAP_FRAME_POOL);
    }

    public GamepadController(long windowHandle, long gamepadType, long screencapMethod) {
        this(windowHandle == 0 ? null : new Pointer(windowHandle), gamepadType, screencapMethod);
    }

    public GamepadController(Pointer windowHandle, long gamepadType, long screencapMethod) {
        super();
        setHandle(MaaLibrary.framework().MaaGamepadControllerCreate(windowHandle, gamepadType, screencapMethod));
    }
}
