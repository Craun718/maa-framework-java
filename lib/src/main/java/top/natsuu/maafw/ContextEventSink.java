package top.natsuu.maafw;

import com.sun.jna.Pointer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Event sink for notifications emitted while a pipeline node is executing. */
public class ContextEventSink extends EventSink {

    public record NodeAttr(String name, boolean jumpBack, boolean anchor) {
    }

    public record NodeWaitFreezesDetail(long taskId, long wfId, String name, String phase, MaaRect roi, Map<String, Object> param,
            List<Long> recoIds, Long elapsed, Object focus, Long contextId, Long parentTaskId, Map<String, Object> taskParent,
            Map<String, Object> parent, String reason) {

        public NodeWaitFreezesDetail(long taskId, long wfId, String name, String phase, MaaRect roi, Map<String, Object> param,
                List<Long> recoIds, Long elapsed, Object focus) {
            this(taskId, wfId, name, phase, roi, param, recoIds, elapsed, focus, null, null, null, null, null);
        }
    }

    public record NodeNextListDetail(long taskId, String name, List<NodeAttr> nextList, Object focus, Long listId, Long nodeId,
            String listType, Long round, Long selectedIndex, Long recoId, Long contextId, Long parentTaskId, Map<String, Object> taskParent,
            Map<String, Object> parent, String reason) {

        public NodeNextListDetail(long taskId, String name, List<NodeAttr> nextList, Object focus) {
            this(taskId, name, nextList, focus, null, null, null, null, null, null, null, null, null, null, null);
        }
    }

    public record NodeRecognitionDetail(long taskId, long recoId, String name, Object focus, String anchor, Map<String, Object> pipeline,
            Long contextId, Long parentTaskId, Map<String, Object> taskParent, Map<String, Object> parent, String reason) {

        public NodeRecognitionDetail(long taskId, long recoId, String name, Object focus, String anchor) {
            this(taskId, recoId, name, focus, anchor, null, null, null, null, null, null);
        }
    }

    public record NodeActionDetail(long taskId, long actionId, String name, Object focus, Map<String, Object> pipeline, Long recoId,
            Long contextId, Long parentTaskId, Map<String, Object> taskParent, Map<String, Object> parent, String reason) {

        public NodeActionDetail(long taskId, long actionId, String name, Object focus) {
            this(taskId, actionId, name, focus, null, null, null, null, null, null, null);
        }
    }

    public record NodePipelineNodeDetail(long taskId, long nodeId, String name, Object focus, Long contextId, Long parentTaskId,
            Map<String, Object> taskParent, Map<String, Object> parent, String reason) {

        public NodePipelineNodeDetail(long taskId, long nodeId, String name, Object focus) {
            this(taskId, nodeId, name, focus, null, null, null, null, null);
        }
    }

    public record NodeRecognitionNodeDetail(long taskId, long nodeId, String name, Object focus, Long contextId, Long parentTaskId,
            Map<String, Object> taskParent, Map<String, Object> parent, String reason) {

        public NodeRecognitionNodeDetail(long taskId, long nodeId, String name, Object focus) {
            this(taskId, nodeId, name, focus, null, null, null, null, null);
        }
    }

    public record NodeActionNodeDetail(long taskId, long nodeId, String name, Object focus, Long contextId, Long parentTaskId,
            Map<String, Object> taskParent, Map<String, Object> parent, String reason) {

        public NodeActionNodeDetail(long taskId, long nodeId, String name, Object focus) {
            this(taskId, nodeId, name, focus, null, null, null, null, null);
        }
    }

    public record ContextTaskDetail(long taskId, String entry, Long contextId, Long parentTaskId, Map<String, Object> taskParent,
            Map<String, Object> parent, String reason) {
    }

    public void onContextTask(Context context, MaaDef.NotificationType notificationType, ContextTaskDetail detail) {
    }

    /** Receives the scheduler transition, including execution IDs, names and reason. */
    public void onNodeTransition(Context context, Map<String, Object> details) {
    }

    protected ContextEventSink() {
    }

    public void onNodeWaitFreezes(Context context, MaaDef.NotificationType notificationType, NodeWaitFreezesDetail detail) {
    }

