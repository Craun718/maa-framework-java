package io.github.craun718.maafw.pipeline;

import java.util.ArrayList;
import java.util.List;

/** Pipeline v2 action section. */
public final class JAction {

    public JActionType type;
    public JActionParam param;

    public static JAction of(JActionType type, JActionParam param) {
        JAction action = new JAction();
        action.type = type;
        action.param = param;
        return action;
    }

    public static JAction doNothing() {
        return of(JActionType.DO_NOTHING, new JDoNothing());
    }

    public static JAction click() {
        return click(new JClick());
    }

    public static JAction click(JClick param) {
        return of(JActionType.CLICK, param);
    }

    public static JAction longPress() {
        return longPress(new JLongPress());
    }

    public static JAction longPress(JLongPress param) {
        return of(JActionType.LONG_PRESS, param);
    }

    public static JAction swipe() {
        return swipe(new JSwipe());
    }

    public static JAction swipe(JSwipe param) {
        return of(JActionType.SWIPE, param);
    }

    public static JAction multiSwipe(List<JSwipe> swipes) {
        JMultiSwipe param = new JMultiSwipe();
        param.swipes = List.copyOf(swipes);
        return of(JActionType.MULTI_SWIPE, param);
    }

    public static JAction touchDown() {
        return touchDown(new JTouch());
    }

    public static JAction touchDown(JTouch param) {
        return of(JActionType.TOUCH_DOWN, param);
    }

    public static JAction touchMove() {
        return touchMove(new JTouch());
    }

    public static JAction touchMove(JTouch param) {
        return of(JActionType.TOUCH_MOVE, param);
    }

    public static JAction touchUp(long contact) {
        JTouchUp param = new JTouchUp();
        param.contact = contact;
        return of(JActionType.TOUCH_UP, param);
    }

    public static JAction clickKey(int... keys) {
        List<Integer> values = new ArrayList<>(keys.length);
        for (int key : keys) {
            values.add(key);
        }
        return clickKey(values);
    }

    public static JAction clickKey(List<Integer> keys) {
        JClickKey param = new JClickKey();
        param.key = List.copyOf(keys);
        return of(JActionType.CLICK_KEY, param);
    }

    public static JAction longPressKey(int... keys) {
        List<Integer> values = new ArrayList<>(keys.length);
        for (int key : keys) {
            values.add(key);
        }
        return longPressKey(values);
    }

    public static JAction longPressKey(List<Integer> keys) {
        JLongPressKey param = new JLongPressKey();
        param.key = List.copyOf(keys);
        return of(JActionType.LONG_PRESS_KEY, param);
    }

    public static JAction longPressKey(JLongPressKey param) {
        return of(JActionType.LONG_PRESS_KEY, param);
    }

    public static JAction keyDown(int key) {
        JKey param = new JKey();
        param.key = key;
        return of(JActionType.KEY_DOWN, param);
    }

    public static JAction keyUp(int key) {
        JKey param = new JKey();
        param.key = key;
        return of(JActionType.KEY_UP, param);
    }

    public static JAction inputText(String text) {
        JInputText param = new JInputText();
        param.inputText = text;
        return of(JActionType.INPUT_TEXT, param);
    }

    public static JAction startApp(String packageName) {
        JStartApp param = new JStartApp();
        param.packageName = packageName;
        return of(JActionType.START_APP, param);
    }

    public static JAction stopApp(String packageName) {
        JStopApp param = new JStopApp();
        param.packageName = packageName;
        return of(JActionType.STOP_APP, param);
    }

    public static JAction stopTask() {
        return of(JActionType.STOP_TASK, new JStopTask());
    }

    public static JAction scroll() {
        return scroll(new JScroll());
    }

    public static JAction scroll(JScroll param) {
        return of(JActionType.SCROLL, param);
    }

    public static JAction command(String exec) {
        JCommand param = new JCommand();
        param.exec = exec;
        return of(JActionType.COMMAND, param);
    }

    public static JAction command(JCommand param) {
        return of(JActionType.COMMAND, param);
    }

    public static JAction shell(String cmd) {
        JShell param = new JShell();
        param.cmd = cmd;
        return of(JActionType.SHELL, param);
    }

    public static JAction shell(JShell param) {
        return of(JActionType.SHELL, param);
    }

    public static JAction screencap() {
        return screencap(new JScreencap());
    }

    public static JAction screencap(JScreencap param) {
        return of(JActionType.SCREENCAP, param);
    }

    public static JAction custom(String customAction) {
        JCustomAction param = new JCustomAction();
        param.customAction = customAction;
        return of(JActionType.CUSTOM, param);
    }

    public static JAction custom(JCustomAction param) {
        return of(JActionType.CUSTOM, param);
    }

    public JAction type(JActionType type) {
        this.type = type;
        return this;
    }

    public JAction param(JActionParam param) {
        this.param = param;
        return this;
    }
}
