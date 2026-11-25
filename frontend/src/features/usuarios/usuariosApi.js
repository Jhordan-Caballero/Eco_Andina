import { api, datos, guardar } from "@/lib/api"

// CU02: usuarios, roles y permisos

export const listarUsuarios = () => api.get("/usuarios").then(datos)

export const listarRoles = () => api.get("/roles").then(datos)

export const listarPermisos = () => api.get("/permisos").then(datos)

export const guardarUsuario = ({ valores }) => guardar("/usuarios", valores.idUsuario, valores)

export const cambiarEstadoUsuario = ({ idUsuario }) => api.patch(`/usuarios/${idUsuario}/estado`).then(datos)

// cambios: { CODIGO_ROL: [permisos...] } con la lista completa de cada rol modificado
export const guardarPermisosRoles = ({ cambios }) => api.put("/roles/permisos", cambios).then(datos)
