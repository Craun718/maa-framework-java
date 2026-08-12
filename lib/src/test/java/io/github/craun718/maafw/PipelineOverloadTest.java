package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.craun718.maafw.pipeline.JNodeAttr;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class PipelineOverloadTest {

    @Test
    void jsonStringOverloadsAreExposed() throws Exception {
        assertMethod(Resource.class, "overridePipeline", String.class);
        assertMethod(Context.class, "runTask", String.class, String.class);
        assertMethod(Context.class, "runRecognition", String.class, MaaImage.class, String.class);
        assertMethod(
                Context.class, "runAction", String.class, MaaRect.class, String.class, String.class);
        assertMethod(Context.class, "overridePipeline", String.class);
        assertMethod(Tasker.class, "postTask", String.class, String.class);
        assertMethod(Tasker.class, "overridePipeline", long.class, String.class);
        assertMethod(TaskJob.class, "overridePipeline", String.class);
        assertMethod(Resource.class, "overrideNext", String.class, List.class);
        assertMethod(Context.class, "overrideNext", String.class, List.class);
    }

    @Test
    void taskJobPassesJsonStringAndMapOverridesThrough() {
        AtomicReference<String> received = new AtomicReference<>();
        TaskJob job = new TaskJob(
                1L,
                id -> MaaDef.Status.PENDING,
                id -> {},
                id -> null,
                (taskId, pipelineJson) -> {
                    received.set(pipelineJson);
                    return true;
                });

        assertTrue(job.overridePipeline("{\"a\":1}"));
        assertEquals("{\"a\":1}", received.get());

        assertTrue(job.overridePipeline((String) null));
        assertEquals("{}", received.get());

        assertTrue(job.overridePipeline("  "));
        assertEquals("{}", received.get());

        Map<String, Object> override = Map.of("A", Map.of("action", "DoNothing"));
        assertTrue(job.overridePipeline(override));
        assertEquals(override, MaaJson.parseObject(received.get()));
    }

    @Test
    void blankJsonIsNormalizedToEmptyObject() {
        assertEquals("{}", MaaJson.objectJsonOrEmpty(null));
        assertEquals("{}", MaaJson.objectJsonOrEmpty("  "));
        assertEquals("{\"a\":1}", MaaJson.objectJsonOrEmpty("{\"a\":1}"));
    }

    @Test
    void nextListNormalizesRawStringsAndNodeAttrs() {
        assertEquals(
                List.of("A", "[JumpBack]B", "[Anchor]C", "[JumpBack][Anchor]D"),
                MaaNextItems.names(
                        List.of(
                                "A",
                                JNodeAttr.of("B", true, false),
                                JNodeAttr.of("C", false, true),
                                JNodeAttr.of("D", true, true))));
        assertEquals(List.of(), MaaNextItems.names(null));
        assertThrows(
                IllegalArgumentException.class, () -> MaaNextItems.names(List.of(42)));
    }

    private static void assertMethod(Class<?> owner, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = owner.getMethod(name, parameterTypes);
        assertNotNull(method);
    }
}
