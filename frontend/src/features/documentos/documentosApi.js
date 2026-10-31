import { TIPO_DOCUMENTO } from "@/lib/estados"
import { ahora, auditar, db, esperar, fallar, siguienteId } from "@/mocks/store"

export const PERMISOS_REVISION = ["GENERACION_VALIDAR", "RECOLECCION_PROGRAMAR"]

export const listarDocumentos = () =>
  esperar([...db.documentos].sort((a, b) => b.fecha.localeCompare(a.fecha)))

export const referenciasDisponibles = () =>
  esperar([
    ...[...db.recolecciones]
      .sort((a, b) => b.idRecoleccion - a.idRecoleccion)
      .map((item) => ({ value: item.numeroOrden, label: `${item.numeroOrden} · Orden de recolección` })),
    ...[...db.generaciones]
      .sort((a, b) => b.idGeneracion - a.idGeneracion)
      .map((item) => ({ value: item.codigo, label: `${item.codigo} · Registro de generación` })),
    ...db.incidencias.map((item) => ({ value: item.codigo, label: `${item.codigo} · Incidencia` })),
  ])

export function registrarDocumento({ tipo, nombreArchivo, referencia, descripcion }, actor) {
  const documento = {
    idDocumento: siguienteId(db.documentos, "idDocumento"),
    tipo,
    nombreArchivo,
    referencia,
    descripcion: descripcion?.trim() || TIPO_DOCUMENTO[tipo],
    fecha: ahora(),
    subidoPor: actor,
    estado: "PENDIENTE",
    motivoRechazo: "",
  }
  db.documentos.unshift(documento)
  auditar(actor, "ADJUNTAR", "Documento", nombreArchivo, `Adjuntó ${TIPO_DOCUMENTO[tipo].toLowerCase()} a ${referencia}`)
  return documento
}

export function adjuntarDocumento({ valores, actor }) {
  const orden = db.recolecciones.find((item) => item.numeroOrden === valores.referencia)
  if (orden?.estado === "CERRADA") {
    return fallar(`La orden ${orden.numeroOrden} ya está cerrada.`)
  }
  return esperar(registrarDocumento(valores, actor))
}

const documentoPorId = (id) => db.documentos.find((item) => item.idDocumento === Number(id))

export function validarDocumento({ idDocumento, actor }) {
  const documento = documentoPorId(idDocumento)
  if (documento.estado !== "PENDIENTE") return fallar("El documento ya fue revisado.")
  Object.assign(documento, { estado: "VALIDADO", motivoRechazo: "" })
  auditar(actor, "VALIDAR", "Documento", documento.nombreArchivo, `Validó el documento de ${documento.referencia}`)
  return esperar(documento)
}

export function rechazarDocumento({ idDocumento, motivo, actor }) {
  const documento = documentoPorId(idDocumento)
  if (documento.estado !== "PENDIENTE") return fallar("El documento ya fue revisado.")
  Object.assign(documento, { estado: "RECHAZADO", motivoRechazo: motivo.trim() })
  auditar(actor, "RECHAZAR", "Documento", documento.nombreArchivo, `Rechazó el documento: ${motivo.trim()}`)
  return esperar(documento)
}
