package io.github.craun718.maafw;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

/** FFI surface of MaaFramework dynamic library. */
public interface MaaFrameworkLibrary extends Library {

    String MaaVersion();

    byte MaaGlobalSetOption(int key, Pointer value, long valueSize);

    byte MaaGlobalLoadPlugin(String libraryPath);

    byte MaaSetGlobalOption(int key, Pointer value, long valueSize);

    Pointer MaaStringBufferCreate();

    void MaaStringBufferDestroy(Pointer handle);

    byte MaaStringBufferIsEmpty(Pointer handle);

    byte MaaStringBufferClear(Pointer handle);

    String MaaStringBufferGet(Pointer handle);

    long MaaStringBufferSize(Pointer handle);

    byte MaaStringBufferSet(Pointer handle, String value);

    byte MaaStringBufferSetEx(Pointer handle, byte[] value, long size);

    Pointer MaaStringListBufferCreate();

    void MaaStringListBufferDestroy(Pointer handle);

    byte MaaStringListBufferIsEmpty(Pointer handle);

    long MaaStringListBufferSize(Pointer handle);

    Pointer MaaStringListBufferAt(Pointer handle, long index);

    byte MaaStringListBufferAppend(Pointer handle, Pointer value);

    byte MaaStringListBufferRemove(Pointer handle, long index);

    byte MaaStringListBufferClear(Pointer handle);

    Pointer MaaImageBufferCreate();

    void MaaImageBufferDestroy(Pointer handle);

    byte MaaImageBufferIsEmpty(Pointer handle);

    byte MaaImageBufferClear(Pointer handle);

    Pointer MaaImageBufferGetRawData(Pointer handle);

    int MaaImageBufferWidth(Pointer handle);

    int MaaImageBufferHeight(Pointer handle);

    int MaaImageBufferChannels(Pointer handle);

    int MaaImageBufferType(Pointer handle);

    byte MaaImageBufferSetRawData(Pointer handle, Pointer data, int width, int height, int type);

    byte MaaImageBufferResize(Pointer handle, int width, int height);

    Pointer MaaImageBufferGetEncoded(Pointer handle);

    long MaaImageBufferGetEncodedSize(Pointer handle);

    byte MaaImageBufferSetEncoded(Pointer handle, Pointer data, long size);

    Pointer MaaImageListBufferCreate();

    void MaaImageListBufferDestroy(Pointer handle);

    byte MaaImageListBufferIsEmpty(Pointer handle);

    long MaaImageListBufferSize(Pointer handle);

    Pointer MaaImageListBufferAt(Pointer handle, long index);

    byte MaaImageListBufferAppend(Pointer handle, Pointer value);

    byte MaaImageListBufferRemove(Pointer handle, long index);

    byte MaaImageListBufferClear(Pointer handle);

    Pointer MaaRectCreate();

    void MaaRectDestroy(Pointer handle);

    int MaaRectGetX(Pointer handle);

    int MaaRectGetY(Pointer handle);

    int MaaRectGetW(Pointer handle);

    int MaaRectGetH(Pointer handle);

    byte MaaRectSet(Pointer handle, int x, int y, int w, int h);

    Pointer MaaResourceCreate();

    void MaaResourceDestroy(Pointer resource);

    long MaaResourceAddSink(Pointer resource, MaaCallbacks.EventCallback sink, Pointer transArg);

    void MaaResourceRemoveSink(Pointer resource, long sinkId);

    void MaaResourceClearSinks(Pointer resource);

    byte MaaResourceRegisterCustomRecognition(
            Pointer resource,
            String name,
            MaaCallbacks.CustomRecognitionCallback recognition,
            Pointer transArg);

    byte MaaResourceUnregisterCustomRecognition(Pointer resource, String name);

    byte MaaResourceClearCustomRecognition(Pointer resource);

    byte MaaResourceRegisterCustomAction(
            Pointer resource,
            String name,
            MaaCallbacks.CustomActionCallback action,
            Pointer transArg);

    byte MaaResourceUnregisterCustomAction(Pointer resource, String name);

    byte MaaResourceClearCustomAction(Pointer resource);

    long MaaResourcePostBundle(Pointer resource, String path);

    long MaaResourcePostOcrModel(Pointer resource, String path);

    long MaaResourcePostPipeline(Pointer resource, String path);

    long MaaResourcePostImage(Pointer resource, String path);

    byte MaaResourceOverridePipeline(Pointer resource, String pipelineOverride);

    byte MaaResourceOverrideNext(Pointer resource, String nodeName, Pointer nextList);

    byte MaaResourceOverrideImage(Pointer resource, String imageName, Pointer image);

    byte MaaResourceGetNodeData(Pointer resource, String nodeName, Pointer buffer);

    byte MaaResourceClear(Pointer resource);

