package io.github.craun718.maafw;

import java.util.Objects;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;

/** Asynchronous operation handle, equivalent to the Python binding's {@code Job}. */
public class Job {

    protected final long jobId;
    private final LongFunction<MaaDef.Status> statusFunc;
    private final LongConsumer waitFunc;

    public Job(long jobId, LongFunction<MaaDef.Status> statusFunc, LongConsumer waitFunc) {
        this.jobId = jobId;
        this.statusFunc = Objects.requireNonNull(statusFunc, "statusFunc");
        this.waitFunc = Objects.requireNonNull(waitFunc, "waitFunc");
    }

    public long jobId() {
        return jobId;
    }

    /** Blocks until the native operation completes and returns this job for chaining. */
    public Job waitFor() {
        waitFunc.accept(jobId);
        return this;
    }

    /** Alias for {@link #waitFor()}, since Java's {@code Object.wait()} cannot be overridden. */
    public Job await() {
        return waitFor();
    }

    public MaaDef.Status status() {
        return statusFunc.apply(jobId);
    }

    public boolean done() {
        return status().done();
    }

    public boolean succeeded() {
        return status().succeeded();
    }

    public boolean failed() {
        return status().failed();
    }

    public boolean pending() {
        return status().pending();
    }

    public boolean running() {
        return status().running();
    }

    @Override
    public String toString() {
        return "Job(id=" + jobId + ", status=" + status() + ")";
    }
}
