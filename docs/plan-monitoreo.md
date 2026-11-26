# Plan de monitoreo — EcoAndina

Este plan define qué se vigila en EcoAndina, con qué herramienta, cada cuánto y qué hacer cuando algo falla.
Cubre los tres frentes del monitoreo de aplicaciones: **logs**, **rendimiento** y **salud del sistema operativo**.

## 1. Objetivos

| Objetivo | Por qué importa en EcoAndina |
|---|---|
| Detectar caídas del backend o de la base de datos en minutos | Sin el sistema no se registran generaciones (CU08) ni se cierran órdenes (CU14) |
| Detectar que no se pueden guardar adjuntos | Sin manifiesto ni certificado ninguna orden puede cerrarse (RNF003) |
| Ver peticiones lentas antes de que los usuarios se quejen | El operario registra desde planta y necesita respuestas rápidas |
| Detectar intentos de acceso indebido | Varios inicios de sesión fallidos o muchos 401/403 pueden indicar un ataque de fuerza bruta |
| Vigilar autorizaciones por vencer | Un transportista o gestor vencido no puede recibir órdenes (CU15) |

## 2. Herramientas implementadas

### 2.1 Logs (Logback) — `src/main/resources/logback.xml`

| Archivo | Contenido | Rotación y retención |
|---|---|---|
| `logs/ecoandina.log` | Todo desde nivel INFO: arranque, auditoría, tareas programadas | Diario o al llegar a 20 MB; 30 días; máximo 1 GB |
| `logs/ecoandina-alertas.log` | Solo WARN y ERROR | Diario; 90 días |
| `logs/ecoandina-peticiones.log` | Una línea por petición a `/api`: método, ruta, código, tiempo e IP | Diario; 14 días |

Los archivos rotados se guardan comprimidos en `logs/historial/`. La carpeta se puede cambiar con la variable `LOG_DIR`.
En Docker los logs quedan en el volumen `backend-logs`, y también se pueden ver con `docker compose logs -f backend`.

Ejemplo de `ecoandina-peticiones.log`:

```
2026-11-25 10:14:03 INFO  POST /api/generaciones 201 84ms ip=192.168.1.20
2026-11-25 10:15:41 INFO  POST /api/auth/login 429 3ms ip=192.168.1.37
2026-11-25 10:16:12 WARN  PETICION_LENTA GET /api/reportes 200 1830ms ip=192.168.1.15
```

Mensajes clave que hay que buscar en `ecoandina-alertas.log`:

| Texto | Significado |
|---|---|
| `PETICION_LENTA` | Una petición tardó más de `app.monitoreo.peticion-lenta-ms` (1000 ms por defecto) |
| `ERROR_SERVIDOR` | Una petición terminó con error 5xx |
| `Verificación de archivos` (ERROR) | Hay documentos registrados cuyo archivo ya no está en disco |
| `Vigencia:` | Un transportista o gestor tiene la autorización vencida o por vencer |
| `Tarea programada ... falló` | Una tarea de mantenimiento no pudo terminar |

### 2.2 Salud y métricas (Spring Boot Actuator)

| Endpoint | Acceso | Qué muestra |
|---|---|---|
| `GET /actuator/health` | Público | `UP` o `DOWN`. Con el token de un administrador muestra además el detalle por componente |
| `GET /actuator/health/liveness` y `/readiness` | Público | Si el proceso está vivo y si está listo para atender |
| `GET /actuator/info` | Público | Nombre, versión y fecha de compilación, Java, sistema operativo y tiempo encendido |
| `GET /actuator/metrics` | Solo ADMINISTRADOR | Lista de métricas disponibles |
| `GET /actuator/metrics/{nombre}` | Solo ADMINISTRADOR | Valor de una métrica |
| `GET /actuator/loggers` | Solo ADMINISTRADOR | Niveles de log; con POST se cambia uno sin reiniciar (por ejemplo, DEBUG para investigar) |
| `GET /actuator/scheduledtasks` | Solo ADMINISTRADOR | Tareas de mantenimiento programadas y sus horarios |

Componentes de `/actuator/health`:

- `db`: conexión a PostgreSQL.
- `diskSpace`: espacio libre en el disco del servidor.
- `archivos`: indicador propio de EcoAndina (`ArchivosHealthIndicator`). Revisa que la carpeta de adjuntos exista, que se pueda escribir en ella y que tenga al menos `app.monitoreo.espacio-minimo-mb` libres (500 MB por defecto).
- `ping`, `livenessState` y `readinessState`.

Métricas más útiles:

