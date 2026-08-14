package io.github.craun718.maafw.pipeline;

import io.github.craun718.maafw.MaaJson;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Converts pipeline v2 node JSON into typed pipeline models. */
public final class JPipelineParser {

    private JPipelineParser() {
    }

    public static JPipelineData parse(String json) {
        return parse(MaaJson.parseObject(json));
    }

    public static JPipelineData parse(Object data) {
        if (data instanceof String json) {
            return parse(json);
        }
        if (data instanceof Map<?, ?> raw) {
            Map<String, Object> values = new LinkedHashMap<>();
            raw.forEach((key, item) -> values.put(String.valueOf(key), item));
            return parse(values);
        }
        throw new IllegalArgumentException("Pipeline node must be a JSON object");
    }

    public static JPipelineData parse(Map<String, Object> data) {
        Objects.requireNonNull(data, "data");
        return parse(data, null);
    }

    public static JPipelineData parse(Object data, String nodeName) {
        if (data instanceof String json) {
            return parse(json, nodeName);
        }
        if (data instanceof Map<?, ?> raw) {
            Map<String, Object> values = new LinkedHashMap<>();
            raw.forEach((key, item) -> values.put(String.valueOf(key), item));
            return parse(values, nodeName);
        }
        throw new IllegalArgumentException("Pipeline node must be a JSON object");
    }

    public static JPipelineData parse(String json, String nodeName) {
        return parse(MaaJson.parseObject(json), nodeName);
    }

    private static JPipelineData parse(Map<String, Object> data, String nodeName) {
        Objects.requireNonNull(data, "data");

        JRecognition recognition = null;
        Map<String, Object> recognitionData = map(data.get("recognition"));
        if (recognitionData != null) {
            recognition = new JRecognition();
            String recognitionTypeName = string(recognitionData.get("type"));
            recognition.type = JRecognitionType.of(recognitionTypeName);
            recognition.param = parseRecognitionParam(recognition.type, mapOrEmpty(recognitionData.get("param")));
        }

        JAction action = null;
        Map<String, Object> actionData = map(data.get("action"));
        if (actionData != null) {
            action = new JAction();
            String actionTypeName = string(actionData.get("type"));
            action.type = JActionType.of(actionTypeName);
            action.param = parseActionParam(action.type, mapOrEmpty(actionData.get("param")));
        }

        JPipelineData pipeline = new JPipelineData();
        pipeline.name = nodeName;
        pipeline.recognition = recognition;
        pipeline.action = action;
        pipeline.next = parseNodeAttrList(data.get("next"));
        pipeline.rateLimit = longValue(data.get("rate_limit"), 1000);
        pipeline.timeout = longValue(data.get("timeout"), 20000);
        pipeline.onError = parseNodeAttrList(data.get("on_error"));
        pipeline.anchor = parseAnchor(nodeName, data.get("anchor"));
        pipeline.inverse = booleanValue(data.get("inverse"), false);
        pipeline.enabled = booleanValue(data.get("enabled"), true);
        pipeline.preDelay = longValue(data.get("pre_delay"), 200);
        pipeline.postDelay = longValue(data.get("post_delay"), 200);
        pipeline.preWaitFreezes = parseWaitFreezes(data.get("pre_wait_freezes"));
        pipeline.postWaitFreezes = parseWaitFreezes(data.get("post_wait_freezes"));
        pipeline.repeat = longValue(data.get("repeat"), 1);
        pipeline.repeatDelay = longValue(data.get("repeat_delay"), 0);
        pipeline.repeatWaitFreezes = parseWaitFreezes(data.get("repeat_wait_freezes"));
        pipeline.maxHit = longValue(data.get("max_hit"), 4294967295L);
        pipeline.focus = data.get("focus");
        pipeline.attach = objectMap(data.get("attach"));
        return pipeline;
    }

    public static Map<String, JPipelineData> parseAll(String json) {
        Map<String, Object> root = MaaJson.parseObject(json);
        Map<String, JPipelineData> nodes = new LinkedHashMap<>();
        root.forEach((name, value) -> nodes.put(name, parse(value, name)));
        return nodes;
    }

