package io.github.craun718.maafw;

import java.nio.file.Path;

/** Controller that wraps another controller and records every operation to JSONL. */
public class RecordController extends Controller {

    private final Controller inner;

    public RecordController(Controller inner, Path recordingPath) {
        this(inner, recordingPath.toString());
    }

    public RecordController(Controller inner, String recordingPath) {
        super();
        this.inner = inner;
        setHandle(MaaLibrary.framework().MaaRecordControllerCreate(inner.handle, recordingPath));
    }

    public Controller inner() {
        return inner;
    }
}
