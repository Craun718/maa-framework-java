package io.github.craun718.maafw;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/** Native callback prototypes from MaaFramework headers. */
public final class MaaCallbacks {

    private MaaCallbacks() {
    }

    @FunctionalInterface
    public interface EventCallback extends Callback {

        void invoke(Pointer handle, String message, String detailsJson, Pointer transArg);
    }

    @FunctionalInterface
    public interface CustomRecognitionCallback extends Callback {

        byte invoke(Pointer context, long taskId, String nodeName, String customRecognitionName, String customRecognitionParam,
                Pointer image, Pointer roi, Pointer transArg, Pointer outBox, Pointer outDetail);
    }

    @FunctionalInterface
    public interface CustomActionCallback extends Callback {

        byte invoke(Pointer context, long taskId, String nodeName, String customActionName, String customActionParam, long recoId,
                Pointer box, Pointer transArg);
    }

    @FunctionalInterface
    public interface BoolCallback extends Callback {

        byte invoke(Pointer transArg);
    }

    @FunctionalInterface
    public interface RequestUuidCallback extends Callback {

        byte invoke(Pointer transArg, Pointer outBuffer);
    }

    @FunctionalInterface
    public interface GetFeaturesCallback extends Callback {

        long invoke(Pointer transArg);
    }

    @FunctionalInterface
    public interface AppCallback extends Callback {

        byte invoke(String value, Pointer transArg);
    }

    @FunctionalInterface
    public interface ScreencapCallback extends Callback {

        byte invoke(Pointer transArg, Pointer outBuffer);
    }

    @FunctionalInterface
    public interface ClickCallback extends Callback {

        byte invoke(int x, int y, Pointer transArg);
    }

    @FunctionalInterface
    public interface SwipeCallback extends Callback {

        byte invoke(int x1, int y1, int x2, int y2, int duration, Pointer transArg);
    }

    @FunctionalInterface
    public interface TouchCallback extends Callback {

        byte invoke(int contact, int x, int y, int pressure, Pointer transArg);
    }

    @FunctionalInterface
    public interface TouchUpCallback extends Callback {

        byte invoke(int contact, Pointer transArg);
    }

    @FunctionalInterface
    public interface KeyCallback extends Callback {

        byte invoke(int keycode, Pointer transArg);
    }

    @FunctionalInterface
    public interface InputTextCallback extends Callback {

        byte invoke(String text, Pointer transArg);
    }

    @FunctionalInterface
    public interface OffsetCallback extends Callback {

        byte invoke(int dx, int dy, Pointer transArg);
    }

    @FunctionalInterface
    public interface ShellCallback extends Callback {

        byte invoke(String command, long timeout, Pointer transArg, Pointer outBuffer);
    }

    /** MaaCustomControllerCallbacks with exactly the field order from the C header. */
    public static class CustomControllerCallbacks extends Structure {

        public BoolCallback connect;
        public BoolCallback connected;
        public RequestUuidCallback requestUuid;
        public GetFeaturesCallback getFeatures;
        public AppCallback startApp;
        public AppCallback stopApp;
        public ScreencapCallback screencap;
        public ClickCallback click;
        public SwipeCallback swipe;
        public TouchCallback touchDown;
        public TouchCallback touchMove;
        public TouchUpCallback touchUp;
        public KeyCallback clickKey;
        public InputTextCallback inputText;
        public KeyCallback keyDown;
        public KeyCallback keyUp;
        public OffsetCallback scroll;
        public OffsetCallback relativeMove;
        public ShellCallback shell;
        public BoolCallback inactive;
        public RequestUuidCallback getInfo;

        @Override
        protected java.util.List<String> getFieldOrder() {
            return java.util.List.of("connect", "connected", "requestUuid", "getFeatures", "startApp", "stopApp", "screencap", "click",
                    "swipe", "touchDown", "touchMove", "touchUp", "clickKey", "inputText", "keyDown", "keyUp", "scroll", "relativeMove",
                    "shell", "inactive", "getInfo");
        }
    }
}