    int MaaResourceStatus(Pointer resource, long id);

    int MaaResourceWait(Pointer resource, long id);

    byte MaaResourceLoaded(Pointer resource);

    byte MaaResourceSetOption(Pointer resource, int key, Pointer value, long valueSize);

    byte MaaResourceGetHash(Pointer resource, Pointer buffer);

    byte MaaResourceGetNodeList(Pointer resource, Pointer buffer);

    byte MaaResourceGetCustomRecognitionList(Pointer resource, Pointer buffer);

    byte MaaResourceGetCustomActionList(Pointer resource, Pointer buffer);

    byte MaaResourceGetDefaultRecognitionParam(Pointer resource, String recoType, Pointer buffer);

    byte MaaResourceGetDefaultActionParam(Pointer resource, String actionType, Pointer buffer);

    Pointer MaaAdbControllerCreate(
            String adbPath,
            String address,
            long screencapMethods,
            long inputMethods,
            String config,
            String agentPath);

    Pointer MaaWin32ControllerCreate(
            Pointer windowHandle,
            long screencapMethod,
            long mouseMethod,
            long keyboardMethod);

    Pointer MaaMacOSControllerCreate(int windowId, long screencapMethod, long inputMethod);

    Pointer MaaAndroidNativeControllerCreate(String configJson);

    Pointer MaaCustomControllerCreate(MaaCallbacks.CustomControllerCallbacks controller, Pointer controllerArg);

    Pointer MaaDbgControllerCreate(String readPath);

    Pointer MaaReplayControllerCreate(String recordingPath);

    Pointer MaaRecordControllerCreate(Pointer innerController, String recordingPath);

    Pointer MaaPlayCoverControllerCreate(String address, String uuid);

    Pointer MaaWlRootsControllerCreate(String wlrSocketPath, byte useWin32VkCode);

    Pointer MaaKWinControllerCreate(
            String deviceNode,
            int screenWidth,
            int screenHeight,
            byte useWin32VkCode);

    Pointer MaaLinuxControllerCreate(String configJson);

    Pointer MaaGamepadControllerCreate(
            Pointer windowHandle,
            long gamepadType,
            long screencapMethod);

    void MaaControllerDestroy(Pointer controller);

    long MaaControllerAddSink(Pointer controller, MaaCallbacks.EventCallback sink, Pointer transArg);

    void MaaControllerRemoveSink(Pointer controller, long sinkId);

    void MaaControllerClearSinks(Pointer controller);

    byte MaaControllerSetOption(Pointer controller, int key, Pointer value, long valueSize);

    long MaaControllerPostConnection(Pointer controller);

    long MaaControllerPostClick(Pointer controller, int x, int y);

    long MaaControllerPostClickV2(Pointer controller, int x, int y, int contact, int pressure);

    long MaaControllerPostSwipe(Pointer controller, int x1, int y1, int x2, int y2, int duration);

    long MaaControllerPostSwipeV2(
            Pointer controller,
            int x1,
            int y1,
            int x2,
            int y2,
            int duration,
            int contact,
            int pressure);

    long MaaControllerPostClickKey(Pointer controller, int keycode);

    long MaaControllerPostInputText(Pointer controller, String text);

    long MaaControllerPostStartApp(Pointer controller, String intent);

    long MaaControllerPostStopApp(Pointer controller, String intent);

    long MaaControllerPostTouchDown(Pointer controller, int contact, int x, int y, int pressure);

    long MaaControllerPostTouchMove(Pointer controller, int contact, int x, int y, int pressure);

    long MaaControllerPostTouchUp(Pointer controller, int contact);

    long MaaControllerPostRelativeMove(Pointer controller, int dx, int dy);

    long MaaControllerPostKeyDown(Pointer controller, int keycode);

    long MaaControllerPostKeyUp(Pointer controller, int keycode);

    long MaaControllerPostScreencap(Pointer controller);

    long MaaControllerPostScroll(Pointer controller, int dx, int dy);

    long MaaControllerPostInactive(Pointer controller);

    long MaaControllerPostShell(Pointer controller, String command, long timeout);

    byte MaaControllerGetShellOutput(Pointer controller, Pointer buffer);

    int MaaControllerStatus(Pointer controller, long id);

    int MaaControllerWait(Pointer controller, long id);

    byte MaaControllerConnected(Pointer controller);

    byte MaaControllerCachedImage(Pointer controller, Pointer buffer);

    byte MaaControllerGetUuid(Pointer controller, Pointer buffer);

    byte MaaControllerGetResolution(Pointer controller, IntByReference width, IntByReference height);

    byte MaaControllerGetInfo(Pointer controller, Pointer buffer);

    long MaaControllerPostPressKey(Pointer controller, int keycode);

