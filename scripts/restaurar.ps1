<#
.SYNOPSIS
    Restaura un backup de EcoAndina hecho con backup.ps1 (base de datos y, si se indica, adjuntos).

.DESCRIPTION
    ATENCION: reemplaza los datos actuales de la base por los del backup. Detener el backend antes.
    Pasos: 1) detener el backend  2) correr este script  3) iniciar el backend
    4) como administrador, GET /api/mantenimiento/archivos para confirmar que los adjuntos coinciden.

.EXAMPLE
    .\scripts\restaurar.ps1 -ArchivoBd .\backups\ecoandina_20261125_2300.dump -ArchivoUploads .\backups\uploads_20261125_2300.zip
#>
param(
    [Parameter(Mandatory = $true)][string]$ArchivoBd,
    [string]$ArchivoUploads,
    [string]$BaseDatos = "gestion_residuos",
    [string]$Usuario = "postgres",
    [string]$Servidor = "localhost",
    [int]$Puerto = 5432,
    [string]$CarpetaUploads = (Join-Path $PSScriptRoot "..\uploads")
)

$ErrorActionPreference = "Stop"
if (-not (Test-Path $ArchivoBd)) { throw "No existe el archivo $ArchivoBd" }

$pgRestore = (Get-Command pg_restore -ErrorAction SilentlyContinue).Source
if (-not $pgRestore) {
    $pgRestore = (Get-ChildItem "C:\Program Files\PostgreSQL\*\bin\pg_restore.exe" -ErrorAction SilentlyContinue |
        Sort-Object { [int]$_.Directory.Parent.Name } -Descending | Select-Object -First 1).FullName
}
if (-not $pgRestore) { throw "No se encontro pg_restore." }

$respuesta = Read-Host "Se reemplazaran los datos de '$BaseDatos' con $ArchivoBd. Escribe SI para continuar"
if ($respuesta -ne "SI") { Write-Host "Cancelado."; exit 0 }

if (-not $env:PGPASSWORD -and -not (Test-Path (Join-Path $env:APPDATA "postgresql\pgpass.conf"))) {
    $clave = Read-Host "Clave de PostgreSQL para el usuario $Usuario" -AsSecureString
    $env:PGPASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($clave))
}

& $pgRestore --host=$Servidor --port=$Puerto --username=$Usuario --no-password --dbname=$BaseDatos `
    --clean --if-exists --no-owner $ArchivoBd
if ($LASTEXITCODE -ne 0) { throw "pg_restore termino con codigo $LASTEXITCODE" }
Write-Host "Base de datos restaurada."

if ($ArchivoUploads) {
    New-Item -ItemType Directory -Force -Path $CarpetaUploads | Out-Null
    Expand-Archive -Path $ArchivoUploads -DestinationPath $CarpetaUploads -Force
    Write-Host "Adjuntos restaurados en $CarpetaUploads"
}
Write-Host "Listo. Inicia el backend y verifica /actuator/health y /api/mantenimiento/archivos."
