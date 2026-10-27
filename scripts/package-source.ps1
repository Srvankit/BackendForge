<#
.SYNOPSIS
    Safely packages BackendForge source code for commercial starter-kit distribution.

.DESCRIPTION
    Creates a clean release ZIP archive from an explicitly specified Git commit or tag.
    Guarantees that:
    1. Only committed files from the specified Git tree are included (via git archive).
    2. Local untracked files, .env, target/ build outputs, crash dumps, and IDE configurations
       can never be accidentally packaged.
    3. Required legal and distribution files (LICENSE.md, THIRD-PARTY-NOTICES.md, README.md,
       pom.xml, .env.example, mvnw, compose.yaml, Dockerfile) are verified to exist in the target ref.
    4. Maintainer-only files (scripts/, docs/, .github/, .gitattributes) are strictly excluded.
    5. Produces a ZIP with a single top-level directory prefix (e.g. BackendForge-v1.0.1/).
    6. Verifies the generated archive manifest and writes a SHA-256 checksum file.
    7. Distinguishes baseline v1.0.0 from subsequent commits and prevents packaging dirty states.

.PARAMETER ReleaseRef
    The Git reference (tag, branch, or commit SHA) to package. Example: 'v1.0.1' or a reviewed commit SHA.

.PARAMETER OutputDir
    Destination directory for the generated ZIP. Defaults to 'target/dist' (ignored by Git).

.PARAMETER DryRun
    When specified, inspects the reference, verifies requirements, and displays the packaging manifest
    WITHOUT creating any ZIP archive.

.PARAMETER AllowDirty
    Allows running the script while the local working tree has uncommitted changes.
    Uncommitted changes are NEVER packaged; only the committed tree at ReleaseRef is used.

.PARAMETER Prefix
    Internal archive directory prefix. Defaults to 'BackendForge-<ref>/'.

.EXAMPLE
    .\scripts\package-source.ps1 -ReleaseRef "v1.0.0" -DryRun -AllowDirty
    Validates v1.0.0 contents and shows what would be packaged without creating an archive.

.EXAMPLE
    .\scripts\package-source.ps1 -ReleaseRef "v1.0.1"
    Packages the committed v1.0.1 reference into target/dist/BackendForge-v1.0.1.zip.
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0, HelpMessage = "Git commit, tag, or branch to package")]
    [string]$ReleaseRef,

    [Parameter(Mandatory = $false)]
    [string]$OutputDir = "target/dist",

    [Parameter(Mandatory = $false)]
    [switch]$DryRun,

    [Parameter(Mandatory = $false)]
    [switch]$AllowDirty,

    [Parameter(Mandatory = $false)]
    [string]$Prefix = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "BackendForge Safe Source Packaging Tool" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

# 1. Verify Git availability and repository root
try {
    $null = Get-Command git -ErrorAction Stop
}
catch {
    Write-Error "Git executable not found in PATH. Git is required for safe source packaging."
    exit 1
}

$isRepo = & git rev-parse --is-inside-work-tree 2>$null
if ($LASTEXITCODE -ne 0 -or $isRepo -ne "true") {
    Write-Error "Current directory is not inside a valid Git repository."
    exit 1
}

# 2. Resolve target Git reference
$commitSha = & git rev-parse --verify "${ReleaseRef}^{commit}" 2>$null
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($commitSha)) {
    Write-Error "Specified release reference '$ReleaseRef' does not resolve to a valid commit or tag in this repository."
    exit 1
}

$commitDate = & git show -s --format=%ci $commitSha
$commitAuthor = & git show -s --format="%an <%ae>" $commitSha
$commitSubject = & git show -s --format=%s $commitSha

Write-Host "Target Reference  : $ReleaseRef" -ForegroundColor Green
Write-Host "Resolved Commit   : $commitSha" -ForegroundColor Green
Write-Host "Commit Date       : $commitDate"
Write-Host "Commit Author     : $commitAuthor"
Write-Host "Commit Subject    : $commitSubject"
Write-Host ""

