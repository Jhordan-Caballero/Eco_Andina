import { api, datos } from "@/lib/api"

// CU19 y CU20: reportes e indicadores y su exportación

export const PERIODOS = [
  { value: "30", label: "Últimos 30 días" },
  { value: "90", label: "Últimos 90 días" },
  { value: "TODO", label: "Todo el registro" },
]

export const generarReporte = (periodo) => api.get("/reportes", { params: { periodo } }).then(datos)

// Devuelve { nombreArchivo, contenido } con el CSV ya armado por el backend
export const exportarReporte = ({ periodo }) =>
  api.post("/reportes/exportar", null, { params: { periodo } }).then(datos)
