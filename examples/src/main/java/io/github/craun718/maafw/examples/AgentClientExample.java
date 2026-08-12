package io.github.craun718.maafw.examples;

import io.github.craun718.maafw.AgentClient;
import io.github.craun718.maafw.CustomController;
import io.github.craun718.maafw.MaaImage;
import io.github.craun718.maafw.MaaLibrary;
import io.github.craun718.maafw.Resource;
import io.github.craun718.maafw.Tasker;
import java.nio.file.Path;

/** Agent client example. Start AgentServerExample in another process first. */
public final class AgentClientExample {

    private AgentClientExample() {}

    public static void main(String[] args) {
        Path libraryDir = args.length > 0 ? Path.of(args[0]) : Path.of("bin");
        MaaLibrary.open(libraryDir, false);

        try (AgentClient client = new AgentClient("my-agent");
                Resource resource = new Resource();
                BlankController controller = new BlankController();
                Tasker tasker = new Tasker()) {
            resource.postBundle(QuickStart.bundlePath("agent-client")).waitFor();
            controller.postConnection().waitFor();
            tasker.bind(resource, controller);
            if (!tasker.inited()) {
                throw new IllegalStateException("Failed to init MAA");
            }

            client.bind(resource);
            client.registerSink(resource, controller, tasker);
            client.connect();
            tasker.postTask("Test", java.util.Map.of()).waitFor();
            client.disconnect();
        }
    }

    static final class BlankController extends CustomController {

        @Override
        public boolean connect() {
            return true;
        }

        @Override
        public String requestUuid() {
            return "blank";
        }

        @Override
        public boolean startApp(String intent) {
            return true;
        }

        @Override
        public boolean stopApp(String intent) {
            return true;
        }

        @Override
        public MaaImage screencap() {
            return MaaImage.empty();
        }

        @Override
        public boolean click(int x, int y) {
            return true;
        }

        @Override
        public boolean swipe(int x1, int y1, int x2, int y2, int duration) {
            return true;
        }

        @Override
        public boolean touchDown(int contact, int x, int y, int pressure) {
            return true;
        }

        @Override
        public boolean touchMove(int contact, int x, int y, int pressure) {
            return true;
        }

        @Override
        public boolean touchUp(int contact) {
            return true;
        }

        @Override
        public boolean clickKey(int keycode) {
            return true;
        }

        @Override
        public boolean inputText(String text) {
            return true;
        }

        @Override
        public boolean keyDown(int keycode) {
            return true;
        }

        @Override
        public boolean keyUp(int keycode) {
            return true;
        }
    }
}
