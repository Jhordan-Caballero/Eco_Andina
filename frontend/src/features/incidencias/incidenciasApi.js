import { registrarDocumento } from "@/features/documentos/documentosApi"
import { AMBITO_OBSERVACION, TIPO_INCIDENCIA } from "@/lib/estados"
import {
  ahora,
  areaPorId,
  auditar,
  codigo,
  db,
  esperar,
  fallar,
  nombreCompleto,
  siguienteId,
} from "@/mocks/store"

const ORDEN_ESTADO = { ABIERTA: 0, EN_SEGUIMIENTO: 1, CERRADA: 2 }

export const listarIncidencias = () =>
  esperar(
    db.incidencias
      .map((item) => ({
        ...item,
        area: areaPorId(item.idArea),
        evidencias: db.documentos.filter((documento) => documento.referencia === item.codigo),
      }))
      .sort((a, b) => ORDEN_ESTADO[a.estado] - ORDEN_ESTADO[b.estado] || b.fecha.localeCompare(a.fecha))
  )

export const listarObservaciones = () =>
  esperar([...db.observaciones].sort((a, b) => b.fecha.localeCompare(a.fecha)))

export const opcionesIncidencia = () =>
  esperar({
    areas: db.areas.filter((item) => item.estado === "ACTIVA"),
    referencias: [
      ...[...db.recolecciones].reverse().map((item) => ({ value: item.numeroOrden, label: `${item.numeroOrden} · Orden de recolección` })),
      ...db.contenedores.map((item) => ({ value: item.codigo, label: `${item.codigo} · Contenedor` })),
      ...[...db.generaciones].reverse().map((item) => ({ value: item.codigo, label: `${item.codigo} · Registro de generación` })),
    ],
    responsables: db.usuarios.filter((item) => item.estado === "ACTIVO").map(nombreCompleto),
  })

const incidenciaPorId = (id) => db.incidencias.find((item) => item.idIncidencia === Number(id))

export function reportarIncidencia({ valores, actor }) {
  const id = siguienteId(db.incidencias, "idIncidencia")
  const incidencia = {
    idIncidencia: id,
    codigo: codigo("INC", id),
    tipo: valores.tipo,
    descripcion: valores.descripcion.trim(),
    idArea: Number(valores.idArea),
    referencia: valores.referencia || "",
    severidad: valores.severidad,
    estado: "ABIERTA",
    fecha: ahora(),
    reportadoPor: actor,
    responsable: null,
    accionCorrectiva: "",
    seguimiento: [],
    fechaCierre: null,
  }
  db.incidencias.unshift(incidencia)
  if (valores.evidencia) {
    registrarDocumento(
      { tipo: "EVIDENCIA_FOTO", nombreArchivo: valores.evidencia, referencia: incidencia.codigo, descripcion: "Evidencia de la incidencia" },
      actor
    )
  }
  auditar(actor, "CREAR", "Incidencia", incidencia.codigo, `Reportó: ${TIPO_INCIDENCIA[incidencia.tipo].toLowerCase()}`)
  return esperar(incidencia)
}

export function registrarSeguimiento({ idIncidencia, valores, actor }) {
  const incidencia = incidenciaPorId(idIncidencia)
  if (incidencia.estado === "CERRADA") return fallar("La incidencia ya está cerrada.")
  Object.assign(incidencia, {
    responsable: valores.responsable,
    accionCorrectiva: valores.accionCorrectiva.trim(),
    estado: "EN_SEGUIMIENTO",
  })
  incidencia.seguimiento.push({ fecha: ahora(), usuario: actor, nota: valores.nota.trim() })
  auditar(actor, "MODIFICAR", "Incidencia", incidencia.codigo, `Registró seguimiento: ${valores.nota.trim()}`)
  return esperar(incidencia)
}

export function cerrarIncidencia({ idIncidencia, nota, actor }) {
  const incidencia = incidenciaPorId(idIncidencia)
  if (!incidencia.responsable || !incidencia.accionCorrectiva) {
    return fallar("Asigna un responsable y una acción correctiva antes de cerrar.")
  }
  Object.assign(incidencia, { estado: "CERRADA", fechaCierre: ahora() })
  incidencia.seguimiento.push({ fecha: incidencia.fechaCierre, usuario: actor, nota: `Incidencia cerrada: ${nota.trim()}` })
  auditar(actor, "CERRAR", "Incidencia", incidencia.codigo, `Cerró la incidencia: ${nota.trim()}`)
  return esperar(incidencia)
}

export function registrarObservacion({ valores, actor, rol }) {
  const observacion = {
    idObservacion: siguienteId(db.observaciones, "idObservacion"),
    fecha: ahora(),
    autor: actor,
    rol,
    ambito: valores.ambito,
    referencia: valores.referencia || "",
    texto: valores.texto.trim(),
  }
  db.observaciones.unshift(observacion)
  auditar(
    actor,
    "REGISTRAR",
    "Observación",
    observacion.referencia || AMBITO_OBSERVACION[observacion.ambito],
    `Registró una observación de ${AMBITO_OBSERVACION[observacion.ambito].toLowerCase()}`
  )
  return esperar(observacion)
}
