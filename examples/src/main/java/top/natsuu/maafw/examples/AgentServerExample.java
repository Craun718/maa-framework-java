package top.natsuu.maafw.examples;

import top.natsuu.maafw.AgentServer;
import top.natsuu.maafw.Context;
import top.natsuu.maafw.CustomAction;
import top.natsuu.maafw.MaaLibrary;
import java.nio.file.Path;

/** Agent server example. The identifier must match the AgentClient identifier. */
public final class AgentServerExample {

    private AgentServerExample() {
    }

    public static void main(String[] args) {
        Path libraryDir = args.length > 0 ? Path.of(args[0]) : Path.of("bin");
        String identifier = args.length > 1 ? args[1] : "my-agent";

        MaaLibrary.open(libraryDir, true);
        AgentServer.registerCustomAction("TestAgentServer", new AgentServerAction());
        AgentServer.startUp(identifier);
        AgentServer.join();
        AgentServer.shutDown();
    }

    static final class AgentServerAction extends CustomAction {

        @Override
        public RunResult run(Context context, RunArg argv) {
            System.out.println("Agent server custom action is running");
            return RunResult.ok();
        }
    }
}
