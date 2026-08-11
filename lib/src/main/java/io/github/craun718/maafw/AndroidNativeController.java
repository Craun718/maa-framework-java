package io.github.craun718.maafw;

import java.util.Map;

/** Android native controller. */
public class AndroidNativeController extends Controller {

    public AndroidNativeController(Map<String, Object> config) {
        super();
        setHandle(MaaLibrary.framework()
                .MaaAndroidNativeControllerCreate(MaaJson.write(config == null ? Map.of() : config)));
    }
}
