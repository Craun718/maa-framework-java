package io.github.craun718.maafw;

import java.util.List;
import java.util.Map;

/** Parsed action result returned inside {@link ActionDetail}. */
public final class ActionResult {

    private final Map<String, Object> raw;

    public ActionResult(Map<String, Object> raw) {
        this.raw = raw == null ? Map.of() : Map.copyOf(raw);
    }

    public Map<String, Object> raw() {
        return raw;
    }

    public MaaPoint point() {
        return MaaResultParsers.point(raw.get("point"));
    }

    public MaaPoint begin() {
        return MaaResultParsers.point(raw.get("begin"));
    }

    public List<MaaPoint> end() {
        return MaaResultParsers.pointList(raw.get("end"));
    }

    public List<Integer> endHold() {
        return MaaResultParsers.integerList(raw.get("end_hold"));
    }

    public List<Integer> durations() {
        return MaaResultParsers.integerList(raw.get("duration"));
    }

    public List<Integer> keycodes() {
        return MaaResultParsers.integerList(raw.get("keycode"));
    }

    public String text() {
        return MaaResultParsers.string(raw.get("text"));
    }

    public String packageName() {
        return MaaResultParsers.string(raw.get("package"));
    }

    public Integer contact() {
        return MaaResultParsers.integer(raw.get("contact"));
    }

    public Integer pressure() {
        return MaaResultParsers.integer(raw.get("pressure"));
    }

    public Integer duration() {
        return MaaResultParsers.integer(raw.get("duration"));
    }

    public Integer dx() {
        return MaaResultParsers.integer(raw.get("dx"));
    }

    public Integer dy() {
        return MaaResultParsers.integer(raw.get("dy"));
    }

    public Boolean onlyHover() {
        return MaaResultParsers.booleanValue(raw.get("only_hover"));
    }

    public Boolean success() {
        return MaaResultParsers.booleanValue(raw.get("success"));
    }

    public String cmd() {
        return MaaResultParsers.string(raw.get("cmd"));
    }

    public Long shellTimeout() {
        return MaaResultParsers.longValue(raw.get("shell_timeout"));
    }

    public String output() {
        return MaaResultParsers.string(raw.get("output"));
    }

    public List<ActionResult> swipes() {
        Object value = raw.get("swipes");
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .filter(item -> item instanceof Map<?, ?>)
                .map(item -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> typed = (Map<String, Object>) item;
                    return new ActionResult(typed);
                })
                .toList();
    }

    @Override
    public String toString() {
        return "ActionResult" + raw;
    }
}
