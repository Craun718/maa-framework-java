package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AgentClientTest {

    @Test
    void tcpPortValidationAcceptsFullUnsignedShortRange() {
        assertEquals(0, AgentClient.requireValidTcpPort(0));
        assertEquals(65535, AgentClient.requireValidTcpPort(65535));
    }

    @Test
    void createTcpRejectsPortsOutsideUnsignedShortRange() {
        assertThrows(IllegalArgumentException.class, () -> AgentClient.createTcp(-1));
        assertThrows(IllegalArgumentException.class, () -> AgentClient.createTcp(65536));
    }
}
