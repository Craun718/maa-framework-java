package io.github.craun718.maafw;

import com.sun.jna.Pointer;

/**
 * Base class for custom MaaFramework pipeline actions.
 *
 * <p>The native callback resolves the current {@link TaskDetail} and {@link RecognitionDetail}
 * before invoking {@link #run}. Missing details, including an unavailable or zero {@code reco_id},
 * cause the callback to return failure without calling {@code run}. This matches the Python
 * binding's behavior.
 */
public abstract class CustomAction {

    public record RunArg(
            TaskDetail taskDetail,
            String nodeName,
            String customActionName,
            String customActionParam,
            RecognitionDetail recoDetail,
            MaaRect box) {}

    public record RunResult(boolean success) {

        public static RunResult ok() {
            return new RunResult(true);
        }

        public static RunResult failed() {
            return new RunResult(false);
        }
    }

    private final MaaCallbacks.CustomActionCallback callback = this::invoke;

    protected CustomAction() {}

    public abstract RunResult run(Context context, RunArg argv);

    MaaCallbacks.CustomActionCallback callback() {
        return callback;
    }

    private byte invoke(
            Pointer contextHandle,
            long taskId,
            String nodeName,
            String customActionName,
            String customActionParam,
            long recoId,
            Pointer boxHandle,
            Pointer transArg) {
        try {
            if (contextHandle == null || contextHandle == Pointer.NULL) {
                return 0;
            }
            Context context = new Context(contextHandle);
            TaskDetail taskDetail = context.tasker().getTaskDetail(taskId);
            RecognitionDetail recoDetail = context.tasker().getRecognitionDetail(recoId);
            if (taskDetail == null || recoDetail == null) {
                return 0;
            }

            MaaRect box = new MaaRectBuffer(boxHandle).get();
            RunResult result =
                    run(context, new RunArg(taskDetail, nodeName, customActionName, customActionParam, recoDetail, box));
            return (result == null || result.success()) ? (byte) 1 : (byte) 0;
        } catch (RuntimeException e) {
            return 0;
        }
    }
}
