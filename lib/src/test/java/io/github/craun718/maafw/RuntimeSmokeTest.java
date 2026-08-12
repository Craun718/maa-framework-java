package io.github.craun718.maafw;

import com.sun.jna.Pointer;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import io.github.craun718.maafw.pipeline.JWaitFreezes;

/**
 * Optional smoke tests against an official MaaFramework release.
 *
 * <p>Set {@code MAA_FRAMEWORK_LIB_DIR} or {@code maafw.libDir} to the directory containing the
 * platform release libraries. The tests are skipped when neither is configured.
 */
class RuntimeSmokeTest {

    @AfterAll
    static void closeLibrary() {
        MaaLibrary.close();
    }

    @Test
    void clientModeLifecycleMatchesReleaseLibrary() throws Exception {
        Path libraryDir = libraryDir();
        Assumptions.assumeTrue(
                libraryDir != null,
                "Set MAA_FRAMEWORK_LIB_DIR or maafw.libDir to run release library smoke tests");

        MaaLibrary.open(libraryDir, false);
        assertTrue(MaaLibrary.isOpen());
        assertFalse(MaaLibrary.version().isBlank(), "MaaVersion should return a release version");

        exerciseGlobalOptions();
        exerciseBuffers();
        exerciseResourceAndTasker();
        exerciseCustomController();
        exerciseRuntimeOverrides();
        exerciseRecordAndReplay();
        exerciseToolkit();
        exerciseAgentClient();
    }

    private static void exerciseGlobalOptions() {
        assertTrue(Tasker.setRecoImageCacheLimit(512), "RecoImageCacheLimit should accept a size_t value");
        assertTrue(Tasker.setSaveDraw(false));
        assertTrue(Tasker.setSaveOnError(false));
    }

    @Test
    void serverModeLoadsAgentServerLibrary() throws Exception {
        Path libraryDir = libraryDir();
        Assumptions.assumeTrue(
                libraryDir != null,
                "Set MAA_FRAMEWORK_LIB_DIR or maafw.libDir to run release library smoke tests");

        MaaLibrary.open(libraryDir, true);
        assertTrue(MaaLibrary.isAgentServer());
        assertFalse(MaaLibrary.version().isBlank(), "MaaAgentServer should export MaaVersion");

        assertTrue(AgentServer.registerCustomRecognition(
                "JavaSmokeReco",
                new CustomRecognition() {
                    @Override
                    public AnalyzeResult analyze(Context context, AnalyzeArg argv) {
                        return AnalyzeResult.miss();
                    }
                }));
    }

    private static void exerciseBuffers() {
        try (MaaStringBuffer buffer = new MaaStringBuffer()) {
            assertTrue(buffer.empty());
            buffer.set("smoke-\u4e2d\u6587");
            assertEquals("smoke-\u4e2d\u6587", buffer.getUtf8());
            assertFalse(buffer.empty());
        }

        try (MaaStringListBuffer list = new MaaStringListBuffer()) {
            list.set(List.of("alpha", "beta"));
            assertEquals(List.of("alpha", "beta"), list.get());
        }

        try (MaaRectBuffer rect = new MaaRectBuffer()) {
            rect.set(MaaRect.of(1, 2, 3, 4));
            assertEquals(MaaRect.of(1, 2, 3, 4), rect.get());
        }

        try (MaaImageBuffer image = new MaaImageBuffer()) {
            assertTrue(image.empty());
            byte[] bgr = new byte[] {(byte) 0x10, (byte) 0x20, (byte) 0x30};
            image.set(new MaaImage(bgr, 1, 1, 3, MaaImage.TYPE_8UC3));
            assertFalse(image.empty());
            MaaImage loaded = image.get();
            assertEquals(1, loaded.width());
            assertEquals(1, loaded.height());
            assertEquals(3, loaded.channels());
            assertArrayEquals(bgr, loaded.data());
        }
    }

