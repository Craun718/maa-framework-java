#!/usr/bin/env python3
"""Signature-level FFI parity check between C headers and JNA interfaces."""

from __future__ import annotations

import os
import re
import sys
from pathlib import Path


FORWARD_EXTRAS = {
    "MaaFramework": {"MaaLinuxControllerCreate"},
    "MaaToolkit": {
        "MaaToolkitPortalHelperCreate",
        "MaaToolkitPortalHelperDestroy",
        "MaaToolkitPortalHelperGetPersist",
        "MaaToolkitPortalHelperGetPipeWireFD",
        "MaaToolkitPortalHelperGetPipeWireNodeID",
        "MaaToolkitPortalHelperGetRestoreToken",
        "MaaToolkitPortalHelperOpenStream",
        "MaaToolkitPortalHelperSetPersist",
        "MaaToolkitPortalHelperSetRestoreToken",
    },
}

MODULES = [
    (
        "MaaFramework",
        "include/MaaFramework",
        "MaaFrameworkLibrary.java",
        "MAA_FRAMEWORK_API",
    ),
    (
        "MaaToolkit",
        "include/MaaToolkit",
        "MaaToolkitLibrary.java",
        "MAA_TOOLKIT_API",
    ),
    (
        "MaaAgentClient",
        "include/MaaAgentClient",
        "MaaAgentClientLibrary.java",
        "MAA_AGENT_CLIENT_API",
    ),
    (
        "MaaAgentServer",
        "include/MaaAgentServer",
        "MaaAgentServerLibrary.java",
        "MAA_AGENT_SERVER_API",
    ),
]


def main() -> int:
    script_dir = Path(__file__).resolve().parent
    repo_root = script_dir.parent
    source_root = Path(sys.argv[1] if len(sys.argv) > 1 else os.environ.get("MAA_FRAMEWORK_SOURCE", ""))
    if not source_root.is_dir():
        print(f"usage: {Path(__file__).name} <MaaFramework source or release root>", file=sys.stderr)
        return 2

    java_dir = repo_root / "lib/src/main/java/io/github/craun718/maafw"
    aliases = c_typedef_aliases(source_root / "include")
    failed = False

    for name, header_rel, java_name, macro in MODULES:
        expected = c_signatures(source_root / header_rel, aliases, macro)
        actual = java_signatures(java_dir / java_name)
        missing = sorted(expected.keys() - actual.keys())
        extra = sorted(actual.keys() - expected.keys() - FORWARD_EXTRAS.get(name, set()))
        mismatches = [
            f"{fn} expected={expected[fn]} java={actual.get(fn)}"
            for fn in sorted(expected.keys() & actual.keys())
            if not expected[fn].matches(actual[fn])
        ]

        print(f"== {name} ==")
        print(f"header functions: {len(expected)}")
        print(f"java functions: {len(actual)}")
        if not missing and not extra and not mismatches:
            print("signature match: yes")
            continue

        failed = True
        if missing:
            print("missing:")
            print("\n".join(missing))
        if extra:
            print("extra:")
            print("\n".join(extra))
        if mismatches:
            print("signature mismatch:")
            print("\n".join(mismatches))

    if failed:
        print("FFI signature mismatch", file=sys.stderr)
        return 1
    print("FFI signature parity OK")
    return 0


def c_signatures(header_dir: Path, aliases: dict[str, str], macro: str) -> dict[str, "Signature"]:
    signatures: dict[str, Signature] = {}
    api_re = re.compile(re.escape(macro) + r"\s*(.*)$", re.S)
    decl_re = re.compile(r"^\s*(.*?)\b(Maa[A-Za-z0-9_]+)\s*\((.*)\)\s*$", re.S)

    for header in sorted(header_dir.rglob("*.h")):
        content = remove_comments(header.read_text(encoding="utf-8"))
        for raw_statement in content.split(";"):
            statement = normalize(raw_statement.replace("MAA_DEPRECATED", " "))
            if macro not in statement:
                continue
            api_match = api_re.search(statement)
            if not api_match:
                continue
            decl_match = decl_re.match(normalize(api_match.group(1)))
            if not decl_match:
                continue
            return_type = normalize(decl_match.group(1))
            name = decl_match.group(2)
            params = decl_match.group(3)
            signature = Signature(
                c_param_types(params, aliases),
                c_return_types(return_type, aliases),
            )
            previous = signatures.get(name)
            if previous is not None:
                raise ValueError(f"{name} is declared more than once in C headers")
            signatures[name] = signature
    return signatures


