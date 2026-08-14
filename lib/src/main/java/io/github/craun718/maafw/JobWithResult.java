package io.github.craun718.maafw;

import java.util.Objects;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;

/** Asynchronous operation handle that can produce a result. */
public class JobWithResult<T> extends Job {

    private final LongFunction<T> getFunc;

    public JobWithResult(long jobId, LongFunction<MaaDef.Status> statusFunc, LongConsumer waitFunc, LongFunction<T> getFunc) {
        super(jobId, statusFunc, waitFunc);
        this.getFunc = Objects.requireNonNull(getFunc, "getFunc");
    }

    @Override
    public JobWithResult<T> waitFor() {
        super.waitFor();
        return this;
    }

    @Override
    public JobWithResult<T> await() {
        return waitFor();
    }

    public T get() {
        return get(false);
    }

    public T get(boolean waitForCompletion) {
        if (waitForCompletion) {
            waitFor();
        }
        return getFunc.apply(jobId);
    }
}