    private static void exerciseResourceAndTasker() throws Exception {
        try (Resource resource = new Resource()) {
            assertNotNull(resource.handle());
            assertTrue(resource.nodeList().isEmpty());

            AtomicBoolean contextChecksOk = new AtomicBoolean();
            AtomicBoolean waitFreezesOk = new AtomicBoolean();
            AtomicBoolean taskerSame = new AtomicBoolean();
            AtomicBoolean setAnchorOk = new AtomicBoolean();
            AtomicReference<String> anchorGot = new AtomicReference<>("");
            AtomicReference<String> nodeName = new AtomicReference<>("");
            Path pipeline = Files.createTempFile("maa-java-pipeline-", ".json");
            try {
                assertTrue(resource.registerCustomRecognition(
                        "JavaContextReco",
                        new CustomRecognition() {
                            @Override
                            public AnalyzeResult analyze(Context context, AnalyzeArg argv) {
                                Context cloned = context.clone();
                                nodeName.set(argv.nodeName());
                                taskerSame.set(
                                        Pointer.nativeValue(cloned.tasker().handle())
                                                == Pointer.nativeValue(context.tasker().handle()));
                                setAnchorOk.set(context.setAnchor("java-anchor", argv.nodeName()));
                                anchorGot.set(context.getAnchor("java-anchor"));
                                boolean checks = taskerSame.get()
                                        && setAnchorOk.get()
                                        && argv.nodeName().equals(anchorGot.get());
                                contextChecksOk.set(checks);

                                JWaitFreezes wait = new JWaitFreezes();
                                wait.time = 1;
                                wait.rateLimit = 1;
                                wait.timeout = 5000;
                                waitFreezesOk.set(
                                        context.waitFreezes(0, MaaRect.of(0, 0, 1, 1), wait));
                                return AnalyzeResult.hit(MaaRect.of(10, 20, 30, 40));
                            }
                        }));
                assertTrue(resource.registerCustomAction(
                        "JavaContextAction",
                        new CustomAction() {
                            @Override
                            public RunResult run(Context context, RunArg argv) {
                                return RunResult.ok();
                            }
                        }));

                Files.writeString(
                        pipeline,
                        """
                        {
                          "StartUpAndClickButton": {
                            "recognition": {"type": "DirectHit", "param": {}},
                            "action": {"type": "DoNothing", "param": {}},
                            "next": ["Click_Button"]
                          },
                          "Click_Button": {
                            "recognition": {"type": "OCR", "param": {"expected": ["Button"]}},
                            "action": {"type": "Click", "param": {}}
                          },
                          "SmokeTask": {
                            "pre_delay": 500,
                            "recognition": {"type": "DirectHit", "param": {}},
                            "action": {"type": "Click", "param": {}},
                            "next": []
                          },
                          "ContextSmoke": {
                            "recognition": {
                              "type": "Custom",
                              "param": {"custom_recognition": "JavaContextReco"}
                            },
                            "action": {
                              "type": "Custom",
                              "param": {"custom_action": "JavaContextAction"}
                            },
                            "next": []
                          }
                        }
                        """);
                assertTrue(resource.postPipeline(pipeline).waitFor().succeeded());
                assertTrue(resource.loaded());
                assertTrue(resource.nodeList().contains("StartUpAndClickButton"));
                assertTrue(resource.customRecognitionList().contains("JavaContextReco"));
                assertTrue(resource.customActionList().contains("JavaContextAction"));

                Map<String, Object> node = resource.getNodeData("StartUpAndClickButton");
                assertNotNull(node);
                List<?> next = (List<?>) node.get("next");
                assertEquals("Click_Button", ((Map<?, ?>) next.getFirst()).get("name"));
                assertNotNull(resource.getNodeObject("Click_Button"));

                Map<String, Object> templateDefaults = resource.getDefaultRecognitionParam("TemplateMatch");
                assertTrue(templateDefaults.containsKey("threshold"));
                Map<String, Object> clickDefaults = resource.getDefaultActionParam("Click");
                assertTrue(clickDefaults.containsKey("target"));
            } finally {
                Files.deleteIfExists(pipeline);
            }

            AtomicInteger clickCount = new AtomicInteger();
            try (CustomController controller = newSmokeController(clickCount);
                    Tasker tasker = new Tasker()) {
                assertTrue(controller.postConnection().waitFor().succeeded());
                assertTrue(tasker.bind(resource, controller));
                assertTrue(tasker.inited());
                assertNotNull(tasker.resource());
                assertNotNull(tasker.controller());

                TaskJob task = tasker.postTask("SmokeTask");
                assertTrue(tasker.running(), "postTask should start a running task");
                TaskDetail taskDetail = task.waitFor().get();
                assertTrue(taskDetail.status().succeeded());
                assertEquals("SmokeTask", taskDetail.entry());
                NodeDetail latest = tasker.getLatestNode("SmokeTask");
                assertNotNull(latest);
                assertEquals(
                        1,
                        clickCount.get(),
                        "Click action should call the controller; action="
                                + latest.action()
                                + " box="
                                + latest.action().box()
                                + " result="
                                + latest.action().result());
                assertFalse(tasker.running());
                assertTrue(latest.completed());
                assertEquals("Click", latest.action().action());
                assertTrue(latest.action().success());

                assertNotNull(tasker.getNodeDetail(taskDetail.nodeIdList().getFirst()));
                assertNotNull(tasker.getActionDetail(latest.action().actionId()));
                assertTrue(tasker.clearCache());

                TaskJob contextTask = tasker.postTask("ContextSmoke");
                TaskDetail contextDetail = contextTask.waitFor().get();
                assertTrue(contextDetail.status().succeeded(), "Context smoke task should succeed");
                assertTrue(
                        contextChecksOk.get(),
                        "Context.clone/anchor: taskerSame="
                                + taskerSame.get()
                                + " setAnchor="
                                + setAnchorOk.get()
                                + " node="
                                + nodeName.get()
                                + " anchorGot="
                                + anchorGot.get());
                assertTrue(
                        waitFreezesOk.get(),
                        "Typed JWaitFreezes should be accepted by MaaContextWaitFreezes");
            }
        }

        try (Tasker tasker = new Tasker()) {
            assertNotNull(tasker.handle());
            assertFalse(tasker.inited());
        }
    }

