<#
.SYNOPSIS
    Programa backup.ps1 en el Programador de tareas de Windows (el "cron" de Windows).

.DESCRIPTION
    Crea la tarea "EcoAndina - Backup diario" que corre todos los dias a la hora indicada.
    Requisito: guardar la clave de PostgreSQL en %APPDATA%\postgresql\pgpass.conf para que el backup
    no se quede esperando que alguien la escriba (ver docs/plan-mantenimiento.md).
    Para quitarla: Unregister-ScheduledTask -TaskName "EcoAndina - Backup diario"

.EXAMPLE
    .\scripts\programar-backup.ps1            # todos los dias a las 23:00
    .\scripts\programar-backup.ps1 -Hora 22:30
#>
param([string]$Hora = "23:00")

$script = (Resolve-Path (Join-Path $PSScriptRoot "backup.ps1")).Path
$accion = New-ScheduledTaskAction -Execute "powershell.exe" `
    -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$script`""
$disparador = New-ScheduledTaskTrigger -Daily -At $Hora
$opciones = New-ScheduledTaskSettingsSet -StartWhenAvailable -ExecutionTimeLimit (New-TimeSpan -Hours 1)

Register-ScheduledTask -TaskName "EcoAndina - Backup diario" -Action $accion -Trigger $disparador `
    -Settings $opciones -Description "Backup de la BD y adjuntos de EcoAndina (scripts/backup.ps1)" -Force | Out-Null
Write-Host "Tarea 'EcoAndina - Backup diario' programada todos los dias a las $Hora."
Write-Host "Probarla ahora: Start-ScheduledTask -TaskName 'EcoAndina - Backup diario'"
