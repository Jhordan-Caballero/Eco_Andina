# Plan de mantenimiento — EcoAndina

Este plan define las tareas para mantener EcoAndina funcionando y con sus datos protegidos: **tareas
programadas (cron jobs)**, **backups** y **scripts** de apoyo, además del mantenimiento correctivo, preventivo y
evolutivo del código.

## 1. Tareas programadas dentro del backend

Se implementan con `@Scheduled` de Spring (`mantenimiento/TareasProgramadas.java`), que funciona como un cron
dentro de la aplicación. Los horarios están en hora de Lima y se configuran en `application.properties`.

| Tarea | Horario (cron de Spring) | Qué hace | Propiedad |
|---|---|---|---|
| Depuración de auditoría | `0 0 2 * * *`, todos los días a las 02:00 | Borra los registros de auditoría más antiguos que la retención configurada en CU22 (365 días por defecto) | `app.mantenimiento.depuracion-auditoria-cron` |
| Verificación de archivos | `0 0 3 * * SUN`, domingos a las 03:00 | Compara los documentos de la BD con la carpeta de adjuntos: avisa de archivos faltantes (ERROR) y huérfanos (WARN). No borra nada | `app.mantenimiento.verificacion-archivos-cron` |
| Revisión de vigencias | `0 0 7 * * MON-FRI`, lunes a viernes a las 07:00 | Avisa de transportistas y gestores con la autorización vencida o que vence en 30 días o menos | `app.mantenimiento.vigencias-cron` |

- Se pueden ver en `GET /actuator/scheduledtasks` (rol ADMINISTRADOR).
- Se desactivan todas con `MANTENIMIENTO_HABILITADO=false`.
- Cada ejecución deja en el log cuánto tardó, y si falla deja el error sin detener la aplicación.

También se pueden lanzar a mano:

| Endpoint | Permiso | Uso |
|---|---|---|
| `POST /api/mantenimiento/depuracion-auditoria` | `AUDITORIA_GESTIONAR` | Depura en el momento; queda registrado en la auditoría |
| `GET /api/mantenimiento/archivos` | Rol ADMINISTRADOR | Verificación de archivos; usar después de restaurar un backup |
| `GET /api/mantenimiento/vigencias` | ADMINISTRADOR o `TRANSPORTISTA_GESTOR_CONSULTAR` | Lista de autorizaciones vencidas o por vencer |

## 2. Backups

### 2.1 Qué se respalda

| Dato | Dónde vive | Cómo se respalda |
|---|---|---|
| Base de datos PostgreSQL (usuarios, generaciones, órdenes, auditoría, etc.) | Servicio local o volumen `postgres-data` | `pg_dump` en formato custom (`.dump`) |
| Archivos adjuntos (manifiestos, guías, actas, certificados, evidencias) | Carpeta `uploads/` o volumen `backend-uploads` | `.zip` en Windows o `.tar.gz` en Linux |
| Configuración (`.env`, variables de Dokploy) | Servidor | Copia manual cada vez que cambia; nunca se sube a GitHub |
| Código fuente | GitHub | El repositorio ya es su respaldo |

### 2.2 Política

| Parámetro | Valor |
|---|---|
| Frecuencia | Diario a las 23:00 (fuera del horario de operación) |
| Retención | 14 días (configurable con `-DiasRetencion` o `DIAS_RETENCION`) |
| Ubicación | Carpeta `backups/` del proyecto (ignorada por git) y, una vez por semana, una copia fuera del servidor (Google Drive o disco externo) |
| RPO (datos que se pueden perder) | Hasta 24 horas |
| RTO (tiempo para restaurar) | Menos de 30 minutos |
| Prueba de restauración | Una vez al mes, en una base de prueba |

### 2.3 Scripts

| Script | Sistema | Uso |
|---|---|---|
| `scripts/backup.ps1` | Windows | Backup local con `pg_dump` y zip de `uploads/`; borra los backups vencidos y registra en `backups/backup.log` |
| `scripts/restaurar.ps1` | Windows | Restaura un `.dump` con `pg_restore` y, opcionalmente, el zip de adjuntos. Pide confirmación |
| `scripts/programar-backup.ps1` | Windows | Registra el backup diario en el Programador de tareas de Windows |
| `scripts/backup.sh` | Linux con Docker | Backup del contenedor `db` y del volumen de adjuntos |
| `scripts/restaurar.sh` | Linux con Docker | Restaura la BD y los adjuntos; detiene y vuelve a iniciar el backend |

