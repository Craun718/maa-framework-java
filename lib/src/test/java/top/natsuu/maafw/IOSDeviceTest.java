package top.natsuu.maafw;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IOSDeviceTest {

    @Test
    void deviceCopiesConnectionsAndRejectsInvalidUdid() {
        Map<String, Object> connection = new HashMap<>(Map.of("transport", "USB"));
        List<Map<String, Object>> connections = new ArrayList<>(List.of(connection));
        IOSDevice device = new IOSDevice("phone", "id", MaaDef.IOS_SCREENCAP_STREAM, connections, "paired");
        connection.put("transport", "WiFi");
        connections.clear();
        assertEquals("USB", device.connections().get(0).get("transport"));
        assertThrows(UnsupportedOperationException.class, () -> device.connections().clear());
        assertThrows(UnsupportedOperationException.class, () -> device.connections().get(0).put("transport", "WiFi"));
        assertThrows(IllegalArgumentException.class, () -> new IOSController(""));
        assertThrows(IllegalArgumentException.class, () -> new IOSController("a\0b"));
    }
}