# 3. Working tree cleanliness check
$porcelainStatus = & git status --porcelain=v1
$isDirty = -not [string]::IsNullOrWhiteSpace($porcelainStatus)

if ($isDirty) {
    Write-Host "[WARN] Working directory contains uncommitted or untracked changes." -ForegroundColor Yellow
    if (-not $AllowDirty -and -not $DryRun) {
        Write-Error @"
Working tree is dirty. Release packaging requires a clean repository state to ensure repeatability.
If you are inspecting a historical commit or testing, use the -DryRun switch.
If you intentionally want to package committed reference '$ReleaseRef' while keeping uncommitted
workspace changes, specify -AllowDirty (note: uncommitted changes will NOT be packaged).
"@
        exit 1
    }
    elseif ($AllowDirty) {
        Write-Host "[NOTICE] Proceeding with -AllowDirty. Uncommitted working files will strictly be EXCLUDED." -ForegroundColor Yellow
    }
}

# 4. Fetch tracked files list from the target Git commit tree
$trackedFiles = & git ls-tree -r --name-only $commitSha
if ($LASTEXITCODE -ne 0 -or $trackedFiles.Count -eq 0) {
    Write-Error "Failed to retrieve file tree for commit $commitSha."
    exit 1
}

# 5. Customer manifest definitions
$customerAllowedRoots = @(
    "src",
    ".mvn",
    "mvnw",
    "mvnw.cmd",
    "pom.xml",
    "Dockerfile",
    "compose.yaml",
    ".gitignore",
    ".dockerignore",
    ".env.example",
    "README.md",
    "LICENSE.md",
    "THIRD-PARTY-NOTICES.md"
)

$maintainerOnlyPatterns = @(
    "^scripts(/|$)",
    "^docs(/|$)",
    "^\.github(/|$)",
    "^\.gitattributes$"
)

# 6. Required files audit for commercial source starter kit
$requiredFiles = @(
    "LICENSE.md",
    "THIRD-PARTY-NOTICES.md",
    "README.md",
    "pom.xml",
    ".env.example",
    "compose.yaml",
    "Dockerfile",
    "mvnw",
    "mvnw.cmd",
    ".mvn/wrapper/maven-wrapper.properties",
    "src/main/resources/application.yml",
    "src/main/java/com/thecreatorco/backendforge/user/UserController.java",
    "src/main/java/com/thecreatorco/backendforge/user/UserService.java",
    "src/main/java/com/thecreatorco/backendforge/security/SecurityConfig.java"
)

$missingRequired = @()
foreach ($req in $requiredFiles) {
    if ($trackedFiles -notcontains $req) {
        $missingRequired += $req
    }
}

# 7. Forbidden / sensitive files audit against the commit tree
$forbiddenPatterns = @(
    "^\.env$",
    "^\.env\.(?!example).*$",
    ".*\.key$",
    ".*\.pem$",
    ".*\.p12$",
    ".*\.pkcs12$",
    ".*id_rsa.*",
    "^target/.*",
    ".*\.class$",
    ".*\.jar$",
    ".*\.war$",
    ".*\.log$",
    ".*hs_err_pid.*",
    ".*replay_pid.*",
    "^\.idea/.*",
    "^\.vscode/.*",
    ".*\.iml$",
    ".*\.code-workspace$",
    ".*\.DS_Store$",
    ".*Thumbs\.db$"
)

$foundForbidden = @()
foreach ($file in $trackedFiles) {
    foreach ($pat in $forbiddenPatterns) {
        if ($file -match $pat) {
            $foundForbidden += $file
            break
        }
    }
}