    private static void exerciseCustomController() {
        try (CustomController controller = newSmokeController()) {
            assertTrue(controller.postConnection().waitFor().succeeded());
            assertTrue(controller.connected());
            assertEquals("java-smoke", controller.uuid());

            assertTrue(controller.setScreenshotUseRawSize(true));
            MaaImage screen = controller.postScreencap().waitFor().get();
            assertFalse(screen.isEmpty());
            assertEquals(1, screen.width());
            assertEquals(1, screen.height());
            assertArrayEquals(
                    new byte[] {(byte) 0x10, (byte) 0x20, (byte) 0x30}, screen.data());
            assertEquals(Map.of("type", "custom"), controller.info());

            assertTrue(controller.setScreenshotTargetLongSide(1280));
            assertTrue(controller.setScreenshotTargetShortSide(720));
            assertTrue(controller.setScreenshotResizeMethod(1));
        }
    }

    private static void exerciseRuntimeOverrides() throws Exception {
        Path pipeline = Files.createTempFile("maa-java-runtime-overrides-", ".json");
        try {
            Files.writeString(
                    pipeline,
                    """
                    {
                      "ResourceTemplateHit": {
                        "recognition": {
                          "type": "TemplateMatch",
                          "param": {
                            "template": ["JavaResourceTemplate.png"],
                            "threshold": 0.9
                          }
                        },
                        "action": {"type": "DoNothing", "param": {}},
                        "next": []
                      },
                      "ResourceOriginalHit": {
                        "recognition": {"type": "DirectHit", "param": {}},
                        "action": {"type": "DoNothing", "param": {}},
                        "next": []
                      },
                      "ContextEntry": {
                        "recognition": {
                          "type": "Custom",
                          "param": {"custom_recognition": "JavaContextOverrideReco"}
                        },
                        "action": {
                          "type": "Custom",
                          "param": {"custom_action": "JavaContextOverrideAction"}
                        },
                        "next": ["ContextOriginalHit"]
                      },
                      "ContextOriginalHit": {
                        "recognition": {"type": "DirectHit", "param": {}},
                        "action": {"type": "DoNothing", "param": {}},
                        "next": []
                      },
                      "ContextTemplateHit": {
                        "recognition": {
                          "type": "TemplateMatch",
                          "param": {
                            "template": ["JavaRuntimeTemplate.png"],
                            "threshold": 0.9
                          }
                        },
                        "action": {"type": "DoNothing", "param": {}},
                        "next": []
                      },
                      "TaskJobEntry": {
                        "pre_delay": 1000,
                        "recognition": {"type": "DirectHit", "param": {}},
                        "action": {"type": "DoNothing", "param": {}},
                        "next": ["TaskJobOriginalHit"]
                      },
                      "TaskJobOriginalHit": {
                        "recognition": {"type": "DirectHit", "param": {}},
                        "action": {"type": "DoNothing", "param": {}},
                        "next": []
                      },
                      "TaskJobOverrideHit": {
                        "recognition": {"type": "DirectHit", "param": {}},
                        "action": {"type": "DoNothing", "param": {}},
                        "next": []
                      }
                    }
                    """);

            exerciseResourceOverrides(pipeline);
            exerciseContextOverrides(pipeline);
            exerciseTaskJobOverride(pipeline);
            exerciseStopOverride(pipeline);

            try (CustomController controller = newRuntimeSmokeController()) {
                assertTrue(controller.postConnection().waitFor().succeeded());
                assertFalse(
                        controller.setBackgroundManagedKeys(List.of(0x12, 0x34)),
                        "Custom controllers do not support Win32 background managed keys");
                assertFalse(
                        controller.setBackgroundManagedKeys(List.of()),
                        "Custom controllers do not support Win32 background managed keys");
            }
        } finally {
            Files.deleteIfExists(pipeline);
        }
    }

