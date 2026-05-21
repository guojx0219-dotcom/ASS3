param(
    [string]$BackupName = "",
    [int]$Slot = 0
)

# This script restores one deleted save from recovery folder.
# Run without parameters to see all recoverable saves.

$saveRoot = Join-Path $env:APPDATA ".shatteredpixel\Shattered Pixel Dungeon"
$recoverRoot = Join-Path $saveRoot "recovery"

if (-not (Test-Path -LiteralPath $recoverRoot)) {
    Write-Host "No recovery folder found."
    Write-Host "Expected path: $recoverRoot"
    exit 0
}

$backups = @(Get-ChildItem -LiteralPath $recoverRoot -Directory | Sort-Object LastWriteTime -Descending)

if ($backups.Count -eq 0) {
    Write-Host "No deleted saves can be restored."
    exit 0
}

if ($BackupName -eq "") {
    Write-Host "Deleted saves:"
    foreach ($backup in $backups) {
        Write-Host ("  " + $backup.Name + "    " + $backup.LastWriteTime)
    }
    Write-Host ""
    Write-Host "Restore example:"
    Write-Host "  powershell -ExecutionPolicy Bypass -File scripts\restore_deleted_save.ps1 -BackupName $($backups[0].Name)"
    exit 0
}

$selected = Get-Item -LiteralPath (Join-Path $recoverRoot $BackupName) -ErrorAction SilentlyContinue
if ($null -eq $selected -or -not $selected.PSIsContainer) {
    Write-Host "Backup not found: $BackupName"
    exit 1
}

if ($Slot -le 0) {
    if ($BackupName -match "-slot-([0-9]+)$") {
        $Slot = [int]$Matches[1]
    } else {
        Write-Host "Cannot find slot number from backup name. Please add -Slot 1."
        exit 1
    }
}

$target = Join-Path $saveRoot ("game" + $Slot)
if (Test-Path -LiteralPath $target) {
    Write-Host "Target save already exists: $target"
    Write-Host "Delete or move it first, then run restore again."
    exit 1
}

Move-Item -LiteralPath $selected.FullName -Destination $target
Write-Host "Restore finished:"
Write-Host "  $($selected.Name) -> game$Slot"
