package io.github.craun718.maafw;

/** Deprecated wlroots controller; prefer {@link LinuxController}. */
@Deprecated
public class WlRootsController extends Controller {

    public WlRootsController(String wlrSocketPath) {
        this(wlrSocketPath, false);
    }

    public WlRootsController(String wlrSocketPath, boolean useWin32VkCode) {
        super();
        setHandle(MaaLibrary.framework()
                .MaaWlRootsControllerCreate(wlrSocketPath, (byte) (useWin32VkCode ? 1 : 0)));
    }
}
