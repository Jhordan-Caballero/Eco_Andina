#!/usr/bin/env bash
# Chequeo de salud del servidor y de EcoAndina (monitoreo, ver docs/plan-monitoreo.md).
# Muestra CPU, memoria, disco, estado de los contenedores y /actuator/health. Sale con código 1 si algo falla,
# así se puede programar en cron y revisar el log:
#   */15 * * * * /ruta/Eco_Andina/scripts/salud.sh >> /ruta/Eco_Andina/logs/salud.log 2>&1
set -uo pipefail

PROYECTO="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROYECTO"
problemas=0

echo "=== $(hostname) - $(date '+%Y-%m-%d %H:%M') ==="
echo "Carga promedio (1/5/15 min): $(cut -d' ' -f1-3 /proc/loadavg)   Núcleos: $(nproc)"
free -m | awk '/Mem:/ {printf "Memoria: %d%% usada de %d MB\n", ($2-$7)*100/$2, $2}'
df -h / | awk 'NR==2 {print "Disco /: " $4 " libres (" $5 " usado)"}'
uso_disco="$(df / | awk 'NR==2 {gsub("%","",$5); print $5}')"
if [ "$uso_disco" -ge 90 ]; then echo "  ALERTA: disco casi lleno"; problemas=$((problemas+1)); fi

echo; echo "=== Contenedores ==="
docker compose ps --format 'table {{.Service}}\t{{.Status}}'
docker stats --no-stream --format 'table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}' $(docker compose ps -q) 2>/dev/null

echo; echo "=== Backend ==="
salud="$(docker compose exec -T backend curl -fsS http://localhost:8082/actuator/health 2>/dev/null || true)"
if echo "$salud" | grep -q '"status":"UP"'; then
    echo "Backend: UP"
else
    echo "Backend: DOWN o sin respuesta ($salud)"; problemas=$((problemas+1))
fi

echo; echo "=== Logs de hoy ==="
hoy="$(date +%Y-%m-%d)"
errores="$(docker compose exec -T backend sh -c "grep -c '^$hoy.* ERROR ' /app/logs/ecoandina-alertas.log" 2>/dev/null || echo 0)"
echo "Errores registrados hoy: ${errores//[^0-9]/}"

if [ "$problemas" -eq 0 ]; then echo; echo "Todo en orden."; exit 0; fi
echo; echo "$problemas problema(s) detectado(s)."; exit 1
