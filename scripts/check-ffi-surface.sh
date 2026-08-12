#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
SOURCE_ROOT="${1:-${MAA_FRAMEWORK_SOURCE:-}}"

if [[ -z "${SOURCE_ROOT}" ]]; then
    echo "usage: $0 <MaaFramework source root>" >&2
    echo "       MAA_FRAMEWORK_SOURCE=<path> $0" >&2
    exit 2
fi

FAILED=0

check_surface() {
    local name="$1"
    local header_dir="$2"
    local java_file="$3"
    local macro="$4"
    local headers java
    local header_count java_count

    headers="$(mktemp)"
    java="$(mktemp)"
    trap 'rm -f "${headers}" "${java}"' RETURN

    MACRO="${macro}" find "${header_dir}" -type f -name '*.h' -exec \
        perl -0777 -ne '
            while (/\Q$ENV{MACRO}\E\s*(.*?);/sg) {
                $chunk = $1;
                $name = "";
                while ($chunk =~ /([A-Za-z_][A-Za-z0-9_]*)\s*\(/g) {
                    $name = $1;
                }
                print "$name\n" if $name;
            }
        ' {} + | sort -u > "${headers}"
    rg -o '\bMaa[A-Za-z0-9_]*\s*\(' "${java_file}" |
        sed -E 's/[[:space:]]*\($//' |
        sort -u > "${java}"
    sort -u -o "${headers}" "${headers}"
    sort -u -o "${java}" "${java}"

    header_count="$(wc -l < "${headers}" | tr -d ' ')"
    java_count="$(wc -l < "${java}" | tr -d ' ')"
    echo "== ${name} =="
    echo "header functions: ${header_count}"
    echo "java functions: ${java_count}"

    if diff -u "${headers}" "${java}" >/dev/null; then
        echo "match: yes"
    else
        echo "missing:"
        comm -23 "${headers}" "${java}"
        echo "extra:"
        comm -13 "${headers}" "${java}"
        FAILED=1
    fi
}

check_surface \
    "MaaFramework" \
    "${SOURCE_ROOT}/include/MaaFramework" \
    "${REPO_ROOT}/lib/src/main/java/io/github/craun718/maafw/MaaFrameworkLibrary.java" \
    "MAA_FRAMEWORK_API"

check_surface \
    "MaaToolkit" \
    "${SOURCE_ROOT}/include/MaaToolkit" \
    "${REPO_ROOT}/lib/src/main/java/io/github/craun718/maafw/MaaToolkitLibrary.java" \
    "MAA_TOOLKIT_API"

check_surface \
    "MaaAgentClient" \
    "${SOURCE_ROOT}/include/MaaAgentClient" \
    "${REPO_ROOT}/lib/src/main/java/io/github/craun718/maafw/MaaAgentClientLibrary.java" \
    "MAA_AGENT_CLIENT_API"

check_surface \
    "MaaAgentServer" \
    "${SOURCE_ROOT}/include/MaaAgentServer" \
    "${REPO_ROOT}/lib/src/main/java/io/github/craun718/maafw/MaaAgentServerLibrary.java" \
    "MAA_AGENT_SERVER_API"

if [[ "${FAILED}" -ne 0 ]]; then
    echo "FFI surface mismatch" >&2
    exit 1
fi

echo "FFI surface parity OK"
