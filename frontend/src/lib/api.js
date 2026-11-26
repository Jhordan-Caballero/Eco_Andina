import axios from "axios"
import { session } from "@/lib/session"

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? "http://localhost:8082/api",
})

api.interceptors.request.use((config) => {
  const token = session.getToken()
  if (token && !config.url?.includes("/auth/login")) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let onUnauthorized = () => {}

export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler
}

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isLoginRequest = error.config?.url?.includes("/auth/login")
    if (error.response?.status === 401 && !isLoginRequest) {
      onUnauthorized()
    }
    return Promise.reject(error)
  }
)

export function getErrorMessage(error, fallback = "Ocurrió un error inesperado.") {
  if (error?.response) {
    return error.response.data?.message ?? fallback
  }
  if (error?.request) {
    return "No se pudo conectar con el servidor. Verifica que el backend esté en ejecución."
  }
  return fallback
}

/** Devuelve el cuerpo de la respuesta. */
export const datos = (respuesta) => respuesta.data

/** Crea (POST) o actualiza (PUT ruta/id) según venga o no el id. */
export const guardar = (ruta, id, valores) =>
  (id ? api.put(`${ruta}/${id}`, valores) : api.post(ruta, valores)).then(datos)

/**
 * Arma un multipart/form-data con la parte "datos" (JSON) y los archivos que vengan.
 * El backend recibe la parte "datos" con @RequestPart y cada archivo como MultipartFile.
 */
export function formulario(valores, archivos = {}) {
  const form = new FormData()
  form.append("datos", new Blob([JSON.stringify(valores)], { type: "application/json" }))
  Object.entries(archivos).forEach(([nombre, archivo]) => {
    if (archivo instanceof File) form.append(nombre, archivo)
  })
  return form
}
