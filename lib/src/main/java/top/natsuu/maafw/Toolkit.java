package top.natsuu.maafw;

import com.sun.jna.Pointer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Static helpers for device discovery and platform-specific MaaToolkit features. */
public final class Toolkit {

    private Toolkit() {
    }

    public static boolean initOption(String userPath) {
        return initOption(userPath, Map.of());
    }

    public static boolean initOption(String userPath, Map<String, Object> defaultConfig) {
        if (MaaLibrary.isAgentServer()) {
            Object logging = defaultConfig == null ? null : defaultConfig.get("logging");
            String logDir = logging == null || !Boolean.FALSE.equals(logging) ? Path.of(userPath, "debug").toString() : "";
            return Tasker.setLogDir(logDir);
        }
        return MaaStringBuffer.toBoolean(
                MaaLibrary.toolkit().MaaToolkitConfigInitOption(userPath, MaaJson.write(defaultConfig == null ? Map.of() : defaultConfig)));
    }

    public static boolean initOption(Path userPath) {
        return initOption(userPath, Map.of());
    }

    public static boolean initOption(Path userPath, Map<String, Object> defaultConfig) {
        return initOption(userPath.toString(), defaultConfig);
    }

    public static List<AdbDevice> findAdbDevices() {
        return findAdbDevices((String) null);
    }

    public static List<AdbDevice> findAdbDevices(String specifiedAdb) {
        Pointer list = MaaLibrary.toolkit().MaaToolkitAdbDeviceListCreate();
        if (list == null || list == Pointer.NULL) {
            throw new IllegalStateException("Failed to create ADB device list");
        }
        try {
            boolean found;
            if (specifiedAdb == null || specifiedAdb.isBlank()) {
                found = MaaStringBuffer.toBoolean(MaaLibrary.toolkit().MaaToolkitAdbDeviceFind(list));
            } else {
                found = MaaStringBuffer.toBoolean(MaaLibrary.toolkit().MaaToolkitAdbDeviceFindSpecified(specifiedAdb, list));
            }
            if (!found) {
                throw new IllegalStateException(specifiedAdb == null || specifiedAdb.isBlank() ? "Failed to find ADB devices"
                        : "Failed to find ADB devices with adb: " + specifiedAdb);
            }

            long size = MaaLibrary.toolkit().MaaToolkitAdbDeviceListSize(list);
            List<AdbDevice> devices = new ArrayList<>((int) Math.min(size, Integer.MAX_VALUE));
            for (long i = 0; i < size; i++) {
                Pointer device = MaaLibrary.toolkit().MaaToolkitAdbDeviceListAt(list, i);
                devices.add(new AdbDevice(stringOrEmpty(MaaLibrary.toolkit().MaaToolkitAdbDeviceGetName(device)),
                    Path.of(stringOrEmpty(MaaLibrary.toolkit().MaaToolkitAdbDeviceGetAdbPath(device))),
                    stringOrEmpty(MaaLibrary.toolkit().MaaToolkitAdbDeviceGetAddress(device)),
                    MaaLibrary.toolkit().MaaToolkitAdbDeviceGetScreencapMethods(device),
                    MaaLibrary.toolkit().MaaToolkitAdbDeviceGetInputMethods(device),
                    MaaJson.parseObject(stringOrEmpty(MaaLibrary.toolkit().MaaToolkitAdbDeviceGetConfig(device)))));
            }
            return List.copyOf(devices);
        } finally {
            MaaLibrary.toolkit().MaaToolkitAdbDeviceListDestroy(list);
        }
    }

    public static List<AdbDevice> findAdbDevices(Path specifiedAdb) {
        return findAdbDevices(specifiedAdb.toString());
    }

