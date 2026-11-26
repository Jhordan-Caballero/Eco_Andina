# EcoAndina — Sistema de Gestión de Residuos Industriales

Plataforma web para centralizar la gestión y trazabilidad de los residuos industriales de la planta **EcoAndina**, organizada en áreas de producción, mantenimiento, almacenes, oficinas y patio de residuos.

## Problema

La gestión de residuos depende actualmente de formatos manuales y registros aislados por área, lo que dificulta conocer con precisión cuánto residuo se genera, verificar la segregación, controlar las fechas de retiro, demostrar la entrega a un operador autorizado y producir indicadores para reducir riesgos, costos e impactos ambientales.

## Objetivo general

Desarrollar una plataforma web para gestionar y mantener la trazabilidad de los residuos industriales de la planta EcoAndina, centralizando la información de su generación, almacenamiento, recolección, transporte, recepción y destino, de acuerdo con las responsabilidades de los actores involucrados.

## Actores / roles

| Rol | Responsabilidad principal |
|---|---|
| Generador | Registra la generación, segregación, cantidad y área de origen |
| Supervisor ambiental | Valida registros, programa recolecciones, revisa incidencias y reportes |
| Almacenero (encargado de residuos) | Controla almacenamiento temporal, pesaje y movimientos |
| Transportista | Retira residuos autorizados y registra el traslado |
| Gestor autorizado | Confirma recepción, tratamiento, valorización o disposición |
| Administrador | Gestiona usuarios, roles, permisos, catálogos y parámetros |
| Auditor / Gerencia | Consulta indicadores, evidencias y cumplimiento de procedimientos |

## Módulos principales

- **Catálogos**: residuos, áreas, contenedores, transportistas y gestores
- **Generación y segregación**
- **Almacenamiento y movimientos**
- **Recolecciones** (programación, asignación de transportista)
- **Transporte y recepción**
- **Incidencias y reportes** (derrames, mezclas, retrasos, rechazos, indicadores)
- **Usuarios, roles y permisos** (autenticación, control de acceso, auditoría)

Desarrollo planificado en dos etapas:

- **Etapa 01**: catálogos, áreas, residuos, contenedores, generación, segregación, usuarios y almacenamiento.
- **Etapa 02**: recolecciones, transportistas, gestores, documentos, incidencias, reportes, auditoría y puesta en marcha.

## Arquitectura y stack tecnológico

Arquitectura cliente-servidor con base de datos relacional y acceso diferenciado por rol de usuario.

- **Backend** (raíz del repo): Java 21, Spring Boot 4.1.1, Spring Data JPA, Spring Security (JWT), Spring Validation, Spring Web MVC
- **Base de datos**: PostgreSQL
- **Frontend** ([`frontend/`](frontend)): React 19, Vite, Tailwind CSS v4, shadcn/ui, React Router, React Hook Form + Zod, TanStack Query
- **Comunicación**: API REST con autenticación JWT
- **Herramientas**: GitHub (control de versiones), Postman/Bruno (pruebas de API)

Diseño de datos en [`docs/`](docs): [diagrama de clases](docs/diagrama-clases.md), [modelo
entidad-relación](docs/modelo-entidad-relacion.md) (con el estado de implementación y las decisiones de
simplificación) y [roles y permisos](docs/roles-y-permisos.md).

## Estructura del repositorio

```
.
├── src/                # Backend Spring Boot (paquete com.gestion.residuos.Eco_Andino)
├── frontend/           # Frontend React
├── docs/               # Diagramas, modelo entidad-relación y planes de monitoreo y mantenimiento
├── scripts/            # Backup, restauración y chequeo de salud (PowerShell y Bash)
└── pom.xml
```

## Cómo ejecutar el proyecto

Requisitos: JDK 21, Node.js 20+ y PostgreSQL. Comprueba que `java -version` muestre 21: si tu terminal usa
otra versión, el backend no arranca; en Windows apunta `JAVA_HOME` (y el `PATH`) al JDK 21.

### 1. Base de datos

Crea una base PostgreSQL local llamada `gestion_residuos` y define tus credenciales en un archivo
`.env` en la raíz (ignorado por git). Copia `.env.example` y ajusta los valores:

```bash
cp .env.example .env
```

Las tablas se crean solas al iniciar el backend (`ddl-auto=update`) y, si la base está vacía, se cargan datos
de demostración (residuos, áreas, contenedores, generaciones, órdenes, incidencias, documentos y auditoría)
con fechas relativas al día de inicio. Si vienes de una versión anterior del modelo, borra y vuelve a crear la
base `gestion_residuos` para que se genere con las tablas nuevas.

### 2. Backend

```bash
./mvnw spring-boot:run
```

Levanta en `http://localhost:8082`. Al iniciar por primera vez carga datos de demostración (roles,
permisos y usuarios de prueba, ver más abajo). Se desactiva con `SEED_ENABLED=false`.

