export const ESTADO_GENERACION = {
  PENDIENTE: { label: "Pendiente", tone: "warning" },
  VALIDADA: { label: "Validada", tone: "success" },
  OBSERVADA: { label: "Observada", tone: "danger" },
}

export const ESTADO_RECOLECCION = {
  PENDIENTE: { label: "Pendiente", tone: "neutral" },
  PROGRAMADA: { label: "Programada", tone: "primary" },
  EN_TRANSITO: { label: "En tránsito", tone: "info" },
  RECIBIDA: { label: "Recibida", tone: "success" },
  CERRADA: { label: "Cerrada", tone: "muted" },
}

export const ESTADO_AREA = {
  ACTIVA: { label: "Activa", tone: "success" },
  INACTIVA: { label: "Inactiva", tone: "muted" },
}

export const SITUACION_CONTENEDOR = {
  DISPONIBLE: { label: "Disponible", tone: "success" },
  EN_USO: { label: "En uso", tone: "primary" },
  LLENO: { label: "Lleno", tone: "danger" },
  MANTENIMIENTO: { label: "En mantenimiento", tone: "muted" },
}

export const ESTADO_VEHICULO = {
  OPERATIVO: { label: "Operativo", tone: "success" },
  EN_MANTENIMIENTO: { label: "En mantenimiento", tone: "warning" },
  INACTIVO: { label: "Inactivo", tone: "muted" },
}

export const ESTADO_VIGENCIA = {
  VIGENTE: { label: "Vigente", tone: "success" },
  POR_VENCER: { label: "Por vencer", tone: "warning" },
  VENCIDA: { label: "Vencida", tone: "danger" },
}

export const ESTADO_INCIDENCIA = {
  ABIERTA: { label: "Abierta", tone: "danger" },
  EN_SEGUIMIENTO: { label: "En seguimiento", tone: "warning" },
  CERRADA: { label: "Cerrada", tone: "muted" },
}

export const SEVERIDAD = {
  ALTA: { label: "Alta", tone: "danger" },
  MEDIA: { label: "Media", tone: "warning" },
  BAJA: { label: "Baja", tone: "neutral" },
}

export const ESTADO_DOCUMENTO = {
  PENDIENTE: { label: "Pendiente", tone: "warning" },
  VALIDADO: { label: "Validado", tone: "success" },
  RECHAZADO: { label: "Rechazado", tone: "danger" },
}

export const ESTADO_USUARIO = {
  ACTIVO: { label: "Activo", tone: "success" },
  INACTIVO: { label: "Inactivo", tone: "muted" },
}

export const CATEGORIA_RESIDUO = {
  COMUN: { label: "Común", tone: "neutral" },
  APROVECHABLE: { label: "Aprovechable", tone: "success" },
  PELIGROSO: { label: "Peligroso", tone: "danger" },
  ESPECIAL: { label: "Especial / biocontaminado", tone: "warning" },
}

export const CONDICION_ENVASE = {
  BUEN_ESTADO: "Buen estado",
  DETERIORADO: "Envase deteriorado",
  SIN_ROTULO: "Sin rótulo",
  SIN_TAPA: "Sin tapa",
}

export const PELIGROSIDAD = {
  BAJA: "Baja",
  MEDIA: "Media",
  ALTA: "Alta",
}

export const TIPO_MOVIMIENTO = {
  INGRESO: { label: "Ingreso", tone: "success" },
  TRASLADO: { label: "Traslado interno", tone: "info" },
  PESAJE: { label: "Pesaje", tone: "neutral" },
  SALIDA: { label: "Salida", tone: "primary" },
}

export const TIPO_INCIDENCIA = {
  DERRAME: "Derrame",
  MEZCLA: "Mezcla de residuos",
  ENVASE_DANADO: "Envase dañado",
  RETRASO: "Retraso en recolección",
  RECHAZO: "Rechazo en recepción",
}

export const TIPO_DOCUMENTO = {
  MANIFIESTO: "Manifiesto de residuos peligrosos",
  GUIA_REMISION: "Guía de remisión",
  ACTA_RECEPCION: "Acta de recepción",
  CERTIFICADO: "Certificado de tratamiento o disposición",
  EVIDENCIA_FOTO: "Fotografía de evidencia",
  OTRO: "Otro documento",
}

export const TIPO_DESTINO = {
  VALORIZACION: "Valorización",
  TRATAMIENTO: "Tratamiento",
  DISPOSICION: "Disposición final",
}

export const AMBITO_OBSERVACION = {
  GENERACION: "Generación",
  ALMACENAMIENTO: "Almacenamiento",
  TRASLADO: "Traslado",
  RECEPCION: "Recepción",
}

export const ACCION_AUDITORIA = {
  CREAR: { label: "Crear", tone: "success" },
  MODIFICAR: { label: "Modificar", tone: "info" },
  VALIDAR: { label: "Validar", tone: "success" },
  OBSERVAR: { label: "Observar", tone: "warning" },
  APROBAR: { label: "Aprobar", tone: "primary" },
  REGISTRAR: { label: "Registrar", tone: "info" },
  CERRAR: { label: "Cerrar", tone: "muted" },
  ACTIVAR: { label: "Activar", tone: "success" },
  DESACTIVAR: { label: "Desactivar", tone: "danger" },
  ADJUNTAR: { label: "Adjuntar", tone: "neutral" },
  RECHAZAR: { label: "Rechazar", tone: "danger" },
  EXPORTAR: { label: "Exportar", tone: "info" },
  ACCESO: { label: "Inicio de sesión", tone: "neutral" },
}

export const opciones = (mapa) =>
  Object.entries(mapa).map(([value, item]) => ({
    value,
    label: typeof item === "string" ? item : item.label,
  }))

export const etiqueta = (mapa, valor) => {
  const item = mapa[valor]
  if (!item) return valor ?? "—"
  return typeof item === "string" ? item : item.label
}
