package io.github.craun718.maafw.pipeline;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pipeline v2 recognition section. */
public final class JRecognition {

    public JRecognitionType type;
    public JRecognitionParam param;

    public static JRecognition of(JRecognitionType type, JRecognitionParam param) {
        JRecognition recognition = new JRecognition();
        recognition.type = type;
        recognition.param = param;
        return recognition;
    }

    public static JRecognition directHit() {
        return of(JRecognitionType.DIRECT_HIT, new JDirectHit());
    }

    public static JRecognition templateMatch(JTemplateMatch param) {
        return of(JRecognitionType.TEMPLATE_MATCH, param);
    }

    public static JRecognition templateMatch(List<String> template) {
        JTemplateMatch param = new JTemplateMatch();
        param.template = List.copyOf(template);
        return templateMatch(param);
    }

    public static JRecognition featureMatch(JFeatureMatch param) {
        return of(JRecognitionType.FEATURE_MATCH, param);
    }

    public static JRecognition featureMatch(List<String> template) {
        JFeatureMatch param = new JFeatureMatch();
        param.template = List.copyOf(template);
        return featureMatch(param);
    }

    public static JRecognition colorMatch(JColorMatch param) {
        return of(JRecognitionType.COLOR_MATCH, param);
    }

    public static JRecognition ocr(JOCR param) {
        return of(JRecognitionType.OCR, param);
    }

    public static JRecognition neuralNetworkClassify(JNeuralNetworkClassify param) {
        return of(JRecognitionType.NEURAL_NETWORK_CLASSIFY, param);
    }

    public static JRecognition neuralNetworkClassify(String model) {
        JNeuralNetworkClassify param = new JNeuralNetworkClassify();
        param.model = model;
        return neuralNetworkClassify(param);
    }

    public static JRecognition neuralNetworkDetect(JNeuralNetworkDetect param) {
        return of(JRecognitionType.NEURAL_NETWORK_DETECT, param);
    }

    public static JRecognition neuralNetworkDetect(String model) {
        JNeuralNetworkDetect param = new JNeuralNetworkDetect();
        param.model = model;
        return neuralNetworkDetect(param);
    }

    public static JRecognition and(Object... allOf) {
        JAnd param = new JAnd();
        param.allOf = subRecognitionItems(allOf);
        return of(JRecognitionType.AND, param);
    }

    public static JRecognition and(JSubRecognitionItem... allOf) {
        JAnd param = new JAnd();
        param.allOf = List.of(allOf);
        return of(JRecognitionType.AND, param);
    }

    public static JRecognition and(List<?> allOf) {
        return and(allOf == null ? new Object[0] : allOf.toArray());
    }

    public static JRecognition or(Object... anyOf) {
        JOr param = new JOr();
        param.anyOf = subRecognitionItems(anyOf);
        return of(JRecognitionType.OR, param);
    }

    public static JRecognition or(JSubRecognitionItem... anyOf) {
        JOr param = new JOr();
        param.anyOf = List.of(anyOf);
        return of(JRecognitionType.OR, param);
    }

    public static JRecognition or(List<?> anyOf) {
        return or(anyOf == null ? new Object[0] : anyOf.toArray());
    }

    private static List<JSubRecognitionItem> subRecognitionItems(Object... items) {
        List<JSubRecognitionItem> result = new ArrayList<>(items.length);
        for (Object item : items) {
            if (item instanceof JSubRecognitionItem sub) {
                result.add(sub);
            } else if (item instanceof String name) {
                result.add(JSubRecognitionItem.ref(name));
            } else if (item instanceof JRecognition recognition) {
                result.add(JSubRecognitionItem.inline(recognition));
            } else if (item instanceof Map<?, ?> raw) {
                Map<String, Object> values = new LinkedHashMap<>();
                raw.forEach((key, value) -> values.put(String.valueOf(key), value));
                result.add(JPipelineParser.parseSubRecognitionItem(values));
            } else {
                throw new IllegalArgumentException(
                        "Sub-recognition item must be a node name, inline recognition, or typed item");
            }
        }
        return List.copyOf(result);
    }

    public static JRecognition custom(JCustomRecognition param) {
        return of(JRecognitionType.CUSTOM, param);
    }

    public static JRecognition custom(String customRecognition) {
        JCustomRecognition param = new JCustomRecognition();
        param.customRecognition = customRecognition;
        return custom(param);
    }

    public JRecognition type(JRecognitionType type) {
        this.type = type;
        return this;
    }

    public JRecognition param(JRecognitionParam param) {
        this.param = param;
        return this;
    }
}