# 8. Filter commit files into Customer Release Manifest
$customerFiles = @()
foreach ($file in $trackedFiles) {
    # Check if file belongs to any allowed root
    $isAllowed = $false
    foreach ($root in $customerAllowedRoots) {
        if ($file -eq $root -or $file.StartsWith("$root/")) {
            $isAllowed = $true
            break
        }
    }

    # Check if file matches maintainer-only pattern
    $isMaintainer = $false
    foreach ($mPat in $maintainerOnlyPatterns) {
        if ($file -match $mPat) {
            $isMaintainer = $true
            break
        }
    }

    if ($isAllowed -and -not $isMaintainer) {
        $customerFiles += $file
    }
}

# 9. Check POM version in the selected commit
$pomXmlRaw = & git show "${commitSha}:pom.xml" 2>$null
$pomVersion = "UNKNOWN"
if ($pomXmlRaw -match "<version>([^<]+)</version>") {
    $versionMatches = [regex]::Matches($pomXmlRaw, "<version>([^<]+)</version>")
    if ($versionMatches.Count -ge 2) {
        $pomVersion = $versionMatches[1].Groups[1].Value
    }
    elseif ($versionMatches.Count -ge 1) {
        $pomVersion = $versionMatches[0].Groups[1].Value
    }
}

Write-Host "POM Version in Ref: $pomVersion" -ForegroundColor Yellow

# Evaluate validation findings
$hasErrors = $false

if ($missingRequired.Count -gt 0) {
    Write-Host "`n[ERROR] Required distribution files are MISSING in reference '$ReleaseRef' ($commitSha):" -ForegroundColor Red
    foreach ($m in $missingRequired) {
        Write-Host "  - Missing: $m" -ForegroundColor Red
    }
    Write-Host @"
Explanation:
The reference '$ReleaseRef' lacks required commercial distribution files.
A commercial source starter kit release requires all required files
(LICENSE.md, THIRD-PARTY-NOTICES.md, Maven Wrapper, application code) to be committed.
"@ -ForegroundColor Yellow
    $hasErrors = $true
}

if ($foundForbidden.Count -gt 0) {
    Write-Host "`n[SECURITY ALERT] Target commit tree contains FORBIDDEN or sensitive files:" -ForegroundColor Red
    foreach ($f in $foundForbidden) {
        Write-Host "  - Forbidden: $f" -ForegroundColor Red
    }
    $hasErrors = $true
}

if ($hasErrors -and -not $DryRun) {
    Write-Error "Packaging aborted due to validation errors. Fix issues in git reference before packaging."
    exit 1
}

# 10. Set prefix and output path
$cleanRefName = $ReleaseRef -replace '[\\/:]', '-'
if ([string]::IsNullOrWhiteSpace($Prefix)) {
    $Prefix = "BackendForge-$cleanRefName/"
}
if (-not $Prefix.EndsWith("/")) {
    $Prefix = "$Prefix/"
}

$archiveName = "BackendForge-$cleanRefName.zip"
$archivePath = Join-Path $OutputDir $archiveName
$checksumPath = "$archivePath.sha256"

Write-Host "`nDistribution Pre-Check Summary:" -ForegroundColor Cyan
Write-Host "  Total Files in Commit Tree : $($trackedFiles.Count)"
Write-Host "  Customer Files to Package  : $($customerFiles.Count)"
Write-Host "  Excluded Maintainer Files  : $($trackedFiles.Count - $customerFiles.Count)"
Write-Host "  Missing Required Files     : $($missingRequired.Count)"
Write-Host "  Forbidden Files Found      : $($foundForbidden.Count)"
Write-Host "  Target Archive Path        : $archivePath"
Write-Host "  Archive Internal Prefix    : $Prefix"

