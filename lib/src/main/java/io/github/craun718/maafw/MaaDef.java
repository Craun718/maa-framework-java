package io.github.craun718.maafw;

import java.util.Locale;

/** Native type aliases and enum values from MaaFramework/MaaDef.h. */
public final class MaaDef {

    public static final long NULL_SIZE = -1L;
    public static final long INVALID_ID = 0L;

    public static final long ADB_SCREENCAP_ENCODE_TO_FILE_AND_PULL = 1L;
    public static final long ADB_SCREENCAP_ENCODE = 1L << 1;
    public static final long ADB_SCREENCAP_RAW_WITH_GZIP = 1L << 2;
    public static final long ADB_SCREENCAP_RAW_BY_NETCAT = 1L << 3;
    public static final long ADB_SCREENCAP_MINICAP_DIRECT = 1L << 4;
    public static final long ADB_SCREENCAP_MINICAP_STREAM = 1L << 5;
    public static final long ADB_SCREENCAP_EMULATOR_EXTRAS = 1L << 6;
    public static final long ADB_SCREENCAP_NONE = 0L;
    public static final long ADB_SCREENCAP_ALL = -1L;
    public static final long ADB_SCREENCAP_DEFAULT =
            ADB_SCREENCAP_ALL & ~ADB_SCREENCAP_RAW_BY_NETCAT & ~ADB_SCREENCAP_MINICAP_DIRECT
                    & ~ADB_SCREENCAP_MINICAP_STREAM;

    public static final long ADB_INPUT_ADB_SHELL = 1L;
    public static final long ADB_INPUT_MINITOUCH_AND_ADB_KEY = 1L << 1;
    public static final long ADB_INPUT_MAATOUCH = 1L << 2;
    public static final long ADB_INPUT_EMULATOR_EXTRAS = 1L << 3;
    public static final long ADB_INPUT_NONE = 0L;
    public static final long ADB_INPUT_ALL = -1L;
    public static final long ADB_INPUT_DEFAULT = ADB_INPUT_ALL & ~ADB_INPUT_EMULATOR_EXTRAS;

    public static final long WIN32_SCREENCAP_NONE = 0L;
    public static final long WIN32_SCREENCAP_GDI = 1L;
    public static final long WIN32_SCREENCAP_FRAME_POOL = 1L << 1;
    public static final long WIN32_SCREENCAP_DXGI_DESKTOP_DUP = 1L << 2;
    public static final long WIN32_SCREENCAP_DXGI_DESKTOP_DUP_WINDOW = 1L << 3;
    public static final long WIN32_SCREENCAP_PRINT_WINDOW = 1L << 4;
    public static final long WIN32_SCREENCAP_SCREEN_DC = 1L << 5;
    public static final long WIN32_SCREENCAP_ALL = -1L;
    public static final long WIN32_SCREENCAP_FOREGROUND =
            WIN32_SCREENCAP_DXGI_DESKTOP_DUP_WINDOW | WIN32_SCREENCAP_SCREEN_DC;
    public static final long WIN32_SCREENCAP_BACKGROUND =
            WIN32_SCREENCAP_FRAME_POOL | WIN32_SCREENCAP_PRINT_WINDOW;

    public static final long WIN32_INPUT_NONE = 0L;
    public static final long WIN32_INPUT_SEIZE = 1L;
    public static final long WIN32_INPUT_SEND_MESSAGE = 1L << 1;
    public static final long WIN32_INPUT_POST_MESSAGE = 1L << 2;
    public static final long WIN32_INPUT_LEGACY_EVENT = 1L << 3;
    public static final long WIN32_INPUT_POST_THREAD_MESSAGE = 1L << 4;
    public static final long WIN32_INPUT_SEND_MESSAGE_WITH_CURSOR_POS = 1L << 5;
    public static final long WIN32_INPUT_POST_MESSAGE_WITH_CURSOR_POS = 1L << 6;
    public static final long WIN32_INPUT_SEND_MESSAGE_WITH_WINDOW_POS = 1L << 7;
    public static final long WIN32_INPUT_POST_MESSAGE_WITH_WINDOW_POS = 1L << 8;
    public static final long WIN32_INPUT_INTERCEPTION = 1L << 9;

    public static final long MACOS_SCREENCAP_NONE = 0L;
    public static final long MACOS_SCREENCAP_SCREEN_CAPTURE_KIT = 1L;
    public static final long MACOS_INPUT_NONE = 0L;
    public static final long MACOS_INPUT_GLOBAL_EVENT = 1L;
    public static final long MACOS_INPUT_POST_TO_PID = 1L << 1;

