package io.github.craun718.maafw;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Parsed recognition result returned inside {@link RecognitionDetail}. */
public final class RecognitionResult {

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
        this.raw = raw == null ? Map.of() : Map.copyOf(raw);
        this.box = MaaResultParsers.rect(this.raw.get("box"));
        this.score = MaaResultParsers.number(this.raw.get("score"));
        this.count = MaaResultParsers.integer(this.raw.get("count"));
        this.text = MaaResultParsers.string(this.raw.get("text"));
        this.clsIndex = MaaResultParsers.integer(this.raw.get("cls_index"));
        this.label = MaaResultParsers.string(this.raw.get("label"));
        this.detail = this.raw.get("detail");
        this.subResults = Collections.emptyList();
    }

    public RecognitionResult(Map<String, Object> raw, List<RecognitionDetail> subResults) {
        this.raw = raw == null ? Map.of() : Map.copyOf(raw);
        this.box = null;
        this.score = null;
        this.count = null;
        this.text = null;
        this.clsIndex = null;
        this.label = null;
        this.detail = null;
        this.subResults = subResults == null ? List.of() : List.copyOf(subResults);
    }

    public Map<String, Object> raw() {
        return raw;
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

    @Override
    public String toString() {
        return "RecognitionResult" + raw;
    }
}
