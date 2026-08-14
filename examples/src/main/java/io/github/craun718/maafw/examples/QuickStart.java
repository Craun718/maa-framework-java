package io.github.craun718.maafw.examples;

import io.github.craun718.maafw.AdbController;
import io.github.craun718.maafw.AdbDevice;
import io.github.craun718.maafw.MaaLibrary;
import io.github.craun718.maafw.Resource;
import io.github.craun718.maafw.TaskDetail;
import io.github.craun718.maafw.TaskJob;
import io.github.craun718.maafw.Tasker;
import io.github.craun718.maafw.Toolkit;
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
