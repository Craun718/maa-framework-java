package io.github.craun718.maafw.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.craun718.maafw.MaaJson;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class JPipelineTest {

    @Test
    void buildsPipelineAndSerializesNativeNodeMap() {
        JTemplateMatch match = new JTemplateMatch();
        match.template = List.of("start.png");

        JClick click = new JClick();
        click.target = List.of(100, 200);

        JPipeline pipeline = new JPipeline();
        pipeline.add(
                new JPipelineData()
                        .name("Startup")
                        .recognition(JRecognition.templateMatch(match))
                        .action(JAction.click(click))
                        .addNext("Idle")
                        .addNext(JNodeAttr.of("Retry", true, true))
                        .addOnError("Fail")
                        .addAnchor("entry")
                        .setAnchorTarget("next", "Idle")
                        .preDelay(10)
                        .timeout(5000)
                        .attach(Map.of("k", "v")));
        pipeline.add("Idle", new JPipelineData().action(JAction.doNothing()));

        assertEquals(2, pipeline.size());
        assertTrue(pipeline.has("Startup"));
        assertEquals("Idle", pipeline.get("Startup").next.getFirst().name);
        assertEquals("Retry", pipeline.get("Startup").next.get(1).name);
        assertEquals(Map.of("entry", "Startup", "next", "Idle"), pipeline.get("Startup").anchor);

        Map<String, Object> root = MaaJson.parseObject(pipeline.toJson());
        assertEquals(Set.of("Startup", "Idle"), root.keySet());

        @SuppressWarnings("unchecked")
        Map<String, Object> startup = (Map<String, Object>) root.get("Startup");
        assertFalse(startup.containsKey("name"));

        @SuppressWarnings("unchecked")
        Map<String, Object> recognition = (Map<String, Object>) startup.get("recognition");
        assertEquals("TemplateMatch", recognition.get("type"));
        assertEquals(List.of("start.png"), map(recognition.get("param")).get("template"));

        @SuppressWarnings("unchecked")
        Map<String, Object> action = (Map<String, Object>) startup.get("action");
        assertEquals("Click", action.get("type"));
        assertEquals(List.of(100, 200), map(action.get("param")).get("target"));

        assertEquals(List.of("Idle", "[JumpBack][Anchor]Retry"), nodeNames(startup.get("next")));
        assertEquals(List.of("Fail"), nodeNames(startup.get("on_error")));
        assertEquals(10, startup.get("pre_delay"));
        assertEquals(5000, startup.get("timeout"));
        assertEquals(Map.of("k", "v"), startup.get("attach"));
    }

    @Test
    void factoriesCreateTypedRecognitionAndActionSections() {
        JRecognition directHit = JRecognition.directHit();
        assertEquals(JRecognitionType.DIRECT_HIT, directHit.type);
        assertInstanceOf(JDirectHit.class, directHit.param);

        JRecognition and = JRecognition.and("A", "B");
        assertEquals(JRecognitionType.AND, and.type);
        assertEquals(List.of("A", "B"), assertInstanceOf(JAnd.class, and.param).allOf);

        JRecognition custom = JRecognition.custom("MyReco");
        assertEquals(JRecognitionType.CUSTOM, custom.type);
        assertEquals(
                "MyReco",
                assertInstanceOf(JCustomRecognition.class, custom.param).customRecognition);

        assertEquals(JActionType.DO_NOTHING, JAction.doNothing().type);
        assertEquals(JActionType.CLICK, JAction.click().type);
        JAction input = JAction.inputText("hello");
        assertEquals(JActionType.INPUT_TEXT, input.type);
        assertEquals("hello", assertInstanceOf(JInputText.class, input.param).inputText);
        assertEquals(JActionType.START_APP, JAction.startApp("com.example").type);
        assertEquals(JActionType.CUSTOM, JAction.custom("MyAct").type);
        assertEquals(List.of(1, 2), assertInstanceOf(JClickKey.class, JAction.clickKey(1, 2).param).key);
        assertEquals(3, assertInstanceOf(JTouchUp.class, JAction.touchUp(3).param).contact);
    }

    @Test
    void fromJsonRoundTripsBuilderOutput() {
        JPipeline original = new JPipeline();
        original.add(
                new JPipelineData()
                        .name("Main")
                        .recognition(JRecognition.ocr(new JOCR()))
                        .action(JAction.inputText("hello"))
                        .addNext("End")
                        .rateLimit(250));
        original.add("End", new JPipelineData().action(JAction.doNothing()));

        JPipeline parsed = JPipeline.fromJson(original.toJson());

        assertEquals(Set.of("Main", "End"), parsed.nodes().keySet());
        assertEquals("Main", parsed.get("Main").name);
        assertEquals(JRecognitionType.OCR, parsed.get("Main").recognition.type);
        assertEquals(JActionType.INPUT_TEXT, parsed.get("Main").action.type);
        assertEquals(List.of("End"), nodeNames(MaaJson.parseObject(parsed.get("Main").toJson()).get("next")));
        assertEquals(250, parsed.get("Main").rateLimit);
        assertEquals(
                MaaJson.parseObject(original.toJson()),
                MaaJson.parseObject(parsed.toJson()));
    }

    @Test
    void pipelineNodeListsSupportReplacementAndRemoval() {
        JPipelineData node = new JPipelineData()
                .name("Node")
                .addNext("A")
                .addNext("A")
                .addNext("B")
                .addOnError("E")
                .addAnchor("self");

        assertEquals(List.of("A", "B"), node.next.stream().map(attr -> attr.name).toList());
        assertEquals(List.of("E"), node.onError.stream().map(attr -> attr.name).toList());

        node.removeNext("A").removeOnError("E").removeAnchor("self");
        assertEquals(List.of("B"), node.next.stream().map(attr -> attr.name).toList());
        assertTrue(node.onError.isEmpty());
        assertTrue(node.anchor.isEmpty());

        JPipeline pipeline = new JPipeline();
        pipeline.add(node);
        pipeline.remove("Node");
        assertFalse(pipeline.has("Node"));
        assertNull(pipeline.get("Node"));
        pipeline.add("Replaced", node);
        assertEquals(1, pipeline.size());
        assertEquals("Replaced", pipeline.get("Replaced").name);
        pipeline.clear();
        assertEquals(0, pipeline.size());
    }

    @Test
    void requiresNameBeforeAddingNode() {
        JPipeline pipeline = new JPipeline();
        assertThrows(
                IllegalArgumentException.class,
                () -> pipeline.add(new JPipelineData().action(JAction.doNothing())));
    }

    private static Map<String, Object> map(Object value) {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) value;
        return result;
    }

    private static List<String> nodeNames(Object value) {
        return mapList(value).stream()
                .map(item -> formatName(map(item)))
                .toList();
    }

    private static String formatName(Map<String, Object> attr) {
        String prefix = "";
        if (Boolean.TRUE.equals(attr.get("jump_back"))) {
            prefix += "[JumpBack]";
        }
        if (Boolean.TRUE.equals(attr.get("anchor"))) {
            prefix += "[Anchor]";
        }
        return prefix + attr.get("name");
    }

    private static List<?> mapList(Object value) {
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) value;
        return result;
    }
}
