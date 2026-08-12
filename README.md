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

## Examples

The `examples` Gradle project contains runnable Java samples under
`examples/src/main/java/io/github/craun718/maafw/examples`. The first argument is the MaaFramework
release `bin` directory; the agent server example also accepts the connection identifier as the
second argument.

```bash
./gradlew :examples:run --args="/path/to/MaaFramework/bin"
./gradlew :examples:runQuickStart --args="/path/to/MaaFramework/bin"
./gradlew :examples:runCustomRecognition --args="/path/to/MaaFramework/bin"
./gradlew :examples:runCustomAction --args="/path/to/MaaFramework/bin"
./gradlew :examples:runAgentClient --args="/path/to/MaaFramework/bin"
./gradlew :examples:runAgentServer --args="/path/to/MaaFramework/bin my-agent"
```

`QuickStart`, `CustomRecognitionExample`, `CustomActionExample`, and `AgentClientExample` expect an
ADB device or a running `AgentServerExample`. The resource JSON files are loaded from the example
classpath.

## Official Release Distribution

The Java jar itself is platform-neutral and does not bundle native binaries. To distribute it
with the same files as an official MaaFramework release, extract the per-platform release
directories into one folder and run:

```bash
MAA_FRAMEWORK_VERSION=v5.12.3 \
MAA_FRAMEWORK_RELEASES=/path/to/extracted-releases \
./scripts/package-official-release.sh
```

The script recognizes `MAA-win-*`, `MAA-linux-*`, `MAA-macos-*`, and `MAA-android-*`
directories, including extracted folders whose names carry a release tag such as
`MAA-macos-aarch64-v5.12.3`. For every available platform it builds
`lib/maa-framework-java.jar` and copies the complete official release tree, including
`bin/`, `include/`, `symbols/`, documentation, samples, schemas, licenses, and
`share/MaaAgentBinary/`. Each zip is written to:

```text
build/distributions/maa-framework-java-${MAA_FRAMEWORK_VERSION}-${platform}.zip
```

The output layout mirrors the official release scope:

```text
lib/maa-framework-java.jar
lib/README.md
LICENSE.md
README.md
README_en.md
bin/...
docs/...
include/...
sample/...
share/MaaAgentBinary/...
symbols/...
tools/...
```

`MAA_FRAMEWORK_OUTPUT_DIR` overrides the output directory. The first argument to the script can
also be used instead of `MAA_FRAMEWORK_RELEASES`.

## FFI Surface Verification

`scripts/check-ffi-surface.sh` compares exported function names, parameter types, and return types
in the official C headers with the JNA interface methods. It covers `MaaFramework`, `MaaToolkit`,
`MaaAgentClient`, and `MaaAgentServer`. `MaaControlUnit` is intentionally excluded because those
functions ship in separate plugin libraries, not in the official release core libraries.

The same check works against either a source checkout or an extracted official release directory.
When it is pointed at an older release such as `v5.12.3`, the newer current-main APIs
`MaaLinuxControllerCreate` and `MaaToolkitPortalHelper*` are treated as known forward-compatible
extras: every release symbol must still be covered, but those extras do not fail the check.

```bash
./scripts/check-ffi-surface.sh /path/to/MaaFramework
# or
MAA_FRAMEWORK_SOURCE=/path/to/MaaFramework ./scripts/check-ffi-surface.sh
# or an extracted release directory
./scripts/check-ffi-surface.sh /path/to/MAA-macos-aarch64-v5.12.3
```

The same check is available as `FfiSurfaceTest`; it runs when `MAA_FRAMEWORK_SOURCE` or
`maafw.maaFrameworkSource` is set, and skips otherwise.

## Completeness and Parity

This binding is tracked against the current MaaFramework headers, documentation, and Python
binding:

- `check-ffi-surface.sh` verifies every exported function in `MaaFramework`, `MaaToolkit`,
  `MaaAgentClient`, and `MaaAgentServer`, including parameter and return signatures;
  `MaaControlUnit` is excluded only because those functions ship in separate plugin libraries.
  Against an older release, current-main additions such as `MaaLinuxControllerCreate` and
  `MaaToolkitPortalHelper*` are allowed as documented forward-compatible extras.
- The high-level wrappers mirror the Python binding's `Resource`, `Tasker`, `Context`, controller
  subclasses, `Toolkit`, buffers, event sinks, custom recognition/action/controller callbacks,
  and `AgentClient`/`AgentServer` APIs.
- The `pipeline` package parses project interface v2 node JSON into typed recognition and action
  parameters, including nested `And`/`Or`, `MultiSwipe`, wait-freezes, `focus`, and `attach`
  values, instead of exposing only raw JSON.
