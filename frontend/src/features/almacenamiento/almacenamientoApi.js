import { cantidad as formatoCantidad } from "@/lib/formato"
import {
  RETIRADA,
  ahora,
  auditar,
  contenedorPorId,
  db,
  esperar,
  fallar,
  recoleccionDeGeneracion,
  residuoPorId,
  siguienteId,
  vistaContenedor,
  vistaGeneracion,
} from "@/mocks/store"

const prioridad = (contenedor) =>
  (contenedor.alertaPlazo ? 2 : 0) + (contenedor.alertaCapacidad ? 1 : 0)

export const listarAlmacenamiento = () =>
  esperar(
    db.contenedores
      .map(vistaContenedor)
      .sort((a, b) => prioridad(b) - prioridad(a) || b.porcentaje - a.porcentaje)
  )

export const listarMovimientos = () =>
  esperar(
    db.movimientos
      .map((item) => ({ ...item, residuo: residuoPorId(item.idResiduo) }))
      .sort((a, b) => b.fecha.localeCompare(a.fecha))
  )

export function detalleContenedor(idContenedor) {
  const contenedor = vistaContenedor(contenedorPorId(idContenedor))
  const lotes = db.generaciones
    .filter((item) => {
      if (item.idContenedor !== contenedor.idContenedor) return false
      const orden = recoleccionDeGeneracion(item.idGeneracion)
      return !orden || !RETIRADA.includes(orden.estado)
    })
    .map(vistaGeneracion)
    .sort((a, b) => a.fecha.localeCompare(b.fecha))
  return esperar({
    ...contenedor,
    lotes,
    movimientos: db.movimientos
      .filter((item) => item.origen === contenedor.codigo || item.destino === contenedor.codigo)
      .sort((a, b) => b.fecha.localeCompare(a.fecha))
      .slice(0, 6),
    observaciones: db.observaciones.filter((item) => item.referencia === contenedor.codigo),
  })
}

function nuevoMovimiento(datos) {
  const idMovimiento = siguienteId(db.movimientos, "idMovimiento")
  const movimiento = {
    idMovimiento,
    codigo: `MOV-0${200 + idMovimiento}`,
    fecha: ahora(),
    afectaStock: true,
    ...datos,
  }
  db.movimientos.unshift(movimiento)
  return movimiento
}

function registrarTraslado({ valores, actor }) {
  const origen = vistaContenedor(contenedorPorId(valores.idOrigen))
  const destino = vistaContenedor(contenedorPorId(valores.idDestino))
  const valor = Number(valores.cantidad)

  if (origen.idContenedor === destino.idContenedor) {
    return fallar("El contenedor de destino debe ser distinto al de origen.")
  }
  if (origen.idResiduo !== destino.idResiduo) {
    return fallar(`${destino.codigo} no admite ${origen.residuo.nombre.toLowerCase()}.`)
  }
  if (destino.estado === "MANTENIMIENTO") {
    return fallar(`${destino.codigo} está en mantenimiento.`)
  }
  if (valor > origen.ocupacion) {
    return fallar(`${origen.codigo} solo contiene ${formatoCantidad(origen.ocupacion, origen.unidad)}.`)
  }
  if (valor > destino.disponible) {
    return fallar(`${destino.codigo} solo tiene ${formatoCantidad(destino.disponible, destino.unidad)} disponibles.`)
  }

  const movimiento = nuevoMovimiento({
    tipo: "TRASLADO",
    idResiduo: origen.idResiduo,
    origen: origen.codigo,
    destino: destino.codigo,
    cantidad: valor,
    referencia: origen.codigo,
    usuario: actor,
    motivo: valores.motivo.trim(),
  })
  auditar(
    actor,
    "REGISTRAR",
    "Movimiento",
    movimiento.codigo,
    `Trasladó ${formatoCantidad(valor, origen.unidad)} de ${origen.codigo} a ${destino.codigo}`
  )
  return esperar(movimiento)
}

function registrarPesaje({ valores, actor }) {
  const contenedor = vistaContenedor(contenedorPorId(valores.idOrigen))
  const valor = Number(valores.cantidad)

  if (valor > contenedor.capacidad) {
    return fallar(`El peso verificado supera la capacidad de ${contenedor.codigo}.`)
  }

  const ajuste = Math.round((valor - contenedor.ocupacion) * 100) / 100
  const movimiento = nuevoMovimiento({
    tipo: "PESAJE",
    idResiduo: contenedor.idResiduo,
    origen: contenedor.codigo,
    destino: contenedor.codigo,
    cantidad: valor,
    ajuste,
    referencia: contenedor.codigo,
    usuario: actor,
    motivo: valores.motivo.trim(),
  })
  auditar(
    actor,
    "REGISTRAR",
    "Movimiento",
    movimiento.codigo,
    `Pesaje de ${contenedor.codigo}: ${formatoCantidad(valor, contenedor.unidad)} (ajuste ${ajuste >= 0 ? "+" : ""}${formatoCantidad(ajuste, contenedor.unidad)})`
  )
  return esperar(movimiento)
}

export const registrarMovimiento = (datos) =>
  datos.valores.tipo === "TRASLADO" ? registrarTraslado(datos) : registrarPesaje(datos)
