package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MaaDefTest {

    @Test
    void statusMapsCodesAndState() {
        assertEquals(MaaDef.Status.INVALID, MaaDef.Status.of(0));
        assertEquals(MaaDef.Status.PENDING, MaaDef.Status.of(1000));
        assertEquals(MaaDef.Status.RUNNING, MaaDef.Status.of(2000));
        assertEquals(MaaDef.Status.SUCCEEDED, MaaDef.Status.of(3000));
        assertEquals(MaaDef.Status.FAILED, MaaDef.Status.of(4000));

        assertTrue(MaaDef.Status.SUCCEEDED.succeeded());
        assertTrue(MaaDef.Status.FAILED.failed());
        assertTrue(MaaDef.Status.FAILED.done());
        assertFalse(MaaDef.Status.RUNNING.done());
    }

    @Test
    void notificationTypeParsesSuffixes() {
        assertEquals(MaaDef.NotificationType.STARTING, MaaDef.NotificationType.of("Resource.Loading.Starting"));
        assertEquals(MaaDef.NotificationType.SUCCEEDED, MaaDef.NotificationType.of("Tasker.Task.Succeeded"));
        assertEquals(MaaDef.NotificationType.FAILED, MaaDef.NotificationType.of("Node.Action.Failed"));
        assertEquals(MaaDef.NotificationType.UNKNOWN, MaaDef.NotificationType.of("Node.Action"));
        assertEquals(MaaDef.NotificationType.UNKNOWN, MaaDef.NotificationType.of(null));
    }

    @Test
    void algorithmAndActionLookupUsesNativeNames() {
        assertEquals(MaaDef.Algorithm.TEMPLATE_MATCH, MaaDef.Algorithm.of("TemplateMatch"));
        assertEquals(MaaDef.Algorithm.CUSTOM, MaaDef.Algorithm.of("Custom"));
        assertNull(MaaDef.Algorithm.of("Missing"));

        assertEquals(MaaDef.Action.CLICK, MaaDef.Action.of("Click"));
        assertEquals(MaaDef.Action.SCREENCAP, MaaDef.Action.of("Screencap"));
        assertEquals(MaaDef.Action.CUSTOM, MaaDef.Action.of("Custom"));
        assertNull(MaaDef.Action.of("Missing"));
    }

    @Test
    void defaultAdbFlagsExcludeUnsafeStreamingMethods() {
        assertEquals(0L, MaaDef.INVALID_ID);
        assertTrue((MaaDef.ADB_SCREENCAP_DEFAULT & MaaDef.ADB_SCREENCAP_ENCODE) != 0);
        assertTrue((MaaDef.ADB_SCREENCAP_DEFAULT & MaaDef.ADB_SCREENCAP_RAW_BY_NETCAT) == 0);
        assertTrue((MaaDef.ADB_SCREENCAP_DEFAULT & MaaDef.ADB_SCREENCAP_MINICAP_STREAM) == 0);
    }
}
