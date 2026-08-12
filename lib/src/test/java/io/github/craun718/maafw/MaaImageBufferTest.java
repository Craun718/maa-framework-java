package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MaaImageBufferTest {

    @Test
    void requireJavaArraySizeAcceptsJavaArrayLimits() {
        assertEquals(0, MaaImageBuffer.requireJavaArraySize(0, "raw"));
        assertEquals(
                Integer.MAX_VALUE,
                MaaImageBuffer.requireJavaArraySize(Integer.MAX_VALUE, "raw"));
    }

    @Test
    void requireJavaArraySizeRejectsNegativeSizes() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> MaaImageBuffer.requireJavaArraySize(-1, "encoded"));
        assertEquals("encoded size must not be negative: -1", error.getMessage());
    }

    @Test
    void requireJavaArraySizeRejectsOversizedBuffers() {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> MaaImageBuffer.requireJavaArraySize(Integer.MAX_VALUE + 1L, "raw"));
        assertEquals(
                "raw is too large for a Java byte[]: " + (Integer.MAX_VALUE + 1L) + " bytes",
                error.getMessage());
    }
}
