package io.github.craun718.maafw;

/** Deprecated KWin controller; prefer {@link LinuxController}. */
@Deprecated
public class KWinController extends Controller {

    public KWinController(String deviceNode, int screenWidth, int screenHeight) {
        this(deviceNode, screenWidth, screenHeight, false);
    }

    public KWinController(String deviceNode, int screenWidth, int screenHeight, boolean useWin32VkCode) {
        super();
        setHandle(MaaLibrary.framework()
                .MaaKWinControllerCreate(
                        deviceNode, screenWidth, screenHeight, (byte) (useWin32VkCode ? 1 : 0)));
    }
}
