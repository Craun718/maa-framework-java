package io.github.craun718.maafw;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.util.Map;

/** Abstract custom controller that forwards Java callbacks to MaaFramework. */
public abstract class CustomController extends Controller {

    private final MaaCallbacks.CustomControllerCallbacks callbacks;
    private final Memory transArg = new Memory(Native.POINTER_SIZE);

    protected CustomController() {
        super();
        callbacks = new MaaCallbacks.CustomControllerCallbacks();
        callbacks.connect = this::connect;
        callbacks.connected = this::connectedByte;
        callbacks.requestUuid = this::requestUuidCallback;
        callbacks.getFeatures = this::getFeaturesCallback;
        callbacks.startApp = this::startAppCallback;
        callbacks.stopApp = this::stopAppCallback;
        callbacks.screencap = this::screencapCallback;
        callbacks.click = this::clickCallback;
        callbacks.swipe = this::swipeCallback;
        callbacks.touchDown = this::touchDownCallback;
        callbacks.touchMove = this::touchMoveCallback;
        callbacks.touchUp = this::touchUpCallback;
        callbacks.clickKey = this::clickKeyCallback;
        callbacks.inputText = this::inputTextCallback;
        callbacks.keyDown = this::keyDownCallback;
        callbacks.keyUp = this::keyUpCallback;
        callbacks.scroll = this::scrollCallback;
        callbacks.relativeMove = this::relativeMoveCallback;
        callbacks.shell = this::shellCallback;
        callbacks.inactive = this::inactiveCallback;
        callbacks.getInfo = this::getInfoCallback;
        callbacks.write();

        setHandle(MaaLibrary.framework().MaaCustomControllerCreate(callbacks, transArg));
    }

    public abstract boolean connect();

    /** Optional connection check; defaults to true. */
    public boolean connected() {
        return true;
    }

    public abstract String requestUuid();

    public long getFeatures() {
        return MaaDef.CONTROLLER_FEATURE_USE_MOUSE_DOWN_UP_INSTEAD_OF_CLICK
            | MaaDef.CONTROLLER_FEATURE_USE_KEYBOARD_DOWN_UP_INSTEAD_OF_CLICK;
    }

    public abstract boolean startApp(String intent);

    public abstract boolean stopApp(String intent);

    public abstract MaaImage screencap();

    public abstract boolean click(int x, int y);

    public abstract boolean swipe(int x1, int y1, int x2, int y2, int duration);

    public abstract boolean touchDown(int contact, int x, int y, int pressure);

    public abstract boolean touchMove(int contact, int x, int y, int pressure);

    public abstract boolean touchUp(int contact);

    public abstract boolean clickKey(int keycode);

    public abstract boolean inputText(String text);

    public abstract boolean keyDown(int keycode);

    public abstract boolean keyUp(int keycode);

    public boolean scroll(int dx, int dy) {
        return false;
    }

    public boolean relativeMove(int dx, int dy) {
        return false;
    }

    public String shell(String command, long timeout) {
        return null;
    }

    public boolean inactive() {
        return true;
    }

    public Map<String, Object> getCustomInfo() {
        return Map.of();
    }

    private byte toByte(boolean value) {
        return (byte) (value ? 1 : 0);
    }

    private byte connect(Pointer ignored) {
        return toByte(connect());
    }

    private long getFeaturesCallback(Pointer ignored) {
        return getFeatures();
    }

    private byte connectedByte(Pointer ignored) {
        return toByte(connected());
    }

    private byte requestUuidCallback(Pointer ignored, Pointer outBuffer) {
        try (MaaStringBuffer buffer = new MaaStringBuffer(outBuffer)) {
            buffer.set(requestUuid());
        }
        return 1;
    }

    private byte startAppCallback(String value, Pointer ignored) {
        return toByte(startApp(value));
    }

    private byte stopAppCallback(String value, Pointer ignored) {
        return toByte(stopApp(value));
    }

    private byte screencapCallback(Pointer ignored, Pointer outBuffer) {
        try (MaaImageBuffer buffer = new MaaImageBuffer(outBuffer)) {
            buffer.set(screencap());
        }
        return 1;
    }

    private byte clickCallback(int x, int y, Pointer ignored) {
        return toByte(click(x, y));
    }

    private byte swipeCallback(int x1, int y1, int x2, int y2, int duration, Pointer ignored) {
        return toByte(swipe(x1, y1, x2, y2, duration));
    }

    private byte touchDownCallback(int contact, int x, int y, int pressure, Pointer ignored) {
        return toByte(touchDown(contact, x, y, pressure));
    }

    private byte touchMoveCallback(int contact, int x, int y, int pressure, Pointer ignored) {
        return toByte(touchMove(contact, x, y, pressure));
    }

    private byte touchUpCallback(int contact, Pointer ignored) {
        return toByte(touchUp(contact));
    }

    private byte clickKeyCallback(int keycode, Pointer ignored) {
        return toByte(clickKey(keycode));
    }

    private byte inputTextCallback(String text, Pointer ignored) {
        return toByte(inputText(text));
    }

    private byte keyDownCallback(int keycode, Pointer ignored) {
        return toByte(keyDown(keycode));
    }

    private byte keyUpCallback(int keycode, Pointer ignored) {
        return toByte(keyUp(keycode));
    }

    private byte scrollCallback(int dx, int dy, Pointer ignored) {
        return toByte(scroll(dx, dy));
    }

    private byte relativeMoveCallback(int dx, int dy, Pointer ignored) {
        return toByte(relativeMove(dx, dy));
    }

    private byte shellCallback(String command, long timeout, Pointer ignored, Pointer outBuffer) {
        String result = shell(command, timeout);
        if (result == null) {
            return 0;
        }
        try (MaaStringBuffer buffer = new MaaStringBuffer(outBuffer)) {
            buffer.set(result);
        }
        return 1;
    }

    private byte inactiveCallback(Pointer ignored) {
        return toByte(inactive());
    }

    private byte getInfoCallback(Pointer ignored, Pointer outBuffer) {
        try (MaaStringBuffer buffer = new MaaStringBuffer(outBuffer)) {
            buffer.set(MaaJson.write(getCustomInfo()));
        }
        return 1;
    }
}