def java_signatures(java_file: Path) -> dict[str, "Signature"]:
    content = remove_comments(java_file.read_text(encoding="utf-8"))
    body_start = content.find("{")
    if body_start >= 0:
        content = content[body_start + 1 :]
    decl_re = re.compile(r"^\s*(.+?)\s+(Maa[A-Za-z0-9_]+)\s*\((.*)\)\s*$", re.S)
    signatures: dict[str, Signature] = {}
    for raw_statement in content.split(";"):
        statement = normalize(raw_statement)
        if "Maa" not in statement:
            continue
        match = decl_re.match(statement)
        if not match:
            continue
        return_type = normalize(match.group(1))
        name = match.group(2)
        params = match.group(3)
        signature = Signature(
            [java_type(strip_param_name(p)) for p in split_top_level(params)],
            java_return(return_type),
        )
        previous = signatures.get(name)
        if previous is not None:
            raise ValueError(f"{name} is declared more than once in Java")
        signatures[name] = signature
    return signatures


def c_typedef_aliases(include_root: Path) -> dict[str, str]:
    aliases: dict[str, str] = {}
    typedef_re = re.compile(r"\btypedef\s+(.*?)\s+([A-Za-z_][A-Za-z0-9_]*)\s*;", re.S)
    for header in sorted(include_root.rglob("*.h")):
        for match in typedef_re.finditer(remove_comments(header.read_text(encoding="utf-8"))):
            rhs = normalize(match.group(1))
            lhs = match.group(2)
            if any(token in rhs for token in ("(", "{", "}", "[")):
                continue
            aliases[lhs] = rhs
    return aliases


def c_param_types(raw: str, aliases: dict[str, str]) -> list[str]:
    raw = normalize(raw)
    if not raw or raw == "void":
        return []
    return [normalize_c_type(strip_param_name(part), aliases) for part in split_top_level(raw)]


def c_return_types(raw: str, aliases: dict[str, str]) -> list[str]:
    normalized = normalize_c_type(raw, aliases)
    return [] if normalized == "void" else [normalized]


def java_return(raw: str) -> list[str]:
    normalized = java_type(raw)
    return [] if normalized == "void" else [normalized]


def java_type(raw: str) -> str:
    if raw in {"void", "byte", "short", "int", "long"}:
        return raw
    if raw == "String":
        return "text"
    if raw == "byte[]":
        return "bytes"
    if raw == "Pointer" or raw.endswith("ByReference"):
        return "ptr"
    if raw.endswith("Callback"):
        return "callback"
    if raw.endswith("Callbacks"):
        return "ptr"
    return "named:" + raw


def normalize_c_type(raw: str, aliases: dict[str, str]) -> str:
    value = normalize(raw.replace("MAA_CALL", " "))
    pointer_count = value.count("*")
    base = normalize(value.replace("const", " ").replace("volatile", " ").replace("*", " "))
    if base == "MaaBool" and pointer_count == 0:
        return "byte"

    for _ in range(16):
        alias = aliases.get(base)
        if alias is None:
            break
        pointer_count += alias.count("*")
        base = normalize(alias.replace("const", " ").replace("volatile", " ").replace("*", " "))

    if base == "char" and pointer_count > 0:
        return "text"
    if pointer_count > 0:
        return "ptr"
    if base.endswith("Callback"):
        return "callback"
    if base in {"void"}:
        return "void"
    if base in {"bool", "uint8_t", "int8_t"}:
        return "byte"
    if base in {"uint16_t", "int16_t"}:
        return "short"
    if base in {"int", "int32_t", "uint32_t"}:
        return "int"
    if base in {"int64_t", "uint64_t", "size_t"}:
        return "long"
    if base.startswith("struct "):
        return "ptr"
    return "named:" + base


def strip_param_name(raw: str) -> str:
    normalized = normalize(raw)
    match = re.match(r"^(.+?)\s+([A-Za-z_][A-Za-z0-9_]*)$", normalized)
    return match.group(1) if match else normalized


def split_top_level(raw: str) -> list[str]:
    parts = []
    current = []
    depth = 0
    for ch in raw:
        if ch == "(":
            depth += 1
        elif ch == ")":
            depth = max(0, depth - 1)
        if ch == "," and depth == 0:
            part = "".join(current).strip()
            if part:
                parts.append(part)
            current = []
        else:
            current.append(ch)
    tail = "".join(current).strip()
    if tail:
        parts.append(tail)
    return parts


def remove_comments(content: str) -> str:
    content = re.sub(r"(?s)/\*.*?\*/", " ", content)
    return re.sub(r"(?m)//.*$", " ", content)


def normalize(value: str) -> str:
    return re.sub(r"\s+", " ", value).strip()


class Signature:
    def __init__(self, params: list[str], returns: list[str]) -> None:
        self.params = params
        self.returns = returns

    def matches(self, other: "Signature") -> bool:
        return (
            len(self.params) == len(other.params)
            and len(self.returns) == len(other.returns)
            and all(_compatible(c, j) for c, j in zip(self.params, other.params))
            and all(_compatible(c, j) for c, j in zip(self.returns, other.returns))
        )

    def __repr__(self) -> str:
        return f"params={self.params}, returns={self.returns}"


def _compatible(c_type: str, java_type: str) -> bool:
    return c_type == java_type or (c_type == "text" and java_type == "bytes")


if __name__ == "__main__":
    sys.exit(main())
