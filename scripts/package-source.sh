#!/usr/bin/env bash
# ==============================================================================
# BackendForge Safe Source Packaging Script (POSIX / Bash)
# ==============================================================================
# Safely packages BackendForge source code for commercial starter-kit distribution.
# Guarantees:
# 1. Archives strictly from an explicitly specified Git commit or tag.
# 2. Never includes local untracked files, .env secrets, target/ build outputs,
#    crash dumps, or IDE configurations.
# 3. Verifies that required distribution files (LICENSE.md, THIRD-PARTY-NOTICES.md,
#    README.md, pom.xml, .env.example, mvnw, compose.yaml, Dockerfile) are in the ref.
# 4. Strictly excludes maintainer-only files (scripts/, docs/, .github/, .gitattributes).
# 5. Produces an archive with a single top-level directory prefix.
# 6. Verifies archive manifest integrity and writes a SHA-256 checksum file.
# 7. Supports --dry-run to audit without creating an archive.
# ==============================================================================

set -euo pipefail

RELEASE_REF=""
OUTPUT_DIR="target/dist"
DRY_RUN=false
ALLOW_DIRTY=false
PREFIX=""

print_usage() {
    cat <<EOF
Usage: $0 --ref <git-ref> [options]

Required Arguments:
  -r, --ref <git-ref>       Git commit SHA, tag, or branch to package (e.g. v1.0.1, commit-sha)

Optional Arguments:
  -o, --output-dir <dir>    Destination directory for ZIP (default: target/dist)
  -d, --dry-run             Audit target reference without creating an archive
  --allow-dirty             Proceed even if local workspace has uncommitted changes
  -p, --prefix <prefix>     Internal archive directory prefix (default: BackendForge-<ref>/)
  -h, --help                Show this help message

Examples:
  $0 --ref v1.0.0 --dry-run --allow-dirty
  $0 --ref <reviewed-commit-sha> --output-dir releases
EOF
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        -r|--ref)
            RELEASE_REF="$2"
            shift 2
            ;;
        -o|--output-dir)
            OUTPUT_DIR="$2"
            shift 2
            ;;
        -d|--dry-run)
            DRY_RUN=true
            shift
            ;;
        --allow-dirty)
            ALLOW_DIRTY=true
            shift
            ;;
        -p|--prefix)
            PREFIX="$2"
            shift 2
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            echo "Unknown argument: $1" >&2
            print_usage
            exit 1
            ;;
    esac
done

if [[ -z "$RELEASE_REF" ]]; then
    echo "ERROR: --ref argument is mandatory." >&2
    print_usage
    exit 1
fi

echo "============================================================"
echo "BackendForge Safe Source Packaging Tool (POSIX/Bash)"
echo "============================================================"

# 1. Verify Git and repository
if ! command -v git >/dev/null 2>&1; then
    echo "ERROR: 'git' is not installed or not in PATH." >&2
    exit 1
fi

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
    echo "ERROR: Current directory is not a Git repository." >&2
    exit 1
fi

# 2. Resolve commit SHA
COMMIT_SHA=$(git rev-parse --verify "${RELEASE_REF}^{commit}" 2>/dev/null || true)
if [[ -z "$COMMIT_SHA" ]]; then
    echo "ERROR: Reference '$RELEASE_REF' does not resolve to a valid commit or tag." >&2
    exit 1
fi

COMMIT_DATE=$(git show -s --format=%ci "$COMMIT_SHA")
COMMIT_AUTHOR=$(git show -s --format="%an <%ae>" "$COMMIT_SHA")
COMMIT_SUBJECT=$(git show -s --format=%s "$COMMIT_SHA")

echo "Target Reference  : $RELEASE_REF"
echo "Resolved Commit   : $COMMIT_SHA"
echo "Commit Date       : $COMMIT_DATE"
echo "Commit Author     : $COMMIT_AUTHOR"
echo "Commit Subject    : $COMMIT_SUBJECT"
echo ""

# 3. Cleanliness check
PORCELAIN_STATUS=$(git status --porcelain=v1)
if [[ -n "$PORCELAIN_STATUS" ]]; then
    echo "[WARN] Working directory contains uncommitted or untracked changes." >&2
    if [[ "$ALLOW_DIRTY" != "true" && "$DRY_RUN" != "true" ]]; then
        echo "ERROR: Working tree is dirty. Release packaging requires a clean workspace." >&2
        echo "Use --dry-run to inspect, or --allow-dirty to force packaging the committed ref." >&2
        exit 1
    elif [[ "$ALLOW_DIRTY" == "true" ]]; then
        echo "[NOTICE] Proceeding with --allow-dirty. Uncommitted files will NOT be included."
    fi
fi

# 4. Fetch tracked files in target commit
TRACKED_FILES=$(git ls-tree -r --name-only "$COMMIT_SHA")
FILE_COUNT=$(echo "$TRACKED_FILES" | wc -l | tr -d ' ')

