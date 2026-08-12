package io.github.craun718.maafw;

import java.nio.file.Path;

/** Test-side AgentServer process used by {@link RuntimeSmokeTest}. */
public final class AgentServerProcess {

    private AgentServerProcess() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: AgentServerProcess <identifier>");
        }
        String libraryDir = System.getProperty("maafw.libDir");
        if (libraryDir == null || libraryDir.isBlank()) {
            throw new IllegalStateException("Missing maafw.libDir system property");
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
            if (!AgentServer.startUp(args[0])) {
                throw new IllegalStateException("Failed to start agent server");
            }
            AgentServer.join();
            AgentServer.shutDown();
        } finally {
            MaaLibrary.close();
        }
    }
}
