param()

# Lightweight component tests for Save Recovery.
# These tests use a temp folder and do not touch real game saves.

$ErrorActionPreference = "Stop"
$root = Join-Path $env:TEMP ("spd-save-recovery-test-" + [DateTimeOffset]::Now.ToUnixTimeMilliseconds())
$saveRoot = Join-Path $root "Shattered Pixel Dungeon"
$recovery = Join-Path $saveRoot "recovery"

function New-FakeSave {
    param([int]$Slot)

    $folder = Join-Path $saveRoot ("game" + $Slot)
    New-Item -ItemType Directory -Force -Path $folder | Out-Null
    Set-Content -LiteralPath (Join-Path $folder "game.dat") -Value "fake game data"
    Set-Content -LiteralPath (Join-Path $folder "depth1.dat") -Value "fake level data"
    return $folder
}

function Backup-Before-Delete {
    param([int]$Slot, [int64]$Time)

    $source = Join-Path $saveRoot ("game" + $Slot)
    $target = Join-Path $recovery ("deleted-" + $Time + "-slot-" + $Slot)

    if (-not (Test-Path -LiteralPath $source)) {
        Write-Host "[SaveRecovery] Save folder not found: $source"
        return $false
    }

    New-Item -ItemType Directory -Force -Path $recovery | Out-Null
    Copy-Item -LiteralPath $source -Destination $target -Recurse
    Prune-Old-Recoveries
    return (Test-Path -LiteralPath $target)
}

function Prune-Old-Recoveries {
    if (-not (Test-Path -LiteralPath $recovery)) {
        return
    }

    $folders = @(Get-ChildItem -LiteralPath $recovery -Directory | Sort-Object {
        if ($_.Name -match "^deleted-([0-9]+)-slot-[0-9]+$") {
            -[int64]$Matches[1]
        } else {
            0
        }
    })

    for ($i = 5; $i -lt $folders.Count; $i++) {
        Remove-Item -LiteralPath $folders[$i].FullName -Recurse -Force
    }
}

function Assert-True {
    param([bool]$Condition, [string]$Name)

    if ($Condition) {
        Write-Host ("PASS - " + $Name)
    } else {
        Write-Host ("FAIL - " + $Name)
        exit 1
    }
}

try {
    New-Item -ItemType Directory -Force -Path $saveRoot | Out-Null

    New-FakeSave -Slot 1 | Out-Null
    $ok = Backup-Before-Delete -Slot 1 -Time 1001
    Assert-True $ok "T1 backup is created after deleting save"

    $backup = Join-Path $recovery "deleted-1001-slot-1"
    Assert-True (Test-Path -LiteralPath (Join-Path $backup "game.dat")) "T2 backup contains game.dat"
    Assert-True (Test-Path -LiteralPath (Join-Path $backup "depth1.dat")) "T2 backup contains depth file"

    for ($i = 2; $i -le 7; $i++) {
        New-FakeSave -Slot $i | Out-Null
        Backup-Before-Delete -Slot $i -Time (1000 + $i) | Out-Null
    }
    $count = @(Get-ChildItem -LiteralPath $recovery -Directory).Count
    Assert-True ($count -eq 5) "T3 recovery keeps only latest 5 backups"

    Assert-True (Test-Path -LiteralPath $recovery) "T4 recovery folder is created automatically"

    $missing = Backup-Before-Delete -Slot 99 -Time 9999
    Assert-True (-not $missing) "T5 missing save returns false and does not crash"

    $oldestGone = -not (Test-Path -LiteralPath (Join-Path $recovery "deleted-1001-slot-1"))
    Assert-True $oldestGone "T6 oldest backup is pruned"

    Write-Host ""
    Write-Host "All lightweight Save Recovery component tests passed."
    Write-Host "Test folder: $root"
} finally {
    if (Test-Path -LiteralPath $root) {
        Remove-Item -LiteralPath $root -Recurse -Force
    }
}
