package io.github.craun718.maafw;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/** FFI surface of MaaAgentServer dynamic library. */
public interface MaaAgentServerLibrary extends MaaFrameworkLibrary {

    byte MaaAgentServerRegisterCustomRecognition(
            String name,
            MaaCallbacks.CustomRecognitionCallback recognition,
            Pointer transArg);

    byte MaaAgentServerRegisterCustomAction(
            String name,
            MaaCallbacks.CustomActionCallback action,
            Pointer transArg);

    long MaaAgentServerAddResourceSink(MaaCallbacks.EventCallback sink, Pointer transArg);

    long MaaAgentServerAddControllerSink(MaaCallbacks.EventCallback sink, Pointer transArg);

    long MaaAgentServerAddTaskerSink(MaaCallbacks.EventCallback sink, Pointer transArg);

    long MaaAgentServerAddContextSink(MaaCallbacks.EventCallback sink, Pointer transArg);

    byte MaaAgentServerStartUp(String identifier);

    void MaaAgentServerShutDown();

    void MaaAgentServerJoin();

    void MaaAgentServerDetach();
}
