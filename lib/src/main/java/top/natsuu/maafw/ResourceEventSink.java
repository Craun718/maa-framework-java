package top.natsuu.maafw;

import com.sun.jna.Pointer;
import java.util.Map;
import java.util.List;

/** Event sink for resource loading notifications. */
public class ResourceEventSink extends EventSink {

    public record ResourceLoadingDetail(long resId, String path, String type, String hash, Long status, List<Map<String, Object>> errors) {

        public ResourceLoadingDetail(long resId, String path, String type, String hash) {
            this(resId, path, type, hash, null, null);
        }
    }

    protected ResourceEventSink() {
    }

    public void onResourceLoading(Resource resource, MaaDef.NotificationType notificationType, ResourceLoadingDetail detail) {
    }

    public void onRawNotification(Resource resource, String message, Map<String, Object> details) {
    }

    @Override
    protected void onRawNotification(Pointer handle, String message, Map<String, Object> details) {
        Resource resource = new Resource(handle, false);
        onRawNotification(resource, message, details);

        MaaDef.NotificationType notificationType = notificationType(message);
        if (message != null && message.startsWith("Resource.Loading")) {
            onResourceLoading(resource, notificationType, new ResourceLoadingDetail(
                MaaResultParsers.longValue(details.get("res_id")) == null ? 0L : MaaResultParsers.longValue(details.get("res_id")),
                MaaResultParsers.string(details.get("path")), details.getOrDefault("type", "Bundle").toString(),
                MaaResultParsers.string(details.get("hash")), MaaResultParsers.longValue(details.get("status")),
                details.get("errors") instanceof List<?> errors ? errors.stream().map(MaaResultParsers::objectMap).toList() : null));
        } else {
            onUnknownNotification(handle, message, details);
        }
    }
}
