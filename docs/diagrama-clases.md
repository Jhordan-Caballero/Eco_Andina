# Diagrama de clases — EcoAndina

Corresponde a la sección **3.2.2 Diseño de clases** del informe.

Catorce clases de dominio agrupadas en control de acceso (Usuario, Rol, Permiso), catálogos
(Área, Residuo, Contenedor, Transportista, Gestor) y operación (Generación, Movimiento,
Recolección, DetalleRecoleccion, Incidencia, Documento). Ver
[docs/modelo-entidad-relacion.md](modelo-entidad-relacion.md) para el mapeo a tablas y las
decisiones de simplificación (no está normalizado a 3FN a propósito, salvo roles/permisos).

```mermaid
classDiagram
    class Usuario {
        +long id
        +string nombre
        +string email
        +string estado
    }
    class Rol {
        +long id
        +string nombre
        +string descripcion
    }
    class Permiso {
        +long id
        +string codigo
        +string nombre
    }
    class Area {
        +long id
        +string codigo
        +string nombre
        +string proceso
        +string estado
    }
    class Residuo {
        +long id
        +string codigo
        +string nombre
        +string categoria
        +string peligrosidad
        +string unidadMedida
        +string tratamiento
        +string requisitos
    }
    class Contenedor {
        +long id
        +string codigo
        +string tipo
        +decimal capacidad
        +string ubicacion
        +string estado
        +tieneEspacio(cantidad) bool
    }
    class Generacion {
        +long id
        +decimal cantidad
        +date fecha
        +string evidenciaUrl
    }
    class Movimiento {
        +long id
        +decimal cantidad
        +datetime fecha
        +string motivo
    }
    class Transportista {
        +long id
        +string empresa
        +string autorizacion
        +string contacto
        +string vehiculo
        +date vigencia
        +estaVigente() bool
    }
    class Gestor {
        +long id
        +string empresa
        +string autorizacion
        +string procesoDestino
        +date vigencia
    }
    class Recoleccion {
        +long id
        +string codigoOrden
        +date fechaProgramada
        +string vehiculo
        +string estado
        +cerrar()
    }
    class DetalleRecoleccion {
        +long id
        +decimal cantidad
    }
    class Incidencia {
        +long id
        +string tipo
        +string descripcion
        +string severidad
        +string estado
        +cerrar()
    }
    class Documento {
        +long id
        +string tipo
        +string archivoUrl
        +string estadoValidacion
    }

    Usuario "0..*" -- "0..*" Rol : tiene
    Rol "0..*" -- "0..*" Permiso : otorga
    Usuario "0..1" --> "0..*" Area : responsable
    Usuario "1" --> "0..*" Generacion : registra
    Usuario "1" --> "0..*" Movimiento : realiza
    Usuario "1" --> "0..*" Incidencia : reporta
    Area "1" --> "0..*" Generacion : origina
    Area "1" --> "0..*" Incidencia : ocurre en
    Residuo "1" --> "0..*" Contenedor : tipo permitido
    Residuo "1" --> "0..*" Generacion
    Residuo "1" --> "0..*" Movimiento
    Residuo "1" --> "0..*" DetalleRecoleccion
    Contenedor "1" --> "0..*" Generacion
    Contenedor "1" --> "0..*" Movimiento : origen/destino
    Contenedor "1" --> "0..*" DetalleRecoleccion
    Transportista "1" --> "0..*" Recoleccion : ejecuta
    Gestor "0..1" --> "0..*" Recoleccion : recibe
    Recoleccion "1" --> "1..*" DetalleRecoleccion : incluye
    Recoleccion "1" --> "0..*" Documento
    Generacion "1" --> "0..*" Documento
    Incidencia "1" --> "0..*" Documento
```

## Responsabilidad de cada clase

| Clase | Responsabilidad |
|---|---|
| `Usuario` | Cuenta de acceso; puede tener varios `Rol` y referencia opcional a Área, Transportista o Gestor |
| `Rol` | Perfil de acceso (Generador, Supervisor, Administrador…); agrupa uno o varios `Permiso` |
| `Permiso` | Acción concreta autorizable (ej. validar generación, cerrar incidencia); reutilizable entre roles |
| `Area` | Área generadora de residuos (producción, mantenimiento, almacén…) |
| `Residuo` | Catálogo de tipos de residuo, su peligrosidad y requisitos de manejo |
| `Contenedor` | Punto de acopio físico; admite un único tipo de residuo |
| `Generacion` | Registro de residuo generado: cuánto, dónde, cuándo, por quién |
| `Movimiento` | Traslado interno de residuo entre contenedores |
| `Transportista` | Empresa autorizada de transporte externo |
| `Gestor` | Empresa autorizada de tratamiento/disposición final |
| `Recoleccion` | Orden de retiro: agenda, transportista, gestor, vehículo del viaje y su estado |
| `DetalleRecoleccion` | Línea de una recolección: qué contenedor/residuo y cuánta cantidad |
| `Incidencia` | Derrame, mezcla, retraso o rechazo, con severidad y cierre |
| `Documento` | Manifiesto, constancia o evidencia adjunta a otra entidad |

## Nota sobre roles y permisos

`Usuario` – `Rol` y `Rol` – `Permiso` son relaciones muchos-a-muchos reales (no un campo de
texto): un usuario puede tener varios roles, y un rol agrupa varios permisos. Es la única
parte del modelo que sí se normaliza — ver
[docs/modelo-entidad-relacion.md](modelo-entidad-relacion.md#por-qué-no-está-normalizado-a-3fn).
