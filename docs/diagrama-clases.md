# Diagrama de clases — EcoAndina

Corresponde a la sección **3.2.2 Diseño de clases** del informe.

Quince clases de dominio agrupadas en control de acceso (`Usuario`, `Rol`, `Permiso`), catálogos
(`Area`, `Residuo`, `Contenedor`, `Transportista`, `Gestor`) y operación (`Generacion`, `Movimiento`,
`Recoleccion`, `DetalleRecoleccion`, `Incidencia`, `Documento`, `Auditoria`).

Las clases marcadas `<<implementado>>` ya existen en el backend; el resto es el diseño de los módulos
pendientes. Ver [modelo-entidad-relacion.md](modelo-entidad-relacion.md) para las tablas, el estado de
implementación y las decisiones de simplificación (no está normalizado a 3FN a propósito, salvo roles y
permisos), y [roles-y-permisos.md](roles-y-permisos.md) para la matriz de acceso.

```mermaid
classDiagram
    class Usuario {
        <<implementado>>
        +Integer idUsuario
        +String nombres
        +String apellidos
        +String username
        +String email
        +String password
        +String estado
        +isActivo() Boolean
    }
    class Rol {
        <<implementado>>
        +Integer idRol
        +String codigo
        +String nombre
        +String descripcion
    }
    class Permiso {
        <<implementado>>
        +Integer idPermiso
        +String codigo
        +String nombre
        +String descripcion
    }
    class Residuo {
        <<implementado>>
        +Integer idResiduo
        +String codigo
        +String nombre
        +String categoria
        +String peligrosidad
        +String unidad
        +String tratamiento
        +String requisitos
    }
    class Area {
        +Integer idArea
        +String codigo
        +String nombre
        +String proceso
        +String estado
    }
    class Contenedor {
        +Integer idContenedor
        +String codigo
        +String tipo
        +Decimal capacidad
        +String ubicacion
        +String estado
        +tieneEspacio(Decimal cantidad) Boolean
    }
    class Generacion {
        +Integer idGeneracion
        +Decimal cantidad
        +DateTime fecha
        +String condicionEnvase
        +String estadoValidacion
        +String observaciones
        +String evidenciaUrl
    }
    class Movimiento {
        +Integer idMovimiento
        +String origen
        +String destino
        +Decimal cantidad
        +DateTime fecha
        +String motivo
        +String observaciones
    }
    class Transportista {
        +Integer idTransportista
        +String empresa
        +String ruc
        +String autorizacion
        +String contacto
        +String vehiculo
        +Date vigencia
        +String estado
        +estaVigente() Boolean
    }
    class Gestor {
        +Integer idGestor
        +String empresa
        +String ruc
        +String autorizacion
        +String procesoDestino
        +Date vigencia
        +String estado
        +estaVigente() Boolean
    }
    class Recoleccion {
        +Integer idRecoleccion
        +String numeroOrden
        +DateTime fechaProgramada
        +String vehiculo
        +DateTime fechaSalida
        +DateTime fechaRecepcion
        +Decimal cantidadRecibida
        +String tipoDestino
        +String destinoFinal
        +String estado
        +String observaciones
        +cerrar()
    }
    class DetalleRecoleccion {
        +Integer idDetalle
        +Decimal cantidad
    }
    class Incidencia {
        +Integer idIncidencia
        +String tipo
        +String descripcion
        +DateTime fecha
        +String severidad
        +String estado
        +String accionCorrectiva
        +DateTime fechaCierre
        +cerrar()
    }
    class Documento {
        +Integer idDocumento
        +String tipo
        +String nombreArchivo
        +String rutaArchivo
        +DateTime fecha
        +String estadoValidacion
    }
    class Auditoria {
        +Integer idAuditoria
        +String accion
        +String entidad
        +Integer idRegistro
        +DateTime fecha
        +String detalle
    }

    Usuario "0..*" -- "0..*" Rol : tiene
    Rol "0..*" -- "0..*" Permiso : otorga
    Usuario "0..1" --> "0..*" Area : responsable
    Usuario "1" --> "0..*" Generacion : registra
    Usuario "1" --> "0..*" Movimiento : realiza
    Usuario "1" --> "0..*" Incidencia : reporta
    Usuario "1" --> "0..*" Auditoria : genera
    Area "1" --> "0..*" Generacion : origina
    Area "1" --> "0..*" Incidencia : ocurre en
    Residuo "1" --> "0..*" Contenedor : tipo permitido
    Residuo "1" --> "0..*" Generacion
    Residuo "1" --> "0..*" Movimiento
    Residuo "1" --> "0..*" DetalleRecoleccion
    Contenedor "1" --> "0..*" Generacion
    Contenedor "0..1" --> "0..*" Movimiento : afecta
    Contenedor "1" --> "0..*" DetalleRecoleccion
    Transportista "1" --> "0..*" Recoleccion : ejecuta
    Gestor "0..1" --> "0..*" Recoleccion : recibe
    Recoleccion "1" --> "1..*" DetalleRecoleccion : incluye
    Recoleccion "1" --> "0..*" Documento
    Generacion "1" --> "0..*" Documento
    Incidencia "1" --> "0..*" Documento
```

