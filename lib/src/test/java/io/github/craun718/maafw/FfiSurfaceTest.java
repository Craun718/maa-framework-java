package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Verifies the JNA interface method names against the official C headers when a local
 * MaaFramework source checkout is available.
 */
class FfiSurfaceTest {

    @Test
    void exportedNamesMatchOfficialReleaseHeaders() throws Exception {
        Path sourceRoot = sourceRoot();
        Assumptions.assumeTrue(
                sourceRoot != null,
                "Set MAA_FRAMEWORK_SOURCE or maafw.maaFrameworkSource to run FFI parity test");

        checkSurface(
                "MaaFramework",
                sourceRoot.resolve("include/MaaFramework"),
                "MaaFrameworkLibrary.java",
                "MAA_FRAMEWORK_API");
        checkSurface(
                "MaaToolkit",
                sourceRoot.resolve("include/MaaToolkit"),
                "MaaToolkitLibrary.java",
                "MAA_TOOLKIT_API");
        checkSurface(
                "MaaAgentClient",
                sourceRoot.resolve("include/MaaAgentClient"),
                "MaaAgentClientLibrary.java",
                "MAA_AGENT_CLIENT_API");
        checkSurface(
                "MaaAgentServer",
                sourceRoot.resolve("include/MaaAgentServer"),
                "MaaAgentServerLibrary.java",
                "MAA_AGENT_SERVER_API");
    }

    private static void checkSurface(String name, Path headerDir, String javaFileName, String macro)
            throws IOException {
        Set<String> expected = headerFunctions(headerDir, macro);
        Path repo = repoRoot();
        Path javaFile = repo == null
                ? Path.of("src/main/java/io/github/craun718/maafw", javaFileName)
                : repo.resolve("lib/src/main/java/io/github/craun718/maafw").resolve(javaFileName);
        Set<String> actual = javaFunctions(javaFile);

        assertFalse(expected.isEmpty(), name + " headers should export at least one function");
        assertEquals(expected, actual, name + " FFI surface mismatch");
    }

    private static Set<String> headerFunctions(Path headerDir, String macro) throws IOException {
        Set<String> names = new TreeSet<>();
        Pattern declaration = Pattern.compile(Pattern.quote(macro) + "\\s*(.*?);", Pattern.DOTALL);
        Pattern call = Pattern.compile("([A-Za-z_][A-Za-z0-9_]*)\\s*\\(");

        List<Path> headers;
        try (Stream<Path> files = Files.walk(headerDir)) {
            headers = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".h"))
                    .toList();
        }

        for (Path header : headers) {
            Matcher matcher = declaration.matcher(Files.readString(header));
            while (matcher.find()) {
                Matcher callMatcher = call.matcher(matcher.group(1));
                String name = "";
                while (callMatcher.find()) {
                    name = callMatcher.group(1);
                }
                if (!name.isEmpty()) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    private static Set<String> javaFunctions(Path javaFile) throws IOException {
        Set<String> names = new TreeSet<>();
        Matcher matcher =
                Pattern.compile("\\bMaa[A-Za-z0-9_]*\\s*\\(").matcher(Files.readString(javaFile));
        while (matcher.find()) {
            names.add(matcher.group().replaceFirst("\\s*\\($", ""));
        }
        return names;
    }

    private static Path sourceRoot() {
        String property = System.getProperty("maafw.maaFrameworkSource");
        if (property != null && !property.isBlank()) {
            return Path.of(property);
        }
        String env = System.getenv("MAA_FRAMEWORK_SOURCE");
        if (env != null && !env.isBlank()) {
            return Path.of(env);
        }
        Path repo = repoRoot();
        if (repo == null) {
            return null;
        }
        Path candidate = repo.resolve("../clone-github/MaaFramework").normalize();
        return Files.isDirectory(candidate) ? candidate : null;
    }

    private static Path repoRoot() {
        Path current = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("settings.gradle.kts"))) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }
}
