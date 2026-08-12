package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.jna.Pointer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class DataModelTest {

    @Test
    void imageCopiesBytesAndReportsEmptyState() {
        byte[] source = {1, 2, 3, 4, 5, 6};
        MaaImage image = new MaaImage(source, 2, 1, 3, MaaImage.TYPE_8UC3);

        source[0] = 99;
        image.data()[1] = 99;

        assertEquals(2, image.width());
        assertEquals(1, image.height());
        assertEquals(3, image.channels());
        assertEquals(MaaImage.TYPE_8UC3, image.type());
        assertEquals(1, image.data()[0]);
        assertEquals(2, image.data()[1]);
        assertFalse(image.isEmpty());

        assertTrue(MaaImage.empty().isEmpty());
        assertTrue(new MaaImage(new byte[0], 0, 0, 0, 0).isEmpty());
    }

    @Test
    void rectAndPointConvertArrays() {
        MaaRect rect = MaaRect.of(10, 20, 30, 40);
        assertEquals(10, rect.x());
        assertEquals(20, rect.y());
        assertEquals(30, rect.width());
        assertEquals(40, rect.height());
        assertArrayEquals(new int[] {10, 20, 30, 40}, rect.toArray());
        assertArrayEquals(rect.toArray(), MaaRect.from(new int[] {10, 20, 30, 40}).toArray());
        assertThrows(IllegalArgumentException.class, () -> MaaRect.from(new int[] {1, 2, 3}));

        MaaPoint point = MaaPoint.of(7, 8);
        assertEquals(7, point.x());
        assertEquals(8, point.y());
        assertArrayEquals(new int[] {7, 8}, point.toArray());
        assertArrayEquals(point.toArray(), MaaPoint.from(new int[] {7, 8}).toArray());
        assertThrows(IllegalArgumentException.class, () -> MaaPoint.from(new int[] {7}));
    }

    @Test
    void actionResultParsesCommonAccessors() {
        ActionResult result = new ActionResult(MaaJson.parseObject("""
                {
                  "point": [10, 20],
                  "begin": [1, 2],
                  "end": [[3, 4], [5, 6]],
                  "end_hold": [100, 200],
                  "keycode": [7, 8],
                  "text": "hello",
                  "package": "com.example",
                  "contact": 3,
                  "pressure": 50,
                  "dx": 12,
                  "dy": -4,
                  "only_hover": true,
                  "success": true,
                  "cmd": "ls",
                  "shell_timeout": 5000,
                  "output": "out"
                }
                """));

        assertEquals(MaaPoint.of(10, 20), result.point());
        assertEquals(1, result.begin().x());
        assertEquals(2, result.begin().y());
        assertEquals(List.of(MaaPoint.of(3, 4), MaaPoint.of(5, 6)), result.end());
        assertEquals(List.of(100, 200), result.endHold());
        assertEquals(List.of(7, 8), result.keycodes());
        assertEquals("hello", result.text());
        assertEquals("com.example", result.packageName());
        assertEquals(3, result.contact());
        assertEquals(50, result.pressure());
        assertEquals(12, result.dx());
        assertEquals(-4, result.dy());
        assertEquals(true, result.onlyHover());
        assertEquals(true, result.success());
        assertEquals("ls", result.cmd());
        assertEquals(5000L, result.shellTimeout());
        assertEquals("out", result.output());
        assertThrows(UnsupportedOperationException.class, () -> result.raw().put("extra", "value"));
    }

    @Test
    void actionResultParsesSwipesAndSingularDuration() {
        ActionResult swipe = new ActionResult(MaaJson.parseObject("""
                {
                  "duration": [30, 40],
                  "swipes": [
                    {"begin": [1, 2], "end": [[7, 8]], "duration": 60},
                    {"begin": [5, 6], "end": [[9, 10]], "duration": 80}
                  ]
                }
                """));

        assertEquals(List.of(30, 40), swipe.durations());
        assertNull(swipe.duration());
        assertEquals(2, swipe.swipes().size());
        assertEquals(MaaPoint.of(1, 2), swipe.swipes().get(0).begin());
        assertEquals(List.of(MaaPoint.of(7, 8)), swipe.swipes().get(0).end());
        assertEquals(List.of(MaaPoint.of(9, 10)), swipe.swipes().get(1).end());
        assertEquals(60, swipe.swipes().get(0).duration());
        assertEquals(80, swipe.swipes().get(1).duration());
        assertEquals(250, new ActionResult(MaaJson.parseObject("{\"duration\":250}")).duration());
    }

    @Test
    void recognitionResultParsesBoxScoreCountTextAndLabel() {
        RecognitionResult result = new RecognitionResult(MaaJson.parseObject("""
                {
                  "box": [10, 20, 30, 40],
                  "score": 0.95,
                  "count": 2,
                  "text": "Start",
                  "cls_index": 3,
                  "label": "button",
                  "detail": {"confidence": 0.9}
                }
                """));

        assertEquals(MaaRect.of(10, 20, 30, 40), result.box());
        assertEquals(0.95, result.score());
        assertEquals(2, result.count());
        assertEquals("Start", result.text());
        assertEquals(3, result.clsIndex());
        assertEquals("button", result.label());
        assertEquals(0.9, ((Number) ((Map<?, ?>) result.detail()).get("confidence")).doubleValue());
        assertTrue(result.subResults().isEmpty());
    }

    @Test
    void recognitionDetailKeepsNonObjectRawValueAndCopiesLists() {
        List<RecognitionResult> all = new ArrayList<>();
        List<MaaImage> draws = new ArrayList<>();
        Object rawValue = List.of("And", Map.of("node", "A"), Map.of("node", "B"));

        RecognitionDetail detail = new RecognitionDetail(
                11L,
                "Reco",
                "And",
                true,
                MaaRect.of(1, 2, 3, 4),
                all,
                null,
                null,
                rawValue,
                MaaImage.empty(),
                draws);

        all.add(new RecognitionResult(Map.of()));
        draws.add(new MaaImage(new byte[] {1}, 1, 1, 1, 0));

        assertEquals(List.of(), detail.allResults());
        assertEquals(List.of(), detail.drawImages());
        assertSame(rawValue, detail.rawDetailValue());
        assertTrue(detail.rawDetail().isEmpty());
        assertTrue(MaaImage.empty().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> detail.allResults().add(new RecognitionResult(Map.of())));
        assertThrows(UnsupportedOperationException.class, () -> detail.drawImages().add(MaaImage.empty()));
    }

    @Test
    void taskDetailLoadsAndCachesNodes() {
        AtomicInteger loads = new AtomicInteger();
        TaskDetail detail = new TaskDetail(
                9L,
                "Main",
                new ArrayList<>(List.of(1L, 2L)),
                MaaDef.Status.SUCCEEDED,
                nodeId -> {
                    loads.incrementAndGet();
                    return new NodeDetail(nodeId, "Node-" + nodeId, null, null, true);
                });

        List<NodeDetail> first = detail.nodes();
        List<NodeDetail> second = detail.nodes();

        assertEquals(List.of(1L, 2L), detail.nodeIdList());
        assertEquals(2, first.size());
        assertEquals("Node-1", first.get(0).name());
        assertEquals("Node-2", first.get(1).name());
        assertSame(first, second);
        assertEquals(2, loads.get());
        assertThrows(UnsupportedOperationException.class, () -> detail.nodes().add(new NodeDetail(3L, "X", null, null, false)));
    }

    @Test
    void contextSinkParsesNodeWaitFreezesDetails() {
        class Sink extends ContextEventSink {
            ContextEventSink.NodeWaitFreezesDetail detail;

            @Override
            public void onNodeWaitFreezes(
                    Context context,
                    MaaDef.NotificationType notificationType,
                    ContextEventSink.NodeWaitFreezesDetail detail) {
                this.detail = detail;
            }
        }

        Sink sink = new Sink();
        sink.onRawNotification(
                new Pointer(0),
                "Node.WaitFreezes.Succeeded",
                MaaJson.parseObject("""
                        {
                          "task_id": 4,
                          "wf_id": 5,
                          "name": "Wait",
                          "phase": "Post",
                          "roi": [1, 2, 30, 40],
                          "param": {"target": 200},
                          "reco_ids": [7, 8],
                          "elapsed": 321,
                          "focus": "F"
                        }
                        """));

        assertEquals(4L, sink.detail.taskId());
        assertEquals(5L, sink.detail.wfId());
        assertEquals("Wait", sink.detail.name());
        assertEquals("Post", sink.detail.phase());
        assertEquals(MaaRect.of(1, 2, 30, 40), sink.detail.roi());
        assertEquals(200, ((Number) sink.detail.param().get("target")).intValue());
        assertEquals(List.of(7L, 8L), sink.detail.recoIds());
        assertEquals(321L, sink.detail.elapsed());
        assertEquals("F", sink.detail.focus());
    }
}