    public static final long LINUX_SCREENCAP_NONE = 0L;
    public static final long LINUX_SCREENCAP_WLR = 1L;
    public static final long LINUX_SCREENCAP_EXT_IMAGE = 1L << 1;
    public static final long LINUX_SCREENCAP_PIPE_WIRE = 1L << 2;
    public static final long LINUX_INPUT_NONE = 0L;
    public static final long LINUX_INPUT_WLR = 1L;
    public static final long LINUX_INPUT_UINPUT = 1L << 1;

    public static final long GAMEPAD_XBOX360 = 0L;
    public static final long GAMEPAD_DUAL_SHOCK_4 = 1L;

    public static final long GAMEPAD_BUTTON_A = 0x1000L;
    public static final long GAMEPAD_BUTTON_B = 0x2000L;
    public static final long GAMEPAD_BUTTON_X = 0x4000L;
    public static final long GAMEPAD_BUTTON_Y = 0x8000L;
    public static final long GAMEPAD_BUTTON_LB = 0x0100L;
    public static final long GAMEPAD_BUTTON_RB = 0x0200L;
    public static final long GAMEPAD_BUTTON_LEFT_THUMB = 0x0040L;
    public static final long GAMEPAD_BUTTON_RIGHT_THUMB = 0x0080L;
    public static final long GAMEPAD_BUTTON_START = 0x0010L;
    public static final long GAMEPAD_BUTTON_BACK = 0x0020L;
    public static final long GAMEPAD_BUTTON_GUIDE = 0x0400L;
    public static final long GAMEPAD_BUTTON_DPAD_UP = 0x0001L;
    public static final long GAMEPAD_BUTTON_DPAD_DOWN = 0x0002L;
    public static final long GAMEPAD_BUTTON_DPAD_LEFT = 0x0004L;
    public static final long GAMEPAD_BUTTON_DPAD_RIGHT = 0x0008L;
    public static final long GAMEPAD_BUTTON_CROSS = GAMEPAD_BUTTON_A;
    public static final long GAMEPAD_BUTTON_CIRCLE = GAMEPAD_BUTTON_B;
    public static final long GAMEPAD_BUTTON_SQUARE = GAMEPAD_BUTTON_X;
    public static final long GAMEPAD_BUTTON_TRIANGLE = GAMEPAD_BUTTON_Y;
    public static final long GAMEPAD_BUTTON_L1 = GAMEPAD_BUTTON_LB;
    public static final long GAMEPAD_BUTTON_R1 = GAMEPAD_BUTTON_RB;
    public static final long GAMEPAD_BUTTON_L3 = GAMEPAD_BUTTON_LEFT_THUMB;
    public static final long GAMEPAD_BUTTON_R3 = GAMEPAD_BUTTON_RIGHT_THUMB;
    public static final long GAMEPAD_BUTTON_OPTIONS = GAMEPAD_BUTTON_START;
    public static final long GAMEPAD_BUTTON_SHARE = GAMEPAD_BUTTON_BACK;
    public static final long GAMEPAD_BUTTON_PS = 0x10000L;
    public static final long GAMEPAD_BUTTON_TOUCHPAD = 0x20000L;

    public static final int GAMEPAD_TOUCH_LEFT_STICK = 0;
    public static final int GAMEPAD_TOUCH_RIGHT_STICK = 1;
    public static final int GAMEPAD_TOUCH_LEFT_TRIGGER = 2;
    public static final int GAMEPAD_TOUCH_RIGHT_TRIGGER = 3;

    public static final long CONTROLLER_FEATURE_NONE = 0L;
    public static final long CONTROLLER_FEATURE_USE_MOUSE_DOWN_UP_INSTEAD_OF_CLICK = 1L;
    public static final long CONTROLLER_FEATURE_USE_KEYBOARD_DOWN_UP_INSTEAD_OF_CLICK = 1L << 1;
    public static final long CONTROLLER_FEATURE_NO_SCALING_TOUCH_POINTS = 1L << 2;

    public static final int INFERENCE_DEVICE_CPU = -2;
    public static final int INFERENCE_DEVICE_AUTO = -1;
    public static final int INFERENCE_EXECUTION_PROVIDER_AUTO = 0;
    public static final int INFERENCE_EXECUTION_PROVIDER_CPU = 1;
    public static final int INFERENCE_EXECUTION_PROVIDER_DIRECT_ML = 2;
    public static final int INFERENCE_EXECUTION_PROVIDER_CORE_ML = 3;
    public static final int INFERENCE_EXECUTION_PROVIDER_CUDA = 4;

