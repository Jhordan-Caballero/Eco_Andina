# Roles y permisos — EcoAndina

Control de acceso por roles (RNF002). Un **usuario** puede tener varios **roles** y un rol agrupa varios
**permisos**; el acceso a cada función se decide por el permiso, no por el nombre del rol. Los permisos y
roles iniciales salen de los actores de cada caso de uso (CU02–CU22) del informe.

## Cómo se aplica

- Al iniciar sesión, el backend firma un JWT con los claims `roles` (códigos de rol) y `permisos` (unión,
  sin repetidos, de los permisos de todos sus roles).
- Spring Security los convierte en authorities: `ROLE_<codigo>` para los roles y el código del permiso tal
  cual, así que un endpoint se protege con `@PreAuthorize("hasAuthority('RECOLECCION_PROGRAMAR')")`.
- El frontend recibe `user.roles` y `user.permisos` en el login y en `GET /api/auth/me`; el menú y cada ruta
  se filtran con `hasPermission(codigo)` y `RequirePermission`.
- El token guarda una foto de los permisos: si un administrador cambia los roles de un usuario, el cambio se
  ve al volver a iniciar sesión.

## Permisos

| Código | Descripción | Caso de uso |
|---|---|---|
| `USUARIO_GESTIONAR` | Gestionar usuarios, roles y permisos | CU02 |
| `CATALOGO_CONSULTAR` | Consultar catálogos (residuos, áreas y contenedores) | consulta de CU03–CU05 y apoyo a CU08 |
| `RESIDUO_GESTIONAR` | Gestionar catálogo de residuos | CU03 |
| `AREA_GESTIONAR` | Gestionar áreas generadoras | CU04 |
| `CONTENEDOR_GESTIONAR` | Gestionar contenedores | CU05 |
| `TRANSPORTISTA_GESTIONAR` | Gestionar transportistas y vehículos | CU06 |
| `GESTOR_GESTIONAR` | Gestionar gestores autorizados | CU07 |
| `TRANSPORTISTA_GESTOR_CONSULTAR` | Consultar transportistas y gestores | consulta de CU06/CU07 y apoyo a CU12 |
| `GENERACION_REGISTRAR` | Registrar generación y segregación | CU08 |
| `GENERACION_VALIDAR` | Validar generación de residuos | CU09 |
| `ALMACENAMIENTO_GESTIONAR` | Gestionar almacenamiento y movimientos | CU10 |
| `ALMACENAMIENTO_CONSULTAR` | Consultar residuos y almacenamiento | CU11 |
| `RECOLECCION_PROGRAMAR` | Programar y aprobar recolecciones | CU12 |
| `RECOLECCION_CONSULTAR` | Consultar órdenes de recolección | CU13 |
| `TRASLADO_REGISTRAR` | Registrar salida y traslado | CU14 |
| `RECEPCION_REGISTRAR` | Registrar recepción y destino | CU15 |
| `DOCUMENTO_GESTIONAR` | Gestionar documentos y evidencias | CU16 |
| `INCIDENCIA_GESTIONAR` | Gestionar incidencias y acciones correctivas | CU17 |
| `OBSERVACION_REGISTRAR` | Registrar observaciones operativas | CU18 |
| `REPORTE_GENERAR` | Generar reportes e indicadores | CU19 |
| `REPORTE_EXPORTAR` | Exportar reportes | CU20 |
| `AUDITORIA_CONSULTAR` | Consultar auditoría | CU21 |
| `AUDITORIA_GESTIONAR` | Gestionar auditoría | CU22 |

## Matriz de roles y permisos

