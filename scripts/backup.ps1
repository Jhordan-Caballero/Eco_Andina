<#
.SYNOPSIS
    Backup de EcoAndina en Windows: base de datos PostgreSQL (pg_dump) + carpeta de archivos adjuntos.

.DESCRIPTION
    Crea en la carpeta backups/ (junto al proyecto):
      - ecoandina_AAAAMMDD_HHMM.dump  -> copia de la BD en formato custom (se restaura con restaurar.ps1)
      - uploads_AAAAMMDD_HHMM.zip     -> manifiestos, guias, actas y certificados subidos (CU16)
    Borra los backups con mas de -DiasRetencion dias y deja un registro en backups/backup.log.
    Para correr sin preguntar la clave (tarea programada) define PGPASSWORD o usa el archivo pgpass.conf
    (ver docs/plan-mantenimiento.md).

.EXAMPLE
    .\scripts\backup.ps1
    .\scripts\backup.ps1 -Usuario postgres -DiasRetencion 30
#>
param(
    [string]$BaseDatos = "gestion_residuos",
    [string]$Usuario = "postgres",
    [string]$Servidor = "localhost",
    [int]$Puerto = 5432,
    [string]$Destino = (Join-Path $PSScriptRoot "..\backups"),
    [string]$CarpetaUploads = (Join-Path $PSScriptRoot "..\uploads"),
    [int]$DiasRetencion = 14
)

$ErrorActionPreference = "Stop"
New-Item -ItemType Directory -Force -Path $Destino | Out-Null
$Destino = (Resolve-Path $Destino).Path
$registro = Join-Path $Destino "backup.log"

function Escribir([string]$nivel, [string]$mensaje) {
    $linea = "{0} [{1}] {2}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"), $nivel, $mensaje
    Write-Host $linea
    Add-Content -Path $registro -Value $linea -Encoding UTF8
}

# pg_dump: primero el del PATH; si no, el de la version mas reciente instalada en Program Files
function Buscar-PgDump {
    $enPath = Get-Command pg_dump -ErrorAction SilentlyContinue
    if ($enPath) { return $enPath.Source }
    $instalado = Get-ChildItem "C:\Program Files\PostgreSQL\*\bin\pg_dump.exe" -ErrorAction SilentlyContinue |
        Sort-Object { [int]$_.Directory.Parent.Name } -Descending | Select-Object -First 1
    if ($instalado) { return $instalado.FullName }
    throw "No se encontro pg_dump. Instala PostgreSQL o agrega su carpeta bin al PATH."
}

try {
    $fecha = Get-Date -Format "yyyyMMdd_HHmm"
    $pgDump = Buscar-PgDump
    $pgpass = Join-Path $env:APPDATA "postgresql\pgpass.conf"
    if (-not $env:PGPASSWORD -and -not (Test-Path $pgpass)) {
        $clave = Read-Host "Clave de PostgreSQL para el usuario $Usuario" -AsSecureString
        $env:PGPASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
            [Runtime.InteropServices.Marshal]::SecureStringToBSTR($clave))
    }

    # 1. Base de datos
    $archivoBd = Join-Path $Destino "ecoandina_$fecha.dump"
    Escribir "INFO" "Iniciando backup de la base $BaseDatos en $Servidor`:$Puerto"
    & $pgDump --host=$Servidor --port=$Puerto --username=$Usuario --format=custom --no-password `
        --file=$archivoBd $BaseDatos
    if ($LASTEXITCODE -ne 0) { throw "pg_dump termino con codigo $LASTEXITCODE" }
    $tamano = [math]::Round((Get-Item $archivoBd).Length / 1KB, 1)
    Escribir "INFO" "Base de datos respaldada: $archivoBd ($tamano KB)"

    # 2. Archivos adjuntos
    if ((Test-Path $CarpetaUploads) -and (Get-ChildItem $CarpetaUploads -File | Select-Object -First 1)) {
        $archivoZip = Join-Path $Destino "uploads_$fecha.zip"
        Compress-Archive -Path (Join-Path $CarpetaUploads "*") -DestinationPath $archivoZip -Force
        $cantidad = (Get-ChildItem $CarpetaUploads -File).Count
        Escribir "INFO" "Adjuntos respaldados: $cantidad archivos en $archivoZip"
    } else {
        Escribir "INFO" "Carpeta de adjuntos vacia o inexistente ($CarpetaUploads): no se respalda"
    }

    # 3. Retencion: se borran los backups viejos
    $limite = (Get-Date).AddDays(-$DiasRetencion)
    $viejos = Get-ChildItem $Destino -File | Where-Object {
        ($_.Name -like "ecoandina_*.dump" -or $_.Name -like "uploads_*.zip") -and $_.LastWriteTime -lt $limite
    }
    foreach ($viejo in $viejos) {
        Remove-Item $viejo.FullName
        Escribir "INFO" "Backup antiguo eliminado: $($viejo.Name)"
    }
    Escribir "INFO" "Backup terminado correctamente"
    exit 0
} catch {
    Escribir "ERROR" "Backup fallido: $($_.Exception.Message)"
    exit 1
}
