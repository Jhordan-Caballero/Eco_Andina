import { api, datos } from "@/lib/api"

// CU21 y CU22: consulta y configuración de la auditoría.
// Los inicios de sesión los registra el backend al autenticar.

export const listarAuditoria = () => api.get("/auditoria").then(datos)

export const obtenerConfiguracion = () => api.get("/auditoria/configuracion").then(datos)

export const guardarConfiguracion = ({ valores }) => api.put("/auditoria/configuracion", valores).then(datos)
