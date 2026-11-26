import { api, datos, guardar } from "@/lib/api"

// CU06 y CU07: transportistas, vehículos y gestores autorizados

export const listarTransportistas = () => api.get("/transportistas").then(datos)

export const listarGestores = () => api.get("/gestores").then(datos)

export const guardarTransportista = ({ valores }) => guardar("/transportistas", valores.idTransportista, valores)

export const guardarVehiculo = ({ idTransportista, valores }) =>
  guardar(`/transportistas/${idTransportista}/vehiculos`, valores.idVehiculo, valores)

export const guardarGestor = ({ valores }) => guardar("/gestores", valores.idGestor, valores)
