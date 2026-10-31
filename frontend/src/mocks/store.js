import { diasDesde, diasHasta } from "@/lib/formato"
import { crearSemilla } from "./seed"

export const db = structuredClone(crearSemilla())

const LATENCIA = 280

export const esperar = (valor, ms = LATENCIA) =>
  new Promise((resolve) => setTimeout(() => resolve(structuredClone(valor)), ms))

export const fallar = (mensaje, ms = LATENCIA) =>
  new Promise((_, reject) => setTimeout(() => reject(new Error(mensaje)), ms))

export const siguienteId = (lista, campo) =>
  lista.reduce((maximo, item) => Math.max(maximo, item[campo]), 0) + 1

export const codigo = (prefijo, numero, ancho = 4) =>
  `${prefijo}-${String(numero).padStart(ancho, "0")}`

export const ahora = () => new Date().toISOString()

const CATEGORIA_AUDITORIA = {
  VALIDAR: "validaciones",
  OBSERVAR: "validaciones",
  APROBAR: "validaciones",
  RECHAZAR: "validaciones",
  CERRAR: "cierres",
  ACCESO: "accesos",
}

export function auditar(usuario, accion, entidad, registro, detalle) {
  const categoria = CATEGORIA_AUDITORIA[accion] ?? "modificaciones"
  if (entidad !== "Auditoría" && !db.configuracionAuditoria.eventos[categoria]) return
  db.auditoria.unshift({
    idAuditoria: siguienteId(db.auditoria, "idAuditoria"),
    fecha: ahora(),
    usuario,
    accion,
    entidad,
    registro,
    detalle,
  })
}

export const residuoPorId = (id) => db.residuos.find((item) => item.idResiduo === Number(id))
export const areaPorId = (id) => db.areas.find((item) => item.idArea === Number(id))
export const contenedorPorId = (id) => db.contenedores.find((item) => item.idContenedor === Number(id))
export const contenedorPorCodigo = (valor) => db.contenedores.find((item) => item.codigo === valor)
export const generacionPorId = (id) => db.generaciones.find((item) => item.idGeneracion === Number(id))
export const transportistaPorId = (id) => db.transportistas.find((item) => item.idTransportista === Number(id))
export const gestorPorId = (id) => db.gestores.find((item) => item.idGestor === Number(id))
export const recoleccionPorId = (id) => db.recolecciones.find((item) => item.idRecoleccion === Number(id))

export const vehiculoPorId = (id) =>
  db.transportistas.flatMap((item) => item.vehiculos).find((item) => item.idVehiculo === Number(id))

export const recoleccionDeGeneracion = (idGeneracion) =>
  db.recolecciones.find((orden) =>
    orden.detalles.some((detalle) => detalle.idGeneracion === Number(idGeneracion))
  )

export const RETIRADA = ["EN_TRANSITO", "RECIBIDA", "CERRADA"]

const generacionRetirada = (idGeneracion) => {
  const orden = recoleccionDeGeneracion(idGeneracion)
  return Boolean(orden && RETIRADA.includes(orden.estado))
}

export function estadoVigencia(fecha) {
  const dias = diasHasta(fecha)
  if (dias < 0) return "VENCIDA"
  if (dias <= 30) return "POR_VENCER"
  return "VIGENTE"
}

