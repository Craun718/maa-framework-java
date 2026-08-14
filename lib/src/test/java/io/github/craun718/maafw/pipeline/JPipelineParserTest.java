package io.github.craun718.maafw.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.craun718.maafw.MaaJson;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JPipelineParserTest {

    @Test
    void parsesTypedNodeSectionsAndAttributes() {
        String json = """
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
                  "next": ["A", {"name": "B", "jump_back": true}, "[JumpBack][Anchor]C"],
                  "rate_limit": 500,
                  "timeout": 1000,
                  "on_error": ["[Anchor]D", "E"],
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

        assertEquals("A", data.next.getFirst().name);
        assertEquals("B", data.next.get(1).name);
        assertTrue(data.next.get(1).jumpBack);
        assertEquals(3, data.next.size());
        assertEquals("C", data.next.get(2).name);
        assertTrue(data.next.get(2).jumpBack);
        assertTrue(data.next.get(2).anchor);
        assertEquals(2, data.onError.size());
        assertEquals("D", data.onError.getFirst().name);
        assertTrue(data.onError.getFirst().anchor);
        assertEquals("E", data.onError.get(1).name);
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
        assertInstanceOf(JTemplateMatch.class, parseRecognition(JRecognitionType.TEMPLATE_MATCH, Map.of("template", "a.png")));
        assertInstanceOf(JFeatureMatch.class, parseRecognition(JRecognitionType.FEATURE_MATCH, Map.of("template", "a.png")));
        assertInstanceOf(JColorMatch.class, parseRecognition(JRecognitionType.COLOR_MATCH,
                Map.of("lower", List.of(List.of(0, 0, 0)), "upper", List.of(List.of(255, 255, 255)))));
        assertInstanceOf(JOCR.class, parseRecognition(JRecognitionType.OCR, empty));
        assertInstanceOf(JNeuralNetworkClassify.class, parseRecognition(JRecognitionType.NEURAL_NETWORK_CLASSIFY, Map.of("model", "m")));
        assertInstanceOf(JNeuralNetworkDetect.class, parseRecognition(JRecognitionType.NEURAL_NETWORK_DETECT, Map.of("model", "m")));
        assertInstanceOf(JCustomRecognition.class, parseRecognition(JRecognitionType.CUSTOM, Map.of("custom_recognition", "reco")));
        JAnd and = assertInstanceOf(JAnd.class, parseRecognition(JRecognitionType.AND, Map.of("all_of", List.of("A"), "box_index", 1)));
        assertEquals(1, and.boxIndex);
        assertInstanceOf(JOr.class, parseRecognition(JRecognitionType.OR, Map.of("any_of", List.of("A"))));
    }

    @Test
    void parsesMixedAndOrSubRecognitionItems() {
        String andJson = """
                {
                  "Main": {
                    "recognition": {
                      "type": "And",
                      "param": {
                        "all_of": [
                          "RefNode",
                          {"sub_name": "InlineSub", "type": "DirectHit", "param": {}}
                        ],
                        "box_index": 1
                      }
                    }
                  }
                }
                """;

        JAnd and = assertInstanceOf(JAnd.class, JPipelineParser.parseAll(andJson).get("Main").recognition.param);
        assertEquals(1, and.boxIndex);
        assertEquals(2, and.allOf.size());
        assertFalse(and.allOf.getFirst().isInline());
        assertEquals("RefNode", and.allOf.getFirst().nodeName());
        JInlineRecognition inline = and.allOf.get(1).inline();
        assertTrue(and.allOf.get(1).isInline());
        assertEquals("InlineSub", inline.subName);
        assertEquals(JRecognitionType.DIRECT_HIT, inline.type);
        assertInstanceOf(JDirectHit.class, inline.param);

        Map<String, Object> andJsonObject = MaaJson.parseObject(MaaJson.write(and));
        assertFalse(andJsonObject.containsKey("sub_name"));
        List<?> andItems = (List<?>) andJsonObject.get("all_of");
        assertEquals("RefNode", andItems.getFirst());
        Map<String, Object> inlineJson = map(andItems.get(1));
        assertEquals("InlineSub", inlineJson.get("sub_name"));
        assertEquals("DirectHit", inlineJson.get("type"));
        assertEquals(Map.of("roi", List.of(0, 0, 0, 0), "roi_offset", List.of(0, 0, 0, 0)), inlineJson.get("param"));

        String orJson = """
                {
                  "Main": {
                    "recognition": {
                      "type": "Or",
                      "param": {
                        "any_of": [
                          "OtherRef",
                          {
                            "sub_name": "Sub2",
                            "type": "TemplateMatch",
                            "param": {"template": ["a.png"], "threshold": [0.8]}
                          }
                        ]
                      }
                    }
                  }
                }
                """;

        JOr or = assertInstanceOf(JOr.class, JPipelineParser.parseAll(orJson).get("Main").recognition.param);
        assertEquals(2, or.anyOf.size());
        assertEquals("OtherRef", or.anyOf.getFirst().nodeName());
        JInlineRecognition orInline = or.anyOf.get(1).inline();
        assertEquals("Sub2", orInline.subName);
        assertEquals(JRecognitionType.TEMPLATE_MATCH, orInline.type);
        JTemplateMatch template = assertInstanceOf(JTemplateMatch.class, orInline.param);
        assertEquals(List.of("a.png"), template.template);
        assertEquals(List.of(0.8), template.threshold);
    }

    @Test
    void parsesLegacyInlineSubRecognitionShape() {
        JAnd and = assertInstanceOf(JAnd.class, parseRecognition(JRecognitionType.AND, Map.of("all_of",
                List.of(Map.of("sub_name", "Legacy", "recognition", "TemplateMatch", "template", "a.png", "threshold", 0.8)))));

        JSubRecognitionItem item = and.allOf.getFirst();
        assertTrue(item.isInline());
        assertEquals("Legacy", item.inline().subName);
        JTemplateMatch param = assertInstanceOf(JTemplateMatch.class, item.inline().param);
        assertEquals(List.of("a.png"), param.template);
        assertEquals(List.of(0.8), param.threshold);
    }

    @Test
    void builderAcceptsMixedSubRecognitionItemsAndRoundTrips() {
        JRecognition and = JRecognition.and("A", JRecognition.directHit(), JSubRecognitionItem.inline("Sub", JRecognition.ocr(new JOCR())));
        JAnd andParam = assertInstanceOf(JAnd.class, and.param);
        assertEquals(3, andParam.allOf.size());
        assertEquals("A", andParam.allOf.getFirst().nodeName());
        assertTrue(andParam.allOf.get(1).isInline());
        assertNull(andParam.allOf.get(1).inline().subName);
        assertEquals(JRecognitionType.DIRECT_HIT, andParam.allOf.get(1).inline().type);
        assertTrue(andParam.allOf.get(2).isInline());
        assertEquals("Sub", andParam.allOf.get(2).inline().subName);
        assertEquals(JRecognitionType.OCR, andParam.allOf.get(2).inline().type);
        andParam.boxIndex = 1;

        Map<String, Object> andJson = MaaJson.parseObject(MaaJson.write(and));
        assertEquals("And", andJson.get("type"));
        Map<String, Object> andParamJson = map(andJson.get("param"));
        assertEquals(1, ((Number) andParamJson.get("box_index")).intValue());
        List<?> andItems = (List<?>) andParamJson.get("all_of");
        assertEquals("A", andItems.getFirst());
        assertEquals(JRecognitionType.DIRECT_HIT.nativeName(), map(andItems.get(1)).get("type"));
        assertEquals("Sub", map(andItems.get(2)).get("sub_name"));
        assertEquals(JRecognitionType.OCR.nativeName(), map(andItems.get(2)).get("type"));

        JRecognition or = JRecognition.or(List.of("B", JSubRecognitionItem.inline("Sub2", JRecognition.directHit())));
        JOr orParam = assertInstanceOf(JOr.class, or.param);
        assertEquals(2, orParam.anyOf.size());
        assertEquals("B", orParam.anyOf.getFirst().nodeName());
        assertEquals("Sub2", orParam.anyOf.get(1).inline().subName);

        Map<String, Object> orJson = MaaJson.parseObject(MaaJson.write(or));
        Map<String, Object> orParamJson = map(orJson.get("param"));
        List<?> orItems = (List<?>) orParamJson.get("any_of");
        assertEquals("B", orItems.getFirst());
        assertEquals("Sub2", map(orItems.get(1)).get("sub_name"));
        assertEquals("DirectHit", map(orItems.get(1)).get("type"));
    }

    @Test
    void parsesAllActionVariants() {
        assertInstanceOf(JDoNothing.class, parseAction(JActionType.DO_NOTHING, Map.of()));
        assertInstanceOf(JClick.class, parseAction(JActionType.CLICK, Map.of()));
        assertInstanceOf(JLongPress.class, parseAction(JActionType.LONG_PRESS, Map.of()));
        assertInstanceOf(JSwipe.class, parseAction(JActionType.SWIPE, Map.of()));
        assertInstanceOf(JMultiSwipe.class,
                parseAction(JActionType.MULTI_SWIPE, Map.of("swipes", List.of(Map.of("end", List.of(Map.of("x", 1)))))));
        assertInstanceOf(JTouch.class, parseAction(JActionType.TOUCH_DOWN, Map.of()));
        assertInstanceOf(JTouch.class, parseAction(JActionType.TOUCH_MOVE, Map.of()));
        assertInstanceOf(JTouchUp.class, parseAction(JActionType.TOUCH_UP, Map.of()));
        assertInstanceOf(JClickKey.class, parseAction(JActionType.CLICK_KEY, Map.of("key", List.of(1))));
        assertInstanceOf(JLongPressKey.class, parseAction(JActionType.LONG_PRESS_KEY, Map.of("key", List.of(1))));
        assertInstanceOf(JKey.class, parseAction(JActionType.KEY_DOWN, Map.of("key", 1)));
        assertInstanceOf(JKey.class, parseAction(JActionType.KEY_UP, Map.of("key", 1)));
        assertInstanceOf(JInputText.class, parseAction(JActionType.INPUT_TEXT, Map.of("input_text", "hello")));
        assertInstanceOf(JStartApp.class, parseAction(JActionType.START_APP, Map.of("package", "app")));
        assertInstanceOf(JStopApp.class, parseAction(JActionType.STOP_APP, Map.of("package", "app")));
        assertInstanceOf(JStopTask.class, parseAction(JActionType.STOP_TASK, Map.of()));
        assertInstanceOf(JScroll.class, parseAction(JActionType.SCROLL, Map.of()));
        assertInstanceOf(JCommand.class, parseAction(JActionType.COMMAND, Map.of("exec", "echo")));
        assertInstanceOf(JShell.class, parseAction(JActionType.SHELL, Map.of("cmd", "echo hi")));
        assertInstanceOf(JScreencap.class, parseAction(JActionType.SCREENCAP, Map.of()));
        assertInstanceOf(JCustomAction.class, parseAction(JActionType.CUSTOM, Map.of("custom_action", "action")));
    }

    @Test
    void parsesScalarKeysAndLegacyKeyCode() {
        JClickKey clickKey = assertInstanceOf(JClickKey.class, parseAction(JActionType.CLICK_KEY, Map.of("key", 27)));
        assertEquals(List.of(27), clickKey.key);

        JClickKey clickKeyCode = assertInstanceOf(JClickKey.class, parseAction(JActionType.CLICK_KEY, Map.of("key_code", 28)));
        assertEquals(List.of(28), clickKeyCode.key);

        JLongPressKey longPressKey = assertInstanceOf(JLongPressKey.class, parseAction(JActionType.LONG_PRESS_KEY, Map.of("key_code", 29)));
        assertEquals(List.of(29), longPressKey.key);

        JKey keyDown = assertInstanceOf(JKey.class, parseAction(JActionType.KEY_DOWN, Map.of("key_code", 30)));
        assertEquals(30, keyDown.key);

        JKey keyUp = assertInstanceOf(JKey.class, parseAction(JActionType.KEY_UP, Map.of("key_code", 31)));
        assertEquals(31, keyUp.key);
    }

    @Test
    void parseAllNormalizesStringAndListAnchors() {
        String json = """
                {
                  "A": {"anchor": "selfA", "next": ["B"]},
                  "B": {"anchor": ["selfB", "shared"], "next": []}
                }
                """;

        Map<String, JPipelineData> nodes = JPipelineParser.parseAll(json);
        assertEquals("A", nodes.get("A").name);
        assertEquals(Map.of("selfA", "A"), nodes.get("A").anchor);
        assertEquals("B", nodes.get("B").name);
        assertEquals(Map.of("selfB", "B", "shared", "B"), nodes.get("B").anchor);
    }

    @Test
    void parsesSwipeActionWithNestedEndpoints() {
        JSwipe swipe = assertInstanceOf(JSwipe.class,
                parseAction(JActionType.SWIPE,
                        Map.of("starting", 2, "begin", List.of(10, 20), "begin_offset", List.of(1, 2, 3, 4), "end",
                                List.of(Map.of("x", 100, "y", 200)), "end_offset", List.of(List.of(5, 6, 7, 8)), "end_hold", List.of(150),
                                "duration", List.of(300, 400), "only_hover", true, "contact", 2, "pressure", 3)));

        assertEquals(2, swipe.starting);
        assertEquals(List.of(10, 20), swipe.begin);
        assertEquals(List.of(1, 2, 3, 4), swipe.beginOffset);
        assertEquals(List.of(Map.of("x", 100, "y", 200)), swipe.end);
        assertEquals(List.of(List.of(5, 6, 7, 8)), swipe.endOffset);
        assertEquals(List.of(150L), swipe.endHold);
        assertEquals(List.of(300L, 400L), swipe.duration);
        assertTrue(swipe.onlyHover);
        assertEquals(2, swipe.contact);
        assertEquals(3, swipe.pressure);
    }

    @Test
    void parsesSwipeScalarEndDurationAndEndHold() {
        JSwipe swipe = assertInstanceOf(JSwipe.class,
                parseAction(JActionType.SWIPE, Map.of("end", "someNode", "duration", 500, "end_hold", 60)));

        assertEquals(List.of("someNode"), swipe.end);
        assertEquals(List.of(500L), swipe.duration);
        assertEquals(List.of(60L), swipe.endHold);
    }

    @Test
    void parsesMultiSwipeWithNestedSwipeFields() {
        JMultiSwipe multi = assertInstanceOf(JMultiSwipe.class, parseAction(JActionType.MULTI_SWIPE, Map.of("swipes", List.of(
                Map.of("starting", 1, "begin", List.of(1, 2), "end", List.of(Map.of("x", 3, "y", 4)), "end_offset",
                        List.of(List.of(5, 6, 7, 8)), "duration", List.of(250), "contact", 1, "pressure", 2),
                Map.of("begin", List.of(9, 10), "end", List.of(Map.of("x", 11, "y", 12)), "only_hover", true, "end_hold", List.of(500))))));

        assertEquals(2, multi.swipes.size());
        JSwipe first = multi.swipes.getFirst();
        assertEquals(1, first.starting);
        assertEquals(List.of(1, 2), first.begin);
        assertEquals(List.of(Map.of("x", 3, "y", 4)), first.end);
        assertEquals(List.of(List.of(5, 6, 7, 8)), first.endOffset);
        assertEquals(List.of(250L), first.duration);
        assertEquals(1, first.contact);
        assertEquals(2, first.pressure);

        JSwipe second = multi.swipes.get(1);
        assertEquals(0, second.starting);
        assertEquals(List.of(9, 10), second.begin);
        assertEquals(List.of(Map.of("x", 11, "y", 12)), second.end);
        assertTrue(second.onlyHover);
        assertEquals(List.of(500L), second.endHold);
    }

    @Test
    void parsesCommandShellAndScreencapWithNativeFields() {
        JCommand command = assertInstanceOf(JCommand.class,
                parseAction(JActionType.COMMAND, Map.of("exec", "adb", "args", List.of("shell", "echo"), "detach", true)));
        assertEquals("adb", command.exec);
        assertEquals(List.of("shell", "echo"), command.args);
        assertTrue(command.detach);

        JCommand defaultCommand = assertInstanceOf(JCommand.class, parseAction(JActionType.COMMAND, Map.of("exec", "adb")));
        assertEquals(List.of(), defaultCommand.args);
        assertFalse(defaultCommand.detach);

        JShell shell = assertInstanceOf(JShell.class, parseAction(JActionType.SHELL, Map.of("cmd", "echo hi", "shell_timeout", -1)));
        assertEquals("echo hi", shell.cmd);
        assertEquals(-1, shell.shellTimeout);

        JShell defaultShell = assertInstanceOf(JShell.class, parseAction(JActionType.SHELL, Map.of("cmd", "echo hi")));
        assertEquals(20000, defaultShell.shellTimeout);

        JScreencap screencap = assertInstanceOf(JScreencap.class,
                parseAction(JActionType.SCREENCAP, Map.of("filename", "shot.png", "format", "jpg", "quality", 85)));
        assertEquals("shot.png", screencap.filename);
        assertEquals("jpg", screencap.format);
        assertEquals(85, screencap.quality);

        JScreencap defaultScreencap = assertInstanceOf(JScreencap.class, parseAction(JActionType.SCREENCAP, Map.of()));
        assertEquals("", defaultScreencap.filename);
        assertEquals("png", defaultScreencap.format);
        assertEquals(100, defaultScreencap.quality);
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

    @Test
    void serializesCommandScreencapAndMultiSwipeWithNativeJsonKeys() {
        JCommand command = new JCommand();
        command.exec = "adb";
        command.args = List.of("shell", "echo");
        command.detach = true;

        JScreencap screencap = new JScreencap();
        screencap.filename = "shot.png";
        screencap.format = "jpg";
        screencap.quality = 85;

        JSwipe swipe = new JSwipe();
        swipe.starting = 1;
        swipe.begin = List.of(10, 20);
        swipe.beginOffset = List.of(1, 2, 3, 4);
        swipe.end = List.of(List.of(30, 40));
        swipe.endOffset = List.of(List.of(5, 6, 7, 8));
        swipe.endHold = List.of(500L);
        swipe.duration = List.of(600L, 700L);
        swipe.onlyHover = true;
        swipe.contact = 2;
        swipe.pressure = 3;

        JMultiSwipe multi = new JMultiSwipe();
        multi.swipes = List.of(swipe);

        Map<String, Object> commandJson = MaaJson.parseObject(MaaJson.write(command));
        assertEquals("adb", commandJson.get("exec"));
        assertEquals(List.of("shell", "echo"), commandJson.get("args"));
        assertEquals(true, commandJson.get("detach"));

        Map<String, Object> screencapJson = MaaJson.parseObject(MaaJson.write(screencap));
        assertEquals("shot.png", screencapJson.get("filename"));
        assertEquals("jpg", screencapJson.get("format"));
        assertEquals(85, screencapJson.get("quality"));

        Map<String, Object> multiJson = MaaJson.parseObject(MaaJson.write(multi));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> swipes = (List<Map<String, Object>>) multiJson.get("swipes");
        assertEquals(1, swipes.size());
        Map<String, Object> swipeJson = swipes.getFirst();
        assertEquals(1, swipeJson.get("starting"));
        assertEquals(List.of(10, 20), swipeJson.get("begin"));
        assertEquals(List.of(1, 2, 3, 4), swipeJson.get("begin_offset"));
        assertEquals(List.of(List.of(30, 40)), swipeJson.get("end"));
        assertEquals(List.of(List.of(5, 6, 7, 8)), swipeJson.get("end_offset"));
        assertEquals(List.of(500L), longValues((List<?>) swipeJson.get("end_hold")));
        assertEquals(List.of(600L, 700L), longValues((List<?>) swipeJson.get("duration")));
        assertEquals(true, swipeJson.get("only_hover"));
        assertEquals(2, ((Number) swipeJson.get("contact")).longValue());
        assertEquals(3, swipeJson.get("pressure"));
    }

    @Test
    void serializesWaitFreezesWithNativeJsonKeys() {
        JWaitFreezes wait = new JWaitFreezes();
        wait.time = 50;
        wait.target = List.of(1, 2, 3, 4);
        wait.targetOffset = List.of(5, 6, 7, 8);
        wait.threshold = 0.9;
        wait.method = 3;
        wait.rateLimit = 200;
        wait.timeout = 5000;

        Map<String, Object> json = MaaJson.parseObject(MaaJson.write(wait));

        assertEquals(50, ((Number) json.get("time")).intValue());
        assertEquals(List.of(1, 2, 3, 4), json.get("target"));
        assertEquals(List.of(5, 6, 7, 8), json.get("target_offset"));
        assertEquals(0.9, ((Number) json.get("threshold")).doubleValue());
        assertEquals(3, ((Number) json.get("method")).intValue());
        assertEquals(200, ((Number) json.get("rate_limit")).intValue());
        assertEquals(5000, ((Number) json.get("timeout")).intValue());
    }

    @Test
    void typedDirectApiEnumsUseNativeNames() {
        assertEquals("\"TemplateMatch\"", MaaJson.write(JRecognitionType.TEMPLATE_MATCH));
        assertEquals("\"OCR\"", MaaJson.write(JRecognitionType.OCR));
        assertEquals("\"Click\"", MaaJson.write(JActionType.CLICK));
        assertEquals("\"Shell\"", MaaJson.write(JActionType.SHELL));
    }

    private static JRecognitionParam parseRecognition(JRecognitionType type, Map<String, Object> params) {
        return JPipelineParser.parseRecognitionParam(type, params);
    }

    private static JActionParam parseAction(JActionType type, Map<String, Object> params) {
        return JPipelineParser.parseActionParam(type, params);
    }

    private static List<Long> longValues(List<?> values) {
        return values.stream().map(value -> ((Number) value).longValue()).toList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }
}
