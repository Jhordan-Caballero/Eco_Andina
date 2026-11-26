#!/usr/bin/env bash
# Restaura un backup hecho con backup.sh. ATENCIÓN: reemplaza los datos actuales.
# Uso: scripts/restaurar.sh backups/ecoandina_20261125_2300.dump [backups/uploads_20261125_2300.tar.gz]
set -euo pipefail

ARCHIVO_BD="${1:?Indica el archivo .dump a restaurar}"
ARCHIVO_UPLOADS="${2:-}"
BD="${BD:-gestion_residuos}"
USUARIO_BD="${USUARIO_BD:-ecoandina}"
PROYECTO="$(cd "$(dirname "$0")/.." && pwd)"

read -r -p "Se reemplazarán los datos de '$BD' con $ARCHIVO_BD. Escribe SI para continuar: " respuesta
[ "$respuesta" = "SI" ] || { echo "Cancelado."; exit 0; }

cd "$PROYECTO"
docker compose stop backend
docker compose exec -T db pg_restore -U "$USUARIO_BD" -d "$BD" --clean --if-exists --no-owner < "$ARCHIVO_BD"
echo "Base de datos restaurada."

if [ -n "$ARCHIVO_UPLOADS" ]; then
    docker compose run --rm --no-deps -T --entrypoint tar backend xzf - -C /app < "$ARCHIVO_UPLOADS"
    echo "Adjuntos restaurados."
fi

docker compose start backend
echo "Listo. Verifica /actuator/health y, como administrador, GET /api/mantenimiento/archivos."
