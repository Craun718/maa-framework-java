package io.github.craun718.maafw;

import com.sun.jna.Pointer;
import java.util.Map;

/**
 * Base class for custom MaaFramework pipeline recognitions.
 *
 * <p>The native callback resolves the current {@link TaskDetail} before invoking {@link #analyze}.
 * If the detail cannot be resolved, the callback returns failure and {@code analyze} is not called.
 * This matches the Python binding's behavior.
 */
public abstract class CustomRecognition {

    public record AnalyzeArg(TaskDetail taskDetail, String nodeName, String customRecognitionName, String customRecognitionParam,
            MaaImage image, MaaRect roi) {
    }

    public record AnalyzeResult(MaaRect box, Map<String, Object> detail) {

        public static AnalyzeResult hit(MaaRect box) {
            return new AnalyzeResult(box, Map.of());
        }

        public static AnalyzeResult miss() {
            return new AnalyzeResult(null, Map.of());
        }
    }

    private final MaaCallbacks.CustomRecognitionCallback callback = this::invoke;

    protected CustomRecognition() {
    }

    public abstract AnalyzeResult analyze(Context context, AnalyzeArg argv);

    MaaCallbacks.CustomRecognitionCallback callback() {
        return callback;
    }

    private byte invoke(Pointer contextHandle, long taskId, String nodeName, String customRecognitionName, String customRecognitionParam,
            Pointer imageHandle, Pointer roiHandle, Pointer transArg, Pointer outBoxHandle, Pointer outDetailHandle) {
        try {
            if (contextHandle == null || contextHandle == Pointer.NULL) {
                return 0;
            }
            Context context = new Context(contextHandle);
            TaskDetail taskDetail = context.tasker().getTaskDetail(taskId);
            if (taskDetail == null) {
                return 0;
            }

            MaaImage image = new MaaImageBuffer(imageHandle).get();
            MaaRect roi = new MaaRectBuffer(roiHandle).get();
            AnalyzeResult result = analyze(context,
                    new AnalyzeArg(taskDetail, nodeName, customRecognitionName, customRecognitionParam, image, roi));

            try (MaaRectBuffer outBox = new MaaRectBuffer(outBoxHandle); MaaStringBuffer outDetail = new MaaStringBuffer(outDetailHandle)) {
                if (result == null) {
                    return 0;
                }
                if (result.box() != null) {
                    outBox.set(result.box());
                }
                outDetail.set(MaaJson.write(result.detail() == null ? Map.of() : result.detail()));
                return result.box() == null ? (byte) 0 : (byte) 1;
            }
        } catch (RuntimeException e) {
            return 0;
        }
    }
}
