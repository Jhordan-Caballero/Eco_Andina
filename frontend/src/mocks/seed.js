import { addDays, format, subDays, subHours, subMonths } from "date-fns"

const hace = (dias, horaDelDia = 9, minuto = 0) => {
  const valor = subDays(new Date(), dias)
  valor.setHours(horaDelDia, minuto, 0, 0)
  return valor.toISOString()
}

const haceHoras = (horas) => subHours(new Date(), horas).toISOString()

const dia = (desplazamiento) => format(addDays(new Date(), desplazamiento), "yyyy-MM-dd")

const mes = (desplazamiento) => format(subMonths(new Date(), desplazamiento), "yyyy-MM")

export const PERMISOS = [
  { codigo: "USUARIO_GESTIONAR", descripcion: "Gestionar usuarios, roles y permisos", casoUso: "CU02" },
  { codigo: "CATALOGO_CONSULTAR", descripcion: "Consultar catálogos (residuos, áreas y contenedores)", casoUso: "CU03–CU05" },
  { codigo: "RESIDUO_GESTIONAR", descripcion: "Gestionar catálogo de residuos", casoUso: "CU03" },
  { codigo: "AREA_GESTIONAR", descripcion: "Gestionar áreas generadoras", casoUso: "CU04" },
  { codigo: "CONTENEDOR_GESTIONAR", descripcion: "Gestionar contenedores", casoUso: "CU05" },
  { codigo: "TRANSPORTISTA_GESTIONAR", descripcion: "Gestionar transportistas y vehículos", casoUso: "CU06" },
  { codigo: "GESTOR_GESTIONAR", descripcion: "Gestionar gestores autorizados", casoUso: "CU07" },
  { codigo: "TRANSPORTISTA_GESTOR_CONSULTAR", descripcion: "Consultar transportistas y gestores", casoUso: "CU06–CU07" },
  { codigo: "GENERACION_REGISTRAR", descripcion: "Registrar generación y segregación", casoUso: "CU08" },
  { codigo: "GENERACION_VALIDAR", descripcion: "Validar generación de residuos", casoUso: "CU09" },
  { codigo: "ALMACENAMIENTO_GESTIONAR", descripcion: "Gestionar almacenamiento y movimientos", casoUso: "CU10" },
  { codigo: "ALMACENAMIENTO_CONSULTAR", descripcion: "Consultar residuos y almacenamiento", casoUso: "CU11" },
  { codigo: "RECOLECCION_PROGRAMAR", descripcion: "Programar y aprobar recolecciones", casoUso: "CU12" },
  { codigo: "RECOLECCION_CONSULTAR", descripcion: "Consultar órdenes de recolección", casoUso: "CU13" },
  { codigo: "TRASLADO_REGISTRAR", descripcion: "Registrar salida y traslado", casoUso: "CU14" },
  { codigo: "RECEPCION_REGISTRAR", descripcion: "Registrar recepción y destino", casoUso: "CU15" },
  { codigo: "DOCUMENTO_GESTIONAR", descripcion: "Gestionar documentos y evidencias", casoUso: "CU16" },
  { codigo: "INCIDENCIA_GESTIONAR", descripcion: "Gestionar incidencias y acciones correctivas", casoUso: "CU17" },
  { codigo: "OBSERVACION_REGISTRAR", descripcion: "Registrar observaciones operativas", casoUso: "CU18" },
  { codigo: "REPORTE_GENERAR", descripcion: "Generar reportes e indicadores", casoUso: "CU19" },
  { codigo: "REPORTE_EXPORTAR", descripcion: "Exportar reportes", casoUso: "CU20" },
  { codigo: "AUDITORIA_CONSULTAR", descripcion: "Consultar auditoría", casoUso: "CU21" },
  { codigo: "AUDITORIA_GESTIONAR", descripcion: "Gestionar auditoría", casoUso: "CU22" },
]

const ROLES = [
  {
    codigo: "ADMINISTRADOR",
    nombre: "Administrador",
    descripcion: "Gestiona usuarios, catálogos y parámetros del sistema",
    permisos: [
      "USUARIO_GESTIONAR", "CATALOGO_CONSULTAR", "RESIDUO_GESTIONAR", "AREA_GESTIONAR",
      "CONTENEDOR_GESTIONAR", "TRANSPORTISTA_GESTIONAR", "GESTOR_GESTIONAR",
      "TRANSPORTISTA_GESTOR_CONSULTAR", "AUDITORIA_CONSULTAR", "AUDITORIA_GESTIONAR",
    ],
  },
  {
    codigo: "GENERADOR",
    nombre: "Generador",
    descripcion: "Registra la generación, segregación, cantidad y área de origen",
    permisos: [
      "CATALOGO_CONSULTAR", "GENERACION_REGISTRAR", "ALMACENAMIENTO_CONSULTAR",
      "DOCUMENTO_GESTIONAR", "OBSERVACION_REGISTRAR",
    ],
  },
  {
    codigo: "SUPERVISOR_AMBIENTAL",
    nombre: "Supervisor ambiental",
    descripcion: "Valida registros, programa recolecciones y revisa incidencias",
    permisos: [
      "CATALOGO_CONSULTAR", "GENERACION_VALIDAR", "ALMACENAMIENTO_CONSULTAR",
      "RECOLECCION_PROGRAMAR", "RECOLECCION_CONSULTAR", "TRANSPORTISTA_GESTOR_CONSULTAR",
      "DOCUMENTO_GESTIONAR", "INCIDENCIA_GESTIONAR", "REPORTE_GENERAR",
    ],
  },
  {
    codigo: "ALMACENERO",
    nombre: "Almacenero",
    descripcion: "Controla el almacenamiento temporal, el pesaje y los movimientos",
    permisos: [
      "CATALOGO_CONSULTAR", "ALMACENAMIENTO_GESTIONAR", "ALMACENAMIENTO_CONSULTAR",
      "DOCUMENTO_GESTIONAR", "OBSERVACION_REGISTRAR",
    ],
  },
  {
    codigo: "TRANSPORTISTA",
    nombre: "Transportista",
    descripcion: "Retira los residuos autorizados y registra el traslado",
    permisos: ["RECOLECCION_CONSULTAR", "TRASLADO_REGISTRAR", "DOCUMENTO_GESTIONAR", "OBSERVACION_REGISTRAR"],
  },
  {
    codigo: "GESTOR_AUTORIZADO",
    nombre: "Gestor autorizado",
    descripcion: "Confirma la recepción y registra el tratamiento o disposición",
    permisos: ["RECOLECCION_CONSULTAR", "RECEPCION_REGISTRAR", "DOCUMENTO_GESTIONAR", "OBSERVACION_REGISTRAR"],
  },
  {
    codigo: "AUDITOR_GERENCIA",
    nombre: "Auditor / Gerencia",
    descripcion: "Consulta indicadores, evidencias y cumplimiento",
    permisos: ["REPORTE_GENERAR", "REPORTE_EXPORTAR", "AUDITORIA_CONSULTAR"],
  },
]