    public static JRecognitionParam parseRecognitionParam(JRecognitionType type, Map<String, Object> data) {
        if (type == null) {
            throw new IllegalArgumentException("Recognition type is null");
        }
        Map<String, Object> values = data == null ? Map.of() : data;
        return switch (type) {
            case DIRECT_HIT -> {
                JDirectHit param = new JDirectHit();
                param.roi = target(values.get("roi"), param.roi);
                param.roiOffset = rect(values.get("roi_offset"), param.roiOffset);
                yield param;
            }
            case TEMPLATE_MATCH -> {
                JTemplateMatch param = new JTemplateMatch();
                param.template = requiredStringList(values.get("template"));
                param.roi = target(values.get("roi"), param.roi);
                param.roiOffset = rect(values.get("roi_offset"), param.roiOffset);
                param.threshold = doubleList(values.get("threshold"), param.threshold);
                param.orderBy = string(values.get("order_by"), param.orderBy);
                param.index = integer(values.get("index"), param.index);
                param.method = integer(values.get("method"), param.method);
                param.greenMask = booleanValue(values.get("green_mask"), param.greenMask);
                yield param;
            }
            case FEATURE_MATCH -> {
                JFeatureMatch param = new JFeatureMatch();
                param.template = requiredStringList(values.get("template"));
                param.roi = target(values.get("roi"), param.roi);
                param.roiOffset = rect(values.get("roi_offset"), param.roiOffset);
                param.detector = string(values.get("detector"), param.detector);
                param.orderBy = string(values.get("order_by"), param.orderBy);
                param.count = integer(values.get("count"), param.count);
                param.index = integer(values.get("index"), param.index);
                param.greenMask = booleanValue(values.get("green_mask"), param.greenMask);
                param.ratio = doubleValue(values.get("ratio"), param.ratio);
                yield param;
            }
            case COLOR_MATCH -> {
                JColorMatch param = new JColorMatch();
                param.lower = requiredIntListList(values.get("lower"));
                param.upper = requiredIntListList(values.get("upper"));
                param.roi = target(values.get("roi"), param.roi);
                param.roiOffset = rect(values.get("roi_offset"), param.roiOffset);
                param.orderBy = string(values.get("order_by"), param.orderBy);
                param.method = integer(values.get("method"), param.method);
                param.count = integer(values.get("count"), param.count);
                param.index = integer(values.get("index"), param.index);
                param.connected = booleanValue(values.get("connected"), param.connected);
                yield param;
            }
            case OCR -> {
                JOCR param = new JOCR();
                param.expected = stringList(values.get("expected"), param.expected);
                param.roi = target(values.get("roi"), param.roi);
                param.roiOffset = rect(values.get("roi_offset"), param.roiOffset);
                param.threshold = doubleValue(values.get("threshold"), param.threshold);
                param.replace = stringListList(values.get("replace"), param.replace);
                param.orderBy = string(values.get("order_by"), param.orderBy);
                param.index = integer(values.get("index"), param.index);
                param.onlyRec = booleanValue(values.get("only_rec"), param.onlyRec);
                param.model = string(values.get("model"), param.model);
                param.colorFilter = string(values.get("color_filter"), param.colorFilter);
                yield param;
            }
            case NEURAL_NETWORK_CLASSIFY -> {
                JNeuralNetworkClassify param = new JNeuralNetworkClassify();
                param.model = requiredString(values.get("model"));
                param.expected = intList(values.get("expected"), param.expected);
                param.roi = target(values.get("roi"), param.roi);
                param.roiOffset = rect(values.get("roi_offset"), param.roiOffset);
                param.labels = stringList(values.get("labels"), param.labels);
                param.orderBy = string(values.get("order_by"), param.orderBy);
                param.index = integer(values.get("index"), param.index);
                yield param;
            }
            case NEURAL_NETWORK_DETECT -> {
                JNeuralNetworkDetect param = new JNeuralNetworkDetect();
                param.model = requiredString(values.get("model"));
                param.expected = intList(values.get("expected"), param.expected);
                param.roi = target(values.get("roi"), param.roi);
                param.roiOffset = rect(values.get("roi_offset"), param.roiOffset);
                param.labels = stringList(values.get("labels"), param.labels);
                param.threshold = doubleList(values.get("threshold"), param.threshold);
                param.orderBy = string(values.get("order_by"), param.orderBy);
                param.index = integer(values.get("index"), param.index);
                yield param;
            }
            case CUSTOM -> {
                JCustomRecognition param = new JCustomRecognition();
                param.customRecognition = requiredString(values.get("custom_recognition"));
                param.roi = target(values.get("roi"), param.roi);
                param.roiOffset = rect(values.get("roi_offset"), param.roiOffset);
                param.customRecognitionParam = values.get("custom_recognition_param");
                yield param;
            }
            case AND -> {
                JAnd param = new JAnd();
                param.allOf = subRecognitionList(values.get("all_of"), param.allOf);
                param.boxIndex = integer(values.get("box_index"), param.boxIndex);
                yield param;
            }
            case OR -> {
                JOr param = new JOr();
                param.anyOf = subRecognitionList(values.get("any_of"), param.anyOf);
                yield param;
            }
        };
    }

