package top.natsuu.maafw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.util.List;
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
    void routesCancellationAndExecutionCorrelation() {
        class Sink extends ContextEventSink {

            ContextTaskDetail task;
            NodeNextListDetail next;
            MaaDef.NotificationType type;
            Map<String, Object> transition;

            @Override
            public void onContextTask(Context context, MaaDef.NotificationType type, ContextTaskDetail detail) {
                this.task = detail;
                this.type = type;
            }

            @Override
            public void onNodeNextList(Context context, MaaDef.NotificationType type, NodeNextListDetail detail) {
                this.next = detail;
            }

            @Override
            public void onNodeTransition(Context context, Map<String, Object> details) {
                this.transition = details;
            }
        }
        Sink sink = new Sink();
        Map<String, Object> payload = MaaJson.parseObject("""
                {"task_id":10,"entry":"Main","context_id":20,"parent_task_id":0,
                 "task_parent":null,"parent":{"kind":"node","id":30},"reason":"cancelled"}
                """);
        sink.onRawNotification(new Pointer(1), "Context.Task.Cancelled", payload);
        assertEquals(MaaDef.NotificationType.CANCELLED, sink.type);
        assertEquals(20L, sink.task.contextId());
        assertEquals(0L, sink.task.parentTaskId());
        assertNull(sink.task.taskParent());
        assertEquals(Map.of("kind", "node", "id", 30), sink.task.parent());
        assertEquals("cancelled", sink.task.reason());
        sink.onRawNotification(new Pointer(1), "Node.NextList.Succeeded", MaaJson.parseObject("""
                {"task_id":10,"name":"Main","list":[],"list_id":40,"node_id":30,
                 "list_type":"next","round":2,"selected_index":0,"reco_id":50,"context_id":20}
                """));
        assertEquals(40L, sink.next.listId());
        assertEquals(30L, sink.next.nodeId());
        assertEquals("next", sink.next.listType());
        assertEquals(2L, sink.next.round());
        assertEquals(0L, sink.next.selectedIndex());
        assertEquals(50L, sink.next.recoId());
        assertEquals(20L, sink.next.contextId());
        Map<String, Object> transition = Map.of("from_node_id", 30, "to_node_id", 31, "reason", "next");
        sink.onRawNotification(new Pointer(1), "Node.Transition", transition);
        assertSame(transition, sink.transition);
    }

    @Test
    void preservesCapturedPipelineAndActionRecognitionId() {
        class Sink extends ContextEventSink {

            NodeRecognitionDetail recognition;
            NodeActionDetail action;

            @Override
            public void onNodeRecognition(Context context, MaaDef.NotificationType type, NodeRecognitionDetail detail) {
                recognition = detail;
            }

            @Override
            public void onNodeAction(Context context, MaaDef.NotificationType type, NodeActionDetail detail) {
                action = detail;
            }
        }
        Sink sink = new Sink();
        Map<String, Object> payload = Map.of("task_id", 1, "reco_id", 2, "action_id", 3, "context_id", 4, "pipeline",
                Map.of("recognition", "DirectHit"));
        sink.onRawNotification(new Pointer(1), "Node.Recognition.Succeeded", payload);
        sink.onRawNotification(new Pointer(1), "Node.Action.Succeeded", payload);
        assertEquals(payload.get("pipeline"), sink.recognition.pipeline());
        assertEquals(payload.get("pipeline"), sink.action.pipeline());
        assertEquals(2L, sink.action.recoId());
        assertEquals(4L, sink.action.contextId());
        // Legacy notifications and existing record constructors keep absent additions null.
        sink.onRawNotification(new Pointer(1), "Node.Action.Starting", Map.of("task_id", 1));
        assertNull(sink.action.pipeline());
        assertNull(sink.action.contextId());
        assertNull(new ContextEventSink.NodeRecognitionDetail(1, 2, "Node", null, null).contextId());
    }

    @Test
    void preservesResourceTerminalDiagnostics() {
        class Sink extends ResourceEventSink {

            ResourceLoadingDetail detail;

            @Override
            public void onResourceLoading(Resource resource, MaaDef.NotificationType type, ResourceLoadingDetail detail) {
                this.detail = detail;
            }
        }
        Sink sink = new Sink();
        List<Map<String, Object>> errors = List.of(Map.of("message", "invalid pipeline"));
        sink.onRawNotification(new Pointer(1), "Resource.Loading.Failed", Map.of("res_id", 7, "status", 4, "errors", errors));
        assertEquals(4L, sink.detail.status());
        assertEquals(errors, sink.detail.errors());
        sink.onRawNotification(new Pointer(1), "Resource.Loading.Starting", Map.of("res_id", 7));
        assertNull(sink.detail.status());
        assertNull(sink.detail.errors());
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
