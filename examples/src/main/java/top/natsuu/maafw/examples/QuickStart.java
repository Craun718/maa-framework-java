package top.natsuu.maafw.examples;

import top.natsuu.maafw.AdbController;
import top.natsuu.maafw.AdbDevice;
import top.natsuu.maafw.MaaLibrary;
import top.natsuu.maafw.Resource;
import top.natsuu.maafw.TaskDetail;
import top.natsuu.maafw.TaskJob;
import top.natsuu.maafw.Tasker;
import top.natsuu.maafw.Toolkit;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

/** Minimal ADB quick-start example, equivalent to the official Python/Go samples. */
public final class QuickStart {

    private QuickStart() {
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
            resource.postBundle(bundlePath("quick-start")).waitFor();
            tasker.bind(resource, controller);
            if (!tasker.inited()) {
                throw new IllegalStateException("Failed to init MAA");
            }

            TaskJob job = tasker.postTask("Startup");
            TaskDetail detail = job.waitFor().get();
            System.out.println(detail);
        }
    }

    static Path bundlePath(String name) {
        try {
            return Path.of(QuickStart.class.getResource("/" + name).toURI());
        } catch (URISyntaxException | NullPointerException e) {
            throw new IllegalStateException("Example resource not found: " + name, e);
        }
    }
}
