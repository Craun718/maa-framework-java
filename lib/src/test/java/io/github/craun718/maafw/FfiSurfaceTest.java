package io.github.craun718.maafw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Verifies JNA interface method names, parameter types, and return types against the official C
 * headers when a local MaaFramework source checkout is available.
 */
class FfiSurfaceTest {

    @Test
    void exportedNamesMatchOfficialReleaseHeaders() throws Exception {
        Path sourceRoot = sourceRoot();
        Assumptions.assumeTrue(
                sourceRoot != null,
                "Set MAA_FRAMEWORK_SOURCE or maafw.maaFrameworkSource to run FFI parity test");
        Map<String, String> aliases = cTypedefAliases(sourceRoot.resolve("include"));

        checkSurface(
                "MaaFramework",
                sourceRoot.resolve("include/MaaFramework"),
                MaaFrameworkLibrary.class,
                "MAA_FRAMEWORK_API",
                aliases);
        checkSurface(
                "MaaToolkit",
                sourceRoot.resolve("include/MaaToolkit"),
                MaaToolkitLibrary.class,
                "MAA_TOOLKIT_API",
                aliases);
        checkSurface(
                "MaaAgentClient",
                sourceRoot.resolve("include/MaaAgentClient"),
                MaaAgentClientLibrary.class,
                "MAA_AGENT_CLIENT_API",
                aliases);
        checkSurface(
                "MaaAgentServer",
                sourceRoot.resolve("include/MaaAgentServer"),
                MaaAgentServerLibrary.class,
                "MAA_AGENT_SERVER_API",
                aliases);
    }

    private static void checkSurface(
            String name,
            Path headerDir,
            Class<?> javaInterface,
            String macro,
            Map<String, String> aliases)
            throws IOException {
        Map<String, Signature> expected = headerSignatures(headerDir, aliases, macro);
        Map<String, Signature> actual = javaSignatures(javaInterface);

        assertFalse(expected.isEmpty(), name + " headers should export at least one function");
        Set<String> missing = new TreeSet<>(expected.keySet());
        missing.removeAll(actual.keySet());
        Set<String> unexpected = new TreeSet<>(actual.keySet());
        unexpected.removeAll(expected.keySet());
        unexpected.removeAll(expectedForwardExtras(name));

        assertTrue(missing.isEmpty(), name + " FFI surface is missing: " + missing);
        assertTrue(
                unexpected.isEmpty(), name + " FFI surface has unexpected extra functions: " + unexpected);

        List<String> signatureMismatches = new ArrayList<>();
        for (String function : expected.keySet()) {
            Signature cSignature = expected.get(function);
            Signature javaSignature = actual.get(function);
            if (javaSignature == null || !cSignature.matches(javaSignature)) {
                signatureMismatches.add(
                        function + " expected=" + cSignature + " java=" + javaSignature);
            }
        }
        assertTrue(
                signatureMismatches.isEmpty(),
                name + " FFI signatures mismatch:\n" + String.join("\n", signatureMismatches));
    }

    private static Set<String> expectedForwardExtras(String name) {
        return switch (name) {
            case "MaaFramework" -> Set.of("MaaLinuxControllerCreate");
            case "MaaToolkit" -> Set.of(
                    "MaaToolkitPortalHelperCreate",
                    "MaaToolkitPortalHelperDestroy",
                    "MaaToolkitPortalHelperGetPersist",
                    "MaaToolkitPortalHelperGetPipeWireFD",
                    "MaaToolkitPortalHelperGetPipeWireNodeID",
                    "MaaToolkitPortalHelperGetRestoreToken",
                    "MaaToolkitPortalHelperOpenStream",
                    "MaaToolkitPortalHelperSetPersist",
                    "MaaToolkitPortalHelperSetRestoreToken");
            default -> Set.of();
        };
    }

