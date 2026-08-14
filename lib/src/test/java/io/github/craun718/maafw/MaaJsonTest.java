package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MaaJsonTest {

    @Test
    void roundTripsNestedObjects() {
        Map<String, Object> value = Map.of("name", "Maa", "count", 3, "nested", Map.of("enabled", true));

        Map<String, Object> parsed = MaaJson.parseObject(MaaJson.write(value));

        assertEquals("Maa", parsed.get("name"));
        assertEquals(3, ((Number) parsed.get("count")).intValue());
        assertTrue((Boolean) ((Map<?, ?>) parsed.get("nested")).get("enabled"));
    }

    @Test
    void parsesObjectLists() {
        List<Map<String, Object>> parsed = MaaJson.parseObjectList("[{\"x\":1},{\"y\":2}]");

        assertEquals(2, parsed.size());
        assertEquals(1, ((Number) parsed.get(0).get("x")).intValue());
        assertEquals(2, ((Number) parsed.get(1).get("y")).intValue());
    }

    @Test
    void treatsBlankInputAsEmpty() {
        assertEquals(Map.of(), MaaJson.parseObject(""));
        assertEquals(Map.of(), MaaJson.parseObject("   "));
        assertNull(MaaJson.parse(""));
    }

    @Test
    void parseObjectOrEmptyReturnsEmptyForNonObjects() {
        assertEquals(Map.of(), MaaJson.parseObjectOrEmpty("42"));
        assertEquals(Map.of(), MaaJson.parseObjectOrEmpty(""));
    }

    @Test
    void rejectsInvalidJson() {
        assertThrows(IllegalArgumentException.class, () -> MaaJson.parseObject("[1]"));
        assertThrows(IllegalArgumentException.class, () -> MaaJson.parse("not-json"));
    }
}
