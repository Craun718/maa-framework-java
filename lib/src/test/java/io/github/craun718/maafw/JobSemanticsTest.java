package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class JobSemanticsTest {

    @Test
    void jobWaitForAndAwaitBlockUntilCompletion() {
        AtomicBoolean waited = new AtomicBoolean();
        Job job = new Job(1L, id -> MaaDef.Status.SUCCEEDED, id -> waited.set(true));

        assertTrue(job.waitFor().succeeded());
        assertTrue(waited.get(), "waitFor should block until the job completes");

        waited.set(false);
        assertTrue(job.await().succeeded());
        assertTrue(waited.get(), "await should block until the job completes");
    }

    @Test
    void jobWithResultGetDoesNotWaitByDefault() {
        AtomicBoolean waited = new AtomicBoolean();
        JobWithResult<String> job = new JobWithResult<>(1L, id -> MaaDef.Status.PENDING, id -> waited.set(true), id -> "result");

        assertEquals("result", job.get());
        assertFalse(waited.get(), "get() should not block by default");
    }

    @Test
    void jobWithResultGetWaitsWhenRequested() {
        AtomicBoolean waited = new AtomicBoolean();
        JobWithResult<String> job = new JobWithResult<>(1L, id -> MaaDef.Status.SUCCEEDED, id -> waited.set(true), id -> "result");

        assertEquals("result", job.get(true));
        assertTrue(waited.get(), "get(true) should block until the job completes");
    }

    @Test
    void taskJobWaitForBlocksButGetDetailDoesNot() {
        AtomicBoolean waited = new AtomicBoolean();
        TaskDetail detail = new TaskDetail(1L, "Entry", List.of(), MaaDef.Status.PENDING, null);
        TaskJob job = new TaskJob(1L, id -> MaaDef.Status.PENDING, id -> waited.set(true), id -> detail, (taskId, pipelineJson) -> true);

        assertSame(detail, job.getDetail());
        assertFalse(waited.get(), "getDetail() should not block");

        assertSame(job, job.waitFor());
        assertTrue(waited.get(), "waitFor() should block until the task completes");
    }
}
