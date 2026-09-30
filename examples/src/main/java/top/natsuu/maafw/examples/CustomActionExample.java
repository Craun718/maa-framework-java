package top.natsuu.maafw.examples;

import top.natsuu.maafw.AdbController;
import top.natsuu.maafw.AdbDevice;
import top.natsuu.maafw.Context;
import top.natsuu.maafw.CustomAction;
import top.natsuu.maafw.MaaLibrary;
import top.natsuu.maafw.Resource;
import top.natsuu.maafw.TaskDetail;
import top.natsuu.maafw.TaskJob;
import top.natsuu.maafw.Tasker;
import top.natsuu.maafw.Toolkit;
import java.nio.file.Path;
import java.util.List;

/** Custom action example. The action-only node deliberately has a zero recognition id. */
public final class CustomActionExample {

    private CustomActionExample() {
    }

    public static void main(String[] args) {
        Path libraryDir = args.length > 0 ? Path.of(args[0]) : Path.of("bin");
        MaaLibrary.open(libraryDir, false);
        Toolkit.initOption(Path.of("."));

        List<AdbDevice> devices = Toolkit.findAdbDevices();
        if (devices.isEmpty()) {
            throw new IllegalStateException("No ADB device found");
        }
        AdbDevice device = devices.getFirst();

        try (AdbController controller = new AdbController(device.adbPath(), device.address(), device.screencapMethods(),
            device.inputMethods(), device.config()); Resource resource = new Resource(); Tasker tasker = new Tasker()) {
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
