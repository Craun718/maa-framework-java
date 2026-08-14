package io.github.craun718.maafw.examples;

import io.github.craun718.maafw.AdbController;
import io.github.craun718.maafw.AdbDevice;
import io.github.craun718.maafw.Context;
import io.github.craun718.maafw.CustomRecognition;
import io.github.craun718.maafw.MaaLibrary;
import io.github.craun718.maafw.MaaRect;
import io.github.craun718.maafw.RecognitionDetail;
import io.github.craun718.maafw.Resource;
import io.github.craun718.maafw.TaskDetail;
import io.github.craun718.maafw.TaskJob;
import io.github.craun718.maafw.Tasker;
import io.github.craun718.maafw.Toolkit;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Custom recognition example using the context APIs available inside the callback. */
public final class CustomRecognitionExample {

    private CustomRecognitionExample() {
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
            resource.postBundle(QuickStart.bundlePath("custom-recognition")).waitFor();
            tasker.bind(resource, controller);
            if (!tasker.inited()) {
                throw new IllegalStateException("Failed to init MAA");
            }
            resource.registerCustomRecognition("MyRec", new MyRecognition());

            TaskJob job = tasker.postTask("Startup");
            TaskDetail detail = job.waitFor().get();
            System.out.println(detail);
        }
    }

    static final class MyRecognition extends CustomRecognition {

        @Override
        public AnalyzeResult analyze(Context context, AnalyzeArg argv) {
            RecognitionDetail recoDetail = context.runRecognition("MyCustomOCR", argv.image(),
                    Map.of("MyCustomOCR", Map.of("recognition", "OCR", "roi", List.of(100, 100, 200, 300))));
            System.out.println(recoDetail);

            context.overridePipeline(Map.of("MyCustomOCR", Map.of("roi", List.of(1, 1, 114, 514))));
            context.tasker().controller().postClick(10, 20).waitFor();

            Context cloned = context.clone();
            cloned.overridePipeline(Map.of("MyCustomOCR", Map.of("roi", List.of(100, 200, 300, 400))));
            cloned.runRecognition("MyCustomOCR", argv.image());

            context.overrideNext(argv.nodeName(), List.of("TaskA", "TaskB"));
            return AnalyzeResult.hit(MaaRect.of(0, 0, 100, 100));
        }
    }
}