    public static JActionParam parseActionParam(JActionType type, Map<String, Object> data) {
        if (type == null) {
            throw new IllegalArgumentException("Action type is null");
        }
        Map<String, Object> values = data == null ? Map.of() : data;
        return switch (type) {
            case DO_NOTHING -> new JDoNothing();
            case CLICK -> {
                JClick param = new JClick();
                param.target = target(values.get("target"), param.target);
                param.targetOffset = rect(values.get("target_offset"), param.targetOffset);
                param.contact = longValue(values.get("contact"), param.contact);
                param.pressure = integer(values.get("pressure"), param.pressure);
                yield param;
            }
            case LONG_PRESS -> {
                JLongPress param = new JLongPress();
                param.target = target(values.get("target"), param.target);
                param.targetOffset = rect(values.get("target_offset"), param.targetOffset);
                param.duration = longValue(values.get("duration"), param.duration);
                param.contact = longValue(values.get("contact"), param.contact);
                param.pressure = integer(values.get("pressure"), param.pressure);
                yield param;
            }
            case SWIPE -> {
                JSwipe param = new JSwipe();
                param.starting = longValue(values.get("starting"), param.starting);
                param.begin = target(values.get("begin"), param.begin);
                param.beginOffset = rect(values.get("begin_offset"), param.beginOffset);
                param.end = objectList(values.get("end"), param.end);
                param.endOffset = intListList(values.get("end_offset"), param.endOffset);
                param.endHold = longList(values.get("end_hold"), param.endHold);
                param.duration = longList(values.get("duration"), param.duration);
                param.onlyHover = booleanValue(values.get("only_hover"), param.onlyHover);
                param.contact = longValue(values.get("contact"), param.contact);
                param.pressure = integer(values.get("pressure"), param.pressure);
                yield param;
            }
            case MULTI_SWIPE -> {
                JMultiSwipe param = new JMultiSwipe();
                param.swipes = requiredSwipeList(values.get("swipes"));
                yield param;
            }
            case TOUCH_DOWN, TOUCH_MOVE -> {
                JTouch param = new JTouch();
                param.contact = longValue(values.get("contact"), param.contact);
                param.target = target(values.get("target"), param.target);
                param.targetOffset = rect(values.get("target_offset"), param.targetOffset);
                param.pressure = integer(values.get("pressure"), param.pressure);
                yield param;
            }
            case TOUCH_UP -> {
                JTouchUp param = new JTouchUp();
                param.contact = longValue(values.get("contact"), param.contact);
                yield param;
            }
            case CLICK_KEY -> {
                JClickKey param = new JClickKey();
                param.key = requiredKeyList(values);
                yield param;
            }
            case LONG_PRESS_KEY -> {
                JLongPressKey param = new JLongPressKey();
                param.key = requiredKeyList(values);
                param.duration = longValue(values.get("duration"), param.duration);
                yield param;
            }
            case KEY_DOWN, KEY_UP -> {
                JKey param = new JKey();
                param.key = keyCode(values);
                yield param;
            }
            case INPUT_TEXT -> {
                JInputText param = new JInputText();
                param.inputText = requiredString(values.get("input_text"));
                yield param;
            }
            case START_APP -> {
                JStartApp param = new JStartApp();
                param.packageName = requiredString(values.get("package"));
                yield param;
            }
            case STOP_APP -> {
                JStopApp param = new JStopApp();
                param.packageName = requiredString(values.get("package"));
                yield param;
            }
            case STOP_TASK -> new JStopTask();
            case SCROLL -> {
                JScroll param = new JScroll();
                param.target = target(values.get("target"), param.target);
                param.targetOffset = rect(values.get("target_offset"), param.targetOffset);
                param.dx = integer(values.get("dx"), param.dx);
                param.dy = integer(values.get("dy"), param.dy);
                yield param;
            }
            case COMMAND -> {
                JCommand param = new JCommand();
                param.exec = requiredString(values.get("exec"));
                param.args = stringList(values.get("args"), param.args);
                param.detach = booleanValue(values.get("detach"), param.detach);
                yield param;
            }
            case SHELL -> {
                JShell param = new JShell();
                param.cmd = requiredString(values.get("cmd"));
                param.shellTimeout = longValue(values.get("shell_timeout"), param.shellTimeout);
                yield param;
            }
            case SCREENCAP -> {
                JScreencap param = new JScreencap();
                param.filename = string(values.get("filename"), param.filename);
                param.format = string(values.get("format"), param.format);
                param.quality = integer(values.get("quality"), param.quality);
                yield param;
            }
            case CUSTOM -> {
                JCustomAction param = new JCustomAction();
                param.customAction = requiredString(values.get("custom_action"));
                param.target = target(values.get("target"), param.target);
                param.customActionParam = values.get("custom_action_param");
                param.targetOffset = rect(values.get("target_offset"), param.targetOffset);
                yield param;
            }
        };
    }