## Responsabilidad de cada clase

| Clase | Estado | Responsabilidad |
|---|---|---|
| `Usuario` | implementado | Cuenta de acceso; puede tener varios `Rol` |
| `Rol` | implementado | Perfil de acceso (Generador, Supervisor, Administrador…); agrupa uno o varios `Permiso` |
| `Permiso` | implementado | Acción concreta autorizable (ej. validar generación, cerrar incidencia); reutilizable entre roles |
| `Residuo` | implementado | Catálogo de tipos de residuo, su peligrosidad y requisitos de manejo |
| `Area` | pendiente | Área generadora de residuos (producción, mantenimiento, almacén…) |
| `Contenedor` | pendiente | Punto de acopio físico; admite un único tipo de residuo |
| `Generacion` | pendiente | Registro de residuo generado: cuánto, dónde, cuándo, por quién y si ya fue validado |
| `Movimiento` | pendiente | Traslado interno de residuo entre áreas o zonas (origen → destino) |
| `Transportista` | pendiente | Empresa autorizada de transporte externo |
| `Gestor` | pendiente | Empresa autorizada de tratamiento o disposición final |
| `Recoleccion` | pendiente | Orden de retiro: agenda, transportista, vehículo, recepción y destino, y su estado |
| `DetalleRecoleccion` | pendiente | Línea de una recolección: qué contenedor/residuo y cuánta cantidad |
| `Incidencia` | pendiente | Derrame, mezcla, retraso o rechazo, con severidad, acción correctiva y cierre |
| `Documento` | pendiente | Manifiesto, constancia o evidencia adjunta a otra entidad |
| `Auditoria` | pendiente | Registro de quién validó, modificó o cerró qué y cuándo |

## Diferencias con el diagrama de clases del informe

El informe (`avance1.docx`, sección 3.2.2) tiene su propio diagrama, más detallado. Este es una versión
simplificada; las diferencias son:

- **Vehículo**: aquí es un texto en `Transportista` y `Recoleccion`; el informe (RF009) tiene una clase
  `Vehiculo` con placa, tipo, capacidad y estado.
- **Recepción y destino**: aquí son atributos de `Recoleccion` (`fechaRecepcion`, `cantidadRecibida`,
  `tipoDestino`, `destinoFinal`); el informe (RF012) los separa en las clases `Recepcion` y `Destino`.
- **Evidencia**: aquí es `evidenciaUrl` en `Generacion` más la clase `Documento`; el informe tiene una
  clase `Evidencia`.
- **Roles y permisos**: el informe relaciona cada usuario con un solo rol y no tiene `Permiso`; aquí un
  usuario puede tener varios roles y un rol varios permisos.
