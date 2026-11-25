import { api, datos, formulario } from "@/lib/api"
import { aIso } from "@/lib/formato"

// CU12–CU15: programación, consulta, salida, recepción y cierre de órdenes de recolección

export function accionPendiente(orden, hasPermission) {
  if (orden.estado === "PENDIENTE" && hasPermission("RECOLECCION_PROGRAMAR")) return "aprobar"
  if (orden.estado === "PROGRAMADA" && hasPermission("TRASLADO_REGISTRAR")) return "salida"
  if (orden.estado === "EN_TRANSITO" && hasPermission("RECEPCION_REGISTRAR")) return "recepcion"
  if (orden.estado === "RECIBIDA" && hasPermission("RECOLECCION_PROGRAMAR")) return "cerrar"
  return null
}

export const listarRecolecciones = () => api.get("/recolecciones").then(datos)

export const detalleRecoleccion = (idRecoleccion) => api.get(`/recolecciones/${idRecoleccion}`).then(datos)

export const opcionesProgramacion = () => api.get("/recolecciones/opciones").then(datos)

export const programarRecoleccion = ({ valores }) => api.post("/recolecciones", valores).then(datos)

export const aprobarRecoleccion = ({ idRecoleccion }) =>
  api.patch(`/recolecciones/${idRecoleccion}/aprobar`).then(datos)

export function registrarSalida({ idRecoleccion, valores }) {
  const { archivo, ...resto } = valores
  return api
    .post(`/recolecciones/${idRecoleccion}/salida`, formulario({ ...resto, fecha: aIso(valores.fecha) }, { archivo }))
    .then(datos)
}

export function registrarRecepcion({ idRecoleccion, valores }) {
  const { acta, certificado, ...resto } = valores
  return api
    .post(
      `/recolecciones/${idRecoleccion}/recepcion`,
      formulario({ ...resto, fecha: aIso(valores.fecha) }, { acta, certificado })
    )
    .then(datos)
}

export const cerrarRecoleccion = ({ idRecoleccion }) =>
  api.patch(`/recolecciones/${idRecoleccion}/cerrar`).then(datos)
