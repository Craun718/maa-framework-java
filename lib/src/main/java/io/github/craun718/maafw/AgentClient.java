package io.github.craun718.maafw;

import com.sun.jna.Pointer;
import java.util.List;
import java.util.Objects;

/**
 * Agent client used to delegate custom recognitions and actions to a separate AgentServer process.
 *
 * <p>This is the Java equivalent of the Python binding's {@code AgentClient}. The client owns a
 * native handle and must be closed when it is no longer needed.
 */
public final class AgentClient implements AutoCloseable {

    private final Pointer handle;
    private Resource resourceHolder;
    private List<Object> sinkHolders = List.of();

    /** Creates an agent client with IPC mode and no explicit identifier. */
    public AgentClient() {
        this((String) null);
    }

    /**
     * Creates an agent client.
     *
     * @param identifier optional connection identifier; a numeric string is treated as a TCP port
     */
    public AgentClient(String identifier) {
        requireClientMode();

        Pointer created;
        if (identifier == null || identifier.isEmpty()) {
            created = MaaLibrary.agentClient().MaaAgentClientCreateV2(null);
        } else {
            try (MaaStringBuffer buffer = new MaaStringBuffer()) {
                buffer.set(identifier);
                created = MaaLibrary.agentClient().MaaAgentClientCreateV2(buffer.handle());
            }
        }
        if (created == null || created == Pointer.NULL) {
            throw new IllegalStateException("Failed to create agent client");
        }
        this.handle = created;
    }

    /**
     * Creates a TCP agent client listening on {@code 127.0.0.1:port}; port 0 selects an available
     * port automatically.
     *
     * @throws IllegalArgumentException if port is outside {@code 0..65535}
     */
    public static AgentClient createTcp(int port) {
        requireValidTcpPort(port);
        requireClientMode();

        Pointer created = MaaLibrary.agentClient().MaaAgentClientCreateTcp((short) port);
        if (created == null || created == Pointer.NULL) {
            throw new IllegalStateException("Failed to create TCP agent client");
        }
        return new AgentClient(created);
    }

    /** Creates a TCP agent client with an automatically selected port. */
    public static AgentClient createTcp() {
        return createTcp(0);
    }

    static int requireValidTcpPort(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port number: " + port + ". Must be between 0 and 65535.");
        }
        return port;
    }

    private AgentClient(Pointer handle) {
        this.handle = Objects.requireNonNull(handle, "handle");
    }

    /** Returns the connection identifier, or {@code null} if it is not available. */
    public String identifier() {
        try (MaaStringBuffer buffer = new MaaStringBuffer()) {
            if (!MaaStringBuffer.toBoolean(MaaLibrary.agentClient().MaaAgentClientIdentifier(handle, buffer.handle()))) {
                return null;
            }
            return buffer.getUtf8();
        }
    }

    /** Binds the resource and keeps it alive for the lifetime of this client. */
    public boolean bind(Resource resource) {
        Objects.requireNonNull(resource, "resource");
        resourceHolder = resource;
        return MaaStringBuffer.toBoolean(MaaLibrary.agentClient().MaaAgentClientBindResource(handle, resource.handle()));
    }

    /** Registers event forwarding sinks and keeps the instances alive. */
    public boolean registerSink(Resource resource, Controller controller, Tasker tasker) {
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(tasker, "tasker");
        sinkHolders = List.of(resource, controller, tasker);

        return MaaStringBuffer.toBoolean(MaaLibrary.agentClient().MaaAgentClientRegisterResourceSink(handle, resource.handle()))
            && MaaStringBuffer.toBoolean(MaaLibrary.agentClient().MaaAgentClientRegisterControllerSink(handle, controller.handle()))
            && MaaStringBuffer.toBoolean(MaaLibrary.agentClient().MaaAgentClientRegisterTaskerSink(handle, tasker.handle()));
    }

    public boolean connect() {
        return MaaStringBuffer.toBoolean(MaaLibrary.agentClient().MaaAgentClientConnect(handle));
    }

    public boolean disconnect() {
        return MaaStringBuffer.toBoolean(MaaLibrary.agentClient().MaaAgentClientDisconnect(handle));
    }

    public boolean connected() {
        return MaaStringBuffer.toBoolean(MaaLibrary.agentClient().MaaAgentClientConnected(handle));
    }

    public boolean alive() {
        return MaaStringBuffer.toBoolean(MaaLibrary.agentClient().MaaAgentClientAlive(handle));
    }

    public boolean setTimeout(long milliseconds) {
        return MaaStringBuffer.toBoolean(MaaLibrary.agentClient().MaaAgentClientSetTimeout(handle, milliseconds));
    }

    public List<String> customRecognitionList() {
        try (MaaStringListBuffer buffer = new MaaStringListBuffer()) {
            MaaStringBuffer.requireOk(MaaLibrary.agentClient().MaaAgentClientGetCustomRecognitionList(handle, buffer.handle()));
            return buffer.get();
        }
    }

    public List<String> customActionList() {
        try (MaaStringListBuffer buffer = new MaaStringListBuffer()) {
            MaaStringBuffer.requireOk(MaaLibrary.agentClient().MaaAgentClientGetCustomActionList(handle, buffer.handle()));
            return buffer.get();
        }
    }

    @Override
    public void close() {
        if (handle != null && handle != Pointer.NULL) {
            MaaLibrary.agentClient().MaaAgentClientDestroy(handle);
        }
    }

    private static void requireClientMode() {
        if (MaaLibrary.isAgentServer()) {
            throw new IllegalStateException("AgentClient is not available in AgentServer mode");
        }
    }
}