const USUARIOS = [
  { idUsuario: 1, username: "admin", nombres: "Carlos", apellidos: "Mendoza Ríos", email: "admin@ecoandina.pe", estado: "ACTIVO", roles: ["ADMINISTRADOR"], ultimoAcceso: haceHoras(1) },
  { idUsuario: 2, username: "aquispe", nombres: "Ana", apellidos: "Quispe Mamani", email: "ana.quispe@ecoandina.pe", estado: "ACTIVO", roles: ["SUPERVISOR_AMBIENTAL"], ultimoAcceso: haceHoras(3) },
  { idUsuario: 3, username: "rhuaman", nombres: "Rosa", apellidos: "Huamán Flores", email: "rosa.huaman@ecoandina.pe", estado: "ACTIVO", roles: ["GENERADOR"], ultimoAcceso: hace(1, 16, 20) },
  { idUsuario: 4, username: "ltorres", nombres: "Luis", apellidos: "Torres Cáceres", email: "luis.torres@ecoandina.pe", estado: "ACTIVO", roles: ["ALMACENERO", "GENERADOR"], ultimoAcceso: haceHoras(5) },
  { idUsuario: 5, username: "jccori", nombres: "Jorge", apellidos: "Ccori Apaza", email: "jorge.ccori@ecoandina.pe", estado: "ACTIVO", roles: ["TRANSPORTISTA"], ultimoAcceso: haceHoras(2) },
  { idUsuario: 6, username: "mvaldivia", nombres: "Marta", apellidos: "Valdivia Salas", email: "marta.valdivia@ecoandina.pe", estado: "ACTIVO", roles: ["GESTOR_AUTORIZADO"], ultimoAcceso: hace(2, 17, 5) },
  { idUsuario: 7, username: "psalas", nombres: "Pedro", apellidos: "Salas Ortiz", email: "pedro.salas@ecoandina.pe", estado: "ACTIVO", roles: ["AUDITOR_GERENCIA"], ultimoAcceso: hace(4, 11, 40) },
  { idUsuario: 8, username: "jperez", nombres: "Juan", apellidos: "Pérez Rojas", email: "juan.perez@ecoandina.pe", estado: "INACTIVO", roles: ["GENERADOR"], ultimoAcceso: hace(41, 10, 15) },
]

const RESIDUOS = [
  { idResiduo: 1, codigo: "RES-001", nombre: "Aceite usado", categoria: "PELIGROSO", peligrosidad: "ALTA", unidad: "L", plazoMaximoDias: 30, tratamiento: "Refinación y reciclaje", requisitos: "Cilindro hermético rotulado sobre bandeja antiderrame" },
  { idResiduo: 2, codigo: "RES-002", nombre: "Chatarra metálica", categoria: "APROVECHABLE", peligrosidad: "BAJA", unidad: "kg", plazoMaximoDias: 90, tratamiento: "Valorización de metales", requisitos: "Separar de otros residuos y almacenar bajo techo" },
  { idResiduo: 3, codigo: "RES-003", nombre: "Envases con residuo químico", categoria: "PELIGROSO", peligrosidad: "ALTA", unidad: "unid", plazoMaximoDias: 30, tratamiento: "Disposición en relleno de seguridad", requisitos: "Envases cerrados, rotulados y con hoja de seguridad" },
  { idResiduo: 4, codigo: "RES-004", nombre: "Guantes y EPP contaminado", categoria: "PELIGROSO", peligrosidad: "MEDIA", unidad: "kg", plazoMaximoDias: 15, tratamiento: "Incineración controlada", requisitos: "Bolsa roja dentro de contenedor con tapa" },
  { idResiduo: 5, codigo: "RES-005", nombre: "Papel y cartón", categoria: "APROVECHABLE", peligrosidad: "BAJA", unidad: "kg", plazoMaximoDias: 60, tratamiento: "Reciclaje", requisitos: "Material seco y libre de grasa" },
  { idResiduo: 6, codigo: "RES-006", nombre: "Plástico PET", categoria: "APROVECHABLE", peligrosidad: "BAJA", unidad: "kg", plazoMaximoDias: 60, tratamiento: "Reciclaje", requisitos: "Envases vacíos y compactados" },
  { idResiduo: 7, codigo: "RES-007", nombre: "Residuos comunes", categoria: "COMUN", peligrosidad: "BAJA", unidad: "kg", plazoMaximoDias: 7, tratamiento: "Relleno sanitario", requisitos: "Bolsa negra en contenedor tapado" },
  { idResiduo: 8, codigo: "RES-008", nombre: "Material absorbente con hidrocarburos", categoria: "PELIGROSO", peligrosidad: "ALTA", unidad: "kg", plazoMaximoDias: 30, tratamiento: "Incineración controlada", requisitos: "Cilindro con tapa hermética rotulado" },
  { idResiduo: 9, codigo: "RES-009", nombre: "Residuos biocontaminados", categoria: "ESPECIAL", peligrosidad: "ALTA", unidad: "kg", plazoMaximoDias: 7, tratamiento: "Esterilización e incineración", requisitos: "Bolsa roja en recipiente rígido con tapa" },
  { idResiduo: 10, codigo: "RES-010", nombre: "Lodos de mantenimiento", categoria: "PELIGROSO", peligrosidad: "MEDIA", unidad: "kg", plazoMaximoDias: 30, tratamiento: "Tratamiento fisicoquímico", requisitos: "Cilindro hermético sin filtraciones" },
  { idResiduo: 11, codigo: "RES-011", nombre: "Madera de embalaje", categoria: "APROVECHABLE", peligrosidad: "BAJA", unidad: "kg", plazoMaximoDias: 90, tratamiento: "Reutilización o compostaje", requisitos: "Sin clavos expuestos ni tratamiento químico" },
]

