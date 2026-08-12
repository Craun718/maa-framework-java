package io.github.craun718.maafw;

import java.nio.file.Path;
import java.util.Map;

/** ADB controller using the official MaaFramework release. */
public class AdbController extends Controller {

    public AdbController(String adbPath, String address) {
        this(adbPath, address, MaaDef.ADB_SCREENCAP_DEFAULT, MaaDef.ADB_INPUT_DEFAULT, Map.of(), "MaaAgentBinary");
    }

    public AdbController(Path adbPath, String address) {
        this(
                adbPath.toString(),
                address,
                MaaDef.ADB_SCREENCAP_DEFAULT,
                MaaDef.ADB_INPUT_DEFAULT,
                Map.of(),
                "MaaAgentBinary");
    }

    public AdbController(String adbPath, String address, Map<String, Object> config) {
        this(
                adbPath,
                address,
                MaaDef.ADB_SCREENCAP_DEFAULT,
                MaaDef.ADB_INPUT_DEFAULT,
                config,
                "MaaAgentBinary");
    }

    public AdbController(Path adbPath, String address, Map<String, Object> config) {
        this(
                adbPath.toString(),
                address,
                MaaDef.ADB_SCREENCAP_DEFAULT,
                MaaDef.ADB_INPUT_DEFAULT,
                config,
                "MaaAgentBinary");
    }

    public AdbController(String adbPath, String address, long screencapMethods, long inputMethods) {
        this(adbPath, address, screencapMethods, inputMethods, Map.of(), "MaaAgentBinary");
    }

    public AdbController(
            Path adbPath, String address, long screencapMethods, long inputMethods) {
        this(
                adbPath.toString(),
                address,
                screencapMethods,
                inputMethods,
                Map.of(),
                "MaaAgentBinary");
    }

    public AdbController(
            String adbPath,
            String address,
            long screencapMethods,
            long inputMethods,
            Map<String, Object> config) {
        this(adbPath, address, screencapMethods, inputMethods, config, "MaaAgentBinary");
    }

    public AdbController(
            Path adbPath,
            String address,
            long screencapMethods,
            long inputMethods,
            Map<String, Object> config) {
        this(adbPath, address, screencapMethods, inputMethods, config, null);
    }

    public AdbController(
            String adbPath,
            String address,
            long screencapMethods,
            long inputMethods,
            Map<String, Object> config,
            String agentPath) {
        super();
        setHandle(MaaLibrary.framework()
                .MaaAdbControllerCreate(
                        adbPath,
                        address,
                        screencapMethods,
                        inputMethods,
                        MaaJson.write(config == null ? Map.of() : config),
                        agentPath));
    }

    public AdbController(
            Path adbPath,
            String address,
            long screencapMethods,
            long inputMethods,
            Map<String, Object> config,
            Path agentPath) {
        this(
                adbPath.toString(),
                address,
                screencapMethods,
                inputMethods,
                config,
                agentPath == null ? "MaaAgentBinary" : agentPath.toString());
    }
}
