import { api, datos, formulario } from "@/lib/api"

// CU17 y CU18: incidencias, acciones correctivas y observaciones operativas

export const listarIncidencias = () => api.get("/incidencias").then(datos)

export const listarObservaciones = () => api.get("/observaciones").then(datos)

export const opcionesIncidencia = () => api.get("/incidencias/opciones").then(datos)

export function reportarIncidencia({ valores }) {
  const { evidencia, ...resto } = valores
  return api.post("/incidencias", formulario(resto, { evidencia })).then(datos)
}

export const registrarSeguimiento = ({ idIncidencia, valores }) =>
  api.post(`/incidencias/${idIncidencia}/seguimiento`, valores).then(datos)

export const cerrarIncidencia = ({ idIncidencia, nota }) =>
  api.patch(`/incidencias/${idIncidencia}/cerrar`, { nota }).then(datos)

// El rol con el que se registra lo decide el backend a partir del usuario autenticado
export const registrarObservacion = ({ valores }) => api.post("/observaciones", valores).then(datos)