const AREAS = [
  { idArea: 1, codigo: "AR-PRO", nombre: "Producción", proceso: "Fabricación y ensamblaje", responsable: "Rosa Huamán Flores", estado: "ACTIVA" },
  { idArea: 2, codigo: "AR-MAN", nombre: "Mantenimiento", proceso: "Mantenimiento de maquinaria y equipos", responsable: "Luis Torres Cáceres", estado: "ACTIVA" },
  { idArea: 3, codigo: "AR-ALM", nombre: "Almacenes", proceso: "Recepción y despacho de materiales", responsable: "Luis Torres Cáceres", estado: "ACTIVA" },
  { idArea: 4, codigo: "AR-OFI", nombre: "Oficinas", proceso: "Administración y tópico", responsable: "Rosa Huamán Flores", estado: "ACTIVA" },
  { idArea: 5, codigo: "AR-PAT", nombre: "Patio de residuos", proceso: "Acopio y almacenamiento temporal", responsable: "Luis Torres Cáceres", estado: "ACTIVA" },
  { idArea: 6, codigo: "AR-LAB", nombre: "Laboratorio de calidad", proceso: "Control de calidad de insumos", responsable: "Ana Quispe Mamani", estado: "INACTIVA" },
]

const CONTENEDORES = [
  { idContenedor: 1, codigo: "CT-101", tipo: "Cilindro metálico 200 L", capacidad: 200, idResiduo: 1, ubicacion: "Patio de residuos · zona A", estado: "DISPONIBLE", stockInicial: 20, fechaStock: hace(27, 10) },
  { idContenedor: 2, codigo: "CT-102", tipo: "Jaula metálica 1 t", capacidad: 1000, idResiduo: 2, ubicacion: "Patio de residuos · zona B", estado: "DISPONIBLE", stockInicial: 310, fechaStock: hace(18, 15) },
  { idContenedor: 3, codigo: "CT-103", tipo: "Contenedor con tapa 240 L", capacidad: 40, idResiduo: 3, ubicacion: "Patio de residuos · zona A", estado: "DISPONIBLE", stockInicial: 6, fechaStock: hace(8, 11) },
  { idContenedor: 4, codigo: "CT-104", tipo: "Cilindro con tapa 200 L", capacidad: 120, idResiduo: 4, ubicacion: "Mantenimiento · taller", estado: "DISPONIBLE", stockInicial: 60, fechaStock: hace(9, 16) },
  { idContenedor: 5, codigo: "CT-105", tipo: "Contenedor azul 240 L", capacidad: 80, idResiduo: 5, ubicacion: "Oficinas · primer piso", estado: "DISPONIBLE", stockInicial: 28, fechaStock: hace(12, 9) },
  { idContenedor: 6, codigo: "CT-106", tipo: "Contenedor blanco 240 L", capacidad: 60, idResiduo: 6, ubicacion: "Producción · línea 2", estado: "DISPONIBLE", stockInicial: 0, fechaStock: null },
  { idContenedor: 7, codigo: "CT-107", tipo: "Contenedor negro 1100 L", capacidad: 300, idResiduo: 7, ubicacion: "Patio de residuos · zona C", estado: "DISPONIBLE", stockInicial: 105, fechaStock: hace(1, 8) },
  { idContenedor: 8, codigo: "CT-108", tipo: "Cilindro con tapa 200 L", capacidad: 100, idResiduo: 8, ubicacion: "Mantenimiento · taller", estado: "DISPONIBLE", stockInicial: 0, fechaStock: null },
  { idContenedor: 9, codigo: "CT-109", tipo: "Recipiente rojo 60 L", capacidad: 20, idResiduo: 9, ubicacion: "Oficinas · tópico", estado: "DISPONIBLE", stockInicial: 0, fechaStock: null },
  { idContenedor: 10, codigo: "CT-110", tipo: "Cilindro metálico 200 L", capacidad: 200, idResiduo: 1, ubicacion: "Patio de residuos · zona A", estado: "MANTENIMIENTO", stockInicial: 0, fechaStock: null },
  { idContenedor: 11, codigo: "CT-111", tipo: "Cilindro hermético 200 L", capacidad: 150, idResiduo: 10, ubicacion: "Mantenimiento · taller", estado: "DISPONIBLE", stockInicial: 0, fechaStock: null },
]

