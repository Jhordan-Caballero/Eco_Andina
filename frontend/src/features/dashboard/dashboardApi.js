import { cantidad, porcentaje } from "@/lib/formato"
import { db, esperar, vistaContenedor, vistaRecoleccion } from "@/mocks/store"

const ACTIVAS = ["PENDIENTE", "PROGRAMADA", "EN_TRANSITO"]

function alertas() {
  const contenedores = db.contenedores.map(vistaContenedor)
  return [
    ...contenedores
      .filter((item) => item.alertaPlazo)
      .sort((a, b) => a.plazoRestante - b.plazoRestante)
      .map((item) => ({
        id: `plazo-${item.codigo}`,
        tipo: "plazo",
        titulo: `${item.residuo?.nombre} · ${item.codigo}`,
        detalle:
          item.plazoRestante < 0
            ? `Superó el plazo máximo hace ${Math.abs(item.plazoRestante)} días`
            : `El plazo máximo de almacenamiento vence en ${item.plazoRestante} días`,
      })),
    ...contenedores
      .filter((item) => item.alertaCapacidad)
      .sort((a, b) => b.porcentaje - a.porcentaje)
      .map((item) => ({
        id: `capacidad-${item.codigo}`,
        tipo: "capacidad",
        titulo: `${item.codigo} · ${item.ubicacion}`,
        detalle: `${item.residuo?.nombre} · ${cantidad(item.ocupacion, item.unidad)} de ${cantidad(item.capacidad, item.unidad)}`,
        ocupacion: Math.min(100, item.porcentaje),
      })),
  ]
}

export function fetchResumen(actor) {
  const ordenes = db.recolecciones.map(vistaRecoleccion).sort((a, b) => b.idRecoleccion - a.idRecoleccion)
  const recibidas = ordenes.filter((item) => item.estado === "RECIBIDA")
  const cerradas = ordenes.filter((item) => item.estado === "CERRADA")
  const incidenciasAbiertas = db.incidencias.filter((item) => item.estado !== "CERRADA")
  const listaAlertas = alertas()

  return esperar({
    kpis: {
      trazabilidad: {
        valor: porcentaje(cerradas.length, cerradas.length + recibidas.length),
        descripcion: "Órdenes recibidas que cerraron con su documentación completa",
      },
      ordenesActivas: {
        valor: ordenes.filter((item) => ACTIVAS.includes(item.estado)).length,
        descripcion: "Pendientes, programadas o en tránsito",
      },
      incidenciasAbiertas: {
        valor: incidenciasAbiertas.length,
        descripcion: "Abiertas o con acción correctiva en curso",
      },
      alertasCapacidad: {
        valor: listaAlertas.filter((item) => item.tipo === "capacidad").length,
        descripcion: "Contenedores al 85% o más de su capacidad",
      },
    },
    ordenesRecientes: ordenes.slice(0, 5).map((orden) => ({
      codigo: orden.numeroOrden,
      area: orden.areas.join(", "),
      residuo: orden.residuos.join(", "),
      cantidad: cantidad(orden.cantidadTotal, orden.unidad),
      transportista: orden.transportista?.empresa,
      estado: orden.estado,
    })),
    alertas: listaAlertas,
    pendientes: {
      porValidar: db.generaciones.filter((item) => item.estadoValidacion === "PENDIENTE").length,
      porCorregir: db.generaciones.filter((item) => item.estadoValidacion === "OBSERVADA" && item.responsable === actor).length,
      porAprobar: ordenes.filter((item) => item.estado === "PENDIENTE").length,
      porSalida: ordenes.filter((item) => item.estado === "PROGRAMADA").length,
      porRecibir: ordenes.filter((item) => item.estado === "EN_TRANSITO").length,
      porCerrar: recibidas.length,
      documentosPorRevisar: db.documentos.filter((item) => item.estado === "PENDIENTE").length,
      incidenciasSinResponsable: incidenciasAbiertas.filter((item) => !item.responsable).length,
    },
  })
}
