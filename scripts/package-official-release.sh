#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RELEASES_ROOT="${1:-${MAA_FRAMEWORK_RELEASES:-}}"
VERSION="${MAA_FRAMEWORK_VERSION:-dev}"
OUT_DIR="${MAA_FRAMEWORK_OUTPUT_DIR:-${REPO_ROOT}/build/distributions}"

if [[ -z "${RELEASES_ROOT}" ]]; then
    echo "usage: $0 <directory containing extracted MAA-* releases>" >&2
    echo "       MAA_FRAMEWORK_RELEASES=<path> $0" >&2
    echo "       MAA_FRAMEWORK_VERSION=v5.12.3 MAA_FRAMEWORK_OUTPUT_DIR=dist $0" >&2
    exit 2
fi

if [[ ! -d "${RELEASES_ROOT}" ]]; then
    echo "release directory does not exist: ${RELEASES_ROOT}" >&2
    exit 2
fi

"${REPO_ROOT}/gradlew" -q :lib:jar

JAR="$(find "${REPO_ROOT}/lib/build/libs" -maxdepth 1 -type f -name '*.jar' -print | head -1)"
if [[ -z "${JAR}" ]]; then
    echo "Java binding jar was not produced by :lib:jar" >&2
    exit 1
fi

mkdir -p "${OUT_DIR}"

PLATFORMS=(
    "win-x86_64 MAA-win-x86_64"
    "win-aarch64 MAA-win-aarch64"
    "linux-x86_64 MAA-linux-x86_64"
    "linux-aarch64 MAA-linux-aarch64"
    "macos-x86_64 MAA-macos-x86_64"
    "macos-aarch64 MAA-macos-aarch64"
    "android-x86_64 MAA-android-x86_64"
    "android-aarch64 MAA-android-aarch64"
)

packed=0
for entry in "${PLATFORMS[@]}"; do
    read -r platform source_dir <<< "${entry}"
    source_root="${RELEASES_ROOT}/${source_dir}"
    if [[ ! -d "${source_root}" ]]; then
        continue
    fi
    if [[ ! -d "${source_root}/bin" ]]; then
        echo "skipping ${source_dir}: missing bin directory" >&2
        continue
    fi

    stage="$(mktemp -d "${TMPDIR:-/tmp}/maa-java-release.XXXXXX")"
    trap 'rm -rf "${stage}"' EXIT

    mkdir -p "${stage}/lib" "${stage}/bin"
    cp "${JAR}" "${stage}/lib/maa-framework-java.jar"
    cp "${REPO_ROOT}/README.md" "${stage}/lib/README.md"
    cp -R "${source_root}/bin/." "${stage}/bin/"
    if [[ -d "${source_root}/share/MaaAgentBinary" ]]; then
        mkdir -p "${stage}/share"
        cp -R "${source_root}/share/MaaAgentBinary" "${stage}/share/"
    fi

    archive="${OUT_DIR}/maa-framework-java-${VERSION}-${platform}.zip"
    (
        cd "${stage}"
        zip -qr "${archive}" .
    )
    rm -rf "${stage}"
    trap - EXIT

    echo "packaged ${archive}"
    packed=1
done

if [[ "${packed}" -eq 0 ]]; then
    echo "no MAA-* release directories were found under ${RELEASES_ROOT}" >&2
    exit 1
fi
