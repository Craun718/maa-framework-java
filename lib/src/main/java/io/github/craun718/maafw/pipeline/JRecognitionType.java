package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Recognition type names used by pipeline v2 {@code recognition.type}. */
public enum JRecognitionType {

    DIRECT_HIT("DirectHit"), TEMPLATE_MATCH("TemplateMatch"), FEATURE_MATCH("FeatureMatch"), COLOR_MATCH("ColorMatch"), OCR(
            "OCR"), NEURAL_NETWORK_CLASSIFY(
                    "NeuralNetworkClassify"), NEURAL_NETWORK_DETECT("NeuralNetworkDetect"), AND("And"), OR("Or"), CUSTOM("Custom");

    private final String nativeName;

    JRecognitionType(String nativeName) {
        this.nativeName = nativeName;
    }

    @JsonValue
    public String nativeName() {
        return nativeName;
    }

    @JsonCreator
    public static JRecognitionType of(String name) {
        if (name == null) {
            return null;
        }
        for (JRecognitionType type : values()) {
            if (type.nativeName.equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown recognition type: " + name);
    }
}