    Pointer MaaTaskerCreate();

    void MaaTaskerDestroy(Pointer tasker);

    long MaaTaskerAddSink(Pointer tasker, MaaCallbacks.EventCallback sink, Pointer transArg);

    void MaaTaskerRemoveSink(Pointer tasker, long sinkId);

    void MaaTaskerClearSinks(Pointer tasker);

    long MaaTaskerAddContextSink(Pointer tasker, MaaCallbacks.EventCallback sink, Pointer transArg);

    void MaaTaskerRemoveContextSink(Pointer tasker, long sinkId);

    void MaaTaskerClearContextSinks(Pointer tasker);

    byte MaaTaskerSetOption(Pointer tasker, int key, Pointer value, long valueSize);

    byte MaaTaskerBindResource(Pointer tasker, Pointer resource);

    byte MaaTaskerBindController(Pointer tasker, Pointer controller);

    byte MaaTaskerInited(Pointer tasker);

    long MaaTaskerPostTask(Pointer tasker, String entry, String pipelineOverride);

    long MaaTaskerPostRecognition(
            Pointer tasker,
            String recoType,
            String recoParam,
            Pointer image);

    long MaaTaskerPostAction(
            Pointer tasker,
            String actionType,
            String actionParam,
            Pointer box,
            String recoDetail);

    int MaaTaskerStatus(Pointer tasker, long id);

    int MaaTaskerWait(Pointer tasker, long id);

    byte MaaTaskerRunning(Pointer tasker);

    long MaaTaskerPostStop(Pointer tasker);

    byte MaaTaskerStopping(Pointer tasker);

    Pointer MaaTaskerGetResource(Pointer tasker);

    Pointer MaaTaskerGetController(Pointer tasker);

    byte MaaTaskerClearCache(Pointer tasker);

    byte MaaTaskerOverridePipeline(Pointer tasker, long taskId, String pipelineOverride);

    byte MaaTaskerGetRecognitionDetail(
            Pointer tasker,
            long recoId,
            Pointer nodeName,
            Pointer algorithm,
            ByteByReference hit,
            Pointer box,
            Pointer detailJson,
            Pointer raw,
            Pointer draws);

    byte MaaTaskerGetActionDetail(
            Pointer tasker,
            long actionId,
            Pointer nodeName,
            Pointer action,
            Pointer box,
            ByteByReference success,
            Pointer detailJson);

    byte MaaTaskerGetWaitFreezesDetail(
            Pointer tasker,
            long wfId,
            Pointer nodeName,
            Pointer phase,
            ByteByReference success,
            LongByReference elapsedMs,
            Pointer recoIdList,
            LongByReference recoIdListSize,
            Pointer roi);

    byte MaaTaskerGetNodeDetail(
            Pointer tasker,
            long nodeId,
            Pointer nodeName,
            LongByReference recoId,
            LongByReference actionId,
            ByteByReference completed);

    byte MaaTaskerGetTaskDetail(
            Pointer tasker,
            long taskId,
            Pointer entry,
            Pointer nodeIdList,
            LongByReference nodeIdListSize,
            IntByReference status);

    byte MaaTaskerGetLatestNode(Pointer tasker, String nodeName, LongByReference latestId);

    long MaaContextRunTask(Pointer context, String entry, String pipelineOverride);

    long MaaContextRunRecognition(
            Pointer context,
            String entry,
            String pipelineOverride,
            Pointer image);

    long MaaContextRunAction(
            Pointer context,
            String entry,
            String pipelineOverride,
            Pointer box,
            String recoDetail);

    long MaaContextRunRecognitionDirect(
            Pointer context,
            String recoType,
            String recoParam,
            Pointer image);

    long MaaContextRunActionDirect(
            Pointer context,
            String actionType,
            String actionParam,
            Pointer box,
            String recoDetail);

    byte MaaContextWaitFreezes(
            Pointer context,
            long time,
            Pointer box,
            String waitFreezesParam);

    byte MaaContextOverridePipeline(Pointer context, String pipelineOverride);

    byte MaaContextOverrideNext(Pointer context, String nodeName, Pointer nextList);

    byte MaaContextOverrideImage(Pointer context, String imageName, Pointer image);

    byte MaaContextGetNodeData(Pointer context, String nodeName, Pointer buffer);

    long MaaContextGetTaskId(Pointer context);

    Pointer MaaContextGetTasker(Pointer context);

    Pointer MaaContextClone(Pointer context);

    byte MaaContextSetAnchor(Pointer context, String anchorName, String nodeName);

    byte MaaContextGetAnchor(Pointer context, String anchorName, Pointer buffer);

    byte MaaContextGetHitCount(Pointer context, String nodeName, LongByReference count);

    byte MaaContextClearHitCount(Pointer context, String nodeName);
}
