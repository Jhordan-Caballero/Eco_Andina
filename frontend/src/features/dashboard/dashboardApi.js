import { api, datos } from "@/lib/api"

// Panel general: indicadores, órdenes recientes, alertas y pendientes del usuario
export const fetchResumen = () => api.get("/dashboard").then(datos)
