<#
.SYNOPSIS
    Audits a candidate release ZIP archive before commercial distribution.

.DESCRIPTION
    Scans the internal manifest of a candidate release ZIP to verify:
    1. Zero presence of secrets, .env files, credentials, or private keys (manifest-level check without reading secret values).
    2. Zero presence of .git/ metadata, target/ build outputs, crash dumps, or IDE settings.
    3. Zero presence of maintainer-only files (scripts/, docs/, .github/, .gitattributes).
    4. Exact single top-level product directory prefix (e.g. BackendForge-v1.0.1/).
    5. Complete presence of required commercial starter-kit documents (LICENSE.md, THIRD-PARTY-NOTICES.md,
       README.md, pom.xml, .env.example, compose.yaml, Dockerfile, Maven Wrapper).
    6. Computes cryptographic SHA-256 checksum for distribution record.

.PARAMETER ZipPath
    Path to the ZIP file to verify.

.EXAMPLE
    .\scripts\verify-package.ps1 -ZipPath "target/dist/BackendForge-v1.0.1.zip"
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0, HelpMessage = "Path to the ZIP file to audit")]
    [string]$ZipPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "BackendForge Release ZIP Package Auditor" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

if (-not (Test-Path $ZipPath -PathType Leaf)) {
    Write-Error "Specified ZIP archive file does not exist: $ZipPath"
    exit 1
}

$resolvedPath = (Resolve-Path $ZipPath).Path
$fileInfo = Get-Item $resolvedPath
$fileSize = $fileInfo.Length
$fileHash = Get-FileHash -Path $resolvedPath -Algorithm SHA256

Write-Host "Auditing File : $resolvedPath"
Write-Host "Archive Size  : $fileSize bytes ($([math]::Round($fileSize / 1KB, 2)) KB)"
Write-Host "SHA-256 Hash  : $($fileHash.Hash)"
Write-Host ""

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = $null
$entries = @()

try {
    $zip = [System.IO.Compression.ZipFile]::OpenRead($resolvedPath)
    $entries = $zip.Entries
}
catch {
    Write-Error "Failed to open ZIP archive. File may be corrupted: $_"
    if ($zip) { $zip.Dispose() }
    exit 1
}

$entryCount = $entries.Count
Write-Host "Total ZIP Entries: $entryCount" -ForegroundColor Cyan

# 1. Define forbidden file/path patterns
$forbiddenChecks = @(
    @{ Name = "Secret/Environment file (.env)"; Pattern = '(^|/)\.env($|\..*)'; Exception = '(^|/)\.env\.example$' },
    @{ Name = "Private key or certificate"; Pattern = '\.(key|pem|pkcs12|p12)$'; Exception = $null },
    @{ Name = "SSH private key"; Pattern = '(^|/)id_rsa'; Exception = $null },
    @{ Name = "Git internal repository/metadata"; Pattern = '(^|/)\.git(/|$)'; Exception = $null },
    @{ Name = "Build output directory (target/)"; Pattern = '(^|/)target(/|$)'; Exception = $null },
    @{ Name = "Compiled Java class"; Pattern = '\.class$'; Exception = $null },
    @{ Name = "Compiled JAR binary"; Pattern = '\.jar$'; Exception = $null },
    @{ Name = "Compiled WAR binary"; Pattern = '\.war$'; Exception = $null },
    @{ Name = "IDE configuration (.idea, .vscode)"; Pattern = '(^|/)(\.idea|\.vscode)(/|$)'; Exception = $null },
    @{ Name = "IDE project file (*.iml, *.code-workspace)"; Pattern = '\.(iml|code-workspace)$'; Exception = $null },
    @{ Name = "OS clutter (.DS_Store, Thumbs.db)"; Pattern = '(^|/)(\.DS_Store|Thumbs\.db)$'; Exception = $null },
    @{ Name = "JVM crash dump (hs_err_pid, replay_pid)"; Pattern = '(^|/)(hs_err_pid.*|replay_pid.*)'; Exception = $null },
    @{ Name = "Log files (*.log)"; Pattern = '\.log$'; Exception = $null },
    @{ Name = "Maintainer scripts directory (scripts/)"; Pattern = '(^|/)scripts(/|$)'; Exception = $null },
    @{ Name = "Maintainer docs directory (docs/)"; Pattern = '(^|/)docs(/|$)'; Exception = $null },
    @{ Name = "Maintainer CI directory (.github/)"; Pattern = '(^|/)\.github(/|$)'; Exception = $null },
    @{ Name = "Maintainer Git attributes (.gitattributes)"; Pattern = '(^|/)\.gitattributes$'; Exception = $null }
)