    private static void exerciseResourceOverrides(Path pipeline) throws Exception {
        try (Resource resource = new Resource();
                CustomController controller = newRuntimeSmokeController();
                Tasker tasker = new Tasker()) {
            assertTrue(controller.postConnection().waitFor().succeeded());
            assertTrue(controller.setScreenshotUseRawSize(true));
            assertTrue(resource.postPipeline(pipeline).waitFor().succeeded());
            assertTrue(tasker.bind(resource, controller));

            MaaImage template = gradientBgrImage(10);
            assertTrue(resource.overrideImage("JavaResourceTemplate.png", template));
            assertTrue(resource.overridePipeline(Map.of("ResourceCreatedByOverride", Map.of())));
            assertTrue(resource.overrideNext("ResourceCreatedByOverride", List.of("ResourceTemplateHit")));
            assertTrue(resource.getNodeData("ResourceCreatedByOverride").containsKey("next"));

            TaskJob task = tasker.postTask("ResourceCreatedByOverride");
            TaskDetail detail = task.waitFor().get();
            assertTrue(
                    detail.status().succeeded(),
                    "Resource overrides should make the task succeed; status="
                            + detail.status()
                            + " nodes="
                            + detail.nodes().stream()
                                    .map(node -> node.name() + "/" + node.completed())
                                    .toList());
            assertEquals(
                    List.of("ResourceCreatedByOverride", "ResourceTemplateHit"),
                    detail.nodes().stream().map(NodeDetail::name).toList());
            assertNull(tasker.getLatestNode("ResourceOriginalHit"));
        }
    }