export function vistaContenedor(contenedor) {
  const residuo = residuoPorId(contenedor.idResiduo)
  const presentes = db.generaciones.filter(
    (item) =>
      item.idContenedor === contenedor.idContenedor && !generacionRetirada(item.idGeneracion)
  )
  const ajustes = db.movimientos
    .filter((item) => item.afectaStock)
    .reduce((total, item) => {
      if (item.tipo === "PESAJE") {
        return item.destino === contenedor.codigo ? total + item.ajuste : total
      }
      const entra = item.destino === contenedor.codigo ? item.cantidad : 0
      const sale = item.origen === contenedor.codigo ? item.cantidad : 0
      return total + entra - sale
    }, 0)
  const ocupacion = Math.max(
    0,
    contenedor.stockInicial + presentes.reduce((total, item) => total + item.cantidad, 0) + ajustes
  )
  const fechas = [
    contenedor.stockInicial > 0 ? contenedor.fechaStock : null,
    ...presentes.map((item) => item.fecha),
  ]
    .filter(Boolean)
    .sort()
  const fechaIngreso = ocupacion > 0 ? (fechas[0] ?? null) : null
  const diasAlmacenado = fechaIngreso ? diasDesde(fechaIngreso) : 0
  const plazoRestante = fechaIngreso && residuo ? residuo.plazoMaximoDias - diasAlmacenado : null
  const porcentaje = Math.round((ocupacion / contenedor.capacidad) * 100)
  const enMantenimiento = contenedor.estado === "MANTENIMIENTO"

  let situacion = "DISPONIBLE"
  if (enMantenimiento) situacion = "MANTENIMIENTO"
  else if (ocupacion >= contenedor.capacidad) situacion = "LLENO"
  else if (ocupacion > 0) situacion = "EN_USO"

  return {
    ...contenedor,
    residuo,
    unidad: residuo?.unidad ?? "",
    ocupacion,
    disponible: Math.max(0, contenedor.capacidad - ocupacion),
    porcentaje,
    fechaIngreso,
    diasAlmacenado,
    plazoRestante,
    situacion,
    alertaCapacidad: !enMantenimiento && porcentaje >= 85,
    alertaPlazo: !enMantenimiento && plazoRestante != null && plazoRestante <= 5,
  }
}

export function vistaGeneracion(generacion) {
  const orden = recoleccionDeGeneracion(generacion.idGeneracion)
  const residuo = residuoPorId(generacion.idResiduo)
  return {
    ...generacion,
    area: areaPorId(generacion.idArea),
    residuo,
    unidad: residuo?.unidad ?? "",
    contenedor: contenedorPorId(generacion.idContenedor),
    recoleccion: orden ? { idRecoleccion: orden.idRecoleccion, numeroOrden: orden.numeroOrden, estado: orden.estado } : null,
  }
}

export const documentosDe = (referencia) =>
  db.documentos.filter((item) => item.referencia === referencia)

export function requisitosCierre(orden) {
  const documentos = documentosDe(orden.numeroOrden).filter((item) => item.estado !== "RECHAZADO")
  return [
    { clave: "transportista", label: "Transportista y vehículo asignados", cumple: Boolean(orden.idTransportista && orden.idVehiculo) },
    { clave: "cantidad", label: "Cantidad recibida registrada", cumple: orden.recepcion?.cantidadRecibida > 0 },
    { clave: "destino", label: "Destino y tratamiento registrados", cumple: Boolean(orden.destino?.tipo) },
    {
      clave: "traslado",
      label: "Documento de traslado (manifiesto o guía)",
      cumple: documentos.some((item) => ["MANIFIESTO", "GUIA_REMISION"].includes(item.tipo)),
    },
    {
      clave: "certificado",
      label: "Certificado de tratamiento o disposición",
      cumple: documentos.some((item) => item.tipo === "CERTIFICADO"),
    },
  ]
}

export function vistaRecoleccion(orden) {
  const detalles = orden.detalles.map((detalle) => vistaGeneracion(generacionPorId(detalle.idGeneracion)))
  const transportista = transportistaPorId(orden.idTransportista)
  const residuos = [...new Set(detalles.map((item) => item.residuo?.nombre))]
  const areas = [...new Set(detalles.map((item) => item.area?.nombre))]
  return {
    ...orden,
    detalles,
    residuos,
    areas,
    cantidadTotal: detalles.reduce((total, item) => total + item.cantidad, 0),
    unidad: detalles[0]?.unidad ?? "",
    peligroso: detalles.some((item) => item.residuo?.categoria === "PELIGROSO"),
    transportista,
    vehiculo: vehiculoPorId(orden.idVehiculo),
    gestor: gestorPorId(orden.idGestor),
    documentos: documentosDe(orden.numeroOrden),
    requisitosCierre: requisitosCierre(orden),
  }
}

export const nombreCompleto = (usuario) => `${usuario.nombres} ${usuario.apellidos}`