    private MaaDef() {}

    public enum Status {
        INVALID(0),
        PENDING(1000),
        RUNNING(2000),
        SUCCEEDED(3000),
        FAILED(4000);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }

        public boolean done() {
            return this == SUCCEEDED || this == FAILED;
        }

        public static Status of(int code) {
            for (Status status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            return INVALID;
        }
    }

    public enum LoggingLevel {
        OFF(0),
        FATAL(1),
        ERROR(2),
        WARN(3),
        INFO(4),
        DEBUG(5),
        TRACE(6),
        ALL(7);

        private final int code;

        LoggingLevel(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }

    public enum GlobalOption {
        INVALID(0),
        LOG_DIR(1),
        SAVE_DRAW(2),
        STDOUT_LEVEL(4),
        DEBUG_MODE(6),
        SAVE_ON_ERROR(7),
        DRAW_QUALITY(8),
        RECO_IMAGE_CACHE_LIMIT(9);

        private final int code;

        GlobalOption(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }

    public enum CtrlOption {
        INVALID(0),
        SCREENSHOT_TARGET_LONG_SIDE(1),
        SCREENSHOT_TARGET_SHORT_SIDE(2),
        SCREENSHOT_USE_RAW_SIZE(3),
        MOUSE_LOCK_FOLLOW(4),
        SCREENSHOT_RESIZE_METHOD(6),
        BACKGROUND_MANAGED_KEYS(7);

        private final int code;

        CtrlOption(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }

    public enum ResOption {
        INVALID(0),
        INFERENCE_DEVICE(1),
        INFERENCE_EXECUTION_PROVIDER(2);

        private final int code;

        ResOption(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }

    public enum MacOSPermission {
        SCREEN_CAPTURE(1),
        ACCESSIBILITY(2);

        private final int code;

        MacOSPermission(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }

    public enum Algorithm {
        DIRECT_HIT("DirectHit"),
        TEMPLATE_MATCH("TemplateMatch"),
        FEATURE_MATCH("FeatureMatch"),
        COLOR_MATCH("ColorMatch"),
        OCR("OCR"),
        NEURAL_NETWORK_CLASSIFY("NeuralNetworkClassify"),
        NEURAL_NETWORK_DETECT("NeuralNetworkDetect"),
        AND("And"),
        OR("Or"),
        CUSTOM("Custom");

        private final String nativeName;

        Algorithm(String nativeName) {
            this.nativeName = nativeName;
        }

        public String nativeName() {
            return nativeName;
        }

        public static Algorithm of(String name) {
            for (Algorithm algorithm : values()) {
                if (algorithm.nativeName.equals(name)) {
                    return algorithm;
                }
            }
            return null;
        }
    }

    public enum Action {
        DO_NOTHING("DoNothing"),
        CLICK("Click"),
        LONG_PRESS("LongPress"),
        SWIPE("Swipe"),
        MULTI_SWIPE("MultiSwipe"),
        CLICK_KEY("ClickKey"),
        LONG_PRESS_KEY("LongPressKey"),
        INPUT_TEXT("InputText"),
        START_APP("StartApp"),
        STOP_APP("StopApp"),
        SCROLL("Scroll"),
        TOUCH_DOWN("TouchDown"),
        TOUCH_MOVE("TouchMove"),
        TOUCH_UP("TouchUp"),
        KEY_DOWN("KeyDown"),
        KEY_UP("KeyUp"),
        STOP_TASK("StopTask"),
        COMMAND("Command"),
        SHELL("Shell"),
        CUSTOM("Custom");

        private final String nativeName;

        Action(String nativeName) {
            this.nativeName = nativeName;
        }

        public String nativeName() {
            return nativeName;
        }

        public static Action of(String name) {
            for (Action action : values()) {
                if (action.nativeName.equals(name)) {
                    return action;
                }
            }
            return null;
        }
    }

    public enum NotificationType {
        UNKNOWN,
        STARTING,
        SUCCEEDED,
        FAILED;

        public static NotificationType of(String message) {
            if (message == null) {
                return UNKNOWN;
            }
            String lower = message.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".starting")) {
                return STARTING;
            }
            if (lower.endsWith(".succeeded")) {
                return SUCCEEDED;
            }
            if (lower.endsWith(".failed")) {
                return FAILED;
            }
            return UNKNOWN;
        }
    }
}
