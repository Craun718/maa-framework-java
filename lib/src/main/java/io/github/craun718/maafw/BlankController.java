package io.github.craun718.maafw;

import java.util.Map;

/**
 * Pure-Java no-op custom controller for testing and development.
 *
 * <p>Unlike {@link DbgController}, this is not a native {@code MaaDbgControllerCreate} wrapper. It
 * implements {@link CustomController} in Java and always succeeds, matching the Go binding's
 * {@code BlankController} convenience API.
 */
public class BlankController extends CustomController {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    public BlankController() {
        super();
    }

    @Override
    public boolean connect() {
        return true;
    }

    @Override
    public boolean connected() {
        return true;
    }

    @Override
    public String requestUuid() {
        return "blank-controller";
    }

    @Override
    public long getFeatures() {
        return MaaDef.CONTROLLER_FEATURE_NONE;
    }

    @Override
    public boolean startApp(String intent) {
        return true;
    }

    @Override
    public boolean stopApp(String intent) {
        return true;
    }

    @Override
    public MaaImage screencap() {
        return new MaaImage(new byte[WIDTH * HEIGHT * 3], WIDTH, HEIGHT, 3, MaaImage.TYPE_8UC3);
    }

    @Override
    public boolean click(int x, int y) {
        return true;
    }

    @Override
    public boolean swipe(int x1, int y1, int x2, int y2, int duration) {
        return true;
    }

    @Override
    public boolean touchDown(int contact, int x, int y, int pressure) {
        return true;
    }

    @Override
    public boolean touchMove(int contact, int x, int y, int pressure) {
        return true;
    }

    @Override
    public boolean touchUp(int contact) {
        return true;
    }

    @Override
    public boolean clickKey(int keycode) {
        return true;
    }

    @Override
    public boolean inputText(String text) {
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        return true;
    }

    @Override
    public boolean scroll(int dx, int dy) {
        return true;
    }

    @Override
    public boolean relativeMove(int dx, int dy) {
        return true;
    }

    @Override
    public String shell(String command, long timeout) {
        return "";
    }

    @Override
    public boolean inactive() {
        return true;
    }

    @Override
    public Map<String, Object> getCustomInfo() {
        return Map.of("type", "blank");
    }
}
