import { api, datos } from "@/lib/api"

// CU10 y CU11: almacenamiento temporal y movimientos internos

export const listarAlmacenamiento = () => api.get("/almacenamiento").then(datos)

export const listarMovimientos = () => api.get("/movimientos").then(datos)

export const detalleContenedor = (idContenedor) => api.get(`/almacenamiento/${idContenedor}`).then(datos)

// valores.tipo: TRASLADO (idOrigen -> idDestino) o PESAJE (idOrigen)
export const registrarMovimiento = ({ valores }) =>
  api.post("/movimientos", { ...valores, idDestino: valores.idDestino || null }).then(datos)
