package io.github.craun718.maafw;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/** FFI surface of MaaToolkit dynamic library. */
public interface MaaToolkitLibrary extends Library {

    byte MaaToolkitConfigInitOption(String userPath, String defaultJson);

    Pointer MaaToolkitAdbDeviceListCreate();

    void MaaToolkitAdbDeviceListDestroy(Pointer handle);

    byte MaaToolkitAdbDeviceFind(Pointer buffer);

    byte MaaToolkitAdbDeviceFindSpecified(String adbPath, Pointer buffer);

    long MaaToolkitAdbDeviceListSize(Pointer list);

    Pointer MaaToolkitAdbDeviceListAt(Pointer list, long index);

    String MaaToolkitAdbDeviceGetName(Pointer device);

    String MaaToolkitAdbDeviceGetAdbPath(Pointer device);

    String MaaToolkitAdbDeviceGetAddress(Pointer device);

    long MaaToolkitAdbDeviceGetScreencapMethods(Pointer device);

    long MaaToolkitAdbDeviceGetInputMethods(Pointer device);

    String MaaToolkitAdbDeviceGetConfig(Pointer device);

    Pointer MaaToolkitDesktopWindowListCreate();

    void MaaToolkitDesktopWindowListDestroy(Pointer handle);

    byte MaaToolkitDesktopWindowFindAll(Pointer buffer);

    long MaaToolkitDesktopWindowListSize(Pointer list);

    Pointer MaaToolkitDesktopWindowListAt(Pointer list, long index);

    Pointer MaaToolkitDesktopWindowGetHandle(Pointer window);

    String MaaToolkitDesktopWindowGetClassName(Pointer window);

    String MaaToolkitDesktopWindowGetWindowName(Pointer window);

    byte MaaToolkitMacOSCheckPermission(int permission);

    byte MaaToolkitMacOSRequestPermission(int permission);

    byte MaaToolkitMacOSRevealPermissionSettings(int permission);

    Pointer MaaToolkitPortalHelperCreate();

    void MaaToolkitPortalHelperDestroy(Pointer helper);

    byte MaaToolkitPortalHelperOpenStream(Pointer helper);

    byte MaaToolkitPortalHelperGetPersist(Pointer helper);

    void MaaToolkitPortalHelperSetPersist(Pointer helper, byte enable);

    int MaaToolkitPortalHelperGetPipeWireFD(Pointer helper);

    int MaaToolkitPortalHelperGetPipeWireNodeID(Pointer helper);

    String MaaToolkitPortalHelperGetRestoreToken(Pointer helper);

    void MaaToolkitPortalHelperSetRestoreToken(Pointer helper, String token);
}
