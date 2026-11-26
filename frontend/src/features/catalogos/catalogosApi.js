import { api, datos, guardar } from "@/lib/api"

// CU03–CU05: residuos, áreas generadoras y contenedores

export const listarResiduos = () => api.get("/residuos").then(datos)

export const listarAreas = () => api.get("/areas").then(datos)

export const listarContenedores = () => api.get("/contenedores").then(datos)

export const guardarResiduo = ({ valores }) => guardar("/residuos", valores.idResiduo, valores)

export const guardarArea = ({ valores }) => guardar("/areas", valores.idArea, valores)

export const cambiarEstadoArea = ({ idArea }) => api.patch(`/areas/${idArea}/estado`).then(datos)

export const guardarContenedor = ({ valores }) => guardar("/contenedores", valores.idContenedor, valores)

export const listarResponsables = () => api.get("/areas/responsables").then(datos)