const generacion = (id, idArea, idResiduo, idContenedor, cantidad, fecha, responsable, extra = {}) => ({
  idGeneracion: id,
  codigo: `GEN-0${100 + id}`,
  idArea,
  idResiduo,
  idContenedor,
  cantidad,
  fecha,
  responsable,
  condicionEnvase: "BUEN_ESTADO",
  segregacionCorrecta: true,
  estadoValidacion: "VALIDADA",
  observaciones: "",
  evidencia: null,
  validadoPor: "Ana Quispe Mamani",
  fechaValidacion: fecha,
  observacionValidacion: "",
  ...extra,
})

const GENERACIONES = [
  generacion(1, 1, 2, 2, 300, hace(26, 10, 30), "Rosa Huamán Flores", { evidencia: "gen-0101-chatarra.jpg", fechaValidacion: hace(26, 15) }),
  generacion(2, 4, 5, 5, 35, hace(19, 9, 15), "Rosa Huamán Flores", { fechaValidacion: hace(19, 12) }),
  generacion(3, 3, 4, 4, 60, hace(14, 11), "Luis Torres Cáceres", { evidencia: "gen-0103-epp.jpg", fechaValidacion: hace(14, 16) }),
  generacion(4, 1, 3, 3, 18, hace(9, 8, 40), "Rosa Huamán Flores", { evidencia: "gen-0104-envases.jpg", fechaValidacion: hace(9, 10) }),
  generacion(5, 1, 2, 2, 120, hace(6, 14, 10), "Rosa Huamán Flores", { fechaValidacion: hace(6, 17) }),
  generacion(6, 2, 1, 1, 40, hace(5, 9, 30), "Luis Torres Cáceres", { evidencia: "gen-0106-aceite.jpg", fechaValidacion: hace(5, 11) }),
  generacion(7, 2, 1, 1, 88, hace(4, 10, 45), "Luis Torres Cáceres", { evidencia: "gen-0107-aceite.jpg", fechaValidacion: hace(4, 15) }),
  generacion(8, 2, 8, 8, 30, hace(4, 16, 20), "Luis Torres Cáceres", {
    condicionEnvase: "DETERIORADO",
    estadoValidacion: "OBSERVADA",
    evidencia: "gen-0108-absorbente.jpg",
    fechaValidacion: hace(3, 11, 5),
    observacionValidacion: "Rotular el cilindro y reemplazar la tapa antes de volver a enviar el registro.",
    observaciones: "La tapa del cilindro está rota.",
  }),
  generacion(9, 1, 6, 6, 22, hace(3, 10), "Rosa Huamán Flores", { fechaValidacion: hace(3, 11) }),
  generacion(10, 2, 4, 4, 50, hace(3, 15, 30), "Luis Torres Cáceres", { evidencia: "gen-0110-epp.jpg", fechaValidacion: hace(3, 17) }),
  generacion(11, 4, 7, 7, 45, hace(2, 17, 10), "Rosa Huamán Flores", { estadoValidacion: "PENDIENTE", validadoPor: null, fechaValidacion: null }),
  generacion(12, 1, 2, 2, 210, hace(1, 11, 25), "Rosa Huamán Flores", {
    estadoValidacion: "PENDIENTE",
    validadoPor: null,
    fechaValidacion: null,
    evidencia: "gen-0112-chatarra.jpg",
    observaciones: "Incluye retazos con restos de pintura separados en saco aparte.",
  }),
  generacion(13, 4, 9, 9, 4, hace(1, 15, 40), "Rosa Huamán Flores", { estadoValidacion: "PENDIENTE", validadoPor: null, fechaValidacion: null }),
  generacion(14, 2, 10, 11, 35, haceHoras(3), "Luis Torres Cáceres", {
    estadoValidacion: "PENDIENTE",
    validadoPor: null,
    fechaValidacion: null,
    segregacionCorrecta: false,
    observaciones: "Se detectó agua en el cilindro; se drenó antes del pesaje.",
  }),
]

const MOVIMIENTOS = [
  { idMovimiento: 15, codigo: "MOV-0215", tipo: "INGRESO", fecha: haceHoras(3), idResiduo: 10, origen: "Mantenimiento", destino: "CT-111", cantidad: 35, referencia: "GEN-0114", usuario: "Luis Torres Cáceres", motivo: "Registro de generación", afectaStock: false },
  { idMovimiento: 14, codigo: "MOV-0214", tipo: "SALIDA", fecha: haceHoras(2), idResiduo: 3, origen: "CT-103", destino: "Transportes Andina SAC · V2A-845", cantidad: 18, referencia: "OR-1040", usuario: "Jorge Ccori Apaza", motivo: "Salida por recolección", afectaStock: false },
  { idMovimiento: 13, codigo: "MOV-0213", tipo: "INGRESO", fecha: hace(1, 15, 40), idResiduo: 9, origen: "Oficinas", destino: "CT-109", cantidad: 4, referencia: "GEN-0113", usuario: "Rosa Huamán Flores", motivo: "Registro de generación", afectaStock: false },
  { idMovimiento: 12, codigo: "MOV-0212", tipo: "INGRESO", fecha: hace(1, 11, 25), idResiduo: 2, origen: "Producción", destino: "CT-102", cantidad: 210, referencia: "GEN-0112", usuario: "Rosa Huamán Flores", motivo: "Registro de generación", afectaStock: false },
  { idMovimiento: 11, codigo: "MOV-0211", tipo: "PESAJE", fecha: hace(2, 8, 30), idResiduo: 4, origen: "CT-104", destino: "CT-104", cantidad: 110, referencia: "CT-104", usuario: "Luis Torres Cáceres", motivo: "Pesaje de verificación semanal", afectaStock: false },
  { idMovimiento: 10, codigo: "MOV-0210", tipo: "INGRESO", fecha: hace(2, 17, 10), idResiduo: 7, origen: "Oficinas", destino: "CT-107", cantidad: 45, referencia: "GEN-0111", usuario: "Rosa Huamán Flores", motivo: "Registro de generación", afectaStock: false },
  { idMovimiento: 9, codigo: "MOV-0209", tipo: "SALIDA", fecha: hace(3, 8, 15), idResiduo: 4, origen: "CT-104", destino: "Transportes Andina SAC · V2A-845", cantidad: 60, referencia: "OR-1039", usuario: "Jorge Ccori Apaza", motivo: "Salida por recolección", afectaStock: false },
  { idMovimiento: 8, codigo: "MOV-0208", tipo: "TRASLADO", fecha: hace(4, 9, 0), idResiduo: 1, origen: "CT-110", destino: "CT-101", cantidad: 20, referencia: "CT-110", usuario: "Luis Torres Cáceres", motivo: "Contenedor en mantenimiento", afectaStock: false },
  { idMovimiento: 7, codigo: "MOV-0207", tipo: "INGRESO", fecha: hace(4, 10, 45), idResiduo: 1, origen: "Mantenimiento", destino: "CT-101", cantidad: 88, referencia: "GEN-0107", usuario: "Luis Torres Cáceres", motivo: "Registro de generación", afectaStock: false },
  { idMovimiento: 6, codigo: "MOV-0206", tipo: "INGRESO", fecha: hace(5, 9, 30), idResiduo: 1, origen: "Mantenimiento", destino: "CT-101", cantidad: 40, referencia: "GEN-0106", usuario: "Luis Torres Cáceres", motivo: "Registro de generación", afectaStock: false },
]

