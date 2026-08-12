# MaaFramework Java Binding

JNA-based Java binding for [MaaFramework](https://github.com/MaaXYZ/MaaFramework). It targets
the official release native libraries and does not bundle platform binaries.

## Requirements

- Java 21
- A MaaFramework release directory containing the native libraries for the current platform

The binding uses JNA and Jackson through Gradle. This repository currently exposes the binding as
the `:lib` project, so a Gradle app can depend on it with:

```kotlin
dependencies {
    implementation(project(":lib"))
}
```

## Build

```bash
./gradlew :lib:build
```

## Loading Native Libraries

Call `MaaLibrary.open(Path, boolean)` once before using high-level wrappers:

- `MaaLibrary.open(path, false)` loads client mode libraries: `MaaFramework`, `MaaToolkit`, and
  `MaaAgentClient`.
- `MaaLibrary.open(path, true)` loads agent server mode only: `MaaAgentServer`. In this mode,
  `MaaLibrary.framework()` returns the agent server library, while `MaaToolkit` and
  `MaaAgentClient` are not available.

Official release file names are resolved automatically:

| Platform | Client files | Agent server file |
| --- | --- | --- |
| Windows | `MaaFramework.dll`, `MaaToolkit.dll`, `MaaAgentClient.dll` | `MaaAgentServer.dll` |
| macOS | `libMaaFramework.dylib`, `libMaaToolkit.dylib`, `libMaaAgentClient.dylib` | `libMaaAgentServer.dylib` |
| Linux | `libMaaFramework.so`, `libMaaToolkit.so`, `libMaaAgentClient.so` | `libMaaAgentServer.so` |

## Client Mode

```java
MaaLibrary.open(Path.of("bin"), false);
Toolkit.initOption(Path.of("config"), Map.of());

List<AdbDevice> devices = Toolkit.findAdbDevices();
AdbDevice device = devices.getFirst();

try (Controller controller =
                new AdbController(
                        device.adbPath().toString(),
                        device.address(),
                        device.screencapMethods(),
                        device.inputMethods(),
                        device.config(),
                        "MaaAgentBinary");
        Resource resource = new Resource();
        Tasker tasker = new Tasker()) {
    resource.postBundle(Path.of("assets"));
    tasker.bind(resource, controller);
    tasker.postTask("Main", Map.of()).waitFor();
}
```

## Typed Pipeline API

The binding exposes pipeline v2 nodes as typed classes under
`io.github.craun718.maafw.pipeline`. `Resource.getNodeData(String)` returns the raw node map, and
`Resource.getNodeObject(String)` parses it into a `JPipelineData`. `Context` provides the same pair
for the current runtime state.

```java
JPipelineData node = resource.getNodeObject("Main");
JTemplateMatch match = (JTemplateMatch) node.recognition.param;
JClick click = (JClick) node.action.param;
```

Default parameters can be fetched from the resource as typed objects:

```java
JRecognitionParam recoParam =
        resource.getDefaultRecognitionParam(JRecognitionType.TEMPLATE_MATCH);
JActionParam actionParam = resource.getDefaultActionParam(JActionType.CLICK);
```

For direct recognition and action calls, pass the typed enum and parameter object to `Tasker` or
`Context`:

```java
TaskJob recoJob = tasker.postRecognition(JRecognitionType.TEMPLATE_MATCH, recoParam, image);
TaskJob actionJob = tasker.postAction(JActionType.CLICK, actionParam, box);

RecognitionDetail reco = context.runRecognitionDirect(JRecognitionType.OCR, ocrParam, image);
ActionDetail action = context.runActionDirect(JActionType.SHELL, shellParam, box, recoJson);
```

The typed parameter classes serialize with the same snake_case JSON keys expected by MaaFramework,
and `JRecognitionType`/`JActionType` serialize using native names such as `TemplateMatch` and
`Click`.

## Agent Mode

Agent mode splits custom logic from the main process. The main process creates an `AgentClient`,
while a separate server process registers custom recognitions, custom actions, and event sinks
with `AgentServer`.

### Server Process

Register custom implementations before calling `startUp`. The server process loads
`MaaAgentServer` and does not create local `Resource`, `Controller`, or `Tasker` objects.

```java
MaaLibrary.open(Path.of("bin"), true);
Toolkit.initOption(Path.of("config"), Map.of());

AgentServer.registerCustomRecognition(
        "MyReco",
        new CustomRecognition() {
            @Override
            public AnalyzeResult analyze(Context context, AnalyzeArg argv) {
                return AnalyzeResult.hit(MaaRect.of(100, 100, 50, 50));
            }
        });

AgentServer.startUp("my-agent");
AgentServer.join();
AgentServer.shutDown();
```

`identifier` may be a named IPC socket, or a numeric string such as `"12345"` for a TCP port on
`127.0.0.1`.

### Client Process

```java
MaaLibrary.open(Path.of("bin"), false);

try (AgentClient client = new AgentClient("my-agent");
        Resource resource = new Resource();
        Controller controller = new AdbController("adb", "127.0.0.1:5555");
        Tasker tasker = new Tasker()) {
    resource.postBundle(Path.of("assets"));
    tasker.bind(resource, controller);

    client.bind(resource);
    client.registerSink(resource, controller, tasker);
    client.connect();

    tasker.postTask("Main", Map.of()).waitFor();
    client.disconnect();
}
```

`AgentClient.createTcp(0)` creates a TCP client with an automatically selected port. The server
process should use the port returned by `client.identifier()` as its identifier.

## Notes

- `AgentServer` holds registered callbacks and sinks internally so JVM garbage collection cannot
  remove native callbacks. `AgentClient` similarly holds the resource and sinks passed to it.
- In agent server mode, the official `MaaAgentServer` library is a stub for local resource,
  controller, and tasker creation and for plugin loading. Use that mode only to host callbacks
  and sinks.
- The binding is not published to Maven Central yet; use it as an included Gradle project.
