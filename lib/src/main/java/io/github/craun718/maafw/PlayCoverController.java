package io.github.craun718.maafw;

/** PlayCover controller for iOS apps on macOS. */
public class PlayCoverController extends Controller {

    public PlayCoverController(String address, String uuid) {
        super();
        setHandle(MaaLibrary.framework().MaaPlayCoverControllerCreate(address, uuid));
    }
}