const TRANSPORTISTAS = [
  {
    idTransportista: 1,
    empresa: "Transportes Andina SAC",
    autorizacion: "EO-RS-00312-2024-MINAM",
    contacto: "Jorge Ccori Apaza · 959 412 377",
    vigencia: dia(160),
    vehiculos: [
      { idVehiculo: 1, placa: "V2A-845", tipo: "Camión furgón", capacidad: 4000, estado: "OPERATIVO" },
      { idVehiculo: 2, placa: "V3B-112", tipo: "Camión cisterna", capacidad: 3000, estado: "OPERATIVO" },
    ],
  },
  {
    idTransportista: 2,
    empresa: "EcoTrans Perú EIRL",
    autorizacion: "EO-RS-00458-2023-MINAM",
    contacto: "Lucía Paredes Zea · 954 118 260",
    vigencia: dia(20),
    vehiculos: [
      { idVehiculo: 3, placa: "VBK-307", tipo: "Camión baranda", capacidad: 6000, estado: "OPERATIVO" },
      { idVehiculo: 4, placa: "VBK-411", tipo: "Camión furgón", capacidad: 3500, estado: "EN_MANTENIMIENTO" },
    ],
  },
  {
    idTransportista: 3,
    empresa: "Recolectora Sur SAC",
    autorizacion: "EO-RS-00127-2022-MINAM",
    contacto: "Raúl Chávez Pinto · 958 330 941",
    vigencia: dia(-45),
    vehiculos: [{ idVehiculo: 5, placa: "V7C-520", tipo: "Camión compactador", capacidad: 8000, estado: "OPERATIVO" }],
  },
]

const GESTORES = [
  { idGestor: 1, empresa: "Gestora Ambiental del Sur SAC", autorizacion: "EO-RS-00089-2023-MINAM", procesoDestino: "Disposición final en relleno de seguridad", vigencia: dia(300) },
  { idGestor: 2, empresa: "Reciclajes Arequipa SAC", autorizacion: "EO-RS-00274-2024-MINAM", procesoDestino: "Valorización de metales, papel y plásticos", vigencia: dia(180) },
  { idGestor: 3, empresa: "Termo Ambiental Perú SAC", autorizacion: "EO-RS-00415-2024-MINAM", procesoDestino: "Tratamiento térmico (incineración controlada)", vigencia: dia(25) },
]