- `package-official-release.sh` builds distributions with the same top-level release tree as
  official MaaFramework releases, including `bin/`, controller plugins, `include/`, `symbols/`,
  docs/samples/schemas, and `share/MaaAgentBinary/`.

The Java row in MaaFramework's integration documentation describes an older third-party v3
binding; this repository targets the current API and covers the surfaces listed above.

## Runtime Smoke Tests

`RuntimeSmokeTest` exercises the release ABI against a real library directory: version lookup,
UTF-8 buffers, rect/image buffers, resource/tasker creation, typed direct recognition/action calls,
resource lifecycle operations, custom controller callbacks, record/replay controllers, toolkit
option/device helpers, an AgentClient TCP round-trip, and agent server registration. It is skipped
unless the library directory is configured:

```bash
MAA_FRAMEWORK_LIB_DIR=/path/to/release/bin ./gradlew :lib:test
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

`MaaLibrary.close()` drops the loaded library references for tests or for swapping release
binaries. Close all resource, controller, tasker, agent client/server, buffer, and sink wrappers
before calling it.

`MaaLibrary.libraryDirectory()` returns the directory passed to `open`. When the directory is the
official release layout's `bin/`, `MaaLibrary.defaultAgentBinaryPath()` resolves the sibling
`share/MaaAgentBinary/` directory automatically. The default `AdbController` constructors use
that resolved path; the overload taking an explicit `Path agentPath` overrides it.

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
                        device.config());
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
`Click`. The direct tasker/context calls are also covered by `RuntimeSmokeTest` against release
binaries.

## Buffers and Event Sinks

Native buffers are wrapped with `AutoCloseable` types. `MaaStringBuffer` and
`MaaStringListBuffer` exchange UTF-8 text, `MaaImageBuffer` and `MaaImageListBuffer` exchange
raw BGR image data, and `MaaRectBuffer` exchanges rectangles. Image data is copied into
immutable `MaaImage` values, so mutating the byte array passed in or returned by `data()` never
changes the buffer value.

```java
try (MaaImageBuffer buffer = new MaaImageBuffer()) {
    buffer.set(MaaImage.empty());
    boolean empty = buffer.empty();
}
```

Event sinks are implemented by subclassing the matching sink type and overriding the notification
methods. Keep the returned sink id while the owner is alive; the binding holds the Java callback
only while the wrapper is registered.

```java
TaskerEventSink sink = new TaskerEventSink() {
    @Override
    public void onTaskerTask(
            Tasker tasker,
            MaaDef.NotificationType notificationType,
            TaskerEventSink.TaskerTaskDetail detail) {
        System.out.println(detail.entry() + " -> " + notificationType);
    }
};

Long sinkId = tasker.addSink(sink);
// tasker.addContextSink(new ContextEventSink() { ... }) for pipeline node events
// resource.addSink(...) and controller.addSink(...) follow the same pattern
tasker.removeSink(sinkId);
```

Result models are pure Java values: `RecognitionDetail`, `RecognitionResult`, `ActionDetail`,
`ActionResult`, `TaskDetail`, `NodeDetail`, and `WaitFreezesDetail` can be inspected directly
after a job completes or from a callback without holding a native buffer open.

## Agent Mode

Agent mode splits custom logic from the main process. The main process creates an `AgentClient`,
while a separate server process registers custom recognitions, custom actions, and event sinks
with `AgentServer`.

### Server Process

Register custom implementations before calling `startUp`. The server process loads
`MaaAgentServer` and does not create local `Resource`, `Controller`, or `Tasker` objects.

```java
MaaLibrary.open(Path.of("bin"), true);

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
process should use the port returned by `client.identifier()` as its identifier. Ports passed to
`AgentClient.createTcp(int)` are validated as `0..65535` before any native call; use `0` for an
automatically selected port.

## Notes

- `AgentServer` holds registered callbacks and sinks internally so JVM garbage collection cannot
  remove native callbacks. `AgentClient` similarly holds the resource and sinks passed to it.
- Custom recognition and action callbacks resolve current task/recognition details before invoking
  Java code. Custom actions receive a nullable recognition detail because action-only pipeline
  nodes use a zero recognition id; missing task details still cause the callback to return failure
  without calling the Java method.
- In agent server mode, the official `MaaAgentServer` library is a stub for local resource,
  controller, and tasker creation and for plugin loading. Use that mode only to host callbacks
  and sinks.
- The binding is not published to Maven Central yet; use it as an included Gradle project.
