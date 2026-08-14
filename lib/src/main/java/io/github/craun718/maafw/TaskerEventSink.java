package io.github.craun718.maafw;

import com.sun.jna.Pointer;
import java.util.Map;

/** Event sink for tasker task notifications. */
public class TaskerEventSink extends EventSink {

    public record TaskerTaskDetail(long taskId, String entry, String uuid, String hash) {
    }

    protected TaskerEventSink() {
    }

    public void onTaskerTask(Tasker tasker, MaaDef.NotificationType notificationType, TaskerTaskDetail detail) {
    }

    public void onRawNotification(Tasker tasker, String message, Map<String, Object> details) {
    }

    @Override
    protected void onRawNotification(Pointer handle, String message, Map<String, Object> details) {
        Tasker tasker = new Tasker(handle, false);
        onRawNotification(tasker, message, details);

        MaaDef.NotificationType notificationType = notificationType(message);
        if (message != null && message.startsWith("Tasker.Task")) {
            onTaskerTask(tasker, notificationType,
                    new TaskerTaskDetail(valueOrZero(details.get("task_id")), MaaResultParsers.string(details.get("entry")),
                        MaaResultParsers.string(details.get("uuid")), MaaResultParsers.string(details.get("hash"))));
        } else {
            onUnknownNotification(handle, message, details);
        }
    }

    private static long valueOrZero(Object value) {
        Long parsed = MaaResultParsers.longValue(value);
        return parsed == null ? 0L : parsed;
    }
}
