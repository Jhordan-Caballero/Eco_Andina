<#
.SYNOPSIS
    Chequeo rapido de salud del servidor y de EcoAndina (monitoreo, ver docs/plan-monitoreo.md).

.DESCRIPTION
    Muestra CPU, memoria y disco del equipo, si PostgreSQL esta escuchando y el estado del backend
    (/actuator/health). Si se pasa -Token (JWT de un administrador) muestra el detalle de cada componente.
    Devuelve codigo 1 si algo esta mal, para usarlo en una tarea programada.

.EXAMPLE
    .\scripts\salud.ps1
    .\scripts\salud.ps1 -Url http://localhost:8082 -Token eyJhbGciOi...
#>
param(
    [string]$Url = "http://localhost:8082",
    [string]$Token,
    [int]$PuertoBd = 5432
)

$problemas = 0
Write-Host "=== Sistema operativo ($env:COMPUTERNAME) - $(Get-Date -Format 'yyyy-MM-dd HH:mm') ==="

$cpu = [math]::Round((Get-CimInstance Win32_Processor | Measure-Object -Property LoadPercentage -Average).Average, 0)
$so = Get-CimInstance Win32_OperatingSystem
$memUso = [math]::Round((($so.TotalVisibleMemorySize - $so.FreePhysicalMemory) / $so.TotalVisibleMemorySize) * 100, 0)
Write-Host ("CPU: {0}%   Memoria: {1}% usada de {2} GB" -f $cpu, $memUso, [math]::Round($so.TotalVisibleMemorySize / 1MB, 1))
if ($cpu -ge 85) { Write-Host "  ALERTA: CPU alta" -ForegroundColor Yellow; $problemas++ }
if ($memUso -ge 90) { Write-Host "  ALERTA: memoria casi llena" -ForegroundColor Yellow; $problemas++ }

Get-CimInstance Win32_LogicalDisk -Filter "DriveType=3" | ForEach-Object {
    $libre = [math]::Round($_.FreeSpace / 1GB, 1)
    $pct = [math]::Round(($_.FreeSpace / $_.Size) * 100, 0)
    Write-Host ("Disco {0} {1} GB libres ({2}%)" -f $_.DeviceID, $libre, $pct)
    if ($pct -lt 10) { Write-Host "  ALERTA: poco espacio en $($_.DeviceID)" -ForegroundColor Yellow; $problemas++ }
}

Write-Host "`n=== Servicios ==="
$bd = Test-NetConnection -ComputerName localhost -Port $PuertoBd -WarningAction SilentlyContinue
if ($bd.TcpTestSucceeded) { Write-Host "PostgreSQL: escuchando en el puerto $PuertoBd" }
else { Write-Host "PostgreSQL: NO responde en el puerto $PuertoBd" -ForegroundColor Red; $problemas++ }

try {
    $cabeceras = @{}
    if ($Token) { $cabeceras["Authorization"] = "Bearer $Token" }
    $salud = Invoke-RestMethod -Uri "$Url/actuator/health" -Headers $cabeceras -TimeoutSec 10
    Write-Host "Backend: $($salud.status)"
    if ($salud.components) {
        $salud.components.PSObject.Properties | ForEach-Object { Write-Host ("  - {0}: {1}" -f $_.Name, $_.Value.status) }
    }
} catch {
    $respuesta = $_.Exception.Response
    if ($respuesta) { Write-Host "Backend: DOWN (HTTP $([int]$respuesta.StatusCode))" -ForegroundColor Red }
    else { Write-Host "Backend: no responde en $Url" -ForegroundColor Red }
    $problemas++
}

$alertas = Join-Path $PSScriptRoot "..\logs\ecoandina-alertas.log"
if (Test-Path $alertas) {
    $hoy = Get-Date -Format "yyyy-MM-dd"
    $deHoy = @(Select-String -Path $alertas -Pattern "^$hoy" -ErrorAction SilentlyContinue)
    $errores = @($deHoy | Where-Object { $_.Line -match " ERROR " })
    Write-Host "`n=== Logs de hoy ==="
    Write-Host "Alertas: $($deHoy.Count)   Errores: $($errores.Count)   (logs\ecoandina-alertas.log)"
    $errores | Select-Object -Last 5 | ForEach-Object { Write-Host "  $($_.Line)" -ForegroundColor Red }
}

if ($problemas -eq 0) { Write-Host "`nTodo en orden." -ForegroundColor Green; exit 0 }
Write-Host "`n$problemas problema(s) detectado(s)." -ForegroundColor Yellow
exit 1