# 5. Customer manifest definitions
CUSTOMER_ALLOWED_ROOTS=(
    "src"
    ".mvn"
    "mvnw"
    "mvnw.cmd"
    "pom.xml"
    "Dockerfile"
    "compose.yaml"
    ".gitignore"
    ".dockerignore"
    ".env.example"
    "README.md"
    "LICENSE.md"
    "THIRD-PARTY-NOTICES.md"
)

MAINTAINER_ONLY_PATTERNS=(
    '^scripts(/|$)'
    '^docs(/|$)'
    '^\.github(/|$)'
    '^\.gitattributes$'
)

# 6. Required files audit
REQUIRED_FILES=(
    "LICENSE.md"
    "THIRD-PARTY-NOTICES.md"
    "README.md"
    "pom.xml"
    ".env.example"
    "compose.yaml"
    "Dockerfile"
    "mvnw"
    "mvnw.cmd"
    ".mvn/wrapper/maven-wrapper.properties"
    "src/main/resources/application.yml"
    "src/main/java/com/thecreatorco/backendforge/user/UserController.java"
    "src/main/java/com/thecreatorco/backendforge/user/UserService.java"
    "src/main/java/com/thecreatorco/backendforge/security/SecurityConfig.java"
)

MISSING_REQUIRED=()
for req in "${REQUIRED_FILES[@]}"; do
    if ! echo "$TRACKED_FILES" | grep -Fxq "$req"; then
        MISSING_REQUIRED+=("$req")
    fi
done

# 7. Forbidden patterns audit
FORBIDDEN_PATTERNS=(
    '^\.env$'
    '^\.env\..*'
    '.*\.key$'
    '.*\.pem$'
    '.*\.p12$'
    '.*\.pkcs12$'
    '.*id_rsa.*'
    '^target/.*'
    '.*\.class$'
    '.*\.jar$'
    '.*\.war$'
    '.*\.log$'
    '.*hs_err_pid.*'
    '.*replay_pid.*'
    '^\.idea/.*'
    '^\.vscode/.*'
    '.*\.iml$'
    '.*\.code-workspace$'
    '.*\.DS_Store$'
    '.*Thumbs\.db$'
)

FOUND_FORBIDDEN=()
while IFS= read -r file; do
    # Allow .env.example
    if [[ "$file" == ".env.example" ]]; then
        continue
    fi
    for pat in "${FORBIDDEN_PATTERNS[@]}"; do
        if echo "$file" | grep -Eq "$pat"; then
            FOUND_FORBIDDEN+=("$file")
            break
        fi
    done
done <<< "$TRACKED_FILES"

# 8. Filter commit files into customer manifest
CUSTOMER_FILES=()
while IFS= read -r file; do
    # Check if file belongs to allowed root
    is_allowed=false
    for root in "${CUSTOMER_ALLOWED_ROOTS[@]}"; do
        if [[ "$file" == "$root" || "$file" == "$root"/* ]]; then
            is_allowed=true
            break
        fi
    done

    # Check if file matches maintainer-only pattern
    is_maintainer=false
    for mpat in "${MAINTAINER_ONLY_PATTERNS[@]}"; do
        if echo "$file" | grep -Eq "$mpat"; then
            is_maintainer=true
            break
        fi
    done

    if [[ "$is_allowed" == "true" && "$is_maintainer" == "false" ]]; then
        CUSTOMER_FILES+=("$file")
    fi
done <<< "$TRACKED_FILES"

HAS_ERRORS=false

if [[ ${#MISSING_REQUIRED[@]} -gt 0 ]]; then
    echo ""
    echo "[ERROR] Required distribution files are MISSING in reference '$RELEASE_REF':" >&2
    for m in "${MISSING_REQUIRED[@]}"; do
        echo "  - Missing: $m" >&2
    done
    echo "Explanation: Reference '$RELEASE_REF' lacks required commercial distribution files." >&2
    HAS_ERRORS=true
fi

if [[ ${#FOUND_FORBIDDEN[@]} -gt 0 ]]; then
    echo ""
    echo "[SECURITY ALERT] Target commit tree contains FORBIDDEN or sensitive files:" >&2
    for f in "${FOUND_FORBIDDEN[@]}"; do
        echo "  - Forbidden: $f" >&2
    done
    HAS_ERRORS=true
fi

if [[ "$HAS_ERRORS" == "true" && "$DRY_RUN" != "true" ]]; then
    echo "Packaging aborted due to validation errors." >&2
    exit 1
fi

# 9. Prefix and destination paths
CLEAN_REF=$(echo "$RELEASE_REF" | tr '/\\:' '-')
if [[ -z "$PREFIX" ]]; then
    PREFIX="BackendForge-$CLEAN_REF/"
fi
[[ "$PREFIX" != */ ]] && PREFIX="$PREFIX/"

ARCHIVE_NAME="BackendForge-$CLEAN_REF.zip"
ARCHIVE_PATH="$OUTPUT_DIR/$ARCHIVE_NAME"
CHECKSUM_PATH="$ARCHIVE_PATH.sha256"