$detectedForbidden = @()

foreach ($entry in $entries) {
    $fullName = $entry.FullName
    foreach ($check in $forbiddenChecks) {
        if ($fullName -match $check.Pattern) {
            if ($check.Exception -and ($fullName -match $check.Exception)) {
                # Permitted exception
                continue
            }
            $detectedForbidden += [PSCustomObject]@{
                Category = $check.Name
                Path     = $fullName
            }
        }
    }
}

# 2. Check top-level directory structure
$topLevelEntries = @{}
$entriesWithoutPrefix = @()
foreach ($entry in $entries) {
    $parts = $entry.FullName.Split('/')
    if ($parts.Length -eq 1 -or [string]::IsNullOrWhiteSpace($parts[0])) {
        $entriesWithoutPrefix += $entry.FullName
    }
    else {
        $topLevelEntries[$parts[0]] = $true
    }
}

$hasPrefixIssue = $false
if ($topLevelEntries.Keys.Count -ne 1 -or $entriesWithoutPrefix.Count -gt 0) {
    $hasPrefixIssue = $true
}

# 3. Required starter-kit items (checked against entry relative paths regardless of prefix)
$requiredBasenames = @(
    "LICENSE.md",
    "THIRD-PARTY-NOTICES.md",
    "README.md",
    "pom.xml",
    ".env.example",
    "compose.yaml",
    "Dockerfile",
    "mvnw",
    "mvnw.cmd",
    "maven-wrapper.properties",
    "application.yml",
    "UserController.java",
    "UserService.java",
    "SecurityConfig.java"
)

$entryNames = $entries | ForEach-Object { $_.FullName }
$missingRequired = @()

foreach ($req in $requiredBasenames) {
    $found = $false
    foreach ($en in $entryNames) {
        if ($en.EndsWith("/$req") -or $en -eq $req) {
            $found = $true
            break
        }
    }
    if (-not $found) {
        $missingRequired += $req
    }
}

$zip.Dispose()

# Audit evaluation
Write-Host "`nAudit Findings:" -ForegroundColor Cyan

$hasViolations = $false

if ($hasPrefixIssue) {
    $hasViolations = $true
    Write-Host "[FAIL] Root prefix violation: Archive does not contain exactly one top-level directory prefix." -ForegroundColor Red
    Write-Host "  Detected Top-Level Roots: $($topLevelEntries.Keys -join ', ')" -ForegroundColor Red
    if ($entriesWithoutPrefix.Count -gt 0) {
        Write-Host "  Files without root prefix: $($entriesWithoutPrefix -join ', ')" -ForegroundColor Red
    }
} else {
    $singlePrefix = ($topLevelEntries.Keys | Select-Object -First 1)
    Write-Host "[PASS] Verified single top-level product directory: $singlePrefix/" -ForegroundColor Green
}

if ($detectedForbidden.Count -gt 0) {
    $hasViolations = $true
    Write-Host "[FAIL] FORBIDDEN, sensitive, or maintainer-only items detected in archive:" -ForegroundColor Red
    foreach ($f in $detectedForbidden) {
        Write-Host "  - [$($f.Category)] $($f.Path)" -ForegroundColor Red
    }
} else {
    Write-Host "[PASS] No secrets, .env files, git metadata, build outputs, crash dumps, or maintainer files found." -ForegroundColor Green
}

if ($missingRequired.Count -gt 0) {
    $hasViolations = $true
    Write-Host "`n[FAIL] Missing required starter-kit files in archive:" -ForegroundColor Red
    foreach ($m in $missingRequired) {
        Write-Host "  - Missing: $m" -ForegroundColor Red
    }
} else {
    Write-Host "[PASS] All essential starter-kit files (LICENSE, notices, README, POM, wrapper, src) are present." -ForegroundColor Green
}

Write-Host ""
if ($hasViolations) {
    Write-Host "============================================================" -ForegroundColor Red
    Write-Host "AUDIT RESULT: REJECTED (Package is NOT safe or complete for distribution)" -ForegroundColor Red
    Write-Host "============================================================" -ForegroundColor Red
    exit 1
} else {
    Write-Host "============================================================" -ForegroundColor Green
    Write-Host "AUDIT RESULT: VERIFIED (Package is safe and complete for distribution)" -ForegroundColor Green
    Write-Host "============================================================" -ForegroundColor Green
    exit 0
}
