package io.github.craun718.maafw;

import io.github.craun718.maafw.pipeline.JRecognitionType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Parsed recognition result returned inside {@link RecognitionDetail}. */
public final class RecognitionResult {

    private final JRecognitionType type;
    private final Map<String, Object> raw;
    private final MaaRect box;
    private final Double score;
    private final Integer count;
    private final String text;
    private final Integer clsIndex;
    private final String label;
    private final Object detail;
    private final List<RecognitionDetail> subResults;

    public RecognitionResult(Map<String, Object> raw) {
        this(null, raw);
    }

    public RecognitionResult(JRecognitionType type, Map<String, Object> raw) {
        this.type = type;
        this.raw = MaaResultParsers.unmodifiableMap(raw);
        this.box = MaaResultParsers.rect(this.raw.get("box"));
        this.score = MaaResultParsers.number(this.raw.get("score"));
        this.count = MaaResultParsers.integer(this.raw.get("count"));
        this.text = MaaResultParsers.string(this.raw.get("text"));
        this.clsIndex = MaaResultParsers.integer(this.raw.get("cls_index"));
        this.label = MaaResultParsers.string(this.raw.get("label"));
        this.detail = this.raw.get("detail");
        this.subResults = List.of();
    }

    public RecognitionResult(Map<String, Object> raw, List<RecognitionDetail> subResults) {
        this(null, raw, subResults);
    }

    public RecognitionResult(JRecognitionType type, Map<String, Object> raw, List<RecognitionDetail> subResults) {
        this.type = type;
        this.raw = MaaResultParsers.unmodifiableMap(raw);
        this.box = null;
        this.score = null;
        this.count = null;
        this.text = null;
        this.clsIndex = null;
        this.label = null;
        this.detail = null;
        this.subResults = subResults == null ? List.of() : List.copyOf(subResults);
    }

    public JRecognitionType type() {
        return type;
    }

    public Map<String, Object> raw() {
        return raw;
    }

    public Optional<TemplateMatchResult> asTemplateMatch() {
        return typed(JRecognitionType.TEMPLATE_MATCH, () -> new TemplateMatchResult(box(), score()));
    }

    public Optional<FeatureMatchResult> asFeatureMatch() {
        return typed(JRecognitionType.FEATURE_MATCH, () -> new FeatureMatchResult(box(), count()));
    }

    public Optional<ColorMatchResult> asColorMatch() {
        return typed(JRecognitionType.COLOR_MATCH, () -> new ColorMatchResult(box(), count()));
    }

    public Optional<OCRResult> asOCR() {
        return typed(JRecognitionType.OCR, () -> new OCRResult(box(), score(), text()));
    }

    public Optional<NeuralNetworkClassifyResult> asNeuralNetworkClassify() {
        return typed(JRecognitionType.NEURAL_NETWORK_CLASSIFY, () -> new NeuralNetworkClassifyResult(box(), score(), clsIndex(), label()));
    }

    public Optional<NeuralNetworkDetectResult> asNeuralNetworkDetect() {
        return typed(JRecognitionType.NEURAL_NETWORK_DETECT, () -> new NeuralNetworkDetectResult(box(), score(), clsIndex(), label()));
    }

    public Optional<CustomRecognitionResult> asCustom() {
        return typed(JRecognitionType.CUSTOM, () -> new CustomRecognitionResult(box(), detail()));
    }

    public Optional<AndRecognitionResult> asAnd() {
        return typed(JRecognitionType.AND, () -> new AndRecognitionResult(subResults()));
    }

    public Optional<OrRecognitionResult> asOr() {
        return typed(JRecognitionType.OR, () -> new OrRecognitionResult(subResults()));
    }

    /** Returns the typed value for the reported algorithm, or {@code null} for unknown types. */
    public Object value() {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case TEMPLATE_MATCH -> asTemplateMatch().orElse(null);
            case FEATURE_MATCH -> asFeatureMatch().orElse(null);
            case COLOR_MATCH -> asColorMatch().orElse(null);
            case OCR -> asOCR().orElse(null);
            case NEURAL_NETWORK_CLASSIFY -> asNeuralNetworkClassify().orElse(null);
            case NEURAL_NETWORK_DETECT -> asNeuralNetworkDetect().orElse(null);
            case CUSTOM -> asCustom().orElse(null);
            case AND -> asAnd().orElse(null);
            case OR -> asOr().orElse(null);
            default -> null;
        };
    }

    public MaaRect box() {
        return box;
    }

    public Double score() {
        return score;
    }

    public Integer count() {
        return count;
    }

    public String text() {
        return text;
    }

    public Integer clsIndex() {
        return clsIndex;
    }

    public String label() {
        return label;
    }

    public Object detail() {
        return detail;
    }

    public List<RecognitionDetail> subResults() {
        return subResults;
    }

    private <T> Optional<T> typed(JRecognitionType expected, java.util.function.Supplier<T> supplier) {
        if (type != expected) {
            return Optional.empty();
        }
        return Optional.ofNullable(supplier.get());
    }

    @Override
    public String toString() {
        return "RecognitionResult" + raw;
    }

    public record TemplateMatchResult(MaaRect box, Double score) {
    }

    public record FeatureMatchResult(MaaRect box, Integer count) {
    }

    public record ColorMatchResult(MaaRect box, Integer count) {
    }

    public record OCRResult(MaaRect box, Double score, String text) {
    }

    public record NeuralNetworkClassifyResult(MaaRect box, Double score, Integer clsIndex, String label) {
    }

    public record NeuralNetworkDetectResult(MaaRect box, Double score, Integer clsIndex, String label) {
    }

    public record CustomRecognitionResult(MaaRect box, Object detail) {
    }

    public record AndRecognitionResult(List<RecognitionDetail> subResults) {
    }

    public record OrRecognitionResult(List<RecognitionDetail> subResults) {
    }
}
