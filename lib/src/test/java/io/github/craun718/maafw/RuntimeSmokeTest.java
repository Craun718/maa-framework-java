package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

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

        exerciseBuffers();
        exerciseResourceAndTasker();
        exerciseCustomController();
        exerciseAgentClient();
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

            Path pipeline = Files.createTempFile("maa-java-pipeline-", ".json");
            try {
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
                          }
                        }
                        """);
                assertTrue(resource.postPipeline(pipeline).waitFor().succeeded());
                assertTrue(resource.loaded());
                assertTrue(resource.nodeList().contains("StartUpAndClickButton"));

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
