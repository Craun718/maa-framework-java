package io.github.craun718.maafw;

/** macOS controller. */
public class MacOSController extends Controller {

    public MacOSController(int windowId) {
        this(windowId, MaaDef.MACOS_SCREENCAP_SCREEN_CAPTURE_KIT, MaaDef.MACOS_INPUT_GLOBAL_EVENT);
    }

    public MacOSController(int windowId, long screencapMethod, long inputMethod) {
        super();
        setHandle(MaaLibrary.framework().MaaMacOSControllerCreate(windowId, screencapMethod, inputMethod));
    }
}
