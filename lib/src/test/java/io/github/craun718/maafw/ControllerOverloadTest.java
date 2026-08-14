package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sun.jna.Pointer;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ControllerOverloadTest {

    @Test
    @SuppressWarnings("deprecation")
    void controllerConstructorsMatchPythonDefaultArguments() throws Exception {
        assertConstructor(Win32Controller.class, long.class, long.class, long.class, long.class);
        assertConstructor(Win32Controller.class, Pointer.class);
        assertConstructor(WlRootsController.class, String.class);
        assertConstructor(KWinController.class, String.class, int.class, int.class);

        assertConstructor(AdbController.class, String.class, String.class, Map.class);
        assertConstructor(AdbController.class, String.class, String.class, long.class, long.class, Map.class);
        assertConstructor(AdbController.class, Path.class, String.class);
        assertConstructor(AdbController.class, Path.class, String.class, Map.class);
        assertConstructor(AdbController.class, Path.class, String.class, long.class, long.class);
        assertConstructor(AdbController.class, Path.class, String.class, long.class, long.class, Map.class);
    }

    private static void assertConstructor(Class<?> type, Class<?>... parameterTypes) throws NoSuchMethodException {
        Constructor<?> constructor = type.getConstructor(parameterTypes);
        assertNotNull(constructor);
    }
}
