import { registrarDocumento } from "@/features/documentos/documentosApi"
import { TIPO_DESTINO } from "@/lib/estados"
import { aIso, cantidad as formatoCantidad, hoyLocal } from "@/lib/formato"
import {
  ahora,
  auditar,
  codigo,
  db,
  esperar,
  estadoVigencia,
  fallar,
  generacionPorId,
  gestorPorId,
  recoleccionDeGeneracion,
  recoleccionPorId,
  siguienteId,
  transportistaPorId,
  vehiculoPorId,
  vistaGeneracion,
  vistaRecoleccion,
} from "@/mocks/store"

export function accionPendiente(orden, hasPermission) {
  if (orden.estado === "PENDIENTE" && hasPermission("RECOLECCION_PROGRAMAR")) return "aprobar"
  if (orden.estado === "PROGRAMADA" && hasPermission("TRASLADO_REGISTRAR")) return "salida"
  if (orden.estado === "EN_TRANSITO" && hasPermission("RECEPCION_REGISTRAR")) return "recepcion"
  if (orden.estado === "RECIBIDA" && hasPermission("RECOLECCION_PROGRAMAR")) return "cerrar"
  return null
}

export const listarRecolecciones = () =>
  esperar(
    db.recolecciones
      .map(vistaRecoleccion)
      .sort((a, b) => b.idRecoleccion - a.idRecoleccion)
  )

export function detalleRecoleccion(idRecoleccion) {
  const orden = vistaRecoleccion(recoleccionPorId(idRecoleccion))
  return esperar({
    ...orden,
    observacionesOperativas: db.observaciones.filter((item) => item.referencia === orden.numeroOrden),
    incidencias: db.incidencias.filter((item) => item.referencia === orden.numeroOrden),
  })
}

export const opcionesProgramacion = () =>
  esperar({
    generaciones: db.generaciones
      .filter((item) => item.estadoValidacion === "VALIDADA" && !recoleccionDeGeneracion(item.idGeneracion))
      .map(vistaGeneracion)
      .sort((a, b) => a.fecha.localeCompare(b.fecha)),
    transportistas: db.transportistas.map((item) => ({ ...item, estadoVigencia: estadoVigencia(item.vigencia) })),
    gestores: db.gestores.map((item) => ({ ...item, estadoVigencia: estadoVigencia(item.vigencia) })),
  })

function obtener(idRecoleccion, estadoEsperado, accion) {
  const orden = recoleccionPorId(idRecoleccion)
  if (orden.estado !== estadoEsperado) {
    throw new Error(`No se puede ${accion}: la orden ${orden.numeroOrden} no está en el estado requerido.`)
  }
  return orden
}

const intentar = (operacion) => {
  try {
    return operacion()
  } catch (error) {
    return fallar(error.message)
  }
}

function validarAsignacion({ idTransportista, idVehiculo, idGestor }) {
  const transportista = transportistaPorId(idTransportista)
  const vehiculo = vehiculoPorId(idVehiculo)
  const gestor = gestorPorId(idGestor)
  if (!transportista || estadoVigencia(transportista.vigencia) === "VENCIDA") {
    throw new Error("La autorización del transportista está vencida.")
  }
  if (!vehiculo || !transportista.vehiculos.some((item) => item.idVehiculo === vehiculo.idVehiculo)) {
    throw new Error("El vehículo no pertenece al transportista seleccionado.")
  }
  if (vehiculo.estado !== "OPERATIVO") {
    throw new Error(`El vehículo ${vehiculo.placa} no está operativo.`)
  }
  if (!gestor || estadoVigencia(gestor.vigencia) === "VENCIDA") {
    throw new Error("La autorización del gestor está vencida.")
  }
  return { transportista, vehiculo, gestor }
}

export const programarRecoleccion = ({ valores, actor }) =>
  intentar(() => {
    const generaciones = valores.idsGeneracion.map((id) => generacionPorId(id))
    if (generaciones.some((item) => item.estadoValidacion !== "VALIDADA" || recoleccionDeGeneracion(item.idGeneracion))) {
      throw new Error("Solo se pueden programar registros validados que no tengan otra orden.")
    }
    if (valores.fechaProgramada < hoyLocal()) {
      throw new Error("La fecha programada no puede ser anterior a hoy.")
    }
    validarAsignacion(valores)

    const id = siguienteId(db.recolecciones, "idRecoleccion")
    const orden = {
      idRecoleccion: id,
      numeroOrden: `OR-${1036 + id}`,
      estado: "PENDIENTE",
      fechaSolicitud: ahora(),
      solicitadoPor: actor,
      fechaProgramada: valores.fechaProgramada,
      idTransportista: Number(valores.idTransportista),
      idVehiculo: Number(valores.idVehiculo),
      idGestor: Number(valores.idGestor),
      detalles: generaciones.map((item) => ({ idGeneracion: item.idGeneracion })),
      observaciones: valores.observaciones?.trim() ?? "",
      aprobacion: null,
      salida: null,
      recepcion: null,
      destino: null,
      cierre: null,
    }
    db.recolecciones.push(orden)
    const vista = vistaRecoleccion(orden)
    auditar(
      actor,
      "CREAR",
      "Recolección",
      orden.numeroOrden,
      `Programó la recolección de ${formatoCantidad(vista.cantidadTotal, vista.unidad)} de ${vista.residuos.join(", ").toLowerCase()}`
    )
    return esperar(vista)
  })