**Windows, una sola vez:** guardar la clave de PostgreSQL para que el backup programado no se quede esperando
que alguien la escriba. Crear `%APPDATA%\postgresql\pgpass.conf` con esta línea:

```
localhost:5432:gestion_residuos:postgres:LA_CLAVE
```

Luego:

```powershell
.\scripts\backup.ps1                 # prueba manual
.\scripts\programar-backup.ps1       # backup diario a las 23:00
```

**Linux, crontab del servidor** (`crontab -e`):

```cron
# Backup diario de EcoAndina a las 23:00
0 23 * * * /opt/Eco_Andina/scripts/backup.sh >> /opt/Eco_Andina/backups/backup.log 2>&1
# Chequeo de salud cada 15 minutos (ver plan de monitoreo)
*/15 * * * * /opt/Eco_Andina/scripts/salud.sh >> /opt/Eco_Andina/logs/salud.log 2>&1
```

### 2.4 Procedimiento de restauración

1. Elegir el backup: el `.dump` y el archivo de adjuntos de la misma fecha.
2. Avisar a los usuarios y detener el backend (`restaurar.sh` lo hace solo).
3. Ejecutar `restaurar.ps1` o `restaurar.sh` y confirmar con `SI`.
4. Iniciar el backend y comprobar que `/actuator/health` responde `UP`.
5. Como administrador, llamar a `GET /api/mantenimiento/archivos`: no debe haber archivos faltantes.
6. Revisar en la auditoría (CU21) la fecha del último registro, para saber desde cuándo hay que volver a cargar datos.

## 3. Mantenimiento de logs y datos

| Elemento | Cómo se mantiene |
|---|---|
| Logs | Logback rota y comprime solo; borra los de más de 30 días (general), 90 (alertas) o 14 (peticiones), con tope de 1 GB |
| Auditoría | La depuración nocturna aplica la retención de CU22 |
| Archivos huérfanos | La verificación semanal los lista. Se borran a mano después de revisarlos, nunca automáticamente |
| Backups | Cada script borra los suyos después de 14 días |
| Base de datos | `VACUUM` lo hace solo el autovacuum de PostgreSQL. Revisar el tamaño de las tablas una vez al mes |

## 4. Mantenimiento del software

| Tipo | Ejemplos en EcoAndina | Cómo se gestiona |
|---|---|---|
| Correctivo | Un error al cerrar una orden o un archivo que no se descarga | Issue en GitHub, rama `fix/...`, PR revisado por otro integrante, merge a `main` |
| Preventivo | Actualizar Spring Boot, el driver de PostgreSQL o las dependencias del frontend con parches de seguridad | Revisión mensual (`./mvnw versions:display-dependency-updates`, `npm outdated`, alertas de Dependabot) |
| Adaptativo | Un cambio en la normativa de residuos, como un nuevo tipo de documento o de plazo | Actualizar el catálogo y las reglas en `VistaService`, con sus pruebas |
| Evolutivo | Exportación a Excel o notificaciones por correo | Rama `feature/...` con PR |

Reglas para cualquier cambio:

1. Trabajar en una rama, nunca directo en `main`.
2. `./mvnw test` debe pasar antes de abrir el PR (pruebas en `src/test/java`).
3. El PR describe qué cambia y cómo probarlo. Lo revisa al menos otro integrante.
4. Si el cambio toca entidades, avisar en el PR: con `ddl-auto=update` las columnas nuevas se crean solas, pero renombrar o borrar columnas requiere migrar los datos a mano.
5. Hacer un backup antes de desplegar cambios que toquen la base de datos.

## 5. Calendario resumen

| Frecuencia | Tarea | Automática |
|---|---|---|
| Cada 30 s | Healthcheck de Docker | Sí |
| Cada 15 min | `salud.sh` | Sí (cron) |
| Diaria 02:00 | Depuración de auditoría | Sí |
| Diaria 07:00 (L-V) | Revisión de vigencias | Sí |
| Diaria 23:00 | Backup de BD y adjuntos | Sí (cron o Programador de tareas) |
| Diaria | Revisión de `ecoandina-alertas.log` | No |
| Semanal (domingo 03:00) | Verificación de archivos | Sí |
| Semanal | Copia de backups fuera del servidor | No |
| Mensual | Prueba de restauración y revisión de dependencias | No |
| Por ciclo | Revisión de este plan | No |
