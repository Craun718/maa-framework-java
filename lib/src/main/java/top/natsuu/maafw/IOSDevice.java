package top.natsuu.maafw;

import java.util.List;
import java.util.Map;

/** A snapshot of a device discovered through XDeviceBridge. */
public record IOSDevice(String name, String udid, long screencapMethods, List<Map<String, Object>> connections, String state) {

    public IOSDevice {
        connections = connections.stream().map(MaaResultParsers::unmodifiableMap).toList();
    }
}
