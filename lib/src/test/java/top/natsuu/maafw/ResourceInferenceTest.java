package top.natsuu.maafw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.jna.Pointer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResourceInferenceTest {

    private Field frameworkField;
    private Object originalFramework;

    @BeforeEach
    void saveFramework() throws Exception {
        frameworkField = MaaLibrary.class.getDeclaredField("framework");
        frameworkField.setAccessible(true);
        originalFramework = frameworkField.get(null);
    }

    @AfterEach
    void restoreFramework() throws Exception {
        frameworkField.set(null, originalFramework);
    }

    @Test
    void rejectedProviderDoesNotWriteDevice() throws Exception {
        List<Integer> options = new ArrayList<>();
        installFramework((proxy, method, args) -> {
            assertEquals("MaaResourceSetOption", method.getName());
            options.add((Integer) args[1]);
            return (byte) 0;
        });

        Resource resource = new Resource(new Pointer(1));
        assertFalse(resource.useNcnn());
        assertEquals(List.of(MaaDef.ResOption.INFERENCE_EXECUTION_PROVIDER.code()), options);
    }

    @Test
    void concurrentSelectionsKeepProviderAndDeviceTogether() throws Exception {
        CountDownLatch firstProviderEntered = new CountDownLatch(1);
        CountDownLatch releaseFirstProvider = new CountDownLatch(1);
        CountDownLatch secondSelectionStarted = new CountDownLatch(1);
        CountDownLatch secondProviderEntered = new CountDownLatch(1);
        List<String> writes = Collections.synchronizedList(new ArrayList<>());
        installFramework((proxy, method, args) -> {
            assertEquals("MaaResourceSetOption", method.getName());
            assertEquals((long) Integer.BYTES, args[3]);
            int key = (Integer) args[1];
            int value = ((Pointer) args[2]).getInt(0);
            writes.add(key + ":" + value);
            if (key == MaaDef.ResOption.INFERENCE_EXECUTION_PROVIDER.code()) {
                if (value == MaaDef.INFERENCE_EXECUTION_PROVIDER_WEB_GPU) {
                    firstProviderEntered.countDown();
                    assertTrue(releaseFirstProvider.await(5, TimeUnit.SECONDS));
                } else {
                    secondProviderEntered.countDown();
                }
            }
            return (byte) 1;
        });

        Resource resource = new Resource(new Pointer(1));
        try (var executor = Executors.newFixedThreadPool(2)) {
            try {
                var first = executor.submit(() -> resource.useWebgpu(7));
                assertTrue(firstProviderEntered.await(5, TimeUnit.SECONDS));
                var second = executor.submit(() -> {
                    secondSelectionStarted.countDown();
                    return resource.useDirectml(9);
                });
                assertTrue(secondSelectionStarted.await(5, TimeUnit.SECONDS));
                assertFalse(secondProviderEntered.await(200, TimeUnit.MILLISECONDS));
                releaseFirstProvider.countDown();
                assertTrue(first.get(5, TimeUnit.SECONDS));
                assertTrue(second.get(5, TimeUnit.SECONDS));
                assertEquals(List.of("2:5", "1:7", "2:2", "1:9"), writes);
            } finally {
                releaseFirstProvider.countDown();
            }
        }
    }

    private void installFramework(InvocationHandler handler) throws Exception {
        frameworkField.set(null,
                Proxy.newProxyInstance(MaaFrameworkLibrary.class.getClassLoader(), new Class<?>[] { MaaFrameworkLibrary.class }, handler));
    }
}
