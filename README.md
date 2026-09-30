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

Diseño de datos (diagrama de clases y modelo entidad-relación, con las decisiones de
simplificación) en [`docs/`](docs).

## Estructura del repositorio

```
.
├── src/                # Backend Spring Boot (paquete com.gestion.residuos.Eco_Andino)
├── frontend/           # Frontend React
├── docs/               # Diagrama de clases y modelo entidad-relación
└── pom.xml
```

## Cómo ejecutar el proyecto

Requisitos: JDK 21, Node.js 20+ y PostgreSQL.

### 1. Base de datos

Crea una base PostgreSQL local llamada `gestion_residuos` y define tus credenciales en un archivo
`.env` en la raíz (ignorado por git). Copia `.env.example` y ajusta los valores:

```bash
cp .env.example .env
```

Las tablas se crean solas al iniciar el backend (`ddl-auto=update`).

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

## Autenticación, roles y permisos

Inicio de sesión con usuario **o** correo y contraseña (`POST /api/auth/login`), que devuelve un JWT
para enviar como `Authorization: Bearer <token>` (en Postman/Bruno: pestaña *Auth → Bearer Token*).
`GET /api/auth/me` devuelve el usuario autenticado con sus roles y permisos.

Un usuario puede tener varios roles y un rol varios permisos (`usuario_rol`, `rol_permiso`). Los
permisos y roles iniciales se derivan de los casos de uso del informe y viven en `DataSeeder`.

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

## Equipo

Proyecto desarrollado para el **Curso Integrador**, Carrera de Ingeniería de Sistemas e Informática, Ciclo VI — UTP, Arequipa, Perú (2026).

- Caballero Oros, Jhordan Moises
- Cruz Gonzales, Luis Ernesto
- Mamani Perez, Daniel Renato
- Veleto Copa, Kevin Jeremi

Docente: MSc. Ing. María Vilma Escobar Castillo
