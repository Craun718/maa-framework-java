package io.github.craun718.maafw.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.craun718.maafw.MaaJson;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JPipelineParserTest {

    @Test
    void parsesTypedNodeSectionsAndAttributes() {
        String json =
                """
                {
                  "recognition": {
                    "type": "TemplateMatch",
                    "param": {
                      "template": "a.png",
                      "roi": [1, 2, 3, 4],
                      "roi_offset": [5, 6, 7, 8],
                      "threshold": [0.8],
                      "order_by": "Score",
                      "method": 5,
                      "green_mask": true
                    }
                  },
                  "action": {
                    "type": "Click",
                    "param": {
                      "target": "boss",
                      "target_offset": [1, 2, 3, 4],
                      "contact": 2,
                      "pressure": 3
                    }
                  },
                  "next": ["A", {"name": "B", "jump_back": true}],
                  "rate_limit": 500,
                  "timeout": 1000,
                  "on_error": ["C"],
                  "anchor": {"a": "b"},
                  "inverse": true,
                  "enabled": false,
                  "pre_delay": 10,
                  "post_delay": 20,
                  "pre_wait_freezes": {
                    "time": 50,
                    "target": [1, 2, 3, 4],
                    "threshold": 0.9
                  },
                  "post_wait_freezes": 30,
                  "repeat": 2,
                  "repeat_delay": 3,
                  "repeat_wait_freezes": {"timeout": 99},
                  "max_hit": 10,
                  "focus": "x",
                  "attach": {"k": "v"}
                }
                """;

        JPipelineData data = JPipelineParser.parse(json);

        assertEquals(JRecognitionType.TEMPLATE_MATCH, data.recognition.type);
        JTemplateMatch template = assertInstanceOf(JTemplateMatch.class, data.recognition.param);
        assertEquals(List.of("a.png"), template.template);
        assertEquals(List.of(1, 2, 3, 4), template.roi);
        assertEquals(List.of(5, 6, 7, 8), template.roiOffset);
        assertEquals(List.of(0.8), template.threshold);
        assertEquals("Score", template.orderBy);
        assertEquals(5, template.method);
        assertTrue(template.greenMask);

        assertEquals(JActionType.CLICK, data.action.type);
        JClick click = assertInstanceOf(JClick.class, data.action.param);
        assertEquals("boss", click.target);
        assertEquals(List.of(1, 2, 3, 4), click.targetOffset);
        assertEquals(2, click.contact);
        assertEquals(3, click.pressure);

        assertEquals(2, data.next.size());
        assertEquals("A", data.next.getFirst().name);
        assertEquals("B", data.next.get(1).name);
        assertTrue(data.next.get(1).jumpBack);
        assertEquals("C", data.onError.getFirst().name);
        assertEquals(500, data.rateLimit);
        assertEquals(1000, data.timeout);
        assertEquals(Map.of("a", "b"), data.anchor);
        assertTrue(data.inverse);
        assertFalse(data.enabled);
        assertEquals(10, data.preDelay);
        assertEquals(20, data.postDelay);
        assertEquals(50, data.preWaitFreezes.time);
        assertEquals(List.of(1, 2, 3, 4), data.preWaitFreezes.target);
        assertEquals(0.9, data.preWaitFreezes.threshold);
        assertEquals(30, data.postWaitFreezes.time);
        assertEquals(99, data.repeatWaitFreezes.timeout);
        assertEquals(2, data.repeat);
        assertEquals(3, data.repeatDelay);
        assertEquals(10, data.maxHit);
        assertEquals("x", data.focus);
        assertEquals(Map.of("k", "v"), data.attach);
    }

    @Test
    void parsesAllRecognitionVariants() {
        Map<String, Object> empty = Map.of();
        assertInstanceOf(JDirectHit.class, parseRecognition(JRecognitionType.DIRECT_HIT, empty));
        assertInstanceOf(
                JTemplateMatch.class,
                parseRecognition(JRecognitionType.TEMPLATE_MATCH, Map.of("template", "a.png")));
        assertInstanceOf(
                JFeatureMatch.class,
                parseRecognition(JRecognitionType.FEATURE_MATCH, Map.of("template", "a.png")));
        assertInstanceOf(
                JColorMatch.class,
                parseRecognition(
                        JRecognitionType.COLOR_MATCH,
                        Map.of("lower", List.of(List.of(0, 0, 0)), "upper", List.of(List.of(255, 255, 255)))));
        assertInstanceOf(JOCR.class, parseRecognition(JRecognitionType.OCR, empty));
        assertInstanceOf(
                JNeuralNetworkClassify.class,
                parseRecognition(JRecognitionType.NEURAL_NETWORK_CLASSIFY, Map.of("model", "m")));
        assertInstanceOf(
                JNeuralNetworkDetect.class,
                parseRecognition(JRecognitionType.NEURAL_NETWORK_DETECT, Map.of("model", "m")));
        assertInstanceOf(
                JCustomRecognition.class,
                parseRecognition(JRecognitionType.CUSTOM, Map.of("custom_recognition", "reco")));
        assertInstanceOf(
                JAnd.class, parseRecognition(JRecognitionType.AND, Map.of("all_of", List.of("A"))));
        assertInstanceOf(
                JOr.class, parseRecognition(JRecognitionType.OR, Map.of("any_of", List.of("A"))));
    }

    @Test
    void parsesAllActionVariants() {
        assertInstanceOf(JDoNothing.class, parseAction(JActionType.DO_NOTHING, Map.of()));
        assertInstanceOf(JClick.class, parseAction(JActionType.CLICK, Map.of()));
        assertInstanceOf(JLongPress.class, parseAction(JActionType.LONG_PRESS, Map.of()));
        assertInstanceOf(JSwipe.class, parseAction(JActionType.SWIPE, Map.of()));
        assertInstanceOf(
                JMultiSwipe.class,
                parseAction(
                        JActionType.MULTI_SWIPE,
                        Map.of("swipes", List.of(Map.of("end", List.of(Map.of("x", 1)))))));
        assertInstanceOf(JTouch.class, parseAction(JActionType.TOUCH_DOWN, Map.of()));
        assertInstanceOf(JTouch.class, parseAction(JActionType.TOUCH_MOVE, Map.of()));
        assertInstanceOf(JTouchUp.class, parseAction(JActionType.TOUCH_UP, Map.of()));
        assertInstanceOf(JClickKey.class, parseAction(JActionType.CLICK_KEY, Map.of("key", List.of(1))));
        assertInstanceOf(
                JLongPressKey.class, parseAction(JActionType.LONG_PRESS_KEY, Map.of("key", List.of(1))));
        assertInstanceOf(JKey.class, parseAction(JActionType.KEY_DOWN, Map.of("key", 1)));
        assertInstanceOf(JKey.class, parseAction(JActionType.KEY_UP, Map.of("key", 1)));
        assertInstanceOf(
                JInputText.class, parseAction(JActionType.INPUT_TEXT, Map.of("input_text", "hello")));
        assertInstanceOf(JStartApp.class, parseAction(JActionType.START_APP, Map.of("package", "app")));
        assertInstanceOf(JStopApp.class, parseAction(JActionType.STOP_APP, Map.of("package", "app")));
        assertInstanceOf(JStopTask.class, parseAction(JActionType.STOP_TASK, Map.of()));
        assertInstanceOf(JScroll.class, parseAction(JActionType.SCROLL, Map.of()));
        assertInstanceOf(JCommand.class, parseAction(JActionType.COMMAND, Map.of("exec", "echo")));
        assertInstanceOf(JShell.class, parseAction(JActionType.SHELL, Map.of("cmd", "echo hi")));
        assertInstanceOf(JScreencap.class, parseAction(JActionType.SCREENCAP, Map.of()));
        assertInstanceOf(
                JCustomAction.class,
                parseAction(JActionType.CUSTOM, Map.of("custom_action", "action")));
    }

    @Test
    void serializesTypedParamsWithNativeJsonKeys() {
        JTemplateMatch template = new JTemplateMatch();
        template.template = List.of("a.png");
        template.roi = List.of(1, 2, 3, 4);
        template.roiOffset = List.of(5, 6, 7, 8);
        template.threshold = List.of(0.9);
        template.orderBy = "Score";
        template.greenMask = true;

        JOCR ocr = new JOCR();
        ocr.onlyRec = true;
        ocr.colorFilter = "gray";

        JFeatureMatch feature = new JFeatureMatch();
        feature.ratio = 0.5;

        JCustomRecognition custom = new JCustomRecognition();
        custom.customRecognition = "reco";
        custom.customRecognitionParam = Map.of("mode", 1);

        JShell shell = new JShell();
        shell.cmd = "echo hi";
        shell.shellTimeout = 3000;

        JInputText input = new JInputText();
        input.inputText = "hello";

        JStartApp start = new JStartApp();
        start.packageName = "com.example";

        Map<String, Object> templateJson = MaaJson.parseObject(MaaJson.write(template));
        assertEquals(List.of(5, 6, 7, 8), templateJson.get("roi_offset"));
        assertEquals(List.of(0.9), templateJson.get("threshold"));
        assertEquals("Score", templateJson.get("order_by"));
        assertEquals(true, templateJson.get("green_mask"));

        Map<String, Object> ocrJson = MaaJson.parseObject(MaaJson.write(ocr));
        assertEquals(true, ocrJson.get("only_rec"));
        assertEquals("gray", ocrJson.get("color_filter"));

        Map<String, Object> featureJson = MaaJson.parseObject(MaaJson.write(feature));
        assertEquals(0.5, featureJson.get("ratio"));

        Map<String, Object> customJson = MaaJson.parseObject(MaaJson.write(custom));
        assertEquals("reco", customJson.get("custom_recognition"));
        assertEquals(Map.of("mode", 1), customJson.get("custom_recognition_param"));

        Map<String, Object> shellJson = MaaJson.parseObject(MaaJson.write(shell));
        assertEquals(3000, shellJson.get("shell_timeout"));

        Map<String, Object> inputJson = MaaJson.parseObject(MaaJson.write(input));
        assertEquals("hello", inputJson.get("input_text"));

        Map<String, Object> startJson = MaaJson.parseObject(MaaJson.write(start));
        assertEquals("com.example", startJson.get("package"));

        assertEquals(Map.of(), MaaJson.parseObject(MaaJson.write(new JDoNothing())));
        assertEquals(Map.of(), MaaJson.parseObject(MaaJson.write(new JStopTask())));
    }

    private static JRecognitionParam parseRecognition(JRecognitionType type, Map<String, Object> params) {
        return JPipelineParser.parseRecognitionParam(type, params);
    }

    private static JActionParam parseAction(JActionType type, Map<String, Object> params) {
        return JPipelineParser.parseActionParam(type, params);
    }
}
