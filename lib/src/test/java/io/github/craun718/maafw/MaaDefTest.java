package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MaaDefTest {

    @Test
    void statusMapsCodesAndState() {
        assertEquals(MaaDef.Status.INVALID, MaaDef.Status.of(0));
        assertEquals(MaaDef.Status.PENDING, MaaDef.Status.of(1000));
        assertEquals(MaaDef.Status.RUNNING, MaaDef.Status.of(2000));
        assertEquals(MaaDef.Status.SUCCEEDED, MaaDef.Status.of(3000));
        assertEquals(MaaDef.Status.FAILED, MaaDef.Status.of(4000));

        assertTrue(MaaDef.Status.SUCCEEDED.succeeded());
        assertTrue(MaaDef.Status.FAILED.failed());
        assertTrue(MaaDef.Status.FAILED.done());
        assertFalse(MaaDef.Status.RUNNING.done());
    }

    @Test
    void notificationTypeParsesSuffixes() {
        assertEquals(MaaDef.NotificationType.STARTING, MaaDef.NotificationType.of("Resource.Loading.Starting"));
        assertEquals(MaaDef.NotificationType.SUCCEEDED, MaaDef.NotificationType.of("Tasker.Task.Succeeded"));
        assertEquals(MaaDef.NotificationType.FAILED, MaaDef.NotificationType.of("Node.Action.Failed"));
        assertEquals(MaaDef.NotificationType.UNKNOWN, MaaDef.NotificationType.of("Node.Action"));
        assertEquals(MaaDef.NotificationType.UNKNOWN, MaaDef.NotificationType.of(null));
    }

    @Test
    void algorithmAndActionLookupUsesNativeNames() {
        assertEquals(MaaDef.Algorithm.TEMPLATE_MATCH, MaaDef.Algorithm.of("TemplateMatch"));
        assertEquals(MaaDef.Algorithm.CUSTOM, MaaDef.Algorithm.of("Custom"));
        assertNull(MaaDef.Algorithm.of("Missing"));

        assertEquals(MaaDef.Action.CLICK, MaaDef.Action.of("Click"));
        assertEquals(MaaDef.Action.SCREENCAP, MaaDef.Action.of("Screencap"));
        assertEquals(MaaDef.Action.CUSTOM, MaaDef.Action.of("Custom"));
        assertNull(MaaDef.Action.of("Missing"));
    }

    @Test
    void defaultAdbFlagsExcludeUnsafeStreamingMethods() {
        assertEquals(0L, MaaDef.INVALID_ID);
        assertTrue((MaaDef.ADB_SCREENCAP_DEFAULT & MaaDef.ADB_SCREENCAP_ENCODE) != 0);
        assertTrue((MaaDef.ADB_SCREENCAP_DEFAULT & MaaDef.ADB_SCREENCAP_RAW_BY_NETCAT) == 0);
        assertTrue((MaaDef.ADB_SCREENCAP_DEFAULT & MaaDef.ADB_SCREENCAP_MINICAP_STREAM) == 0);
    }

    @Test
    void adbMethodFlagsMatchOfficialHeader() {
        assertEquals(1L, MaaDef.ADB_SCREENCAP_ENCODE_TO_FILE_AND_PULL);
        assertEquals(1L << 1, MaaDef.ADB_SCREENCAP_ENCODE);
        assertEquals(1L << 2, MaaDef.ADB_SCREENCAP_RAW_WITH_GZIP);
        assertEquals(1L << 3, MaaDef.ADB_SCREENCAP_RAW_BY_NETCAT);
        assertEquals(1L << 4, MaaDef.ADB_SCREENCAP_MINICAP_DIRECT);
        assertEquals(1L << 5, MaaDef.ADB_SCREENCAP_MINICAP_STREAM);
        assertEquals(1L << 6, MaaDef.ADB_SCREENCAP_EMULATOR_EXTRAS);
        assertEquals(0L, MaaDef.ADB_SCREENCAP_NONE);
        assertEquals(-1L, MaaDef.ADB_SCREENCAP_ALL);
        assertEquals(MaaDef.ADB_SCREENCAP_ALL & ~MaaDef.ADB_SCREENCAP_RAW_BY_NETCAT & ~MaaDef.ADB_SCREENCAP_MINICAP_DIRECT
            & ~MaaDef.ADB_SCREENCAP_MINICAP_STREAM, MaaDef.ADB_SCREENCAP_DEFAULT);

        assertEquals(1L, MaaDef.ADB_INPUT_ADB_SHELL);
        assertEquals(1L << 1, MaaDef.ADB_INPUT_MINITOUCH_AND_ADB_KEY);
        assertEquals(1L << 2, MaaDef.ADB_INPUT_MAATOUCH);
        assertEquals(1L << 3, MaaDef.ADB_INPUT_EMULATOR_EXTRAS);
        assertEquals(0L, MaaDef.ADB_INPUT_NONE);
        assertEquals(-1L, MaaDef.ADB_INPUT_ALL);
        assertEquals(MaaDef.ADB_INPUT_ALL & ~MaaDef.ADB_INPUT_EMULATOR_EXTRAS, MaaDef.ADB_INPUT_DEFAULT);
    }

    @Test
    void win32MethodFlagsMatchOfficialHeader() {
        assertEquals(0L, MaaDef.WIN32_SCREENCAP_NONE);
        assertEquals(1L, MaaDef.WIN32_SCREENCAP_GDI);
        assertEquals(1L << 1, MaaDef.WIN32_SCREENCAP_FRAME_POOL);
        assertEquals(1L << 2, MaaDef.WIN32_SCREENCAP_DXGI_DESKTOP_DUP);
        assertEquals(1L << 3, MaaDef.WIN32_SCREENCAP_DXGI_DESKTOP_DUP_WINDOW);
        assertEquals(1L << 4, MaaDef.WIN32_SCREENCAP_PRINT_WINDOW);
        assertEquals(1L << 5, MaaDef.WIN32_SCREENCAP_SCREEN_DC);
        assertEquals(-1L, MaaDef.WIN32_SCREENCAP_ALL);
        assertEquals(MaaDef.WIN32_SCREENCAP_DXGI_DESKTOP_DUP_WINDOW | MaaDef.WIN32_SCREENCAP_SCREEN_DC, MaaDef.WIN32_SCREENCAP_FOREGROUND);
        assertEquals(MaaDef.WIN32_SCREENCAP_FRAME_POOL | MaaDef.WIN32_SCREENCAP_PRINT_WINDOW, MaaDef.WIN32_SCREENCAP_BACKGROUND);

        assertEquals(0L, MaaDef.WIN32_INPUT_NONE);
        assertEquals(1L, MaaDef.WIN32_INPUT_SEIZE);
        assertEquals(1L << 1, MaaDef.WIN32_INPUT_SEND_MESSAGE);
        assertEquals(1L << 2, MaaDef.WIN32_INPUT_POST_MESSAGE);
        assertEquals(1L << 3, MaaDef.WIN32_INPUT_LEGACY_EVENT);
        assertEquals(1L << 4, MaaDef.WIN32_INPUT_POST_THREAD_MESSAGE);
        assertEquals(1L << 5, MaaDef.WIN32_INPUT_SEND_MESSAGE_WITH_CURSOR_POS);
        assertEquals(1L << 6, MaaDef.WIN32_INPUT_POST_MESSAGE_WITH_CURSOR_POS);
        assertEquals(1L << 7, MaaDef.WIN32_INPUT_SEND_MESSAGE_WITH_WINDOW_POS);
        assertEquals(1L << 8, MaaDef.WIN32_INPUT_POST_MESSAGE_WITH_WINDOW_POS);
        assertEquals(1L << 9, MaaDef.WIN32_INPUT_INTERCEPTION);
    }

    @Test
    void macLinuxAndGamepadFlagsMatchOfficialHeader() {
        assertEquals(0L, MaaDef.MACOS_SCREENCAP_NONE);
        assertEquals(1L, MaaDef.MACOS_SCREENCAP_SCREEN_CAPTURE_KIT);
        assertEquals(0L, MaaDef.MACOS_INPUT_NONE);
        assertEquals(1L, MaaDef.MACOS_INPUT_GLOBAL_EVENT);
        assertEquals(1L << 1, MaaDef.MACOS_INPUT_POST_TO_PID);

        assertEquals(0L, MaaDef.LINUX_SCREENCAP_NONE);
        assertEquals(1L, MaaDef.LINUX_SCREENCAP_WLR);
        assertEquals(1L << 1, MaaDef.LINUX_SCREENCAP_EXT_IMAGE);
        assertEquals(1L << 2, MaaDef.LINUX_SCREENCAP_PIPE_WIRE);
        assertEquals(0L, MaaDef.LINUX_INPUT_NONE);
        assertEquals(1L, MaaDef.LINUX_INPUT_WLR);
        assertEquals(1L << 1, MaaDef.LINUX_INPUT_UINPUT);

        assertEquals(0L, MaaDef.GAMEPAD_XBOX360);
        assertEquals(1L, MaaDef.GAMEPAD_DUAL_SHOCK_4);
        assertEquals(0x1000L, MaaDef.GAMEPAD_BUTTON_A);
        assertEquals(0x2000L, MaaDef.GAMEPAD_BUTTON_B);
        assertEquals(0x4000L, MaaDef.GAMEPAD_BUTTON_X);
        assertEquals(0x8000L, MaaDef.GAMEPAD_BUTTON_Y);
        assertEquals(0x0100L, MaaDef.GAMEPAD_BUTTON_LB);
        assertEquals(0x0200L, MaaDef.GAMEPAD_BUTTON_RB);
        assertEquals(0x0040L, MaaDef.GAMEPAD_BUTTON_LEFT_THUMB);
        assertEquals(0x0080L, MaaDef.GAMEPAD_BUTTON_RIGHT_THUMB);
        assertEquals(0x0010L, MaaDef.GAMEPAD_BUTTON_START);
        assertEquals(0x0020L, MaaDef.GAMEPAD_BUTTON_BACK);
        assertEquals(0x0400L, MaaDef.GAMEPAD_BUTTON_GUIDE);
        assertEquals(0x0001L, MaaDef.GAMEPAD_BUTTON_DPAD_UP);
        assertEquals(0x0002L, MaaDef.GAMEPAD_BUTTON_DPAD_DOWN);
        assertEquals(0x0004L, MaaDef.GAMEPAD_BUTTON_DPAD_LEFT);
        assertEquals(0x0008L, MaaDef.GAMEPAD_BUTTON_DPAD_RIGHT);
        assertEquals(0x10000L, MaaDef.GAMEPAD_BUTTON_PS);
        assertEquals(0x20000L, MaaDef.GAMEPAD_BUTTON_TOUCHPAD);

        assertEquals(MaaDef.GAMEPAD_BUTTON_A, MaaDef.GAMEPAD_BUTTON_CROSS);
        assertEquals(MaaDef.GAMEPAD_BUTTON_B, MaaDef.GAMEPAD_BUTTON_CIRCLE);
        assertEquals(MaaDef.GAMEPAD_BUTTON_X, MaaDef.GAMEPAD_BUTTON_SQUARE);
        assertEquals(MaaDef.GAMEPAD_BUTTON_Y, MaaDef.GAMEPAD_BUTTON_TRIANGLE);
        assertEquals(MaaDef.GAMEPAD_BUTTON_LB, MaaDef.GAMEPAD_BUTTON_L1);
        assertEquals(MaaDef.GAMEPAD_BUTTON_RB, MaaDef.GAMEPAD_BUTTON_R1);
        assertEquals(MaaDef.GAMEPAD_BUTTON_LEFT_THUMB, MaaDef.GAMEPAD_BUTTON_L3);
        assertEquals(MaaDef.GAMEPAD_BUTTON_RIGHT_THUMB, MaaDef.GAMEPAD_BUTTON_R3);
        assertEquals(MaaDef.GAMEPAD_BUTTON_START, MaaDef.GAMEPAD_BUTTON_OPTIONS);
        assertEquals(MaaDef.GAMEPAD_BUTTON_BACK, MaaDef.GAMEPAD_BUTTON_SHARE);

        assertEquals(0, MaaDef.GAMEPAD_TOUCH_LEFT_STICK);
        assertEquals(1, MaaDef.GAMEPAD_TOUCH_RIGHT_STICK);
        assertEquals(2, MaaDef.GAMEPAD_TOUCH_LEFT_TRIGGER);
        assertEquals(3, MaaDef.GAMEPAD_TOUCH_RIGHT_TRIGGER);
    }

    @Test
    void inferenceAndOptionCodesMatchOfficialHeader() {
        assertEquals(-2, MaaDef.INFERENCE_DEVICE_CPU);
        assertEquals(-1, MaaDef.INFERENCE_DEVICE_AUTO);
        assertEquals(0, MaaDef.INFERENCE_EXECUTION_PROVIDER_AUTO);
        assertEquals(1, MaaDef.INFERENCE_EXECUTION_PROVIDER_CPU);
        assertEquals(2, MaaDef.INFERENCE_EXECUTION_PROVIDER_DIRECT_ML);
        assertEquals(3, MaaDef.INFERENCE_EXECUTION_PROVIDER_CORE_ML);
        assertEquals(4, MaaDef.INFERENCE_EXECUTION_PROVIDER_CUDA);

        assertEquals(0, MaaDef.LoggingLevel.OFF.code());
        assertEquals(1, MaaDef.LoggingLevel.FATAL.code());
        assertEquals(2, MaaDef.LoggingLevel.ERROR.code());
        assertEquals(3, MaaDef.LoggingLevel.WARN.code());
        assertEquals(4, MaaDef.LoggingLevel.INFO.code());
        assertEquals(5, MaaDef.LoggingLevel.DEBUG.code());
        assertEquals(6, MaaDef.LoggingLevel.TRACE.code());
        assertEquals(7, MaaDef.LoggingLevel.ALL.code());

        assertEquals(0, MaaDef.GlobalOption.INVALID.code());
        assertEquals(1, MaaDef.GlobalOption.LOG_DIR.code());
        assertEquals(2, MaaDef.GlobalOption.SAVE_DRAW.code());
        assertEquals(4, MaaDef.GlobalOption.STDOUT_LEVEL.code());
        assertEquals(6, MaaDef.GlobalOption.DEBUG_MODE.code());
        assertEquals(7, MaaDef.GlobalOption.SAVE_ON_ERROR.code());
        assertEquals(8, MaaDef.GlobalOption.DRAW_QUALITY.code());
        assertEquals(9, MaaDef.GlobalOption.RECO_IMAGE_CACHE_LIMIT.code());

        assertEquals(0, MaaDef.CtrlOption.INVALID.code());
        assertEquals(1, MaaDef.CtrlOption.SCREENSHOT_TARGET_LONG_SIDE.code());
        assertEquals(2, MaaDef.CtrlOption.SCREENSHOT_TARGET_SHORT_SIDE.code());
        assertEquals(3, MaaDef.CtrlOption.SCREENSHOT_USE_RAW_SIZE.code());
        assertEquals(4, MaaDef.CtrlOption.MOUSE_LOCK_FOLLOW.code());
        assertEquals(6, MaaDef.CtrlOption.SCREENSHOT_RESIZE_METHOD.code());
        assertEquals(7, MaaDef.CtrlOption.BACKGROUND_MANAGED_KEYS.code());

        assertEquals(0, MaaDef.ResOption.INVALID.code());
        assertEquals(1, MaaDef.ResOption.INFERENCE_DEVICE.code());
        assertEquals(2, MaaDef.ResOption.INFERENCE_EXECUTION_PROVIDER.code());

        assertEquals(1, MaaDef.MacOSPermission.SCREEN_CAPTURE.code());
        assertEquals(2, MaaDef.MacOSPermission.ACCESSIBILITY.code());
    }

    @Test
    void controllerFeatureBitsMatchOfficialHeader() {
        assertEquals(0L, MaaDef.CONTROLLER_FEATURE_NONE);
        assertEquals(1L, MaaDef.CONTROLLER_FEATURE_USE_MOUSE_DOWN_UP_INSTEAD_OF_CLICK);
        assertEquals(1L << 1, MaaDef.CONTROLLER_FEATURE_USE_KEYBOARD_DOWN_UP_INSTEAD_OF_CLICK);
        assertEquals(1L << 2, MaaDef.CONTROLLER_FEATURE_NO_SCALING_TOUCH_POINTS);
    }

    @Test
    void recoveryImageCacheLimitRejectsNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> Tasker.setRecoImageCacheLimit(-1));
    }
}
