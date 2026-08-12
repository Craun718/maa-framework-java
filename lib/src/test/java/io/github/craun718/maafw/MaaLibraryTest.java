package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MaaLibraryTest {

    @Test
    void defaultAgentBinaryPathResolvesOfficialReleaseLayout() throws Exception {
        Path release = Files.createTempDirectory("maa-java-release-layout-");
        try {
            Path bin = Files.createDirectories(release.resolve("bin"));
            Path agent = Files.createDirectories(release.resolve("share/MaaAgentBinary"));

            assertEquals(agent.toString(), MaaLibrary.defaultAgentBinaryPath(bin));
        } finally {
            deleteRecursively(release);
        }
    }

    @Test
    void defaultAgentBinaryPathFallsBackWhenReleaseLayoutIsNotPresent() throws Exception {
        Path release = Files.createTempDirectory("maa-java-release-layout-");
        try {
            Path bin = Files.createDirectories(release.resolve("bin"));
            assertEquals("MaaAgentBinary", MaaLibrary.defaultAgentBinaryPath(bin));
        } finally {
            deleteRecursively(release);
        }
    }

    private static void deleteRecursively(Path path) throws Exception {
        if (!Files.exists(path)) {
            return;
        }
        if (Files.isDirectory(path)) {
            try (var paths = Files.walk(path)) {
                for (Path child : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(child);
                }
            }
        } else {
            Files.deleteIfExists(path);
        }
    }
}