| Permiso | Administrador | Generador | Supervisor ambiental | Almacenero | Transportista | Gestor autorizado | Auditor / Gerencia |
|---|---|---|---|---|---|---|---|
| `USUARIO_GESTIONAR` | ✓ |  |  |  |  |  |  |
| `CATALOGO_CONSULTAR` | ✓ | ✓ | ✓ | ✓ |  |  |  |
| `RESIDUO_GESTIONAR` | ✓ |  |  |  |  |  |  |
| `AREA_GESTIONAR` | ✓ |  |  |  |  |  |  |
| `CONTENEDOR_GESTIONAR` | ✓ |  |  |  |  |  |  |
| `TRANSPORTISTA_GESTIONAR` | ✓ |  |  |  |  |  |  |
| `GESTOR_GESTIONAR` | ✓ |  |  |  |  |  |  |
| `TRANSPORTISTA_GESTOR_CONSULTAR` | ✓ |  | ✓ |  |  |  |  |
| `GENERACION_REGISTRAR` |  | ✓ |  |  |  |  |  |
| `GENERACION_VALIDAR` |  |  | ✓ |  |  |  |  |
| `ALMACENAMIENTO_GESTIONAR` |  |  |  | ✓ |  |  |  |
| `ALMACENAMIENTO_CONSULTAR` |  | ✓ | ✓ | ✓ |  |  |  |
| `RECOLECCION_PROGRAMAR` |  |  | ✓ |  |  |  |  |
| `RECOLECCION_CONSULTAR` |  |  | ✓ |  | ✓ | ✓ |  |
| `TRASLADO_REGISTRAR` |  |  |  |  | ✓ |  |  |
| `RECEPCION_REGISTRAR` |  |  |  |  |  | ✓ |  |
| `DOCUMENTO_GESTIONAR` |  | ✓ | ✓ | ✓ | ✓ | ✓ |  |
| `INCIDENCIA_GESTIONAR` |  |  | ✓ |  |  |  |  |
| `OBSERVACION_REGISTRAR` |  | ✓ |  | ✓ | ✓ | ✓ |  |
| `REPORTE_GENERAR` |  |  | ✓ |  |  |  | ✓ |
| `REPORTE_EXPORTAR` |  |  |  |  |  |  | ✓ |
| `AUDITORIA_CONSULTAR` | ✓ |  |  |  |  |  | ✓ |
| `AUDITORIA_GESTIONAR` | ✓ |  |  |  |  |  |  |

## Menú del frontend

| Módulo | Permiso que lo habilita |
|---|---|
| Panel general | cualquier usuario autenticado |
| Catálogos | `CATALOGO_CONSULTAR` |
| Almacenamiento | `ALMACENAMIENTO_CONSULTAR` |
| Recolecciones | `RECOLECCION_CONSULTAR` |
| Transportistas y gestores | `TRANSPORTISTA_GESTOR_CONSULTAR` |

## Usuarios de prueba

El seed crea estos usuarios con la contraseña de `app.seed.default-password`
(`src/main/resources/application.properties`, sobrescribible con `SEED_DEFAULT_PASSWORD`).

| Usuario | Nombre | Rol |
|---|---|---|
| `admin` | Carlos Mendoza Ríos | Administrador |
| `aquispe` | Ana Quispe Mamani | Supervisor ambiental |
| `rhuaman` | Rosa Huamán Flores | Generador |
| `ltorres` | Luis Torres Cáceres | Almacenero y Generador |
| `jccori` | Jorge Ccori Apaza | Transportista |
| `mvaldivia` | Marta Valdivia Salas | Gestor autorizado |
| `psalas` | Pedro Salas Ortiz | Auditor / Gerencia |
| `jperez` | Juan Pérez Rojas | Generador (inactivo) |

## Cómo agregar un permiso o un rol

Agrégalo en `DataSeeder` (`src/main/java/com/gestion/residuos/Eco_Andino/config/DataSeeder.java`). El seed
solo crea lo que falta y nunca modifica lo existente: en una base que ya tiene datos, un permiso nuevo se
crea pero no se asigna solo a los roles que ya existen (hay que asignarlo desde la administración de
roles o con SQL). Desactiva todo el seed con `SEED_ENABLED=false`.