const RECOLECCIONES = [
  {
    idRecoleccion: 6,
    numeroOrden: "OR-1042",
    estado: "PENDIENTE",
    fechaSolicitud: hace(0, 8, 10),
    solicitadoPor: "Ana Quispe Mamani",
    fechaProgramada: dia(2),
    idTransportista: 1,
    idVehiculo: 2,
    idGestor: 1,
    detalles: [{ idGeneracion: 6 }],
    observaciones: "Coordinar el acceso del camión cisterna por la puerta 2.",
    aprobacion: null,
    salida: null,
    recepcion: null,
    destino: null,
    cierre: null,
  },
  {
    idRecoleccion: 5,
    numeroOrden: "OR-1041",
    estado: "PROGRAMADA",
    fechaSolicitud: hace(2, 10),
    solicitadoPor: "Ana Quispe Mamani",
    fechaProgramada: dia(1),
    idTransportista: 2,
    idVehiculo: 3,
    idGestor: 2,
    detalles: [{ idGeneracion: 5 }],
    observaciones: "",
    aprobacion: { fecha: hace(1, 9), usuario: "Ana Quispe Mamani" },
    salida: null,
    recepcion: null,
    destino: null,
    cierre: null,
  },
  {
    idRecoleccion: 4,
    numeroOrden: "OR-1040",
    estado: "EN_TRANSITO",
    fechaSolicitud: hace(3, 9),
    solicitadoPor: "Ana Quispe Mamani",
    fechaProgramada: dia(0),
    idTransportista: 1,
    idVehiculo: 1,
    idGestor: 1,
    detalles: [{ idGeneracion: 4 }],
    observaciones: "Residuo peligroso: verificar rotulado de cada envase.",
    aprobacion: { fecha: hace(2, 15), usuario: "Ana Quispe Mamani" },
    salida: { fecha: haceHoras(2), documento: "MRSP-2026-0412", usuario: "Jorge Ccori Apaza", observaciones: "" },
    recepcion: null,
    destino: null,
    cierre: null,
  },
  {
    idRecoleccion: 3,
    numeroOrden: "OR-1039",
    estado: "RECIBIDA",
    fechaSolicitud: hace(6, 9),
    solicitadoPor: "Ana Quispe Mamani",
    fechaProgramada: dia(-3),
    idTransportista: 1,
    idVehiculo: 1,
    idGestor: 3,
    detalles: [{ idGeneracion: 3 }],
    observaciones: "",
    aprobacion: { fecha: hace(5, 10), usuario: "Ana Quispe Mamani" },
    salida: { fecha: hace(3, 8, 15), documento: "MRSP-2026-0398", usuario: "Jorge Ccori Apaza", observaciones: "" },
    recepcion: { fecha: hace(2, 16, 30), cantidadRecibida: 58, conforme: true, usuario: "Marta Valdivia Salas", observaciones: "Diferencia de 2 kg por humedad del material." },
    destino: { tipo: "TRATAMIENTO", tratamiento: "Incineración controlada" },
    cierre: null,
  },
  {
    idRecoleccion: 2,
    numeroOrden: "OR-1038",
    estado: "CERRADA",
    fechaSolicitud: hace(17, 9),
    solicitadoPor: "Ana Quispe Mamani",
    fechaProgramada: dia(-15),
    idTransportista: 2,
    idVehiculo: 3,
    idGestor: 2,
    detalles: [{ idGeneracion: 2 }],
    observaciones: "",
    aprobacion: { fecha: hace(16, 9), usuario: "Ana Quispe Mamani" },
    salida: { fecha: hace(15, 9, 20), documento: "GR-0001-004588", usuario: "Jorge Ccori Apaza", observaciones: "" },
    recepcion: { fecha: hace(15, 14), cantidadRecibida: 35, conforme: true, usuario: "Marta Valdivia Salas", observaciones: "" },
    destino: { tipo: "VALORIZACION", tratamiento: "Reciclaje de papel y cartón" },
    cierre: { fecha: hace(14, 10), usuario: "Ana Quispe Mamani" },
  },
  {
    idRecoleccion: 1,
    numeroOrden: "OR-1037",
    estado: "CERRADA",
    fechaSolicitud: hace(24, 9),
    solicitadoPor: "Ana Quispe Mamani",
    fechaProgramada: dia(-21),
    idTransportista: 2,
    idVehiculo: 3,
    idGestor: 2,
    detalles: [{ idGeneracion: 1 }],
    observaciones: "",
    aprobacion: { fecha: hace(23, 9), usuario: "Ana Quispe Mamani" },
    salida: { fecha: hace(21, 13, 10), documento: "GR-0001-004512", usuario: "Jorge Ccori Apaza", observaciones: "Llegada con 3 horas de retraso." },
    recepcion: { fecha: hace(21, 17), cantidadRecibida: 298, conforme: true, usuario: "Marta Valdivia Salas", observaciones: "" },
    destino: { tipo: "VALORIZACION", tratamiento: "Reciclaje de metales" },
    cierre: { fecha: hace(20, 10), usuario: "Ana Quispe Mamani" },
  },
]

const INCIDENCIAS = [
  {
    idIncidencia: 31,
    codigo: "INC-0031",
    tipo: "DERRAME",
    descripcion: "Derrame de aproximadamente 3 L de aceite usado al trasvasar al cilindro CT-101.",
    idArea: 2,
    referencia: "CT-101",
    severidad: "ALTA",
    estado: "EN_SEGUIMIENTO",
    fecha: hace(2, 10, 20),
    reportadoPor: "Luis Torres Cáceres",
    responsable: "Luis Torres Cáceres",
    accionCorrectiva: "Contención con material absorbente, limpieza de la zona y capacitación en trasvase seguro.",
    seguimiento: [
      { fecha: hace(2, 11), usuario: "Ana Quispe Mamani", nota: "Se asignó responsable y se aplicó contención inmediata." },
    ],
    fechaCierre: null,
  },
  {
    idIncidencia: 30,
    codigo: "INC-0030",
    tipo: "MEZCLA",
    descripcion: "Se encontraron envases con residuo químico mezclados con la chatarra del patio.",
    idArea: 1,
    referencia: "CT-102",
    severidad: "MEDIA",
    estado: "ABIERTA",
    fecha: hace(1, 15, 5),
    reportadoPor: "Rosa Huamán Flores",
    responsable: null,
    accionCorrectiva: "",
    seguimiento: [],
    fechaCierre: null,
  },
  {
    idIncidencia: 29,
    codigo: "INC-0029",
    tipo: "ENVASE_DANADO",
    descripcion: "Cilindro de material absorbente con la tapa rota y sin rótulo.",
    idArea: 2,
    referencia: "GEN-0108",
    severidad: "MEDIA",
    estado: "ABIERTA",
    fecha: hace(3, 11, 20),
    reportadoPor: "Ana Quispe Mamani",
    responsable: null,
    accionCorrectiva: "",
    seguimiento: [],
    fechaCierre: null,
  },
  {
    idIncidencia: 28,
    codigo: "INC-0028",
    tipo: "RETRASO",
    descripcion: "La unidad de EcoTrans llegó 3 horas después de lo programado para la orden OR-1037.",
    idArea: 5,
    referencia: "OR-1037",
    severidad: "BAJA",
    estado: "CERRADA",
    fecha: hace(21, 13, 30),
    reportadoPor: "Ana Quispe Mamani",
    responsable: "Ana Quispe Mamani",
    accionCorrectiva: "Se acordó con EcoTrans una ventana horaria fija de 8:00 a 10:00.",
    seguimiento: [
      { fecha: hace(21, 15), usuario: "Ana Quispe Mamani", nota: "Se coordinó con el transportista." },
      { fecha: hace(20, 10, 30), usuario: "Ana Quispe Mamani", nota: "Incidencia cerrada: acuerdo firmado." },
    ],
    fechaCierre: hace(20, 10, 30),
  },
  {
    idIncidencia: 27,
    codigo: "INC-0027",
    tipo: "RECHAZO",
    descripcion: "El gestor rechazó 2 envases sin rotular de la orden OR-1034.",
    idArea: 1,
    referencia: "OR-1034",
    severidad: "MEDIA",
    estado: "CERRADA",
    fecha: hace(34, 16),
    reportadoPor: "Marta Valdivia Salas",
    responsable: "Rosa Huamán Flores",
    accionCorrectiva: "Se rotularon los envases y se reprogramó su recolección.",
    seguimiento: [{ fecha: hace(31, 9), usuario: "Ana Quispe Mamani", nota: "Incidencia cerrada." }],
    fechaCierre: hace(31, 9),
  },
]

