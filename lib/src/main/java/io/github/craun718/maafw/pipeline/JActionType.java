package io.github.craun718.maafw.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Action type names used by pipeline v2 {@code action.type}. */
public enum JActionType {

    DO_NOTHING("DoNothing"), CLICK("Click"), LONG_PRESS("LongPress"), SWIPE("Swipe"), MULTI_SWIPE("MultiSwipe"), TOUCH_DOWN(
            "TouchDown"), TOUCH_MOVE("TouchMove"), TOUCH_UP("TouchUp"), CLICK_KEY("ClickKey"), LONG_PRESS_KEY("LongPressKey"), KEY_DOWN(
                    "KeyDown"), KEY_UP("KeyUp"), INPUT_TEXT("InputText"), START_APP("StartApp"), STOP_APP("StopApp"), STOP_TASK(
                            "StopTask"), SCROLL("Scroll"), COMMAND("Command"), SHELL("Shell"), SCREENCAP("Screencap"), CUSTOM("Custom");

    private final String nativeName;

    JActionType(String nativeName) {
        this.nativeName = nativeName;
    }

    @JsonValue
    public String nativeName() {
        return nativeName;
    }

    @JsonCreator
    public static JActionType of(String name) {
        if (name == null) {
            return null;
        }
        for (JActionType type : values()) {
            if (type.nativeName.equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown action type: " + name);
    }
}