export const aprobarRecoleccion = ({ idRecoleccion, actor }) =>
  intentar(() => {
    const orden = obtener(idRecoleccion, "PENDIENTE", "aprobar")
    validarAsignacion(orden)
    orden.estado = "PROGRAMADA"
    orden.aprobacion = { fecha: ahora(), usuario: actor }
    auditar(actor, "APROBAR", "Recolección", orden.numeroOrden, "Aprobó la orden de recolección")
    return esperar(vistaRecoleccion(orden))
  })

export const registrarSalida = ({ idRecoleccion, valores, actor }) =>
  intentar(() => {
    const orden = obtener(idRecoleccion, "PROGRAMADA", "registrar la salida")
    const vista = vistaRecoleccion(orden)
    if (vista.peligroso && valores.tipoDocumento !== "MANIFIESTO") {
      throw new Error("Los residuos peligrosos requieren manifiesto de manejo.")
    }
    const numeroDocumento = valores.numeroDocumento.trim().toUpperCase()
    orden.estado = "EN_TRANSITO"
    orden.salida = {
      fecha: aIso(valores.fecha),
      documento: numeroDocumento,
      usuario: actor,
      observaciones: valores.observaciones?.trim() ?? "",
    }
    registrarDocumento(
      {
        tipo: valores.tipoDocumento,
        nombreArchivo: valores.archivo || `${valores.tipoDocumento === "MANIFIESTO" ? "manifiesto" : "guia"}-${numeroDocumento}.pdf`,
        referencia: orden.numeroOrden,
        descripcion: `Documento de traslado ${numeroDocumento}`,
      },
      actor
    )
    const unidadTransporte = `${vista.transportista.empresa} · ${vista.vehiculo.placa}`
    vista.detalles.forEach((detalle) => {
      const idMovimiento = siguienteId(db.movimientos, "idMovimiento")
      db.movimientos.unshift({
        idMovimiento,
        codigo: `MOV-0${200 + idMovimiento}`,
        tipo: "SALIDA",
        fecha: orden.salida.fecha,
        idResiduo: detalle.idResiduo,
        origen: detalle.contenedor.codigo,
        destino: unidadTransporte,
        cantidad: detalle.cantidad,
        referencia: orden.numeroOrden,
        usuario: actor,
        motivo: "Salida por recolección",
        afectaStock: false,
      })
    })
    auditar(actor, "REGISTRAR", "Recolección", orden.numeroOrden, `Registró la salida con el documento ${numeroDocumento}`)
    return esperar(vistaRecoleccion(orden))
  })

export const registrarRecepcion = ({ idRecoleccion, valores, actor }) =>
  intentar(() => {
    const orden = obtener(idRecoleccion, "EN_TRANSITO", "registrar la recepción")
    const vista = vistaRecoleccion(orden)
    const recibida = Number(valores.cantidadRecibida)
    orden.estado = "RECIBIDA"
    orden.recepcion = {
      fecha: aIso(valores.fecha),
      cantidadRecibida: recibida,
      conforme: valores.conforme,
      usuario: actor,
      observaciones: valores.observaciones?.trim() ?? "",
    }
    orden.destino = { tipo: valores.tipoDestino, tratamiento: valores.tratamiento.trim() }

    if (valores.acta) {
      registrarDocumento(
        { tipo: "ACTA_RECEPCION", nombreArchivo: valores.acta, referencia: orden.numeroOrden, descripcion: "Acta de recepción firmada por el gestor" },
        actor
      )
    }
    if (valores.certificado) {
      registrarDocumento(
        {
          tipo: "CERTIFICADO",
          nombreArchivo: valores.certificado,
          referencia: orden.numeroOrden,
          descripcion: `Certificado de ${TIPO_DESTINO[valores.tipoDestino].toLowerCase()}`,
        },
        actor
      )
    }
    if (!valores.conforme) {
      const idIncidencia = siguienteId(db.incidencias, "idIncidencia")
      db.incidencias.unshift({
        idIncidencia,
        codigo: codigo("INC", idIncidencia),
        tipo: "RECHAZO",
        descripcion: `Recepción no conforme de la orden ${orden.numeroOrden}: ${orden.recepcion.observaciones}`,
        idArea: vista.detalles[0]?.idArea,
        referencia: orden.numeroOrden,
        severidad: "MEDIA",
        estado: "ABIERTA",
        fecha: ahora(),
        reportadoPor: actor,
        responsable: null,
        accionCorrectiva: "",
        seguimiento: [],
        fechaCierre: null,
      })
    }
    auditar(
      actor,
      "REGISTRAR",
      "Recolección",
      orden.numeroOrden,
      `Registró la recepción de ${formatoCantidad(recibida, vista.unidad)} con destino ${TIPO_DESTINO[valores.tipoDestino].toLowerCase()}`
    )
    return esperar(vistaRecoleccion(orden))
  })

export const cerrarRecoleccion = ({ idRecoleccion, actor }) =>
  intentar(() => {
    const orden = obtener(idRecoleccion, "RECIBIDA", "cerrar")
    const pendientes = vistaRecoleccion(orden).requisitosCierre.filter((item) => !item.cumple)
    if (pendientes.length > 0) {
      throw new Error(`Falta: ${pendientes.map((item) => item.label.toLowerCase()).join(", ")}.`)
    }
    orden.estado = "CERRADA"
    orden.cierre = { fecha: ahora(), usuario: actor }
    auditar(actor, "CERRAR", "Recolección", orden.numeroOrden, "Cerró la orden con la documentación completa")
    return esperar(vistaRecoleccion(orden))
  })
