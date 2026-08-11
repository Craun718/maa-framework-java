package io.github.craun718.maafw;

import com.sun.jna.Pointer;
import java.util.Objects;

/** Owned wrapper for the Linux PipeWire portal helper. */
public final class PortalHelper implements AutoCloseable {

    private final Pointer handle;
    private final boolean owned;

    public PortalHelper(Pointer handle) {
        this(handle, true);
    }

    PortalHelper(Pointer handle, boolean owned) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.owned = owned;
    }

    public Pointer handle() {
        return handle;
    }

    public boolean openStream() {
        return MaaStringBuffer.toBoolean(MaaLibrary.toolkit().MaaToolkitPortalHelperOpenStream(handle));
    }

    public boolean getPersist() {
        return MaaStringBuffer.toBoolean(MaaLibrary.toolkit().MaaToolkitPortalHelperGetPersist(handle));
    }

    public void setPersist(boolean enable) {
        MaaLibrary.toolkit().MaaToolkitPortalHelperSetPersist(handle, (byte) (enable ? 1 : 0));
    }

    public int getPipeWireFd() {
        return MaaLibrary.toolkit().MaaToolkitPortalHelperGetPipeWireFD(handle);
    }

    public int getPipeWireNodeId() {
        return MaaLibrary.toolkit().MaaToolkitPortalHelperGetPipeWireNodeID(handle);
    }

    /** Returns the restore token, or {@code null} when the helper has no token. */
    public String getRestoreToken() {
        return MaaLibrary.toolkit().MaaToolkitPortalHelperGetRestoreToken(handle);
    }

    public void setRestoreToken(String token) {
        MaaLibrary.toolkit().MaaToolkitPortalHelperSetRestoreToken(handle, token == null ? "" : token);
    }

    @Override
    public void close() {
        if (owned && handle != Pointer.NULL) {
            MaaLibrary.toolkit().MaaToolkitPortalHelperDestroy(handle);
        }
    }
}
