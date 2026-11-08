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

    @Test
    @SuppressWarnings("unchecked")
    void deviceCopiesNestedConnectionData() {
        Map<String, Object> nested = new HashMap<>();
        nested.put("address", "original");
        nested.put("optional", null);
        List<Object> candidates = new ArrayList<>();
        candidates.add(nested);
        candidates.add(null);
        IOSDevice device = new IOSDevice("phone", "id", 1, List.of(Map.of("candidates", candidates)), "paired");

        nested.put("address", "changed");
        candidates.clear();
        List<Object> snapshot = (List<Object>) device.connections().get(0).get("candidates");
        Map<String, Object> snapshotNested = (Map<String, Object>) snapshot.get(0);
        assertEquals("original", snapshotNested.get("address"));
        assertNull(snapshotNested.get("optional"));
        assertNull(snapshot.get(1));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("new"));
        assertThrows(UnsupportedOperationException.class, () -> snapshotNested.put("address", "changed"));
    }

}