    public static List<DesktopWindow> findDesktopWindows() {
        Pointer list = MaaLibrary.toolkit().MaaToolkitDesktopWindowListCreate();
        if (list == null || list == Pointer.NULL) {
            throw new IllegalStateException("Failed to create desktop window list");
        }
        try {
            if (!MaaStringBuffer.toBoolean(MaaLibrary.toolkit().MaaToolkitDesktopWindowFindAll(list))) {
                throw new IllegalStateException("Failed to find desktop windows");
            }
            long size = MaaLibrary.toolkit().MaaToolkitDesktopWindowListSize(list);
            List<DesktopWindow> windows = new ArrayList<>((int) Math.min(size, Integer.MAX_VALUE));
            for (long i = 0; i < size; i++) {
                Pointer window = MaaLibrary.toolkit().MaaToolkitDesktopWindowListAt(list, i);
                Pointer windowHandle = MaaLibrary.toolkit().MaaToolkitDesktopWindowGetHandle(window);
                if (windowHandle == null) {
                    windowHandle = Pointer.NULL;
                }
                windows.add(new DesktopWindow(windowHandle, stringOrEmpty(MaaLibrary.toolkit().MaaToolkitDesktopWindowGetClassName(window)),
                    stringOrEmpty(MaaLibrary.toolkit().MaaToolkitDesktopWindowGetWindowName(window))));
            }
            return List.copyOf(windows);
        } finally {
            MaaLibrary.toolkit().MaaToolkitDesktopWindowListDestroy(list);
        }
    }

    public static List<GamescopeInstance> findGamescopeInstances() {
        Pointer list = MaaLibrary.toolkit().MaaToolkitGamescopeInstanceListCreate();
        if (list == null || list == Pointer.NULL) {
            throw new IllegalStateException("Failed to create gamescope instance list");
        }
        try {
            if (!MaaStringBuffer.toBoolean(MaaLibrary.toolkit().MaaToolkitGamescopeInstanceFindAll(list))) {
                throw new IllegalStateException("Failed to find gamescope instances");
            }
            long size = MaaLibrary.toolkit().MaaToolkitGamescopeInstanceListSize(list);
            List<GamescopeInstance> instances = new ArrayList<>((int) Math.min(size, Integer.MAX_VALUE));
            for (long i = 0; i < size; i++) {
                Pointer instance = MaaLibrary.toolkit().MaaToolkitGamescopeInstanceListAt(list, i);
                instances.add(new GamescopeInstance(MaaLibrary.toolkit().MaaToolkitGamescopeInstanceGetDisplayNo(instance),
                    MaaLibrary.toolkit().MaaToolkitGamescopeInstanceGetPipeWireNodeId(instance),
                    stringOrEmpty(MaaLibrary.toolkit().MaaToolkitGamescopeInstanceGetEisSocketPath(instance))));
            }
            return List.copyOf(instances);
        } finally {
            MaaLibrary.toolkit().MaaToolkitGamescopeInstanceListDestroy(list);
        }
    }

    public static boolean macosCheckPermission(MaaDef.MacOSPermission permission) {
        return MaaStringBuffer.toBoolean(MaaLibrary.toolkit().MaaToolkitMacOSCheckPermission(permission.code()));
    }

    public static boolean macosRequestPermission(MaaDef.MacOSPermission permission) {
        return MaaStringBuffer.toBoolean(MaaLibrary.toolkit().MaaToolkitMacOSRequestPermission(permission.code()));
    }

    public static boolean macosRevealPermissionSettings(MaaDef.MacOSPermission permission) {
        return MaaStringBuffer.toBoolean(MaaLibrary.toolkit().MaaToolkitMacOSRevealPermissionSettings(permission.code()));
    }

    public static PortalHelper portalHelperCreate() {
        Pointer handle = MaaLibrary.toolkit().MaaToolkitPortalHelperCreate();
        if (handle == null || handle == Pointer.NULL) {
            throw new IllegalStateException("Failed to create portal helper");
        }
        return new PortalHelper(handle, true);
    }

    private static String stringOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