const OBSERVACIONES = [
  { idObservacion: 6, fecha: haceHoras(2), autor: "Jorge Ccori Apaza", rol: "Transportista", ambito: "TRASLADO", referencia: "OR-1040", texto: "Salida con 30 minutos de retraso por la verificación del manifiesto." },
  { idObservacion: 5, fecha: hace(1, 11, 40), autor: "Rosa Huamán Flores", rol: "Generador", ambito: "GENERACION", referencia: "GEN-0112", texto: "Los retazos con restos de pintura se separaron en un saco aparte." },
  { idObservacion: 4, fecha: hace(2, 8, 45), autor: "Luis Torres Cáceres", rol: "Almacenero", ambito: "ALMACENAMIENTO", referencia: "CT-104", texto: "El cilindro de EPP está cerca de su capacidad; conviene programar recolección." },
  { idObservacion: 3, fecha: hace(2, 16, 40), autor: "Marta Valdivia Salas", rol: "Gestor autorizado", ambito: "RECEPCION", referencia: "OR-1039", texto: "Se recibieron 58 kg; la diferencia de 2 kg se debe a la humedad del material." },
  { idObservacion: 2, fecha: hace(4, 9, 10), autor: "Luis Torres Cáceres", rol: "Almacenero", ambito: "ALMACENAMIENTO", referencia: "CT-110", texto: "Se trasladó el aceite del CT-110 al CT-101 para dar mantenimiento al cilindro." },
  { idObservacion: 1, fecha: hace(21, 17, 10), autor: "Marta Valdivia Salas", rol: "Gestor autorizado", ambito: "RECEPCION", referencia: "OR-1037", texto: "Material recibido conforme y sin restos de otros residuos." },
]

const DOCUMENTOS = [
  { idDocumento: 51, tipo: "MANIFIESTO", nombreArchivo: "manifiesto-MRSP-2026-0412.pdf", referencia: "OR-1040", descripcion: "Manifiesto de la salida de envases químicos", fecha: haceHoras(2), subidoPor: "Jorge Ccori Apaza", estado: "PENDIENTE", motivoRechazo: "" },
  { idDocumento: 50, tipo: "EVIDENCIA_FOTO", nombreArchivo: "gen-0112-chatarra.jpg", referencia: "GEN-0112", descripcion: "Fotografía del pesaje de chatarra", fecha: hace(1, 11, 30), subidoPor: "Rosa Huamán Flores", estado: "PENDIENTE", motivoRechazo: "" },
  { idDocumento: 49, tipo: "MANIFIESTO", nombreArchivo: "manifiesto-MRSP-2026-0398.pdf", referencia: "OR-1039", descripcion: "Manifiesto de la salida de EPP contaminado", fecha: hace(3, 8, 20), subidoPor: "Jorge Ccori Apaza", estado: "VALIDADO", motivoRechazo: "" },
  { idDocumento: 48, tipo: "ACTA_RECEPCION", nombreArchivo: "acta-recepcion-OR-1039.pdf", referencia: "OR-1039", descripcion: "Acta de recepción firmada por el gestor", fecha: hace(2, 16, 45), subidoPor: "Marta Valdivia Salas", estado: "VALIDADO", motivoRechazo: "" },
  { idDocumento: 47, tipo: "CERTIFICADO", nombreArchivo: "certificado-CV-2026-0201.pdf", referencia: "OR-1038", descripcion: "Certificado de valorización de papel y cartón", fecha: hace(15, 15), subidoPor: "Marta Valdivia Salas", estado: "VALIDADO", motivoRechazo: "" },
  { idDocumento: 46, tipo: "GUIA_REMISION", nombreArchivo: "guia-GR-0001-004588.pdf", referencia: "OR-1038", descripcion: "Guía de remisión del traslado", fecha: hace(15, 9, 25), subidoPor: "Jorge Ccori Apaza", estado: "VALIDADO", motivoRechazo: "" },
  { idDocumento: 45, tipo: "CERTIFICADO", nombreArchivo: "certificado-CV-2026-0187.pdf", referencia: "OR-1037", descripcion: "Certificado de valorización de metales", fecha: hace(21, 17, 30), subidoPor: "Marta Valdivia Salas", estado: "VALIDADO", motivoRechazo: "" },
  { idDocumento: 44, tipo: "GUIA_REMISION", nombreArchivo: "guia-GR-0001-004512.pdf", referencia: "OR-1037", descripcion: "Guía de remisión del traslado", fecha: hace(21, 13, 15), subidoPor: "Jorge Ccori Apaza", estado: "VALIDADO", motivoRechazo: "" },
  { idDocumento: 43, tipo: "EVIDENCIA_FOTO", nombreArchivo: "gen-0108-absorbente.jpg", referencia: "GEN-0108", descripcion: "Fotografía del cilindro de material absorbente", fecha: hace(4, 16, 25), subidoPor: "Luis Torres Cáceres", estado: "RECHAZADO", motivoRechazo: "La fotografía no muestra el rótulo del cilindro." },
]

