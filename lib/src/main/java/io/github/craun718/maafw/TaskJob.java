package io.github.craun718.maafw;

import java.util.Map;
import java.util.Objects;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;

/** Task operation handle with pipeline override support. */
public class TaskJob extends JobWithResult<TaskDetail> {

    @FunctionalInterface
    public interface PipelineOverride {
        boolean apply(long taskId, String pipelineJson);
    }

    private final PipelineOverride overridePipelineFunc;

    public TaskJob(
            long jobId,
            LongFunction<MaaDef.Status> statusFunc,
            LongConsumer waitFunc,
            LongFunction<TaskDetail> getFunc,
            PipelineOverride overridePipelineFunc) {
        super(jobId, statusFunc, waitFunc, getFunc);
        this.overridePipelineFunc = Objects.requireNonNull(overridePipelineFunc, "overridePipelineFunc");
    }

    @Override
    public TaskJob waitFor() {
        super.waitFor();
        return this;
    }

    @Override
    public TaskJob await() {
        return waitFor();
    }

    public boolean overridePipeline(Map<String, Object> pipelineOverride) {
        return overridePipeline(MaaJson.write(pipelineOverride == null ? Map.of() : pipelineOverride));
    }

    public boolean overridePipeline(String pipelineOverride) {
        return overridePipelineFunc.apply(jobId, MaaJson.objectJsonOrEmpty(pipelineOverride));
    }
}