# 11. Dry-run mode handling
if ($DryRun) {
    Write-Host "`n============================================================" -ForegroundColor Yellow
    Write-Host "DRY-RUN MODE: No release archive was created." -ForegroundColor Yellow
    Write-Host "============================================================" -ForegroundColor Yellow
    Write-Host "The following $($customerFiles.Count) customer files would be packaged from commit ${commitSha}:"
    foreach ($f in $customerFiles) {
        Write-Host "  $Prefix$f"
    }
    if ($hasErrors) {
        Write-Host "`n[DRY-RUN RESULT] Validation FAILED for reference '$ReleaseRef'. See errors above." -ForegroundColor Red
        exit 1
    } else {
        Write-Host "`n[DRY-RUN RESULT] All validation checks PASSED for reference '$ReleaseRef'." -ForegroundColor Green
        exit 0
    }
}

# 12. Execute git archive using explicit customer-allowed paths
Write-Host "`nCreating release archive using 'git archive'..." -ForegroundColor Cyan

if (-not (Test-Path $OutputDir)) {
    $null = New-Item -ItemType Directory -Path $OutputDir -Force
}

# Use customer allowed roots to prevent maintainer-only files from being packaged
& git archive --format=zip --prefix="$Prefix" -o "$archivePath" "$commitSha" -- $customerAllowedRoots
if ($LASTEXITCODE -ne 0 -or -not (Test-Path $archivePath)) {
    Write-Error "Failed to create archive '$archivePath'."
    exit 1
}

# 13. Post-packaging verification
$fileSize = (Get-Item $archivePath).Length
$hashResult = Get-FileHash -Path $archivePath -Algorithm SHA256

# Write SHA-256 checksum file
$checksumContent = "$($hashResult.Hash) *$archiveName"
[System.IO.File]::WriteAllText($checksumPath, "$checksumContent`n")

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($archivePath)
$entries = $zip.Entries
$entryCount = $entries.Count

# Validate all entries in the ZIP
$zipViolations = @()
$foundRequiredInZip = @{}
foreach ($req in $requiredFiles) {
    $foundRequiredInZip[$req] = $false
}

foreach ($entry in $entries) {
    $entryName = $entry.FullName
    if (-not $entryName.StartsWith($Prefix)) {
        $zipViolations += "Entry does not start with expected prefix '$Prefix': $entryName"
    }
    $relativeName = $entryName.Substring($Prefix.Length)

    # Check forbidden
    foreach ($pat in $forbiddenPatterns) {
        if ($relativeName -match $pat) {
            $zipViolations += "Forbidden file found in archive: $entryName"
            break
        }
    }

    # Check maintainer-only
    foreach ($mPat in $maintainerOnlyPatterns) {
        if ($relativeName -match $mPat) {
            $zipViolations += "Maintainer-only file found in archive: $entryName"
            break
        }
    }

    # Check required
    if ($foundRequiredInZip.ContainsKey($relativeName)) {
        $foundRequiredInZip[$relativeName] = $true
    }
}
$zip.Dispose()

foreach ($req in $requiredFiles) {
    if (-not $foundRequiredInZip[$req]) {
        $zipViolations += "Required file missing from archive: $req"
    }
}

if ($zipViolations.Count -gt 0) {
    Remove-Item -Path $archivePath -Force -ErrorAction SilentlyContinue
    Remove-Item -Path $checksumPath -Force -ErrorAction SilentlyContinue
    Write-Host "`n[FATAL] ZIP validation failed. The archive was deleted:" -ForegroundColor Red
    foreach ($v in $zipViolations) {
        Write-Host "  - $v" -ForegroundColor Red
    }
    Write-Error "Package verification failed."
    exit 1
}

Write-Host "============================================================" -ForegroundColor Green
Write-Host "PACKAGE CREATION & VERIFICATION SUCCESSFUL" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host "Archive File : $archivePath"
Write-Host "Archive Size : $fileSize bytes ($([math]::Round($fileSize / 1KB, 2)) KB)"
Write-Host "Entry Count  : $entryCount entries"
Write-Host "SHA-256 Hash : $($hashResult.Hash)"
Write-Host "Checksum File: $checksumPath"
Write-Host "============================================================" -ForegroundColor Green
exit 0