    private static Map<String, Signature> headerSignatures(
            Path headerDir, Map<String, String> aliases, String macro) throws IOException {
        Map<String, Signature> signatures = new TreeMap<>();
        Pattern api = Pattern.compile(Pattern.quote(macro) + "\\s*(.*)$", Pattern.DOTALL);
        Pattern declaration =
                Pattern.compile("^\\s*(.*?)\\b(Maa[A-Za-z0-9_]+)\\s*\\((.*)\\)\\s*$", Pattern.DOTALL);
        List<Path> headers;
        try (Stream<Path> files = Files.walk(headerDir)) {
            headers = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".h"))
                    .toList();
        }

        for (Path header : headers) {
            String content = removeCComments(Files.readString(header));
            for (String rawStatement : content.split(";")) {
                String statement = normalizeSpaces(rawStatement.replace("MAA_DEPRECATED", " "));
                if (!statement.contains(macro)) {
                    continue;
                }
                Matcher apiMatcher = api.matcher(statement);
                if (!apiMatcher.find()) {
                    continue;
                }
                Matcher declarationMatcher =
                        declaration.matcher(normalizeSpaces(apiMatcher.group(1)));
                if (!declarationMatcher.matches()) {
                    continue;
                }
                String returnType = normalizeSpaces(declarationMatcher.group(1));
                String name = declarationMatcher.group(2);
                String params = declarationMatcher.group(3);
                Signature signature = new Signature(
                        cParamTypes(params, aliases), cReturnTypes(returnType, aliases));
                Signature previous = signatures.put(name, signature);
                assertEquals(null, previous, name + " is declared more than once in C headers");
            }
        }
        return signatures;
    }

    private static Map<String, Signature> javaSignatures(Class<?> javaInterface) {
        Map<String, Signature> signatures = new TreeMap<>();
        for (Method method : javaInterface.getDeclaredMethods()) {
            if (!method.getName().startsWith("Maa")) {
                continue;
            }
            List<String> params = new ArrayList<>();
            for (Class<?> type : method.getParameterTypes()) {
                params.add(javaType(type));
            }
            Signature signature = new Signature(params, javaReturn(method.getReturnType()));
            Signature previous = signatures.put(method.getName(), signature);
            assertEquals(null, previous, method.getName() + " is declared more than once in Java");
        }
        return signatures;
    }

    private static List<String> cParamTypes(String raw, Map<String, String> aliases) {
        raw = normalizeSpaces(raw);
        if (raw.isEmpty() || "void".equals(raw)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String part : splitTopLevel(raw)) {
            String type = stripCParamName(part);
            result.add(normalizeCType(type, aliases));
        }
        return result;
    }

    private static List<String> cReturnTypes(String raw, Map<String, String> aliases) {
        String type = normalizeCType(raw, aliases);
        return "void".equals(type) ? List.of() : List.of(type);
    }

    private static List<String> javaReturn(Class<?> type) {
        String normalized = javaType(type);
        return "void".equals(normalized) ? List.of() : List.of(normalized);
    }

    private static String javaType(Class<?> type) {
        if (type == void.class) {
            return "void";
        }
        if (type == byte.class) {
            return "byte";
        }
        if (type == short.class) {
            return "short";
        }
        if (type == int.class) {
            return "int";
        }
        if (type == long.class) {
            return "long";
        }
        if (type == String.class) {
            return "text";
        }
        if (type == byte[].class) {
            return "bytes";
        }
        if (type == Pointer.class) {
            return "ptr";
        }
        if (Callback.class.isAssignableFrom(type)) {
            return "callback";
        }
        if (Structure.class.isAssignableFrom(type)) {
            return "ptr";
        }
        if (type.getName().endsWith("ByReference")) {
            return "ptr";
        }
        return "named:" + type.getName();
    }

    private static Map<String, String> cTypedefAliases(Path includeRoot) throws IOException {
        Map<String, String> aliases = new TreeMap<>();
        Pattern typedef =
                Pattern.compile("\\btypedef\\s+(.*?)\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*;", Pattern.DOTALL);
        List<Path> headers;
        try (Stream<Path> files = Files.walk(includeRoot)) {
            headers = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".h"))
                    .toList();
        }
        for (Path header : headers) {
            Matcher matcher = typedef.matcher(removeCComments(Files.readString(header)));
            while (matcher.find()) {
                String rhs = normalizeSpaces(matcher.group(1));
                String lhs = matcher.group(2);
                if (rhs.contains("(") || rhs.contains("{") || rhs.contains("}") || rhs.contains("[")) {
                    continue;
                }
                aliases.put(lhs, rhs);
            }
        }
        return aliases;
    }

    private static String normalizeCType(String raw, Map<String, String> aliases) {
        String type = normalizeSpaces(raw).replace("MAA_CALL", " ");
        int pointerCount = count(type, '*');
        String base = normalizeSpaces(
                type.replace("const", " ").replace("volatile", " ").replace("*", " "));
        if (base.equals("MaaBool") && pointerCount == 0) {
            return "byte";
        }

        for (int i = 0; i < 16; i++) {
            String alias = aliases.get(base);
            if (alias == null) {
                break;
            }
            pointerCount += count(alias, '*');
            base = normalizeSpaces(
                    alias.replace("const", " ").replace("volatile", " ").replace("*", " "));
        }

        if (base.equals("char") && pointerCount > 0) {
            return "text";
        }
        if (pointerCount > 0) {
            return "ptr";
        }
        if (base.endsWith("Callback")) {
            return "callback";
        }
        return switch (base) {
            case "void" -> "void";
            case "bool", "uint8_t", "int8_t" -> "byte";
            case "uint16_t", "int16_t" -> "short";
            case "int", "int32_t", "uint32_t" -> "int";
            case "int64_t", "uint64_t", "size_t" -> "long";
            default -> base.startsWith("struct ") ? "ptr" : "named:" + base;
        };
    }

    private static String stripCParamName(String raw) {
        String normalized = normalizeSpaces(raw);
        Matcher matcher = Pattern.compile("^(.+?)\\s+([A-Za-z_][A-Za-z0-9_]*)$").matcher(normalized);
        return matcher.matches() ? matcher.group(1) : normalized;
    }

    private static List<String> splitTopLevel(String raw) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                depth = Math.max(0, depth - 1);
            }
            if (ch == ',' && depth == 0) {
                String part = current.toString().trim();
                if (!part.isEmpty()) {
                    parts.add(part);
                }
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        String tail = current.toString().trim();
        if (!tail.isEmpty()) {
            parts.add(tail);
        }
        return parts;
    }

    private static int count(String value, char needle) {
        int result = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == needle) {
                result++;
            }
        }
        return result;
    }

    private static String removeCComments(String content) {
        return content.replaceAll("(?s)/\\*.*?\\*/", " ").replaceAll("(?m)//.*$", " ");
    }

    private static String normalizeSpaces(String value) {
        return value.replaceAll("\\s+", " ").trim();
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

    private record Signature(List<String> params, List<String> returns) {

        boolean matches(Signature other) {
            if (params.size() != other.params.size() || returns.size() != other.returns.size()) {
                return false;
            }
            for (int i = 0; i < params.size(); i++) {
                if (!compatible(params.get(i), other.params.get(i))) {
                    return false;
                }
            }
            for (int i = 0; i < returns.size(); i++) {
                if (!compatible(returns.get(i), other.returns.get(i))) {
                    return false;
                }
            }
            return true;
        }

        private static boolean compatible(String cType, String javaType) {
            if (cType.equals(javaType)) {
                return true;
            }
            return "text".equals(cType) && "bytes".equals(javaType);
        }

        @Override
        public String toString() {
            return "params=" + params + ", returns=" + returns;
        }
    }
}
