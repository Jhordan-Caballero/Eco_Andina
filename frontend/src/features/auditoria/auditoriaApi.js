import { auditar, db, esperar } from "@/mocks/store"

const EVENTOS = {
  validaciones: "validaciones",
  modificaciones: "creación y modificación",
  cierres: "cierres",
  accesos: "inicios de sesión",
}

export const listarAuditoria = () =>
  esperar([...db.auditoria].sort((a, b) => b.fecha.localeCompare(a.fecha)))

export const obtenerConfiguracion = () => esperar(db.configuracionAuditoria)

export function guardarConfiguracion({ valores, actor }) {
  const anterior = db.configuracionAuditoria
  const cambios = [
    ...Object.keys(EVENTOS)
      .filter((clave) => anterior.eventos[clave] !== valores.eventos[clave])
      .map((clave) => `${valores.eventos[clave] ? "activó" : "desactivó"} ${EVENTOS[clave]}`),
    anterior.retencionDias !== valores.retencionDias && `retención de ${valores.retencionDias} días`,
  ].filter(Boolean)
  db.configuracionAuditoria = { eventos: { ...valores.eventos }, retencionDias: valores.retencionDias }
  auditar(actor, "MODIFICAR", "Auditoría", "Configuración", `Configuración: ${cambios.join(", ")}`)
  return esperar(db.configuracionAuditoria)
}

export function registrarAcceso(usuario) {
  auditar(usuario, "ACCESO", "Sesión", usuario, "Inició sesión en el sistema")
}
