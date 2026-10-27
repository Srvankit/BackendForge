#!/usr/bin/env bash
# ==============================================================================
# BackendForge Release ZIP Package Auditor (POSIX / Bash)
# ==============================================================================
# Audits a candidate release ZIP archive before commercial distribution.
# Verifies:
# 1. No secrets, .env files, private keys, or credential files.
# 2. No .git/ internal metadata, target/ build outputs, crash dumps, or IDE settings.
# 3. No maintainer-only files (scripts/, docs/, .github/, .gitattributes).
# 4. Verified single top-level product directory prefix (e.g. BackendForge-v1.0.1/).
# 5. Complete presence of required commercial starter-kit documents.
# 6. Generates SHA-256 checksum for distribution record.
# ==============================================================================

set -euo pipefail

ZIP_PATH=""

print_usage() {
    cat <<EOF
Usage: $0 <path-to-zip>

Example:
  $0 target/dist/BackendForge-v1.0.1.zip
EOF
}

if [[ $# -lt 1 ]]; then
    echo "ERROR: Missing required ZIP file path." >&2
    print_usage
    exit 1
fi

ZIP_PATH="$1"

if [[ ! -f "$ZIP_PATH" ]]; then
    echo "ERROR: Specified ZIP archive does not exist: $ZIP_PATH" >&2
    exit 1
fi

echo "============================================================"
echo "BackendForge Release ZIP Package Auditor (POSIX/Bash)"
echo "============================================================"

FILE_SIZE=$(wc -c < "$ZIP_PATH" | tr -d ' ')
if command -v sha256sum >/dev/null 2>&1; then
    SHA256_VAL=$(sha256sum "$ZIP_PATH" | awk '{print $1}')
elif command -v shasum >/dev/null 2>&1; then
    SHA256_VAL=$(shasum -a 256 "$ZIP_PATH" | awk '{print $1}')
else
    SHA256_VAL="N/A"
fi

echo "Auditing File : $ZIP_PATH"
echo "Archive Size  : $FILE_SIZE bytes"
echo "SHA-256 Hash  : $SHA256_VAL"
echo ""

if ! command -v unzip >/dev/null 2>&1; then
    echo "ERROR: 'unzip' utility is required to inspect the archive." >&2
    exit 1
fi

ENTRIES=$(unzip -Z -1 "$ZIP_PATH")
ENTRY_COUNT=$(echo "$ENTRIES" | wc -l | tr -d ' ')
echo "Total ZIP Entries: $ENTRY_COUNT"

# 1. Forbidden patterns (including maintainer-only files)
FORBIDDEN_PATTERNS=(
    '(\.env$|\.env\.)'
    '\.(key|pem|pkcs12|p12)$'
    'id_rsa'
    '(^|/)\.git(/|$)'
    '(^|/)target(/|$)'
    '\.class$'
    '\.jar$'
    '\.war$'
    '(^|/)(\.idea|\.vscode)(/|$)'
    '\.(iml|code-workspace)$'
    '(^|/)(\.DS_Store|Thumbs\.db)$'
    '(hs_err_pid|replay_pid|\.log$|spring\.log$)'
    '(^|/)scripts(/|$)'
    '(^|/)docs(/|$)'
    '(^|/)\.github(/|$)'
    '(^|/)\.gitattributes$'
)

DETECTED_FORBIDDEN=()
while IFS= read -r entry; do
    # Allow .env.example
    if echo "$entry" | grep -Eq '(\.env\.example)$'; then
        continue
    fi
    for pat in "${FORBIDDEN_PATTERNS[@]}"; do
        if echo "$entry" | grep -Eq "$pat"; then
            DETECTED_FORBIDDEN+=("$entry")
            break
        fi
    done
done <<< "$ENTRIES"

# 2. Check top-level directory prefix
TOP_LEVEL_DIRS=()
HAS_PREFIX_VIOLATION=false
while IFS= read -r entry; do
    if [[ "$entry" != */* ]]; then
        HAS_PREFIX_VIOLATION=true
    else
        prefix="${entry%%/*}"
        if [[ ! " ${TOP_LEVEL_DIRS[*]:-} " =~ " ${prefix} " ]]; then
            TOP_LEVEL_DIRS+=("$prefix")
        fi
    fi
done <<< "$ENTRIES"

if [[ ${#TOP_LEVEL_DIRS[@]} -ne 1 || "$HAS_PREFIX_VIOLATION" == "true" ]]; then
    HAS_PREFIX_VIOLATION=true
fi

# 3. Required starter-kit basenames
REQUIRED_BASENAMES=(
    "LICENSE.md"
    "THIRD-PARTY-NOTICES.md"
    "README.md"
    "pom.xml"
    ".env.example"
    "compose.yaml"
    "Dockerfile"
    "mvnw"
    "mvnw.cmd"
    "maven-wrapper.properties"
    "application.yml"
    "UserController.java"
    "UserService.java"
    "SecurityConfig.java"
)

MISSING_REQUIRED=()
for req in "${REQUIRED_BASENAMES[@]}"; do
    if ! echo "$ENTRIES" | grep -Eq "(^|/)${req}$"; then
        MISSING_REQUIRED+=("$req")
    fi
done

echo ""
echo "Audit Findings:"

HAS_VIOLATIONS=false

if [[ "$HAS_PREFIX_VIOLATION" == "true" ]]; then
    HAS_VIOLATIONS=true
    echo "[FAIL] Root prefix violation: Archive does not contain exactly one top-level directory prefix." >&2
    echo "  Detected Top-Level Roots: ${TOP_LEVEL_DIRS[*]:-none}" >&2
else
    echo "[PASS] Verified single top-level product directory: ${TOP_LEVEL_DIRS[0]}/"
fi

if [[ ${#DETECTED_FORBIDDEN[@]} -gt 0 ]]; then
    HAS_VIOLATIONS=true
    echo "[FAIL] FORBIDDEN, sensitive, or maintainer-only items detected in archive:" >&2
    for f in "${DETECTED_FORBIDDEN[@]}"; do
        echo "  - $f" >&2
    done
else
    echo "[PASS] No secrets, .env files, git metadata, build outputs, crash dumps, or maintainer files found."
fi

if [[ ${#MISSING_REQUIRED[@]} -gt 0 ]]; then
    HAS_VIOLATIONS=true
    echo ""
    echo "[FAIL] Missing required starter-kit files in archive:" >&2
    for m in "${MISSING_REQUIRED[@]}"; do
        echo "  - Missing: $m" >&2
    done
else
    echo "[PASS] All essential starter-kit files (LICENSE, notices, README, POM, wrapper, src) are present."
fi

echo ""
if [[ "$HAS_VIOLATIONS" == "true" ]]; then
    echo "============================================================"
    echo "AUDIT RESULT: REJECTED (Package is NOT safe or complete for distribution)"
    echo "============================================================"
    exit 1
else
    echo "============================================================"
    echo "AUDIT RESULT: VERIFIED (Package is safe and complete for distribution)"
    echo "============================================================"
    exit 0
fi
