package top.natsuu.maafw;

import java.util.Map;

/** Configurable Linux controller. */
public class LinuxController extends Controller {

    public LinuxController(Map<String, Object> config) {
        super();
        setHandle(MaaLibrary.framework().MaaLinuxControllerCreate(MaaJson.write(config == null ? Map.of() : config)));
    }
}
