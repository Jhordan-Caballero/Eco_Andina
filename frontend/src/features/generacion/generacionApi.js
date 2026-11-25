import { api, datos, formulario } from "@/lib/api"
import { aIso } from "@/lib/formato"

// CU08 y CU09: generación y segregación, validación y corrección de registros observados

export const listarGeneraciones = () => api.get("/generaciones").then(datos)

export const opcionesRegistro = () => api.get("/generaciones/opciones").then(datos)

export const detalleGeneracion = (idGeneracion) => api.get(`/generaciones/${idGeneracion}`).then(datos)

export function registrarGeneracion({ valores }) {
  const { evidencia, ...resto } = valores
  return api.post("/generaciones", formulario({ ...resto, fecha: aIso(valores.fecha) }, { evidencia })).then(datos)
}

export const validarGeneracion = ({ idGeneracion }) =>
  api.patch(`/generaciones/${idGeneracion}/validar`).then(datos)

export const observarGeneracion = ({ idGeneracion, observacion }) =>
  api.patch(`/generaciones/${idGeneracion}/observar`, { observacion }).then(datos)

export function corregirGeneracion({ idGeneracion, valores }) {
  const { evidencia, ...resto } = valores
  return api.put(`/generaciones/${idGeneracion}/correccion`, formulario(resto, { evidencia })).then(datos)
}