| Métrica | Tipo | Umbral de alerta |
|---|---|---|
| `http.server.requests` | Rendimiento (cantidad y tiempo de respuesta por endpoint) | Tiempo máximo > 2 s de forma sostenida |
| `jvm.memory.used` | Memoria de la JVM | > 85 % de `jvm.memory.max` |
| `process.cpu.usage` / `system.cpu.usage` | CPU | > 85 % durante más de 5 min |
| `hikaricp.connections.active` | Conexiones a la BD en uso | Igual al máximo del pool (10) |
| `ecoandina.recolecciones.activas` | Negocio (propia) | Seguimiento de la carga de trabajo |
| `ecoandina.generaciones.pendientes` | Negocio (propia) | > 20 pendientes de validación (CU09 atrasado) |
| `ecoandina.incidencias.abiertas` | Negocio (propia) | Cualquier aumento brusco |

Ejemplo, con el token del administrador:

```bash
curl -H "Authorization: Bearer <token>" http://localhost:8082/actuator/health
curl -H "Authorization: Bearer <token>" "http://localhost:8082/actuator/metrics/http.server.requests?tag=uri:/api/recolecciones"
```

### 2.3 Salud del sistema operativo

| Herramienta | Dónde | Qué revisa |
|---|---|---|
| `scripts/salud.ps1` | Windows (desarrollo o servidor local) | CPU, memoria, discos, que PostgreSQL escuche, `/actuator/health` y errores del día en los logs |
| `scripts/salud.sh` | Servidor Linux con Docker | Carga, memoria, disco, estado y consumo de cada contenedor (`docker stats`), `/actuator/health` y errores del día |
| Healthcheck de Docker | `docker-compose.yml` y `Dockerfile` | Cada 30 s consulta `/actuator/health`; si falla 3 veces el contenedor queda `unhealthy` |
| Panel de Dokploy | Servidor de despliegue | CPU, memoria y red por contenedor, y reinicios |

Los scripts terminan con código 1 cuando encuentran un problema, así se pueden programar y revisar su salida.

## 3. Umbrales y respuesta

| Señal | Umbral | Severidad | Acción |
|---|---|---|---|
| `/actuator/health` = DOWN | 1 chequeo | Crítica | Ver qué componente falla (con el token de admin). Si es `db`, revisar el contenedor `db` o el servicio PostgreSQL; si es `archivos`, revisar permisos y espacio del volumen |
| Contenedor `unhealthy` o reiniciándose | 1 vez | Crítica | `docker compose logs --tail 200 backend` y revisar `ecoandina-alertas.log` |
| ERROR en `ecoandina-alertas.log` | Cualquiera | Alta | Revisar la traza; si es un error de código, abrir un issue en GitHub |
| Archivos faltantes (verificación semanal) | 1 o más | Alta | Restaurar esos archivos del último backup (ver plan de mantenimiento) |
| `PETICION_LENTA` | Más de 10 por día en el mismo endpoint | Media | Revisar la consulta del servicio correspondiente y el tamaño de las tablas |
| Muchos 401/429 desde la misma IP | Más de 20 en 10 min | Media | Posible fuerza bruta: revisar la auditoría de accesos (CU21) y bloquear la IP en el firewall o en Dokploy |
| Disco libre | < 10 % o < 500 MB en adjuntos | Media | Depurar backups antiguos o ampliar el disco |
| Autorización por vencer | 30 días o menos | Baja | Avisar al responsable para que renueve la autorización del transportista o gestor |

## 4. Frecuencia y responsables

| Actividad | Frecuencia | Responsable | Herramienta |
|---|---|---|---|
| Chequeo de salud automático | Cada 30 s | Docker | Healthcheck |
| Chequeo de salud con reporte | Cada 15 min (cron) o al iniciar la jornada | Administrador del sistema | `salud.sh` / `salud.ps1` |
| Revisión de `ecoandina-alertas.log` | Diaria | Administrador del sistema | Editor o `grep` |
| Revisión de métricas de rendimiento | Semanal | Equipo de desarrollo | `/actuator/metrics` |
| Revisión de vigencias | Lunes a viernes 07:00 (automática) | Tarea programada; la atiende el supervisor ambiental | Log + `GET /api/mantenimiento/vigencias` |
| Revisión del plan (umbrales, retenciones) | Al cierre de cada ciclo | Equipo | Este documento |

Ejemplo de cron en el servidor:

```cron
*/15 * * * * /opt/Eco_Andina/scripts/salud.sh >> /opt/Eco_Andina/logs/salud.log 2>&1
```

## 5. Seguridad del monitoreo

- Solo `health` (estado general) e `info` son públicos: no revelan datos del negocio. El detalle, las métricas y los niveles de log exigen el rol ADMINISTRADOR.
- El backend no se publica a internet: nginx solo reenvía `/api/`, así que `/actuator` se consulta dentro de la red de Docker o del servidor.
- Los logs no guardan contraseñas ni tokens. Los textos que vienen del usuario se limpian con `TextoUtil.paraLog` para evitar la inyección de líneas falsas en el log.

## 6. Mejoras futuras

- Exportar las métricas a Prometheus (`micrometer-registry-prometheus`) y verlas en un panel de Grafana.
- Enviar alertas por correo o Telegram cuando `salud.sh` termine con código 1.
- Centralizar los logs (Loki o ELK) si el sistema llega a tener varias réplicas.
