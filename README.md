# MaaFramework Java Binding

JNA 实现的 Java 绑定，面向 [MaaFramework](https://github.com/MaaXYZ/MaaFramework)。它对接官方 release 中的原生库，不随仓库打包平台二进制文件。

English [README.en.md](README.en.md)。

## 环境要求

- Java 21
- 包含当前平台原生库的 MaaFramework release 目录

绑定通过 Gradle 使用 JNA 和 Jackson。当前仓库以 `:lib` 项目形式提供绑定，Gradle 应用可以这样依赖：

```kotlin
dependencies {
    implementation(project(":lib"))
}
```

## 构建

```bash
./gradlew :lib:build
```

## 示例

`examples` Gradle 项目包含可运行的 Java 示例，源码位于 `examples/src/main/java/io/github/craun718/maafw/examples`。第一个参数是 MaaFramework release 的 `bin` 目录；agent server 示例还会接收第二个参数作为连接标识。

```bash
./gradlew :examples:run --args="/path/to/MaaFramework/bin"
./gradlew :examples:runQuickStart --args="/path/to/MaaFramework/bin"
./gradlew :examples:runCustomRecognition --args="/path/to/MaaFramework/bin"
./gradlew :examples:runCustomAction --args="/path/to/MaaFramework/bin"
./gradlew :examples:runAgentClient --args="/path/to/MaaFramework/bin"
./gradlew :examples:runAgentServer --args="/path/to/MaaFramework/bin my-agent"
```

`QuickStart`、`CustomRecognitionExample`、`CustomActionExample` 和 `AgentClientExample` 需要 ADB 设备或正在运行的 `AgentServerExample`。资源 JSON 文件从示例 classpath 加载。

## 官方 release 分发

Java jar 本身是平台中立的，不包含原生二进制。若要像官方 MaaFramework release 一样分发，可把各平台的 release 目录解压到同一个目录，然后运行：

```bash
MAA_FRAMEWORK_VERSION=v5.12.3 \
MAA_FRAMEWORK_RELEASES=/path/to/extracted-releases \
./scripts/package-official-release.sh
```

脚本会识别 `MAA-win-*`、`MAA-linux-*`、`MAA-macos-*` 和 `MAA-android-*` 目录，包括名称带 release tag 的解压目录，例如 `MAA-macos-aarch64-v5.12.3`。对每个可用平台，脚本会构建 `lib/maa-framework-java.jar`，并复制完整的官方 release 目录树，包括 `bin/`、`include/`、`symbols/`、文档、示例、schema、许可证和 `share/MaaAgentBinary/`。每个 zip 写入：

```text
build/distributions/maa-framework-java-${MAA_FRAMEWORK_VERSION}-${platform}.zip
```

输出布局与官方 release 范围保持一致：

```text
lib/maa-framework-java.jar
lib/README.md
lib/README.en.md
LICENSE.md
README.md
README.en.md
bin/...
docs/...
include/...
sample/...
share/MaaAgentBinary/...
symbols/...
tools/...
```

`MAA_FRAMEWORK_OUTPUT_DIR` 可覆盖输出目录。脚本的第一个参数也可以代替 `MAA_FRAMEWORK_RELEASES`。

## FFI 表面验证

`scripts/check-ffi-surface.sh` 会比较官方 C 头文件中的导出函数名、参数类型和返回类型与 JNA 接口方法。它覆盖 `MaaFramework`、`MaaToolkit`、`MaaAgentClient` 和 `MaaAgentServer`。`MaaControlUnit` 被有意排除，因为这些函数随独立插件库发布，不在官方 release 核心库中。

同一检查既可用于源码检出，也可用于解压后的官方 release 目录。当指向较旧 release（例如 `v5.12.3`）时，current-main 中的新增 API `MaaLinuxControllerCreate` 和 `MaaToolkitPortalHelper*` 被视为已知的向前兼容扩展：release 中的每个符号仍必须被覆盖，但这些新增项不会导致检查失败。

```bash
./scripts/check-ffi-surface.sh /path/to/MaaFramework
# or
MAA_FRAMEWORK_SOURCE=/path/to/MaaFramework ./scripts/check-ffi-surface.sh
# or an extracted release directory
./scripts/check-ffi-surface.sh /path/to/MAA-macos-aarch64-v5.12.3
```

同样的检查也可以通过 `FfiSurfaceTest` 运行；只有设置了 `MAA_FRAMEWORK_SOURCE` 或 `maafw.maaFrameworkSource` 时才运行，否则跳过。

## 完整性与对齐

本绑定以当前 MaaFramework 头文件、文档和 Python 绑定为跟踪目标：

- `check-ffi-surface.sh` 验证 `MaaFramework`、`MaaToolkit`、`MaaAgentClient` 和 `MaaAgentServer` 中的每个导出函数，包括参数和返回签名；`MaaControlUnit` 仅因这些函数随独立插件库发布而排除。针对较旧 release 时，`MaaLinuxControllerCreate` 和 `MaaToolkitPortalHelper*` 等 current-main 新增项按文档中的向前兼容扩展处理。
- 高层封装对应 Python 绑定中的 `Resource`、`Tasker`、`Context`、controller 子类、`Toolkit`、buffer、事件 sink、自定义识别/动作/controller 回调，以及 `AgentClient`/`AgentServer` API。
- `BlankController` 提供纯 Java 的无操作自定义 controller，用于在无设备时测试 resource/tasker，对应 Go 绑定的便捷 API。与 `DbgController` 不同，它不包装原生 debug controller。
- `pipeline` 包将 project interface v2 的节点 JSON 解析为类型化识别和动作参数，包括嵌套 `And`/`Or`、`MultiSwipe`、wait-freezes、`focus` 和 `attach`，而不是只暴露原始 JSON。
- `package-official-release.sh` 构建与官方 MaaFramework release 相同顶层结构的分发包，包括 `bin/`、controller 插件、`include/`、`symbols/`、文档/示例/schema 和 `share/MaaAgentBinary/`。

MaaFramework 集成文档中的 Java 行描述的是较旧的第三方 v3 绑定；本仓库面向当前 API，并覆盖上述范围。

## 运行时冒烟测试

`RuntimeSmokeTest` 会针对真实库目录验证 release ABI：版本查询、UTF-8 buffer、rect/image buffer、resource/tasker 创建、类型化直接识别/动作调用、资源生命周期操作、自定义 controller 回调、record/replay controller、toolkit 选项/设备辅助、AgentClient TCP 往返和 agent server 注册。未配置库目录时测试会跳过。

该套件已针对官方 `v5.12.3` macOS aarch64 release 验证：16 个 suite 共 74 个测试全部通过，包括 `BlankController`、针对 current-main 源码的 FFI 签名检查，以及官方 release 打包路径。

```bash
MAA_FRAMEWORK_LIB_DIR=/path/to/release/bin ./gradlew :lib:test
```

## 加载原生库

使用高层封装前，先调用一次 `MaaLibrary.open(Path, boolean)`：

- `MaaLibrary.open(path, false)` 加载客户端模式库：`MaaFramework`、`MaaToolkit` 和 `MaaAgentClient`。
- `MaaLibrary.open(path, true)` 仅加载 agent server 模式：`MaaAgentServer`。此模式下 `MaaLibrary.framework()` 返回 agent server 库，`MaaToolkit` 和 `MaaAgentClient` 不可用。

官方 release 文件名会自动解析：

| 平台 | 客户端文件 | agent server 文件 |
| --- | --- | --- |
| Windows | `MaaFramework.dll`、`MaaToolkit.dll`、`MaaAgentClient.dll` | `MaaAgentServer.dll` |
| macOS | `libMaaFramework.dylib`、`libMaaToolkit.dylib`、`libMaaAgentClient.dylib` | `libMaaAgentServer.dylib` |
| Linux | `libMaaFramework.so`、`libMaaToolkit.so`、`libMaaAgentClient.so` | `libMaaAgentServer.so` |

`MaaLibrary.close()` 会释放已加载的库引用，用于测试或在不同 release 二进制之间切换。调用前请先关闭所有 resource、controller、tasker、agent client/server、buffer 和 sink 封装。

`MaaLibrary.libraryDirectory()` 返回传给 `open` 的目录。当目录是官方 release 布局中的 `bin/` 时，`MaaLibrary.defaultAgentBinaryPath()` 会自动解析同级 `share/MaaAgentBinary/` 目录。默认的 `AdbController` 构造器使用解析出的路径；接受显式 `Path agentPath` 的重载可以覆盖它。

## 客户端模式

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

## 类型化 Pipeline API

绑定将 pipeline v2 节点以类型化类暴露在 `io.github.craun718.maafw.pipeline` 下。`Resource.getNodeData(String)` 返回原始节点 map，`Resource.getNodeObject(String)` 将其解析为 `JPipelineData`。`Context` 对当前运行时状态提供相同的两个方法。

```java
JPipelineData node = resource.getNodeObject("Main");
JTemplateMatch match = (JTemplateMatch) node.recognition.param;
JClick click = (JClick) node.action.param;
```

可以从 resource 中以类型化对象获取默认参数：

```java
JRecognitionParam recoParam =
        resource.getDefaultRecognitionParam(JRecognitionType.TEMPLATE_MATCH);
JActionParam actionParam = resource.getDefaultActionParam(JActionType.CLICK);
```

直接识别和动作调用时，向 `Tasker` 或 `Context` 传入类型化枚举和参数对象：

```java
TaskJob recoJob = tasker.postRecognition(JRecognitionType.TEMPLATE_MATCH, recoParam, image);
TaskJob actionJob = tasker.postAction(JActionType.CLICK, actionParam, box);

RecognitionDetail reco = context.runRecognitionDirect(JRecognitionType.OCR, ocrParam, image);
ActionDetail action = context.runActionDirect(JActionType.SHELL, shellParam, box, recoJson);
```

构建器 API 对应 Go 绑定的 `Pipeline`/`Node` 模型。`JPipeline` 按名称保存节点，`JPipelineData.name` 不写入 JSON，`JPipeline.toJson()` 输出原生 pipeline map 形式：

```java
JTemplateMatch match = new JTemplateMatch();
match.template = List.of("start.png");

JClick click = new JClick();
click.target = List.of(100, 200);

JPipeline pipeline = new JPipeline();
pipeline.add(
        new JPipelineData()
                .name("Startup")
                .recognition(JRecognition.templateMatch(match))
                .action(JAction.click(click))
                .addNext("Idle")
                .addNext(JNodeAttr.of("Retry", true, true))
                .addOnError("Fail")
                .addAnchor("entry")
                .timeout(5000));
pipeline.add("Idle", new JPipelineData().action(JAction.doNothing()));

String pipelineJson = pipeline.toJson();
```

`JPipeline.fromJson(String)` 和 `JPipelineParser.parseAll(String)` 会将同一完整节点 map 解析回类型化节点。`JRecognition` 和 `JAction` 工厂方法覆盖全部 pipeline v2 识别/动作类型。

类型化参数类使用 MaaFramework 期望的 snake_case JSON 键序列化，`JRecognitionType`/`JActionType` 使用 `TemplateMatch`、`Click` 等原生名称序列化。`RuntimeSmokeTest` 也使用 release 二进制覆盖了直接 tasker/context 调用。

## Buffer 与事件 Sink

原生 buffer 使用 `AutoCloseable` 类型包装。`MaaStringBuffer` 和 `MaaStringListBuffer` 交换 UTF-8 文本，`MaaImageBuffer` 和 `MaaImageListBuffer` 交换原始 BGR 图像数据，`MaaRectBuffer` 交换矩形。图像数据会复制为不可变的 `MaaImage` 值，因此修改传入或 `data()` 返回的字节数组不会改变 buffer 值。

```java
try (MaaImageBuffer buffer = new MaaImageBuffer()) {
    buffer.set(MaaImage.empty());
    boolean empty = buffer.empty();
}
```

事件 sink 通过继承对应的 sink 类型并重写通知方法实现。所有者存活期间应保留返回的 sink id；绑定只在封装注册期间持有 Java 回调。

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

结果模型是纯 Java 值：`RecognitionDetail`、`RecognitionResult`、`ActionDetail`、`ActionResult`、`TaskDetail`、`NodeDetail` 和 `WaitFreezesDetail` 可以在任务完成后直接检查，或在回调中检查，而无需保持原生 buffer 打开。

## Agent 模式

Agent 模式把自定义逻辑与主进程分离。主进程创建 `AgentClient`，独立 server 进程使用 `AgentServer` 注册自定义识别、自定义动作和事件 sink。

### Server 进程

在调用 `startUp` 前注册自定义实现。server 进程加载 `MaaAgentServer`，不创建本地 `Resource`、`Controller` 或 `Tasker` 对象。

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

`identifier` 可以是具名 IPC socket，也可以是 `"12345"` 这样的数字字符串，表示 `127.0.0.1` 上的 TCP 端口。

### Client 进程

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

`AgentClient.createTcp(0)` 创建自动选择端口的 TCP client。server 进程应使用 `client.identifier()` 返回的端口作为标识。传给 `AgentClient.createTcp(int)` 的端口会在任何原生调用前校验为 `0..65535`；使用 `0` 表示自动选择端口。

## 注意事项

- `AgentServer` 会在内部持有已注册的回调和 sink，避免 JVM 垃圾回收移除原生回调。`AgentClient` 同样持有传入的 resource 和 sink。
- 自定义识别和动作回调在调用 Java 代码前会解析当前 task/recognition 详情。自定义动作收到可空的 recognition 详情，因为仅含动作的 pipeline 节点使用零识别 id；任务详情缺失时回调会在不调用 Java 方法的情况下返回失败。
- 在 agent server 模式下，官方 `MaaAgentServer` 库对本地 resource、controller、tasker 创建和插件加载是 stub。该模式只应用于承载回调和 sink。
- 绑定尚未发布到 Maven Central；请以 included Gradle project 的方式使用。