    private static void exerciseContextOverrides(Path pipeline) throws Exception {
        AtomicBoolean contextNextOverridden = new AtomicBoolean();
        AtomicBoolean contextImageOverridden = new AtomicBoolean();
        try (Resource resource = new Resource();
                CustomController controller = newRuntimeSmokeController();
                Tasker tasker = new Tasker()) {
            assertTrue(controller.postConnection().waitFor().succeeded());
            assertTrue(controller.setScreenshotUseRawSize(true));
            assertTrue(resource.registerCustomRecognition(
                    "JavaContextOverrideReco",
                    new CustomRecognition() {
                        @Override
                        public AnalyzeResult analyze(Context context, AnalyzeArg argv) {
                            contextNextOverridden.set(
                                    context.overrideNext("ContextEntry", List.of("ContextTemplateHit")));
                            contextImageOverridden.set(context.overrideImage(
                                    "JavaRuntimeTemplate.png", gradientBgrImage(10)));
                            return AnalyzeResult.hit(MaaRect.of(0, 0, 20, 20));
                        }
                    }));
            assertTrue(resource.registerCustomAction(
                    "JavaContextOverrideAction",
                    new CustomAction() {
                        @Override
                        public RunResult run(Context context, RunArg argv) {
                            return RunResult.ok();
                        }
                    }));
            assertTrue(resource.postPipeline(pipeline).waitFor().succeeded());
            assertTrue(tasker.bind(resource, controller));

            TaskJob task = tasker.postTask("ContextEntry");
            TaskDetail detail = task.waitFor().get();
            assertTrue(detail.status().succeeded(), "Context overrides should make the task succeed");
            assertTrue(contextNextOverridden.get(), "Context.overrideNext should succeed");
            assertTrue(contextImageOverridden.get(), "Context.overrideImage should succeed");
            assertEquals(
                    List.of("ContextEntry", "ContextTemplateHit"),
                    detail.nodes().stream().map(NodeDetail::name).toList());
        }
    }

    private static void exerciseTaskJobOverride(Path pipeline) throws Exception {
        try (Resource resource = new Resource();
                CustomController controller = newRuntimeSmokeController();
                Tasker tasker = new Tasker()) {
            assertTrue(controller.postConnection().waitFor().succeeded());
            assertTrue(controller.setScreenshotUseRawSize(true));
            assertTrue(resource.postPipeline(pipeline).waitFor().succeeded());
            assertTrue(tasker.bind(resource, controller));

            TaskJob task = tasker.postTask("TaskJobEntry");
            assertTrue(task.overridePipeline(Map.of("TaskJobEntry", Map.of("next", List.of("TaskJobOverrideHit")))));
            assertTrue(tasker.running(), "Task should still be running during the pre-delay override");
            TaskDetail detail = task.waitFor().get();
            assertTrue(detail.status().succeeded(), "TaskJob.overridePipeline should not break the task");
            assertEquals(
                    List.of("TaskJobEntry", "TaskJobOverrideHit"),
                    detail.nodes().stream().map(NodeDetail::name).toList());
        }
    }

    private static void exerciseStopOverride(Path pipeline) throws Exception {
        try (Resource resource = new Resource();
                CustomController controller = newRuntimeSmokeController();
                Tasker tasker = new Tasker()) {
            assertTrue(controller.postConnection().waitFor().succeeded());
            assertTrue(controller.setScreenshotUseRawSize(true));
            assertTrue(resource.postPipeline(pipeline).waitFor().succeeded());
            assertTrue(tasker.bind(resource, controller));

            TaskJob task = tasker.postTask("TaskJobEntry");
            assertTrue(tasker.running(), "Task should start before the stop request");
            Job stop = tasker.postStop();
            assertTrue(tasker.stopping(), "postStop should put the tasker in stopping state");
            assertTrue(stop.waitFor().succeeded(), "Stop job should succeed");
            assertFalse(tasker.running(), "Tasker should not be running after stop");
            assertFalse(tasker.stopping(), "Tasker should not be stopping after stop");
        }
    }

