package io.github.craun718.maafw.examples;

import io.github.craun718.maafw.AdbController;
import io.github.craun718.maafw.AdbDevice;
import io.github.craun718.maafw.Context;
import io.github.craun718.maafw.CustomAction;
import io.github.craun718.maafw.MaaLibrary;
import io.github.craun718.maafw.Resource;
import io.github.craun718.maafw.TaskDetail;
import io.github.craun718.maafw.TaskJob;
import io.github.craun718.maafw.Tasker;
import io.github.craun718.maafw.Toolkit;
import java.nio.file.Path;
import java.util.List;

/** Custom action example. The action-only node deliberately has a zero recognition id. */
public final class CustomActionExample {

    private CustomActionExample() {}

    public static void main(String[] args) {
        Path libraryDir = args.length > 0 ? Path.of(args[0]) : Path.of("bin");
        MaaLibrary.open(libraryDir, false);
        Toolkit.initOption(Path.of("."));

        List<AdbDevice> devices = Toolkit.findAdbDevices();
        if (devices.isEmpty()) {
            throw new IllegalStateException("No ADB device found");
        }
        AdbDevice device = devices.getFirst();

        try (AdbController controller = new AdbController(
                        device.adbPath(),
                        device.address(),
                        device.screencapMethods(),
                        device.inputMethods(),
                        device.config());
                Resource resource = new Resource();
                Tasker tasker = new Tasker()) {
            controller.postConnection().waitFor();
            resource.postBundle(QuickStart.bundlePath("custom-action")).waitFor();
            tasker.bind(resource, controller);
            if (!tasker.inited()) {
                throw new IllegalStateException("Failed to init MAA");
            }
            resource.registerCustomAction("MyAct", new MyAction());

            TaskJob job = tasker.postTask("Startup");
            TaskDetail detail = job.waitFor().get();
            System.out.println(detail);
        }
    }

    static final class MyAction extends CustomAction {

        @Override
        public RunResult run(Context context, RunArg argv) {
            System.out.println("custom action: " + argv.nodeName());
            System.out.println("recognition detail: " + argv.recoDetail());
            return RunResult.ok();
        }
    }
}
