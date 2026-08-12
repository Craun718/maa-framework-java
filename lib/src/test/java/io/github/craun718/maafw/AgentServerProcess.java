package io.github.craun718.maafw;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Test-side AgentServer process used by {@link RuntimeSmokeTest}. */
public final class AgentServerProcess {

    private static Path sinkLog;

    private AgentServerProcess() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: AgentServerProcess <identifier>");
        }
        String libraryDir = System.getProperty("maafw.libDir");
        if (libraryDir == null || libraryDir.isBlank()) {
            throw new IllegalStateException("Missing maafw.libDir system property");
        }
        String sinkLogValue = System.getProperty("maafw.sinkLog");
        if (sinkLogValue != null && !sinkLogValue.isBlank()) {
            sinkLog = Path.of(sinkLogValue);
        }

        MaaLibrary.open(Path.of(libraryDir), true);
        try {
            if (!AgentServer.registerCustomRecognition(
                    "JavaAgentReco",
                    new CustomRecognition() {
                        @Override
                        public AnalyzeResult analyze(Context context, AnalyzeArg argv) {
                            return AnalyzeResult.hit(MaaRect.of(10, 20, 30, 40));
                        }
                    })) {
                throw new IllegalStateException("Failed to register JavaAgentReco");
            }
            if (!AgentServer.registerCustomAction(
                    "JavaAgentAction",
                    new CustomAction() {
                        @Override
                        public RunResult run(Context context, RunArg argv) {
                            return RunResult.ok();
                        }
                    })) {
                throw new IllegalStateException("Failed to register JavaAgentAction");
            }
            requireSink("resource", AgentServer.addResourceSink(new ResourceEventSink() {
                @Override
                public void onResourceLoading(
                        Resource resource,
                        MaaDef.NotificationType notificationType,
                        ResourceEventSink.ResourceLoadingDetail detail) {
                    record("resource", "Resource.Loading", detail);
                }
            }));
            requireSink("controller", AgentServer.addControllerSink(new ControllerEventSink() {
                @Override
                public void onControllerAction(
                        Controller controller,
                        MaaDef.NotificationType notificationType,
                        ControllerEventSink.ControllerActionDetail detail) {
                    record("controller", "Controller.Action", detail);
                }
            }));
            requireSink("tasker", AgentServer.addTaskerSink(new TaskerEventSink() {
                @Override
                public void onTaskerTask(
                        Tasker tasker,
                        MaaDef.NotificationType notificationType,
                        TaskerEventSink.TaskerTaskDetail detail) {
                    record("tasker", "Tasker.Task", detail);
                }
            }));
            requireSink("context", AgentServer.addContextSink(new ContextEventSink() {
                @Override
                public void onNodeRecognition(
                        Context context,
                        MaaDef.NotificationType notificationType,
                        ContextEventSink.NodeRecognitionDetail detail) {
                    record("context", "Node.Recognition", detail);
                }

                @Override
                public void onNodeAction(
                        Context context,
                        MaaDef.NotificationType notificationType,
                        ContextEventSink.NodeActionDetail detail) {
                    record("context", "Node.Action", detail);
                }
            }));
            if (!AgentServer.startUp(args[0])) {
                throw new IllegalStateException("Failed to start agent server");
            }
            AgentServer.join();
            AgentServer.shutDown();
        } finally {
            MaaLibrary.close();
        }
    }

    private static void requireSink(String kind, Long sinkId) {
        if (sinkId == null || sinkId == MaaDef.INVALID_ID) {
            throw new IllegalStateException("Failed to register " + kind + " sink");
        }
    }

    private static synchronized void record(String kind, String event, Object detail) {
        if (sinkLog == null) {
            return;
        }
        try {
            String line = kind + "\t" + event + "\t" + detail + System.lineSeparator();
            Files.writeString(
                    sinkLog,
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (Exception e) {
            throw new RuntimeException("Failed to record agent server sink event", e);
        }
    }
}
