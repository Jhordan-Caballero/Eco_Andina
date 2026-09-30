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