Si no defines `JWT_SECRET` en el `.env`, se genera una clave temporal y las sesiones se pierden en
cada reinicio (devtools reinicia la app al guardar cambios), así que en desarrollo conviene fijarla.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173` e inicia sesión con una cuenta de prueba.

## Despliegue con Docker Compose

`docker-compose.yml` levanta los tres servicios: `db` (PostgreSQL 17 con volumen), `backend` (el
`Dockerfile` de la raíz) y `frontend` (nginx que sirve la SPA y reenvía `/api` al backend). Solo el
frontend publica un puerto (`FRONTEND_PORT`, por defecto 8080); la base de datos y el backend quedan en
la red interna de Docker.

Variables de entorno (en Dokploy, pestaña *Environment*; en local, un `.env` junto al compose):

| Variable | Obligatoria | Descripción |
|---|---|---|
| `DB_PASSWORD` | sí | contraseña del usuario `ecoandina` de PostgreSQL |
| `JWT_SECRET` | sí | secreto para firmar los JWT (mínimo 32 caracteres) |
| `SEED_DEFAULT_PASSWORD` | sí | contraseña de los usuarios de prueba; no dejes la de desarrollo, es pública |
| `CORS_ALLOWED_ORIGINS` | no | origen público del frontend, ej. `http://75.119.141.87:8080` |
| `FRONTEND_PORT` | no | puerto publicado del frontend (por defecto 8080) |
| `SEED_ENABLED` | no | `false` para no crear los usuarios de prueba |
| `JPA_SHOW_SQL` | no | `true` para mostrar las consultas SQL en el log (por defecto `false` en el compose) |

Genera un valor seguro para cada secreto con `python3 -c "import secrets; print(secrets.token_urlsafe(48))"`.
Sirve por HTTP: para usar HTTPS pon un dominio delante (Traefik en Dokploy). El log del backend
(`logs/ecoandina.log`) se guarda en el volumen `backend-logs` y la zona horaria del contenedor es
`America/Lima`.

```bash
docker compose up -d --build
```

## Autenticación, roles y permisos

Inicio de sesión con usuario **o** correo y contraseña (`POST /api/auth/login`), que devuelve un JWT
para enviar como `Authorization: Bearer <token>` (en Postman/Bruno: pestaña *Auth → Bearer Token*).
`GET /api/auth/me` devuelve el usuario autenticado con sus roles y permisos.

Un usuario puede tener varios roles y un rol varios permisos (`usuario_rol`, `rol_permiso`). Los
permisos y roles iniciales se derivan de los casos de uso del informe y viven en `DataSeeder`; la matriz
completa está en [`docs/roles-y-permisos.md`](docs/roles-y-permisos.md).

**Cuentas de prueba** (la contraseña de todas es `app.seed.default-password`, en `application.properties`):

| Usuario | Rol |
|---|---|
| `admin` | Administrador |
| `aquispe` | Supervisor ambiental |
| `rhuaman` | Generador |
| `ltorres` | Almacenero y Generador |
| `jccori` | Transportista |
| `mvaldivia` | Gestor autorizado |
| `psalas` | Auditor / Gerencia |
| `jperez` | Generador (**inactivo**, sirve para probar la denegación de acceso) |

## API REST

Todas las rutas (salvo el login) requieren `Authorization: Bearer <token>` y el permiso indicado en
[`docs/roles-y-permisos.md`](docs/roles-y-permisos.md). Los errores llegan como `{"message": "..."}`.
Las rutas marcadas con *multipart* reciben `multipart/form-data`: una parte `datos` (JSON) y los archivos.

