package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class ConvenienceSurfaceTest {

    @Test
    void parityMethodsAreExposed() throws Exception {
        assertMethod(Resource.class, "getNodeJson", String.class);
        assertMethod(Context.class, "getNodeJson", String.class);
        assertMethod(TaskJob.class, "getDetail");
        assertMethod(Resource.class, "setInference", int.class, int.class);
    }

    @Test
    void taskJobGetDetailDoesNotWait() {
        AtomicBoolean waited = new AtomicBoolean();
        TaskDetail detail = new TaskDetail(1L, "Entry", List.of(), MaaDef.Status.SUCCEEDED, null);
        TaskJob job = new TaskJob(
                1L,
                id -> MaaDef.Status.SUCCEEDED,
                id -> waited.set(true),
                id -> detail,
                (taskId, pipelineJson) -> true);

        assertSame(detail, job.getDetail());
        assertFalse(waited.get(), "getDetail should not block");
    }

    private static void assertMethod(Class<?> owner, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = owner.getMethod(name, parameterTypes);
        assertNotNull(method);
    }
}