const AUDITORIA = [
  { idAuditoria: 16, fecha: haceHoras(2), usuario: "Jorge Ccori Apaza", accion: "REGISTRAR", entidad: "Recolección", registro: "OR-1040", detalle: "Registró la salida con el manifiesto MRSP-2026-0412" },
  { idAuditoria: 15, fecha: haceHoras(3), usuario: "Luis Torres Cáceres", accion: "CREAR", entidad: "Generación", registro: "GEN-0114", detalle: "Registró 35 kg de lodos de mantenimiento" },
  { idAuditoria: 14, fecha: hace(0, 8, 10), usuario: "Ana Quispe Mamani", accion: "CREAR", entidad: "Recolección", registro: "OR-1042", detalle: "Programó la recolección de 40 L de aceite usado" },
  { idAuditoria: 13, fecha: hace(1, 15, 5), usuario: "Rosa Huamán Flores", accion: "CREAR", entidad: "Incidencia", registro: "INC-0030", detalle: "Reportó una mezcla de residuos en el patio" },
  { idAuditoria: 12, fecha: hace(1, 9), usuario: "Ana Quispe Mamani", accion: "APROBAR", entidad: "Recolección", registro: "OR-1041", detalle: "Aprobó la orden de recolección" },
  { idAuditoria: 11, fecha: hace(2, 16, 30), usuario: "Marta Valdivia Salas", accion: "REGISTRAR", entidad: "Recolección", registro: "OR-1039", detalle: "Registró la recepción de 58 kg y su destino" },
  { idAuditoria: 10, fecha: hace(3, 17), usuario: "Ana Quispe Mamani", accion: "VALIDAR", entidad: "Generación", registro: "GEN-0110", detalle: "Validó el registro de generación" },
  { idAuditoria: 9, fecha: hace(3, 11, 5), usuario: "Ana Quispe Mamani", accion: "OBSERVAR", entidad: "Generación", registro: "GEN-0108", detalle: "Observó el registro: rotular el cilindro y reemplazar la tapa" },
  { idAuditoria: 8, fecha: hace(4, 9, 30), usuario: "Carlos Mendoza Ríos", accion: "MODIFICAR", entidad: "Contenedor", registro: "CT-110", detalle: "Cambió el estado a En mantenimiento" },
  { idAuditoria: 7, fecha: hace(5, 10), usuario: "Carlos Mendoza Ríos", accion: "CREAR", entidad: "Residuo", registro: "RES-011", detalle: "Registró el residuo Madera de embalaje" },
  { idAuditoria: 6, fecha: hace(6, 8), usuario: "Carlos Mendoza Ríos", accion: "DESACTIVAR", entidad: "Usuario", registro: "jperez", detalle: "Desactivó la cuenta del usuario" },
  { idAuditoria: 5, fecha: hace(7, 11), usuario: "Carlos Mendoza Ríos", accion: "MODIFICAR", entidad: "Transportista", registro: "EcoTrans Perú EIRL", detalle: "Registró el vehículo VBK-411 en mantenimiento" },
  { idAuditoria: 4, fecha: hace(14, 10), usuario: "Ana Quispe Mamani", accion: "CERRAR", entidad: "Recolección", registro: "OR-1038", detalle: "Cerró la orden con certificado CV-2026-0201" },
  { idAuditoria: 3, fecha: hace(15, 15), usuario: "Marta Valdivia Salas", accion: "ADJUNTAR", entidad: "Documento", registro: "certificado-CV-2026-0201.pdf", detalle: "Adjuntó el certificado de valorización" },
  { idAuditoria: 2, fecha: hace(20, 10), usuario: "Ana Quispe Mamani", accion: "CERRAR", entidad: "Recolección", registro: "OR-1037", detalle: "Cerró la orden con certificado CV-2026-0187" },
  { idAuditoria: 1, fecha: hace(20, 10, 30), usuario: "Ana Quispe Mamani", accion: "CERRAR", entidad: "Incidencia", registro: "INC-0028", detalle: "Cerró la incidencia de retraso en recolección" },
]

const CONFIGURACION_AUDITORIA = {
  eventos: { validaciones: true, modificaciones: true, cierres: true, accesos: false },
  retencionDias: "365",
}

const HISTORICO_MENSUAL = [
  { mes: mes(5), kg: 1180, registros: 42 },
  { mes: mes(4), kg: 1325, registros: 47 },
  { mes: mes(3), kg: 1260, registros: 44 },
  { mes: mes(2), kg: 1415, registros: 51 },
  { mes: mes(1), kg: 1370, registros: 49 },
]

const DESTINOS_SEMESTRE = { VALORIZACION: 3920, TRATAMIENTO: 1630, DISPOSICION: 760 }

export function crearSemilla() {
  return {
    permisos: PERMISOS,
    roles: ROLES,
    usuarios: USUARIOS,
    residuos: RESIDUOS,
    areas: AREAS,
    contenedores: CONTENEDORES,
    generaciones: GENERACIONES,
    movimientos: MOVIMIENTOS,
    transportistas: TRANSPORTISTAS,
    gestores: GESTORES,
    recolecciones: RECOLECCIONES,
    incidencias: INCIDENCIAS,
    observaciones: OBSERVACIONES,
    documentos: DOCUMENTOS,
    auditoria: AUDITORIA,
    configuracionAuditoria: CONFIGURACION_AUDITORIA,
    historicoMensual: HISTORICO_MENSUAL,
    destinosSemestre: DESTINOS_SEMESTRE,
  }
}
