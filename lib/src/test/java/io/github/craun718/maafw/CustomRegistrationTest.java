package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class CustomRegistrationTest {

    @Test
    void instantiatesCustomRecognitionAndActionClasses() {
        assertInstanceOf(
                TestRecognition.class,
                CustomRegistrations.newInstance(TestRecognition.class, "custom recognition"));
        assertInstanceOf(
                TestAction.class,
                CustomRegistrations.newInstance(TestAction.class, "custom action"));
    }

    @Test
    void rejectsClassesWithoutNoArgConstructor() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CustomRegistrations.newInstance(
                        NoNoArgRecognition.class, "custom recognition"));
    }

    @Test
    void classRegistrationOverloadsAreExposed() throws Exception {
        assertMethod(Resource.class, "registerCustomRecognition", String.class, Class.class);
        assertMethod(Resource.class, "registerCustomAction", String.class, Class.class);
        assertMethod(AgentServer.class, "registerCustomRecognition", String.class, Class.class);
        assertMethod(AgentServer.class, "registerCustomAction", String.class, Class.class);
    }

    private static void assertMethod(Class<?> owner, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = owner.getMethod(name, parameterTypes);
        assertNotNull(method);
    }

    public static final class TestRecognition extends CustomRecognition {

        public TestRecognition() {}

        @Override
        public AnalyzeResult analyze(Context context, AnalyzeArg argv) {
            return AnalyzeResult.miss();
        }
    }

    public static final class TestAction extends CustomAction {

        public TestAction() {}

        @Override
        public RunResult run(Context context, RunArg argv) {
            return RunResult.ok();
        }
    }

    public static final class NoNoArgRecognition extends CustomRecognition {

        @SuppressWarnings("unused")
        private NoNoArgRecognition(int ignored) {}

        @Override
        public AnalyzeResult analyze(Context context, AnalyzeArg argv) {
            return AnalyzeResult.miss();
        }
    }
}
