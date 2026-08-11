package io.github.craun718.maafw;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/** FFI surface of MaaAgentClient dynamic library. */
public interface MaaAgentClientLibrary extends Library {

    Pointer MaaAgentClientCreateV2(Pointer identifier);

    Pointer MaaAgentClientCreateTcp(short port);

    Pointer MaaAgentClientCreate();

    byte MaaAgentClientCreateSocket(Pointer client, Pointer identifier);

    void MaaAgentClientDestroy(Pointer client);

    byte MaaAgentClientIdentifier(Pointer client, Pointer identifier);

    byte MaaAgentClientBindResource(Pointer client, Pointer resource);

    byte MaaAgentClientRegisterResourceSink(Pointer client, Pointer resource);

    byte MaaAgentClientRegisterControllerSink(Pointer client, Pointer controller);

    byte MaaAgentClientRegisterTaskerSink(Pointer client, Pointer tasker);

    byte MaaAgentClientConnect(Pointer client);

    byte MaaAgentClientDisconnect(Pointer client);

    byte MaaAgentClientConnected(Pointer client);

    byte MaaAgentClientAlive(Pointer client);

    byte MaaAgentClientSetTimeout(Pointer client, long milliseconds);

    byte MaaAgentClientGetCustomRecognitionList(Pointer client, Pointer buffer);

    byte MaaAgentClientGetCustomActionList(Pointer client, Pointer buffer);
}
