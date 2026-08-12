package io.github.craun718.maafw;

import io.github.craun718.maafw.pipeline.JActionType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Parsed action result returned inside {@link ActionDetail}. */
public final class ActionResult {

    private final JActionType type;
    private final Map<String, Object> raw;

    public ActionResult(Map<String, Object> raw) {
        this(null, raw);
    }

    public ActionResult(JActionType type, Map<String, Object> raw) {
        this.type = type;
        this.raw = MaaResultParsers.unmodifiableMap(raw);
    }

    public JActionType type() {
        return type;
    }

    public Map<String, Object> raw() {
        return raw;
    }

    public Optional<ClickActionResult> asClick() {
        return typed(JActionType.CLICK, () -> new ClickActionResult(point(), contact(), pressure()));
    }

    public Optional<LongPressActionResult> asLongPress() {
        return typed(
                JActionType.LONG_PRESS,
                () -> new LongPressActionResult(point(), duration(), contact(), pressure()));
    }

    public Optional<SwipeActionResult> asSwipe() {
        return typed(
                JActionType.SWIPE,
                () -> new SwipeActionResult(
                        begin(), end(), endHold(), durations(), onlyHover(), starting(), contact(), pressure()));
    }

    public Optional<MultiSwipeActionResult> asMultiSwipe() {
        return typed(JActionType.MULTI_SWIPE, () -> new MultiSwipeActionResult(swipeResults()));
    }

    public Optional<ClickKeyActionResult> asClickKey() {
        if (type != JActionType.CLICK_KEY
                && type != JActionType.KEY_DOWN
                && type != JActionType.KEY_UP) {
            return Optional.empty();
        }
        return Optional.of(new ClickKeyActionResult(keycodes()));
    }

    public Optional<LongPressKeyActionResult> asLongPressKey() {
        return typed(
                JActionType.LONG_PRESS_KEY,
                () -> new LongPressKeyActionResult(keycodes(), duration()));
    }

    public Optional<InputTextActionResult> asInputText() {
        return typed(JActionType.INPUT_TEXT, () -> new InputTextActionResult(text()));
    }

    public Optional<AppActionResult> asApp() {
        if (type != JActionType.START_APP && type != JActionType.STOP_APP) {
            return Optional.empty();
        }
        return Optional.of(new AppActionResult(packageName()));
    }

    public Optional<ScrollActionResult> asScroll() {
        return typed(JActionType.SCROLL, () -> new ScrollActionResult(point(), dx(), dy()));
    }

    public Optional<TouchActionResult> asTouch() {
        if (type != JActionType.TOUCH_DOWN
                && type != JActionType.TOUCH_MOVE
                && type != JActionType.TOUCH_UP) {
            return Optional.empty();
        }
        return Optional.of(new TouchActionResult(contact(), point(), pressure()));
    }

    public Optional<ShellActionResult> asShell() {
        return typed(
                JActionType.SHELL,
                () -> new ShellActionResult(cmd(), shellTimeout(), success(), output()));
    }

    public Optional<ScreencapActionResult> asScreencap() {
        return typed(
                JActionType.SCREENCAP,
                () -> new ScreencapActionResult(filepath(), format(), quality(), success()));
    }

    /** Returns the typed value for the reported action type, or {@code null} for unknown types. */
    public Object value() {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case CLICK -> asClick().orElse(null);
            case LONG_PRESS -> asLongPress().orElse(null);
            case SWIPE -> asSwipe().orElse(null);
            case MULTI_SWIPE -> asMultiSwipe().orElse(null);
            case CLICK_KEY, KEY_DOWN, KEY_UP -> asClickKey().orElse(null);
            case LONG_PRESS_KEY -> asLongPressKey().orElse(null);
            case INPUT_TEXT -> asInputText().orElse(null);
            case START_APP, STOP_APP -> asApp().orElse(null);
            case SCROLL -> asScroll().orElse(null);
            case TOUCH_DOWN, TOUCH_MOVE, TOUCH_UP -> asTouch().orElse(null);
            case SHELL -> asShell().orElse(null);
            case SCREENCAP -> asScreencap().orElse(null);
            default -> null;
        };
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

    /** Returns the swipe start offset for {@code Swipe} / {@code MultiSwipe} results. */
    public Integer starting() {
        return MaaResultParsers.integer(raw.get("starting"));
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

    public String filepath() {
        return MaaResultParsers.string(raw.get("filepath"));
    }

    public String format() {
        return MaaResultParsers.string(raw.get("format"));
    }

    public Integer quality() {
        return MaaResultParsers.integer(raw.get("quality"));
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
                    return new ActionResult(JActionType.SWIPE, typed);
                })
                .toList();
    }

    public List<SwipeActionResult> swipeResults() {
        return swipes().stream()
                .map(ActionResult::asSwipe)
                .filter(Optional::isPresent)
                .map(Optional::orElseThrow)
                .toList();
    }

    private <T> Optional<T> typed(JActionType expected, java.util.function.Supplier<T> supplier) {
        if (type != expected) {
            return Optional.empty();
        }
        return Optional.ofNullable(supplier.get());
    }

    @Override
    public String toString() {
        return "ActionResult" + raw;
    }

    public record ClickActionResult(MaaPoint point, Integer contact, Integer pressure) {}

    public record LongPressActionResult(MaaPoint point, Integer duration, Integer contact, Integer pressure) {}

    public record SwipeActionResult(
            MaaPoint begin,
            List<MaaPoint> end,
            List<Integer> endHold,
            List<Integer> durations,
            Boolean onlyHover,
            Integer starting,
            Integer contact,
            Integer pressure) {}

    public record MultiSwipeActionResult(List<SwipeActionResult> swipes) {}

    public record ClickKeyActionResult(List<Integer> keycodes) {}

    public record LongPressKeyActionResult(List<Integer> keycodes, Integer duration) {}

    public record InputTextActionResult(String text) {}

    public record AppActionResult(String packageName) {}

    public record ScrollActionResult(MaaPoint point, Integer dx, Integer dy) {}

    public record TouchActionResult(Integer contact, MaaPoint point, Integer pressure) {}

    public record ShellActionResult(String cmd, Long shellTimeout, Boolean success, String output) {}

    public record ScreencapActionResult(String filepath, String format, Integer quality, Boolean success) {}
}