    public static JWaitFreezes parseWaitFreezes(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            JWaitFreezes wait = new JWaitFreezes();
            wait.time = number.longValue();
            return wait;
        }
        Map<String, Object> values = map(value);
        if (values == null) {
            return null;
        }
        JWaitFreezes wait = new JWaitFreezes();
        wait.time = longValue(values.get("time"), wait.time);
        wait.target = target(values.get("target"), wait.target);
        wait.targetOffset = rect(values.get("target_offset"), wait.targetOffset);
        wait.threshold = doubleValue(values.get("threshold"), wait.threshold);
        wait.method = integer(values.get("method"), wait.method);
        wait.rateLimit = longValue(values.get("rate_limit"), wait.rateLimit);
        wait.timeout = longValue(values.get("timeout"), wait.timeout);
        return wait;
    }

    public static List<JNodeAttr> parseNodeAttrList(Object value) {
        if (value == null) {
            return List.of();
        }
        List<Object> items = value instanceof String ? List.of(value) : list(value);
        if (items == null) {
            return List.of();
        }
        List<JNodeAttr> result = new ArrayList<>(items.size());
        for (Object item : items) {
            if (item instanceof String name) {
                result.add(parseNodeAttrString(name));
                continue;
            }
            Map<String, Object> values = map(item);
            if (values == null) {
                continue;
            }
            JNodeAttr attr = new JNodeAttr();
            attr.name = requiredString(values.get("name"));
            attr.jumpBack = booleanValue(values.get("jump_back"), attr.jumpBack);
            attr.anchor = booleanValue(values.get("anchor"), attr.anchor);
            result.add(attr);
        }
        return List.copyOf(result);
    }

    private static JNodeAttr parseNodeAttrString(String raw) {
        String remaining = raw;
        JNodeAttr attr = new JNodeAttr();
        while (remaining.startsWith("[")) {
            int end = remaining.indexOf(']');
            if (end < 0) {
                break;
            }
            String prefix = remaining.substring(0, end + 1);
            if ("[JumpBack]".equals(prefix)) {
                attr.jumpBack = true;
            } else if ("[Anchor]".equals(prefix)) {
                attr.anchor = true;
            }
            remaining = remaining.substring(end + 1);
        }
        attr.name = remaining;
        return attr;
    }

    public static String toJson(JPipelineData data) {
        return MaaJson.write(data);
    }

    private static Map<String, Object> map(Object value) {
        if (value instanceof Map<?, ?> raw) {
            Map<String, Object> result = new LinkedHashMap<>();
            raw.forEach((key, item) -> result.put(String.valueOf(key), item));
            return result;
        }
        return null;
    }

    private static Map<String, Object> mapOrEmpty(Object value) {
        Map<String, Object> result = map(value);
        return result == null ? Map.of() : result;
    }

    private static List<Object> list(Object value) {
        if (value instanceof List<?> raw) {
            return new ArrayList<>(raw);
        }
        return null;
    }

    private static Object target(Object value, Object fallback) {
        return value == null ? fallback : value;
    }

    private static String string(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private static String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String requiredString(Object value) {
        String result = string(value);
        if (result == null) {
            throw new IllegalArgumentException("Missing required string parameter");
        }
        return result;
    }

    private static int integer(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return fallback;
    }

    private static long longValue(Object value, long fallback) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return fallback;
    }

    private static double doubleValue(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return fallback;
    }

    private static boolean booleanValue(Object value, boolean fallback) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        if (value instanceof String text) {
            return Boolean.parseBoolean(text);
        }
        return fallback;
    }

    private static List<Integer> rect(Object value, List<Integer> fallback) {
        return intList(value, fallback);
    }

    private static List<String> stringList(Object value, List<String> fallback) {
        if (value instanceof String text) {
            return List.of(text);
        }
        List<Object> items = list(value);
        if (items == null) {
            return fallback;
        }
        List<String> result = new ArrayList<>(items.size());
        for (Object item : items) {
            if (item != null) {
                result.add(String.valueOf(item));
            }
        }
        return List.copyOf(result);
    }

    private static List<String> requiredStringList(Object value) {
        List<String> result = stringList(value, null);
        if (result == null || result.isEmpty()) {
            throw new IllegalArgumentException("Missing required string list parameter");
        }
        return result;
    }

    private static List<Integer> intList(Object value, List<Integer> fallback) {
        if (value instanceof Number number) {
            return List.of(number.intValue());
        }
        List<Object> items = list(value);
        if (items == null) {
            return fallback;
        }
        List<Integer> result = new ArrayList<>(items.size());
        for (Object item : items) {
            result.add(integer(item, 0));
        }
        return List.copyOf(result);
    }

    private static List<Integer> requiredIntList(Object value) {
        List<Integer> result = intList(value, null);
        if (result == null || result.isEmpty()) {
            throw new IllegalArgumentException("Missing required int list parameter");
        }
        return result;
    }

    private static List<Integer> requiredKeyList(Map<String, Object> values) {
        Object key = values.get("key");
        if (key == null) {
            key = values.get("key_code");
        }
        List<Integer> result = intList(key, null);
        if (result == null || result.isEmpty()) {
            throw new IllegalArgumentException("Missing required key parameter");
        }
        return result;
    }

    private static int keyCode(Map<String, Object> values) {
        Object key = values.get("key");
        if (key == null) {
            key = values.get("key_code");
        }
        return integer(key, 0);
    }

    private static List<Long> longList(Object value, List<Long> fallback) {
        if (value instanceof Number number) {
            return List.of(number.longValue());
        }
        List<Object> items = list(value);
        if (items == null) {
            return fallback;
        }
        List<Long> result = new ArrayList<>(items.size());
        for (Object item : items) {
            result.add(longValue(item, 0));
        }
        return List.copyOf(result);
    }

    private static List<Double> doubleList(Object value, List<Double> fallback) {
        if (value instanceof Number number) {
            return List.of(number.doubleValue());
        }
        List<Object> items = list(value);
        if (items == null) {
            return fallback;
        }
        List<Double> result = new ArrayList<>(items.size());
        for (Object item : items) {
            result.add(doubleValue(item, 0));
        }
        return List.copyOf(result);
    }

    private static List<List<Integer>> intListList(Object value, List<List<Integer>> fallback) {
        if (value instanceof List<?> flat && (flat.isEmpty() || !(flat.getFirst() instanceof List<?>))) {
            List<Integer> row = intList(flat, List.of());
            return List.of(row);
        }
        List<Object> items = list(value);
        if (items == null) {
            return fallback;
        }
        List<List<Integer>> result = new ArrayList<>(items.size());
        for (Object item : items) {
            result.add(intList(item, List.of()));
        }
        return List.copyOf(result);
    }

    private static List<List<Integer>> requiredIntListList(Object value) {
        List<List<Integer>> result = intListList(value, null);
        if (result == null || result.isEmpty()) {
            throw new IllegalArgumentException("Missing required nested int list parameter");
        }
        return result;
    }

    private static List<List<String>> stringListList(Object value, List<List<String>> fallback) {
        if (value instanceof List<?> flat && (flat.isEmpty() || !(flat.getFirst() instanceof List<?>))) {
            if (flat.isEmpty()) {
                return List.of();
            }
            List<String> row = stringList(flat, List.of());
            return List.of(row);
        }
        List<Object> items = list(value);
        if (items == null) {
            return fallback;
        }
        List<List<String>> result = new ArrayList<>(items.size());
        for (Object item : items) {
            result.add(stringList(item, List.of()));
        }
        return List.copyOf(result);
    }

    private static List<Object> objectList(Object value, List<Object> fallback) {
        if (value == null) {
            return fallback;
        }
        if (!(value instanceof List<?>)) {
            return List.of(value);
        }
        List<Object> items = list(value);
        return List.copyOf(items);
    }

    static List<JSubRecognitionItem> subRecognitionList(Object value, List<JSubRecognitionItem> fallback) {
        if (value == null) {
            return fallback;
        }
        List<Object> items = list(value);
        if (items == null) {
            return List.of(parseSubRecognitionItem(value));
        }
        List<JSubRecognitionItem> result = new ArrayList<>(items.size());
        for (Object item : items) {
            result.add(parseSubRecognitionItem(item));
        }
        return List.copyOf(result);
    }

    static JSubRecognitionItem parseSubRecognitionItem(Object value) {
        if (value instanceof String name) {
            return JSubRecognitionItem.ref(name);
        }
        Map<String, Object> values = map(value);
        if (values == null) {
            throw new IllegalArgumentException("Sub-recognition item must be a string or object");
        }
        return JSubRecognitionItem.inline(parseInlineRecognition(values));
    }

    private static JInlineRecognition parseInlineRecognition(Map<String, Object> values) {
        JInlineRecognition inline = new JInlineRecognition();
        inline.subName = string(values.get("sub_name"));

        String typeName = string(values.get("type"));
        boolean legacyShape = false;
        if (typeName == null) {
            typeName = string(values.get("recognition"));
            legacyShape = true;
        }
        if (typeName == null) {
            throw new IllegalArgumentException("Inline recognition type is required");
        }
        inline.type = JRecognitionType.of(typeName);

        Map<String, Object> paramValues = mapOrEmpty(values.get("param"));
        if (legacyShape && !values.containsKey("param")) {
            paramValues = values;
        }
        inline.param = parseRecognitionParam(inline.type, paramValues);
        return inline;
    }

    private static List<JSwipe> requiredSwipeList(Object value) {
        List<Object> items = list(value);
        if (items == null) {
            throw new IllegalArgumentException("Missing required swipes parameter");
        }
        List<JSwipe> result = new ArrayList<>(items.size());
        for (Object item : items) {
            Map<String, Object> values = map(item);
            if (values == null) {
                throw new IllegalArgumentException("Swipe entry must be an object");
            }
            JSwipe swipe = new JSwipe();
            swipe.starting = longValue(values.get("starting"), swipe.starting);
            swipe.begin = target(values.get("begin"), swipe.begin);
            swipe.beginOffset = rect(values.get("begin_offset"), swipe.beginOffset);
            swipe.end = objectList(values.get("end"), swipe.end);
            swipe.endOffset = intListList(values.get("end_offset"), swipe.endOffset);
            swipe.endHold = longList(values.get("end_hold"), swipe.endHold);
            swipe.duration = longList(values.get("duration"), swipe.duration);
            swipe.onlyHover = booleanValue(values.get("only_hover"), swipe.onlyHover);
            swipe.contact = longValue(values.get("contact"), swipe.contact);
            swipe.pressure = integer(values.get("pressure"), swipe.pressure);
            result.add(swipe);
        }
        return List.copyOf(result);
    }

    private static Map<String, String> parseAnchor(String nodeName, Object value) {
        if (value instanceof String anchor) {
            return nodeName == null ? Map.of() : Map.of(anchor, nodeName);
        }
        if (value instanceof List<?> anchors) {
            if (nodeName == null) {
                return Map.of();
            }
            Map<String, String> result = new LinkedHashMap<>();
            for (Object item : anchors) {
                if (item instanceof String anchor) {
                    result.put(anchor, nodeName);
                }
            }
            return Map.copyOf(result);
        }
        Map<String, Object> values = map(value);
        if (values == null) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        values.forEach((key, item) -> result.put(key, string(item, "")));
        return Map.copyOf(result);
    }

    private static Map<String, Object> objectMap(Object value) {
        Map<String, Object> result = map(value);
        return result == null ? Map.of() : Map.copyOf(result);
    }
}
