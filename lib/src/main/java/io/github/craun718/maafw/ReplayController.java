package io.github.craun718.maafw;

import java.nio.file.Path;

/** Controller that replays a JSONL recording written by {@link RecordController}. */
public class ReplayController extends Controller {

    public ReplayController(Path recordingPath) {
        this(recordingPath.toString());
    }

    public ReplayController(String recordingPath) {
        super();
        setHandle(MaaLibrary.framework().MaaReplayControllerCreate(recordingPath));
    }
}
