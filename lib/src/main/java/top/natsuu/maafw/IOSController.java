package top.natsuu.maafw;

import java.util.Map;

/** Controller for a physical iOS device through an XDeviceBridge server. */
public class IOSController extends Controller {

    public IOSController(String udid) {
        this(udid, MaaDef.IOS_SCREENCAP_DEFAULT, MaaDef.IOS_CONNECTION_AUTO, Map.of());
    }

    public IOSController(String udid, long screencapMethods, int connectionMethod, Map<String, Object> config) {
        super();
        if (udid == null || udid.isEmpty() || udid.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("A nonempty iOS device UDID without NUL is required");
        }
        setHandle(MaaLibrary.framework().MaaIOSControllerCreate(udid, screencapMethods, connectionMethod,
                MaaJson.write(config == null ? Map.of() : config)));
    }
}