    private static MaaImage gradientBgrImage(int size) {
        byte[] data = new byte[size * size * 3];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int index = (y * size + x) * 3;
                data[index] = (byte) (x * 4);
                data[index + 1] = (byte) (y * 4);
                data[index + 2] = (byte) ((x + y) * 3);
            }
        }
        return new MaaImage(data, size, size, 3, MaaImage.TYPE_8UC3);
    }

    private static void exerciseRecordAndReplay() throws Exception {
        Path tempDir = Files.createTempDirectory("maa-java-recording-");
        try {
            Path recording = tempDir.resolve("smoke.jsonl");
            try (CustomController inner = newSmokeController();
                    RecordController record = new RecordController(inner, recording)) {
                assertSame(inner, record.inner());
                assertTrue(record.postConnection().waitFor().succeeded());
                assertTrue(record.connected());
                assertEquals("java-smoke", record.uuid());

                Map<String, Object> recordInfo = record.info();
                assertEquals(Boolean.TRUE, recordInfo.get("recording"));
                assertEquals(recording.toString(), recordInfo.get("recording_path"));

                assertTrue(record.setScreenshotUseRawSize(true));
                MaaImage recorded = record.postScreencap().waitFor().get();
                assertArrayEquals(
                        new byte[] {(byte) 0x10, (byte) 0x20, (byte) 0x30}, recorded.data());
                assertTrue(record.postClick(11, 22).waitFor().succeeded());
            }

            assertTrue(Files.isRegularFile(recording), "RecordController should write JSONL");

            try (ReplayController replay = new ReplayController(recording)) {
                Map<String, Object> replayInfo = replay.info();
                assertEquals("replay", replayInfo.get("type"));
                assertEquals(3, ((Number) replayInfo.get("record_count")).intValue());
                assertEquals(0, ((Number) replayInfo.get("record_index")).intValue());

                assertTrue(replay.postConnection().waitFor().succeeded());
                assertTrue(replay.connected());
                assertEquals("java-smoke", replay.uuid());

                assertTrue(replay.setScreenshotUseRawSize(true));
                MaaImage replayed = replay.postScreencap().waitFor().get();
                assertArrayEquals(
                        new byte[] {(byte) 0x10, (byte) 0x20, (byte) 0x30}, replayed.data());
                assertTrue(replay.postClick(11, 22).waitFor().succeeded());

                Map<String, Object> consumed = replay.info();
                assertEquals(3, ((Number) consumed.get("record_index")).intValue());
            }
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private static void exerciseToolkit() throws Exception {
        Path configDir = Files.createTempDirectory("maa-java-toolkit-");
        try {
            assertTrue(Toolkit.initOption(configDir));
            if (!isMacOs() || Toolkit.macosCheckPermission(MaaDef.MacOSPermission.SCREEN_CAPTURE)) {
                assertNotNull(Toolkit.findDesktopWindows());
            }
        } finally {
            deleteRecursively(configDir);
        }
    }

    private static boolean isMacOs() {
        return System.getProperty("os.name", "").toLowerCase().contains("mac");
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        if (Files.isDirectory(path)) {
            try (var paths = Files.walk(path)) {
                for (Path child : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(child);
                }
            }
        } else {
            Files.deleteIfExists(path);
        }
    }

    private static CustomController newSmokeController() {
        return newSmokeController(null);
    }

    private static CustomController newSmokeController(AtomicInteger clickCount) {
        byte[] bgr = new byte[] {(byte) 0x10, (byte) 0x20, (byte) 0x30};
        return new CustomController() {
            @Override
            public long getFeatures() {
                return 0;
            }

            @Override
            public boolean connect() {
                return true;
            }

            @Override
            public boolean connected() {
                return true;
            }

            @Override
            public String requestUuid() {
                return "java-smoke";
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
                return new MaaImage(bgr, 1, 1, 3, MaaImage.TYPE_8UC3);
            }

            @Override
            public boolean click(int x, int y) {
                if (clickCount != null) {
                    clickCount.incrementAndGet();
                }
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
        };
    }

    private static CustomController newRuntimeSmokeController() {
        MaaImage screen = gradientBgrImage(20);
        return new CustomController() {
            @Override
            public long getFeatures() {
                return 0;
            }

            @Override
            public boolean connect() {
                return true;
            }

            @Override
            public boolean connected() {
                return true;
            }

            @Override
            public String requestUuid() {
                return "java-runtime-smoke";
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
                return screen;
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
        };
    }

    private static void exerciseAgentClient() throws Exception {
        try (AgentClient client = AgentClient.createTcp(0)) {
            String identifier = client.identifier();
            assertNotNull(identifier);
            assertTrue(identifier.matches("\\d+"), "TCP identifier should be a port number");
            assertTrue(client.setTimeout(30_000));

            try (Resource resource = new Resource();
                    CustomController controller = newSmokeController();
                    Tasker tasker = new Tasker()) {
                assertTrue(controller.postConnection().waitFor().succeeded());
                assertTrue(tasker.bind(resource, controller));
                assertTrue(client.bind(resource));
                assertTrue(client.registerSink(resource, controller, tasker));

                Path pipeline = Files.createTempFile("maa-java-agent-pipeline-", ".json");
                try {
                    Files.writeString(
                            pipeline,
                            """
                            {
                              "AgentEntry": {
                                "recognition": {"type": "DirectHit", "param": {}},
                                "action": {"type": "DoNothing", "param": {}},
                                "next": ["AgentCustom"]
                              },
                              "AgentCustom": {
                                "recognition": {
                                  "type": "Custom",
                                  "param": {"custom_recognition": "JavaAgentReco"}
                                },
                                "action": {
                                  "type": "Custom",
                                  "param": {"custom_action": "JavaAgentAction"}
                                },
                                "next": []
                              }
                            }
                            """);
                    assertTrue(resource.postPipeline(pipeline).waitFor().succeeded());
                    assertTrue(resource.loaded());
                } finally {
                    Files.deleteIfExists(pipeline);
                }

                String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
                ProcessBuilder processBuilder = new ProcessBuilder(
                        java,
                        "-cp",
                        System.getProperty("java.class.path"),
                        "-Dmaafw.libDir=" + libraryDir(),
                        "io.github.craun718.maafw.AgentServerProcess",
                        identifier);
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
                Process process = processBuilder.start();

                try {
                    boolean connected = false;
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
                    while (!connected && process.isAlive() && System.nanoTime() < deadline) {
                        connected = client.connect();
                        if (!connected) {
                            Thread.sleep(250);
                        }
                    }
                    assertTrue(connected, "AgentServer did not accept the TCP connection");
                    assertTrue(client.connected());
                    assertTrue(client.alive());

                    assertTrue(resource.customRecognitionList().contains("JavaAgentReco"));
                    assertTrue(resource.customActionList().contains("JavaAgentAction"));
                    assertTrue(client.customRecognitionList().contains("JavaAgentReco"));
                    assertTrue(client.customActionList().contains("JavaAgentAction"));

                    TaskJob task = tasker.postTask("AgentEntry");
                    TaskDetail taskDetail = task.waitFor().get();
                    assertTrue(taskDetail.status().succeeded(), "Agent custom pipeline should succeed");
                    assertFalse(taskDetail.nodeIdList().isEmpty());

                    NodeDetail customNode = tasker.getLatestNode("AgentCustom");
                    assertNotNull(customNode);
                    assertTrue(customNode.completed());
                    assertNotNull(customNode.recognition());
                    assertEquals("Custom", customNode.recognition().algorithm());
                    assertTrue(customNode.recognition().hit());
                    assertEquals(MaaRect.of(10, 20, 30, 40), customNode.recognition().box());
                    assertNotNull(customNode.action());
                    assertEquals("Custom", customNode.action().action());
                    assertTrue(customNode.action().success());

                    assertTrue(client.disconnect());
                    assertTrue(
                            process.waitFor(30, TimeUnit.SECONDS),
                            "AgentServer process should exit after disconnect");
                    assertEquals(0, process.exitValue());
                } finally {
                    client.disconnect();
                    if (process.isAlive()) {
                        process.destroy();
                    }
                }
            }
        }
    }

    private static Path libraryDir() {
        String property = System.getProperty("maafw.libDir");
        if (property != null && !property.isBlank()) {
            return Path.of(property);
        }
        String env = System.getenv("MAA_FRAMEWORK_LIB_DIR");
        return env == null || env.isBlank() ? null : Path.of(env);
    }
}