| Caso de uso | Endpoints |
|---|---|
| CU01 Autenticar | `POST /api/auth/login`, `GET /api/auth/me` |
| CU02 Usuarios y roles | `GET/POST /api/usuarios`, `PUT /api/usuarios/{id}`, `PATCH /api/usuarios/{id}/estado`, `GET /api/roles`, `PUT /api/roles/permisos`, `GET /api/permisos` |
| CU03 Residuos | `GET/POST /api/residuos`, `PUT /api/residuos/{id}` |
| CU04 Áreas | `GET/POST /api/areas`, `PUT /api/areas/{id}`, `PATCH /api/areas/{id}/estado`, `GET /api/areas/responsables` |
| CU05 Contenedores | `GET/POST /api/contenedores`, `PUT /api/contenedores/{id}` |
| CU06 Transportistas | `GET/POST /api/transportistas`, `PUT /api/transportistas/{id}`, `POST /api/transportistas/{id}/vehiculos`, `PUT /api/transportistas/{id}/vehiculos/{idVehiculo}` |
| CU07 Gestores | `GET/POST /api/gestores`, `PUT /api/gestores/{id}` |
| CU08 Generación | `GET /api/generaciones`, `GET /api/generaciones/opciones`, `GET /api/generaciones/{id}`, `POST /api/generaciones` (*multipart*: `datos`, `evidencia`), `PUT /api/generaciones/{id}/correccion` (*multipart*) |
| CU09 Validación | `PATCH /api/generaciones/{id}/validar`, `PATCH /api/generaciones/{id}/observar` |
| CU10 Movimientos | `POST /api/movimientos` (traslado o pesaje) |
| CU11 Almacenamiento | `GET /api/almacenamiento`, `GET /api/almacenamiento/{idContenedor}`, `GET /api/movimientos` |
| CU12 Programar recolección | `GET /api/recolecciones/opciones`, `POST /api/recolecciones`, `PATCH /api/recolecciones/{id}/aprobar`, `PATCH /api/recolecciones/{id}/cerrar` |
| CU13 Consultar órdenes | `GET /api/recolecciones`, `GET /api/recolecciones/{id}` |
| CU14 Salida y traslado | `POST /api/recolecciones/{id}/salida` (*multipart*: `datos`, `archivo`) |
| CU15 Recepción y destino | `POST /api/recolecciones/{id}/recepcion` (*multipart*: `datos`, `acta`, `certificado`) |
| CU16 Documentos | `GET/POST /api/documentos` (*multipart*: `datos`, `archivo`), `GET /api/documentos/referencias`, `PATCH /api/documentos/{id}/validar`, `PATCH /api/documentos/{id}/rechazar`, `GET /api/documentos/{id}/archivo` |
| CU17 Incidencias | `GET /api/incidencias`, `GET /api/incidencias/opciones`, `POST /api/incidencias` (*multipart*), `POST /api/incidencias/{id}/seguimiento`, `PATCH /api/incidencias/{id}/cerrar` |
| CU18 Observaciones | `GET/POST /api/observaciones` |
| CU19–CU20 Reportes | `GET /api/reportes?periodo=30\|90\|TODO`, `POST /api/reportes/exportar?periodo=...` |
| CU21–CU22 Auditoría | `GET /api/auditoria`, `GET/PUT /api/auditoria/configuracion` |
| Panel general | `GET /api/dashboard` |
| Mantenimiento | `POST /api/mantenimiento/depuracion-auditoria`, `GET /api/mantenimiento/archivos`, `GET /api/mantenimiento/vigencias` |

Reglas de negocio que aplica el backend (no solo el frontend): el responsable de cada registro sale del token;
un contenedor no admite otro residuo ni más de su capacidad; los residuos peligrosos exigen evidencia y
manifiesto; una orden no se cierra sin transportista, cantidad recibida, destino, documento de traslado y
certificado (RNF003); toda validación, modificación y cierre queda en la auditoría (RNF006). Los archivos se
guardan en `UPLOADS_DIR` (por defecto `uploads/`): solo PDF, JPG, PNG o WEBP de hasta 10 MB, comprobando el
contenido real del archivo. Tras 5 intentos fallidos de inicio de sesión el usuario se bloquea 15 minutos.

## Pruebas

Pruebas unitarias con JUnit 5 y Mockito en `src/test/java` (no necesitan base de datos, salvo
`EcoAndinoApplicationTests`, que levanta la aplicación completa y requiere PostgreSQL encendido):

| Prueba | Qué comprueba |
|---|---|
| `RecoleccionServiceTest` | Cierre de orden (CU14): solo con los 5 requisitos de RNF003 y en estado RECIBIDA |
| `VistaServiceTest` | Requisitos de cierre, vigencias (CU15) y ocupación de contenedores (CU10) |
| `ArchivoServiceTest` | Adjuntos (CU16): tipo real del archivo, tamaño máximo y path traversal |
| `LoginAttemptServiceTest` | Bloqueo tras 5 intentos fallidos de inicio de sesión |
| `AuditoriaServiceTest` | Depuración de auditoría según la retención (CU22) |
| `MantenimientoServiceTest` | Verificación de adjuntos y revisión de vigencias |
| `CodigosTest` | Códigos GEN, MOV, OR e INC |

```bash
./mvnw test                                  # todas
./mvnw test -Dtest='*ServiceTest,CodigosTest' # solo las unitarias (sin base de datos)
```

## Monitoreo y mantenimiento

- **Monitoreo** ([plan](docs/plan-monitoreo.md)): logs con rotación en `logs/` (general, alertas y
  peticiones con su tiempo de respuesta); Spring Boot Actuator en `/actuator/health` (BD, disco y carpeta de
  adjuntos) e `/actuator/info`, públicos, y `/actuator/metrics`, `/actuator/loggers` y
  `/actuator/scheduledtasks`, solo para el administrador; métricas propias `ecoandina.*`; healthcheck de
  Docker y `scripts/salud.ps1` o `scripts/salud.sh` para la salud del sistema operativo.
- **Mantenimiento** ([plan](docs/plan-mantenimiento.md)): tareas programadas (depuración de auditoría a
  diario, verificación de adjuntos semanal y aviso de autorizaciones por vencer); backups con
  `scripts/backup.ps1` (Windows, programable con `scripts/programar-backup.ps1`) o `scripts/backup.sh` (cron
  en el servidor), y restauración con `scripts/restaurar.*`.

## Equipo

Proyecto desarrollado para el **Curso Integrador**, Carrera de Ingeniería de Sistemas e Informática, Ciclo VI — UTP, Arequipa, Perú (2026).

- Caballero Oros, Jhordan Moises
- Cruz Gonzales, Luis Ernesto
- Mamani Perez, Daniel Renato
- Veleto Copa, Kevin Jeremi

Docente: MSc. Ing. María Vilma Escobar Castillo
