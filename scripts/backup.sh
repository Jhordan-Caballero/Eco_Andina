#!/usr/bin/env bash
# Backup de EcoAndina en el servidor (despliegue con docker compose).
#   - backups/ecoandina_AAAAMMDD_HHMM.dump  -> base de datos (pg_dump formato custom)
#   - backups/uploads_AAAAMMDD_HHMM.tar.gz  -> archivos adjuntos del volumen backend-uploads
# Borra los backups con más de DIAS_RETENCION días. Pensado para cron (ver docs/plan-mantenimiento.md):
#   0 23 * * * /ruta/Eco_Andina/scripts/backup.sh >> /ruta/Eco_Andina/backups/backup.log 2>&1
set -euo pipefail

PROYECTO="$(cd "$(dirname "$0")/.." && pwd)"
DESTINO="${DESTINO:-$PROYECTO/backups}"
DIAS_RETENCION="${DIAS_RETENCION:-14}"
BD="${BD:-gestion_residuos}"
USUARIO_BD="${USUARIO_BD:-ecoandina}"
FECHA="$(date +%Y%m%d_%H%M)"

log() { echo "$(date '+%Y-%m-%d %H:%M:%S') [$1] $2"; }
trap 'log ERROR "Backup fallido (línea $LINENO)"' ERR

mkdir -p "$DESTINO"
cd "$PROYECTO"

log INFO "Iniciando backup de $BD"
docker compose exec -T db pg_dump -U "$USUARIO_BD" --format=custom "$BD" > "$DESTINO/ecoandina_$FECHA.dump"
log INFO "Base de datos respaldada: ecoandina_$FECHA.dump ($(du -h "$DESTINO/ecoandina_$FECHA.dump" | cut -f1))"

docker compose exec -T backend tar czf - -C /app uploads > "$DESTINO/uploads_$FECHA.tar.gz"
log INFO "Adjuntos respaldados: uploads_$FECHA.tar.gz ($(du -h "$DESTINO/uploads_$FECHA.tar.gz" | cut -f1))"

find "$DESTINO" -maxdepth 1 -type f \( -name 'ecoandina_*.dump' -o -name 'uploads_*.tar.gz' \) \
    -mtime +"$DIAS_RETENCION" -print -delete | while read -r viejo; do log INFO "Backup antiguo eliminado: $viejo"; done

log INFO "Backup terminado correctamente"
