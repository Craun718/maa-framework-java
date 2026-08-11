package io.github.craun718.maafw;

import java.nio.file.Path;
import java.util.Map;

/** ADB device information returned by {@link Toolkit#findAdbDevices()}. */
public final class AdbDevice {

    private final String name;
    private final Path adbPath;
    private final String address;
    private final long screencapMethods;
    private final long inputMethods;
    private final Map<String, Object> config;

    public AdbDevice(
            String name,
            Path adbPath,
            String address,
            long screencapMethods,
            long inputMethods,
            Map<String, Object> config) {
        this.name = name;
        this.adbPath = adbPath;
        this.address = address;
        this.screencapMethods = screencapMethods;
        this.inputMethods = inputMethods;
        this.config = MaaResultParsers.unmodifiableMap(config);
    }

    public String name() {
        return name;
    }

    public Path adbPath() {
        return adbPath;
    }

    public String address() {
        return address;
    }

    public long screencapMethods() {
        return screencapMethods;
    }

    public long inputMethods() {
        return inputMethods;
    }

    public Map<String, Object> config() {
        return config;
    }

    @Override
    public String toString() {
        return "AdbDevice(" + name + ", " + address + ")";
    }
}
