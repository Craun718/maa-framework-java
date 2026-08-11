package io.github.craun718.maafw;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Agent server hosting custom recognitions, actions and event sinks in a separate process. */
public final class AgentServer {

    private static final Map<String, CustomRecognition> CUSTOM_RECOGNITIONS = new HashMap<>();
    private static final Map<String, CustomAction> CUSTOM_ACTIONS = new HashMap<>();
    private static final Map<Long, EventSink> SINK_HOLDERS = new HashMap<>();

    private AgentServer() {}

    public static boolean registerCustomRecognition(String name, CustomRecognition recognition) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(recognition, "recognition");
        CUSTOM_RECOGNITIONS.put(name, recognition);
        return MaaStringBuffer.toBoolean(MaaLibrary.agentServer()
                .MaaAgentServerRegisterCustomRecognition(name, recognition.callback(), null));
    }

    public static boolean registerCustomAction(String name, CustomAction action) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(action, "action");
        CUSTOM_ACTIONS.put(name, action);
        return MaaStringBuffer.toBoolean(
                MaaLibrary.agentServer().MaaAgentServerRegisterCustomAction(name, action.callback(), null));
    }

    public static Long addResourceSink(ResourceEventSink sink) {
        Objects.requireNonNull(sink, "sink");
        long sinkId = MaaLibrary.agentServer().MaaAgentServerAddResourceSink(sink.callback(), null);
        return holdSink(sinkId, sink);
    }

    public static Long addControllerSink(ControllerEventSink sink) {
        Objects.requireNonNull(sink, "sink");
        long sinkId = MaaLibrary.agentServer().MaaAgentServerAddControllerSink(sink.callback(), null);
        return holdSink(sinkId, sink);
    }

    public static Long addTaskerSink(TaskerEventSink sink) {
        Objects.requireNonNull(sink, "sink");
        long sinkId = MaaLibrary.agentServer().MaaAgentServerAddTaskerSink(sink.callback(), null);
        return holdSink(sinkId, sink);
    }

    public static Long addContextSink(ContextEventSink sink) {
        Objects.requireNonNull(sink, "sink");
        long sinkId = MaaLibrary.agentServer().MaaAgentServerAddContextSink(sink.callback(), null);
        return holdSink(sinkId, sink);
    }

    public static boolean startUp(String identifier) {
        Objects.requireNonNull(identifier, "identifier");
        return MaaStringBuffer.toBoolean(MaaLibrary.agentServer().MaaAgentServerStartUp(identifier));
    }

    public static void shutDown() {
        MaaLibrary.agentServer().MaaAgentServerShutDown();
    }

    public static void join() {
        MaaLibrary.agentServer().MaaAgentServerJoin();
    }

    public static void detach() {
        MaaLibrary.agentServer().MaaAgentServerDetach();
    }

    private static Long holdSink(long sinkId, EventSink sink) {
        if (sinkId == MaaDef.INVALID_ID) {
            return null;
        }
        SINK_HOLDERS.put(sinkId, sink);
        return sinkId;
    }
}
