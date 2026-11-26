import { api, datos, formulario } from "@/lib/api"

// CU16: documentos y evidencias

export const PERMISOS_REVISION = ["GENERACION_VALIDAR", "RECOLECCION_PROGRAMAR"]

export const listarDocumentos = () => api.get("/documentos").then(datos)

export const referenciasDisponibles = () => api.get("/documentos/referencias").then(datos)

export function adjuntarDocumento({ valores }) {
  const { archivo, ...resto } = valores
  return api.post("/documentos", formulario(resto, { archivo })).then(datos)
}

export const validarDocumento = ({ idDocumento }) => api.patch(`/documentos/${idDocumento}/validar`).then(datos)

export const rechazarDocumento = ({ idDocumento, motivo }) =>
  api.patch(`/documentos/${idDocumento}/rechazar`, { motivo }).then(datos)

/** Descarga el archivo con el token de la sesión (un enlace normal no enviaría el encabezado Authorization). */
export async function descargarDocumento(documento) {
  const respuesta = await api.get(`/documentos/${documento.idDocumento}/archivo`, { responseType: "blob" })
  const url = URL.createObjectURL(respuesta.data)
  const enlace = document.createElement("a")
  enlace.href = url
  enlace.download = documento.nombreArchivo
  enlace.click()
  URL.revokeObjectURL(url)
}
