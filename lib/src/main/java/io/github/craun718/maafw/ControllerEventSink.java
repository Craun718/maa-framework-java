package io.github.craun718.maafw;

import com.sun.jna.Pointer;
import java.util.Map;

/** Event sink for controller action notifications. */
public class ControllerEventSink extends EventSink {

    public record ControllerActionDetail(long ctrlId, String uuid, String action, Map<String, Object> param, Map<String, Object> info) {
    }

    protected ControllerEventSink() {
    }

    public void onControllerAction(Controller controller, MaaDef.NotificationType notificationType, ControllerActionDetail detail) {
    }

    public void onRawNotification(Controller controller, String message, Map<String, Object> details) {
    }

    @Override
    protected void onRawNotification(Pointer handle, String message, Map<String, Object> details) {
        Controller controller = new Controller(handle, false);
        onRawNotification(controller, message, details);

        MaaDef.NotificationType notificationType = notificationType(message);
        if (message != null && message.startsWith("Controller.Action")) {
            onControllerAction(controller, notificationType,
                    new ControllerActionDetail(valueOrZero(details.get("ctrl_id")), MaaResultParsers.string(details.get("uuid")),
                        MaaResultParsers.string(details.get("action")), MaaResultParsers.objectMap(details.get("param")),
                        MaaResultParsers.objectMap(details.get("info"))));
        } else {
            onUnknownNotification(handle, message, details);
        }
    }

    private static long valueOrZero(Object value) {
        Long parsed = MaaResultParsers.longValue(value);
        return parsed == null ? 0L : parsed;
    }
}