echo ""
echo "Distribution Pre-Check Summary:"
echo "  Total Files in Commit Tree : $FILE_COUNT"
echo "  Customer Files to Package  : ${#CUSTOMER_FILES[@]}"
echo "  Missing Required Files     : ${#MISSING_REQUIRED[@]}"
echo "  Forbidden Files Found      : ${#FOUND_FORBIDDEN[@]}"
echo "  Target Archive Path        : $ARCHIVE_PATH"
echo "  Archive Internal Prefix    : $PREFIX"

# 10. Dry-Run Handling
if [[ "$DRY_RUN" == "true" ]]; then
    echo ""
    echo "============================================================"
    echo "DRY-RUN MODE: No release archive was created."
    echo "============================================================"
    echo "The following ${#CUSTOMER_FILES[@]} customer files would be packaged from commit $COMMIT_SHA:"
    for f in "${CUSTOMER_FILES[@]}"; do
        echo "  $PREFIX$f"
    done
    if [[ "$HAS_ERRORS" == "true" ]]; then
        echo ""
        echo "[DRY-RUN RESULT] Validation FAILED for reference '$RELEASE_REF'. See errors above." >&2
        exit 1
    else
        echo ""
        echo "[DRY-RUN RESULT] All validation checks PASSED for reference '$RELEASE_REF'."
        exit 0
    fi
fi

# 11. Execute git archive with explicit customer allowed roots
echo ""
echo "Creating release archive via 'git archive'..."
mkdir -p "$OUTPUT_DIR"
git archive --format=zip --prefix="$PREFIX" -o "$ARCHIVE_PATH" "$COMMIT_SHA" -- "${CUSTOMER_ALLOWED_ROOTS[@]}"

if [[ ! -f "$ARCHIVE_PATH" ]]; then
    echo "ERROR: Failed to create archive '$ARCHIVE_PATH'." >&2
    exit 1
fi

FILE_SIZE=$(wc -c < "$ARCHIVE_PATH" | tr -d ' ')

if command -v sha256sum >/dev/null 2>&1; then
    SHA256_VAL=$(sha256sum "$ARCHIVE_PATH" | awk '{print $1}')
elif command -v shasum >/dev/null 2>&1; then
    SHA256_VAL=$(shasum -a 256 "$ARCHIVE_PATH" | awk '{print $1}')
else
    SHA256_VAL="N/A"
fi

# Write checksum file
echo "$SHA256_VAL *$ARCHIVE_NAME" > "$CHECKSUM_PATH"

# 12. Verify archive entries
if command -v unzip >/dev/null 2>&1; then
    ZIP_ENTRIES=$(unzip -Z -1 "$ARCHIVE_PATH")
    ENTRY_COUNT=$(echo "$ZIP_ENTRIES" | wc -l | tr -d ' ')

    ZIP_VIOLATIONS=()
    while IFS= read -r entry; do
        if [[ "$entry" != "$PREFIX"* ]]; then
            ZIP_VIOLATIONS+=("Entry does not start with prefix $PREFIX: $entry")
        fi
        rel_entry="${entry#$PREFIX}"

        # check forbidden
        if [[ "$rel_entry" != ".env.example" ]]; then
            for pat in "${FORBIDDEN_PATTERNS[@]}"; do
                if echo "$rel_entry" | grep -Eq "$pat"; then
                    ZIP_VIOLATIONS+=("Forbidden file found in archive: $entry")
                    break
                fi
            done
        fi

        # check maintainer
        for mpat in "${MAINTAINER_ONLY_PATTERNS[@]}"; do
            if echo "$rel_entry" | grep -Eq "$mpat"; then
                ZIP_VIOLATIONS+=("Maintainer file found in archive: $entry")
                break
            fi
        done
    done <<< "$ZIP_ENTRIES"

    for req in "${REQUIRED_FILES[@]}"; do
        if ! echo "$ZIP_ENTRIES" | grep -Eq "(^|/)${req}$"; then
            ZIP_VIOLATIONS+=("Required file missing from archive: $req")
        fi
    done

    if [[ ${#ZIP_VIOLATIONS[@]} -gt 0 ]]; then
        rm -f "$ARCHIVE_PATH" "$CHECKSUM_PATH"
        echo "[FATAL] ZIP validation failed. Archive was deleted:" >&2
        for v in "${ZIP_VIOLATIONS[@]}"; do
            echo "  - $v" >&2
        done
        exit 1
    fi
else
    ENTRY_COUNT="N/A (unzip not found)"
fi

echo "============================================================"
echo "PACKAGE CREATION & VERIFICATION SUCCESSFUL"
echo "============================================================"
echo "Archive File : $ARCHIVE_PATH"
echo "Archive Size : $FILE_SIZE bytes"
echo "Entry Count  : $ENTRY_COUNT entries"
echo "SHA-256 Hash : $SHA256_VAL"
echo "Checksum File: $CHECKSUM_PATH"
echo "============================================================"
exit 0
