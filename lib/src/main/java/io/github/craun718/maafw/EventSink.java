package io.github.craun718.maafw;

import com.sun.jna.Pointer;
import java.util.Map;
import java.util.Objects;

/** Base class for receiving MaaFramework event callbacks. */
public abstract class EventSink {

    private final MaaCallbacks.EventCallback callback =
            (handle, message, detailsJson, transArg) ->
                    onRawNotification(handle, message, MaaJson.parseObjectOrEmpty(detailsJson));

    protected EventSink() {}

    /** Called for every notification before type-specific routing. */
    protected void onRawNotification(Pointer handle, String message, Map<String, Object> details) {
        onUnknownNotification(handle, message, details);
    }

    /** Called when a notification does not match a known event family. */
    public void onUnknownNotification(Pointer handle, String message, Map<String, Object> details) {}

    public static MaaDef.NotificationType notificationType(String message) {
        return MaaDef.NotificationType.of(message);
    }

    MaaCallbacks.EventCallback callback() {
        return callback;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(callback=" + Objects.hashCode(callback) + ")";
    }
}
