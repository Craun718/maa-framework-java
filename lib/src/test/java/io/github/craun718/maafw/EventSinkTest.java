package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.jna.Pointer;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EventSinkTest {

    @Test
    void routesResourceLoadingDetails() {
        class Sink extends ResourceEventSink {

            ResourceEventSink.ResourceLoadingDetail detail;
            MaaDef.NotificationType type;

            @Override
            public void onResourceLoading(Resource resource, MaaDef.NotificationType notificationType,
                    ResourceEventSink.ResourceLoadingDetail detail) {
                this.type = notificationType;
                this.detail = detail;
            }
        }

        Sink sink = new Sink();
        sink.onRawNotification(new Pointer(0), "Resource.Loading.Succeeded",
                MaaJson.parseObject("{\"res_id\":42,\"path\":\"assets\",\"type\":\"Bundle\",\"hash\":\"h\"}"));

        assertEquals(MaaDef.NotificationType.SUCCEEDED, sink.type);
        assertEquals(42L, sink.detail.resId());
        assertEquals("assets", sink.detail.path());
        assertEquals("Bundle", sink.detail.type());
        assertEquals("h", sink.detail.hash());
    }

    @Test
    void routesControllerActionDetails() {
        class Sink extends ControllerEventSink {

            ControllerEventSink.ControllerActionDetail detail;

            @Override
            public void onControllerAction(Controller controller, MaaDef.NotificationType notificationType,
                    ControllerEventSink.ControllerActionDetail detail) {
                this.detail = detail;
            }
        }

        Sink sink = new Sink();
        sink.onRawNotification(new Pointer(0), "Controller.Action.Starting",
                MaaJson.parseObject("{\"ctrl_id\":7,\"uuid\":\"u\",\"action\":\"Click\"," + "\"param\":{\"x\":1},\"info\":{\"ok\":true}}"));

        assertEquals(7L, sink.detail.ctrlId());
        assertEquals("u", sink.detail.uuid());
        assertEquals("Click", sink.detail.action());
        assertEquals(1, ((Number) sink.detail.param().get("x")).intValue());
        assertTrue((Boolean) sink.detail.info().get("ok"));
    }

    @Test
    void routesTaskerTaskDetails() {
        class Sink extends TaskerEventSink {

            TaskerEventSink.TaskerTaskDetail detail;

            @Override
            public void onTaskerTask(Tasker tasker, MaaDef.NotificationType notificationType, TaskerEventSink.TaskerTaskDetail detail) {
                this.detail = detail;
            }
        }

        Sink sink = new Sink();
        sink.onRawNotification(new Pointer(0), "Tasker.Task.Starting",
                MaaJson.parseObject("{\"task_id\":11,\"entry\":\"Main\",\"uuid\":\"u\",\"hash\":\"h\"}"));

        assertEquals(11L, sink.detail.taskId());
        assertEquals("Main", sink.detail.entry());
        assertEquals("u", sink.detail.uuid());
        assertEquals("h", sink.detail.hash());
    }

    @Test
    void routesContextNodeRecognitionDetails() {
        class Sink extends ContextEventSink {

            ContextEventSink.NodeRecognitionDetail detail;

            @Override
            public void onNodeRecognition(Context context, MaaDef.NotificationType notificationType,
                    ContextEventSink.NodeRecognitionDetail detail) {
                this.detail = detail;
            }
        }

        Sink sink = new Sink();
        sink.onRawNotification(new Pointer(0), "Node.Recognition.Failed",
                MaaJson.parseObject("{\"task_id\":9,\"reco_id\":5,\"name\":\"Node\"," + "\"focus\":\"f\",\"anchor\":\"a\"}"));

        assertEquals(9L, sink.detail.taskId());
        assertEquals(5L, sink.detail.recoId());
        assertEquals("Node", sink.detail.name());
        assertEquals("f", sink.detail.focus());
        assertEquals("a", sink.detail.anchor());
    }

    @Test
    void forwardsUnknownNotifications() {
        class Sink extends ResourceEventSink {

            Pointer receivedHandle;
            String receivedMessage;
            Map<String, Object> receivedDetails;

            @Override
            public void onUnknownNotification(Pointer handle, String message, Map<String, Object> details) {
                this.receivedHandle = handle;
                this.receivedMessage = message;
                this.receivedDetails = details;
            }
        }

        Sink sink = new Sink();
        Pointer handle = new Pointer(0);
        Map<String, Object> details = Map.of("key", "value");
        sink.onRawNotification(handle, "Some.Event", details);

        assertSame(handle, sink.receivedHandle);
        assertEquals("Some.Event", sink.receivedMessage);
        assertEquals(details, sink.receivedDetails);
    }
}
