package io.github.craun718.maafw;

import com.sun.jna.Library;
import com.sun.jna.Native;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Loads the MaaFramework dynamic libraries shipped in the official release.
 *
 * <p>Call {@link #open(Path, boolean)} before using any high-level wrapper. The binding
 * intentionally does not bundle native binaries; it resolves the libraries from a user-provided
 * directory so the same Java artifact can target Windows, macOS and Linux releases.
 */
public final class MaaLibrary {

    private static final Map<String, String> LIBRARY_OPTIONS = Map.of(Library.OPTION_STRING_ENCODING, StandardCharsets.UTF_8.name());

    private static volatile boolean agentServerMode;
    private static volatile MaaFrameworkLibrary framework;
    private static volatile MaaToolkitLibrary toolkit;
    private static volatile MaaAgentClientLibrary agentClient;
    private static volatile MaaAgentServerLibrary agentServer;
    private static volatile Path libraryDirectory;
    private static volatile Path frameworkPath;
    private static volatile Path toolkitPath;
    private static volatile Path agentClientPath;
    private static volatile Path agentServerPath;

    private MaaLibrary() {
    }

    /** Returns whether native libraries are currently loaded. */
    public static synchronized boolean isOpen() {
        return framework != null || agentServer != null;
    }

    /** Loads the official MaaFramework release libraries from {@code directory}. */
    public static synchronized void open(Path directory, boolean agentServerMode) {
        Objects.requireNonNull(directory, "directory");
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("MaaFramework library directory does not exist: " + directory);
        }

        libraryDirectory = directory;
        MaaLibrary.agentServerMode = agentServerMode;
        String platform = platform();

        if (agentServerMode) {
            agentServerPath = resolve(directory, "MaaAgentServer", platform);
            if (!Files.isRegularFile(agentServerPath)) {
                throw new IllegalArgumentException("MaaAgentServer library not found: " + agentServerPath);
            }
            MaaAgentServerLibrary loadedAgentServer = Native.loadLibrary(agentServerPath.toString(), MaaAgentServerLibrary.class,
                    LIBRARY_OPTIONS);
            MaaLibrary.agentServer = loadedAgentServer;
        } else {
            frameworkPath = resolve(directory, "MaaFramework", platform);
            toolkitPath = resolve(directory, "MaaToolkit", platform);
            agentClientPath = resolve(directory, "MaaAgentClient", platform);
            if (!Files.isRegularFile(frameworkPath) || !Files.isRegularFile(toolkitPath) || !Files.isRegularFile(agentClientPath)) {
                throw new IllegalArgumentException("MaaFramework, MaaToolkit and MaaAgentClient libraries are required under " + directory);
            }
            framework = Native.load(frameworkPath.toString(), MaaFrameworkLibrary.class, LIBRARY_OPTIONS);
            toolkit = Native.load(toolkitPath.toString(), MaaToolkitLibrary.class, LIBRARY_OPTIONS);
            agentClient = Native.load(agentClientPath.toString(), MaaAgentClientLibrary.class, LIBRARY_OPTIONS);
        }
    }

    /**
     * Drops all loaded library references.
     *
     * <p>Call this only after closing every wrapper that holds a native handle created from the
     * loaded libraries. It is a no-op when no libraries are loaded and is primarily useful for
     * tests or for swapping release binaries.
     */
    public static synchronized void close() {
        framework = null;
        toolkit = null;
        agentClient = null;
        agentServer = null;
        libraryDirectory = null;
        frameworkPath = null;
        toolkitPath = null;
        agentClientPath = null;
        agentServerPath = null;
        agentServerMode = false;
    }

    /** Loads in client mode with the supplied MaaAgentServer library. */
    public static void open(Path directory) {
        open(directory, false);
    }

    /** Returns the MaaFramework-compatible library for the current mode. */
    public static MaaFrameworkLibrary framework() {
        if (agentServerMode) {
            return require(agentServer, "MaaLibrary.open must be called before framework access");
        }
        return require(framework, "MaaLibrary.open must be called before framework access");
    }

    public static MaaToolkitLibrary toolkit() {
        if (agentServerMode) {
            throw new IllegalStateException("MaaToolkit is not available in AgentServer mode");
        }
        return require(toolkit, "MaaLibrary.open must be called before toolkit access");
    }

    public static MaaAgentClientLibrary agentClient() {
        if (agentServerMode) {
            throw new IllegalStateException("MaaAgentClient is not available in AgentServer mode");
        }
        return require(agentClient, "MaaLibrary.open must be called before agent client access");
    }

    public static MaaAgentServerLibrary agentServer() {
        if (!agentServerMode) {
            throw new IllegalStateException("MaaAgentServer is only available in AgentServer mode");
        }
        return require(agentServer, "MaaLibrary.open must be called before agent server access");
    }

    public static boolean isAgentServer() {
        return agentServerMode;
    }

    public static String version() {
        String version = framework().MaaVersion();
        return version == null ? "" : version;
    }

    public static Path frameworkPath() {
        return frameworkPath;
    }

    public static Path toolkitPath() {
        return toolkitPath;
    }

    public static Path agentClientPath() {
        return agentClientPath;
    }

    public static Path agentServerPath() {
        return agentServerPath;
    }

    /** Returns the directory passed to {@link #open(Path, boolean)}. */
    public static Path libraryDirectory() {
        return libraryDirectory;
    }

    /**
     * Returns the official release layout's {@code MaaAgentBinary} directory when available, or the
     * legacy relative path used by callers that manage the binary themselves.
     */
    public static String defaultAgentBinaryPath() {
        return defaultAgentBinaryPath(libraryDirectory);
    }

    static String defaultAgentBinaryPath(Path directory) {
        if (directory != null) {
            Path agentDirectory = directory.resolve("../share/MaaAgentBinary").normalize();
            if (Files.isDirectory(agentDirectory)) {
                return agentDirectory.toString();
            }
        }
        return "MaaAgentBinary";
    }

    private static Path resolve(Path directory, String baseName, String platform) {
        return switch (platform) {
            case "windows" -> directory.resolve(baseName + ".dll");
            case "macos" -> directory.resolve("lib" + baseName + ".dylib");
            default -> directory.resolve("lib" + baseName + ".so");
        };
    }

    private static String platform() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return "windows";
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return "macos";
        }
        if (os.contains("linux")) {
            return "linux";
        }
        throw new UnsupportedOperationException("Unsupported operating system: " + os);
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new IllegalStateException(message);
        }
        return value;
    }
}