    public void onNodeNextList(Context context, MaaDef.NotificationType notificationType, NodeNextListDetail detail) {
    }

    public void onNodeRecognition(Context context, MaaDef.NotificationType notificationType, NodeRecognitionDetail detail) {
    }

    public void onNodeAction(Context context, MaaDef.NotificationType notificationType, NodeActionDetail detail) {
    }

    public void onNodePipelineNode(Context context, MaaDef.NotificationType notificationType, NodePipelineNodeDetail detail) {
    }

    public void onNodeRecognitionNode(Context context, MaaDef.NotificationType notificationType, NodeRecognitionNodeDetail detail) {
    }

    public void onNodeActionNode(Context context, MaaDef.NotificationType notificationType, NodeActionNodeDetail detail) {
    }

    public void onRawNotification(Context context, String message, Map<String, Object> details) {
    }

    @Override
    protected void onRawNotification(Pointer handle, String message, Map<String, Object> details) {
        Context context = new Context(handle);
        onRawNotification(context, message, details);

        MaaDef.NotificationType notificationType = notificationType(message);
        if (message != null && message.startsWith("Node.WaitFreezes")) {
            onNodeWaitFreezes(context, notificationType,
                    new NodeWaitFreezesDetail(valueOrZero(details.get("task_id")), valueOrZero(details.get("wf_id")),
                        MaaResultParsers.string(details.get("name")), MaaResultParsers.string(details.get("phase")),
                        MaaResultParsers.rect(details.get("roi")), mapOrEmpty(details.get("param")), longList(details.get("reco_ids")),
                        MaaResultParsers.longValue(details.get("elapsed")), details.get("focus"),
                        MaaResultParsers.longValue(details.get("context_id")), MaaResultParsers.longValue(details.get("parent_task_id")),
                        MaaResultParsers.objectMap(details.get("task_parent")), MaaResultParsers.objectMap(details.get("parent")),
                        MaaResultParsers.string(details.get("reason"))));
        } else if (message != null && message.startsWith("Node.NextList")) {
            onNodeNextList(context, notificationType,
                    new NodeNextListDetail(valueOrZero(details.get("task_id")), MaaResultParsers.string(details.get("name")),
                        nodeAttrList(details.get("list")), details.get("focus"), MaaResultParsers.longValue(details.get("list_id")),
                        MaaResultParsers.longValue(details.get("node_id")), MaaResultParsers.string(details.get("list_type")),
                        MaaResultParsers.longValue(details.get("round")), MaaResultParsers.longValue(details.get("selected_index")),
                        MaaResultParsers.longValue(details.get("reco_id")), MaaResultParsers.longValue(details.get("context_id")),
                        MaaResultParsers.longValue(details.get("parent_task_id")), MaaResultParsers.objectMap(details.get("task_parent")),
                        MaaResultParsers.objectMap(details.get("parent")), MaaResultParsers.string(details.get("reason"))));
        } else if (message != null && message.startsWith("Node.PipelineNode")) {
            onNodePipelineNode(context, notificationType,
                    new NodePipelineNodeDetail(valueOrZero(details.get("task_id")), valueOrZero(details.get("node_id")),
                        MaaResultParsers.string(details.get("name")), details.get("focus"),
                        MaaResultParsers.longValue(details.get("context_id")), MaaResultParsers.longValue(details.get("parent_task_id")),
                        MaaResultParsers.objectMap(details.get("task_parent")), MaaResultParsers.objectMap(details.get("parent")),
                        MaaResultParsers.string(details.get("reason"))));
        } else if (message != null && message.startsWith("Node.RecognitionNode")) {
            onNodeRecognitionNode(context, notificationType,
                    new NodeRecognitionNodeDetail(valueOrZero(details.get("task_id")), valueOrZero(details.get("node_id")),
                        MaaResultParsers.string(details.get("name")), details.get("focus"),
                        MaaResultParsers.longValue(details.get("context_id")), MaaResultParsers.longValue(details.get("parent_task_id")),
                        MaaResultParsers.objectMap(details.get("task_parent")), MaaResultParsers.objectMap(details.get("parent")),
                        MaaResultParsers.string(details.get("reason"))));
        } else if (message != null && message.startsWith("Node.ActionNode")) {
            onNodeActionNode(context, notificationType,
                    new NodeActionNodeDetail(valueOrZero(details.get("task_id")), valueOrZero(details.get("node_id")),
                        MaaResultParsers.string(details.get("name")), details.get("focus"),
                        MaaResultParsers.longValue(details.get("context_id")), MaaResultParsers.longValue(details.get("parent_task_id")),
                        MaaResultParsers.objectMap(details.get("task_parent")), MaaResultParsers.objectMap(details.get("parent")),
                        MaaResultParsers.string(details.get("reason"))));
        } else if (message != null && message.startsWith("Node.Recognition")) {
            onNodeRecognition(context, notificationType,
                    new NodeRecognitionDetail(valueOrZero(details.get("task_id")), valueOrZero(details.get("reco_id")),
                        MaaResultParsers.string(details.get("name")), details.get("focus"), MaaResultParsers.string(details.get("anchor")),
                        MaaResultParsers.objectMap(details.get("pipeline")), MaaResultParsers.longValue(details.get("context_id")),
                        MaaResultParsers.longValue(details.get("parent_task_id")), MaaResultParsers.objectMap(details.get("task_parent")),
                        MaaResultParsers.objectMap(details.get("parent")), MaaResultParsers.string(details.get("reason"))));
        } else if (message != null && message.startsWith("Node.Action")) {
            onNodeAction(context, notificationType,
                    new NodeActionDetail(valueOrZero(details.get("task_id")), valueOrZero(details.get("action_id")),
                        MaaResultParsers.string(details.get("name")), details.get("focus"),
                        MaaResultParsers.objectMap(details.get("pipeline")), MaaResultParsers.longValue(details.get("reco_id")),
                        MaaResultParsers.longValue(details.get("context_id")), MaaResultParsers.longValue(details.get("parent_task_id")),
                        MaaResultParsers.objectMap(details.get("task_parent")), MaaResultParsers.objectMap(details.get("parent")),
                        MaaResultParsers.string(details.get("reason"))));
        } else if (message != null && message.startsWith("Context.Task.")) {
            onContextTask(context, notificationType,
                    new ContextTaskDetail(valueOrZero(details.get("task_id")), MaaResultParsers.string(details.get("entry")),
                        MaaResultParsers.longValue(details.get("context_id")), MaaResultParsers.longValue(details.get("parent_task_id")),
                        MaaResultParsers.objectMap(details.get("task_parent")), MaaResultParsers.objectMap(details.get("parent")),
                        MaaResultParsers.string(details.get("reason"))));
        } else if ("Node.Transition".equals(message)) {
            onNodeTransition(context, details);
        } else {
            onUnknownNotification(handle, message, details);
        }
    }

    private static Map<String, Object> mapOrEmpty(Object value) {
        Map<String, Object> map = MaaResultParsers.objectMap(value);
        return map == null ? Map.of() : map;
    }

    private static List<Long> longList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Long> result = new ArrayList<>(list.size());
        for (Object item : list) {
            Long parsed = MaaResultParsers.longValue(item);
            if (parsed != null) {
                result.add(parsed);
            }
        }
        return List.copyOf(result);
    }

    private static List<NodeAttr> nodeAttrList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<NodeAttr> result = new ArrayList<>(list.size());
        for (Object item : list) {
            Map<String, Object> map = MaaResultParsers.objectMap(item);
            if (map == null) {
                continue;
            }
            Boolean jumpBack = MaaResultParsers.booleanValue(map.get("jump_back"));
            Boolean anchor = MaaResultParsers.booleanValue(map.get("anchor"));
            result.add(new NodeAttr(MaaResultParsers.string(map.get("name")), jumpBack != null && jumpBack, anchor != null && anchor));
        }
        return List.copyOf(result);
    }

    private static long valueOrZero(Object value) {
        Long parsed = MaaResultParsers.longValue(value);
        return parsed == null ? 0L : parsed;
    }
}
