package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

/**
 * One element of And {@code all_of} / Or {@code any_of}.
 *
 * <p>It is either a node name string reference or an inline recognition object carrying
 * {@code sub_name}, {@code type} and {@code param}.
 */
public final class JSubRecognitionItem {

    private final String nodeName;
    private final JInlineRecognition inline;

    private JSubRecognitionItem(String nodeName, JInlineRecognition inline) {
        this.nodeName = nodeName;
        this.inline = inline;
    }

    public static JSubRecognitionItem ref(String nodeName) {
        return new JSubRecognitionItem(Objects.requireNonNull(nodeName, "nodeName"), null);
    }

    public static JSubRecognitionItem inline(JInlineRecognition inline) {
        return new JSubRecognitionItem(null, Objects.requireNonNull(inline, "inline"));
    }

    public static JSubRecognitionItem inline(JRecognition recognition) {
        return inline(JInlineRecognition.of(null, recognition));
    }

    public static JSubRecognitionItem inline(String subName, JRecognition recognition) {
        return inline(JInlineRecognition.of(subName, recognition));
    }

    public String nodeName() {
        return nodeName;
    }

    public JInlineRecognition inline() {
        return inline;
    }

    public boolean isInline() {
        return inline != null;
    }

    @JsonValue
    public Object toJsonValue() {
        return nodeName != null ? nodeName : inline;
    }
}
