package io.github.craun718.maafw;

import java.nio.file.Path;

/** Debug controller that cycles screenshots from a directory. */
public class DbgController extends Controller {

    public DbgController(Path readPath) {
        this(readPath.toString());
    }

    public DbgController(String readPath) {
        super();
        setHandle(MaaLibrary.framework().MaaDbgControllerCreate(readPath));
    }
}
