#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SOURCE_ROOT="${1:-${MAA_FRAMEWORK_SOURCE:-}}"

if [[ -z "${SOURCE_ROOT}" ]]; then
    echo "usage: $0 <MaaFramework source root>" >&2
    echo "       MAA_FRAMEWORK_SOURCE=<path> $0" >&2
    exit 2
fi

exec python3 "${SCRIPT_DIR}/check-ffi-signatures.py" "${SOURCE_ROOT}"
