import { aIso, cantidad as formatoCantidad } from "@/lib/formato"
import {
  ahora,
  auditar,
  contenedorPorId,
  db,
  esperar,
  fallar,
  generacionPorId,
  residuoPorId,
  areaPorId,
  siguienteId,
  vistaContenedor,
  vistaGeneracion,
} from "@/mocks/store"

export const listarGeneraciones = () =>
  esperar(
    db.generaciones
      .map(vistaGeneracion)
      .sort((a, b) => b.fecha.localeCompare(a.fecha))
  )

export const opcionesRegistro = () =>
  esperar({
    areas: db.areas.filter((item) => item.estado === "ACTIVA"),
    residuos: db.residuos,
    contenedores: db.contenedores
      .filter((item) => item.estado !== "MANTENIMIENTO")
      .map(vistaContenedor),
  })

export const detalleGeneracion = (idGeneracion) => {
  const generacion = vistaGeneracion(generacionPorId(idGeneracion))
  return esperar({
    ...generacion,
    observacionesOperativas: db.observaciones.filter((item) => item.referencia === generacion.codigo),
    documentos: db.documentos.filter((item) => item.referencia === generacion.codigo),
  })
}

function registrarEvidencia(nombreArchivo, referencia, actor) {
  if (!nombreArchivo) return
  db.documentos.unshift({
    idDocumento: siguienteId(db.documentos, "idDocumento"),
    tipo: "EVIDENCIA_FOTO",
    nombreArchivo,
    referencia,
    descripcion: `Evidencia del registro ${referencia}`,
    fecha: ahora(),
    subidoPor: actor,
    estado: "PENDIENTE",
    motivoRechazo: "",
  })
}

export function registrarGeneracion({ valores, actor }) {
  const contenedor = vistaContenedor(contenedorPorId(valores.idContenedor))
  const residuo = residuoPorId(valores.idResiduo)
  const area = areaPorId(valores.idArea)
  const valor = Number(valores.cantidad)

  if (contenedor.idResiduo !== residuo.idResiduo) {
    return fallar(`El contenedor ${contenedor.codigo} no admite ${residuo.nombre}.`)
  }
  if (valor > contenedor.disponible) {
    return fallar(
      `El contenedor ${contenedor.codigo} solo tiene ${formatoCantidad(contenedor.disponible, contenedor.unidad)} disponibles.`
    )
  }

  const id = siguienteId(db.generaciones, "idGeneracion")
  const registro = {
    idGeneracion: id,
    codigo: `GEN-0${100 + id}`,
    idArea: area.idArea,
    idResiduo: residuo.idResiduo,
    idContenedor: contenedor.idContenedor,
    cantidad: valor,
    fecha: aIso(valores.fecha),
    responsable: actor,
    condicionEnvase: valores.condicionEnvase,
    segregacionCorrecta: valores.segregacionCorrecta,
    estadoValidacion: "PENDIENTE",
    observaciones: valores.observaciones?.trim() ?? "",
    evidencia: valores.evidencia || null,
    validadoPor: null,
    fechaValidacion: null,
    observacionValidacion: "",
  }
  db.generaciones.push(registro)

  const idMovimiento = siguienteId(db.movimientos, "idMovimiento")
  db.movimientos.unshift({
    idMovimiento,
    codigo: `MOV-0${200 + idMovimiento}`,
    tipo: "INGRESO",
    fecha: registro.fecha,
    idResiduo: residuo.idResiduo,
    origen: area.nombre,
    destino: contenedor.codigo,
    cantidad: valor,
    referencia: registro.codigo,
    usuario: actor,
    motivo: "Registro de generación",
    afectaStock: false,
  })

  registrarEvidencia(registro.evidencia, registro.codigo, actor)
  auditar(
    actor,
    "CREAR",
    "Generación",
    registro.codigo,
    `Registró ${formatoCantidad(valor, residuo.unidad)} de ${residuo.nombre.toLowerCase()} en ${contenedor.codigo}`
  )
  return esperar(vistaGeneracion(registro))
}

export function validarGeneracion({ idGeneracion, actor }) {
  const generacion = generacionPorId(idGeneracion)
  if (generacion.estadoValidacion !== "PENDIENTE") {
    return fallar("Solo se pueden validar registros pendientes.")
  }
  Object.assign(generacion, {
    estadoValidacion: "VALIDADA",
    validadoPor: actor,
    fechaValidacion: ahora(),
    observacionValidacion: "",
  })
  auditar(actor, "VALIDAR", "Generación", generacion.codigo, "Validó el registro de generación")
  return esperar(vistaGeneracion(generacion))
}

export function observarGeneracion({ idGeneracion, observacion, actor }) {
  const generacion = generacionPorId(idGeneracion)
  if (generacion.estadoValidacion !== "PENDIENTE") {
    return fallar("Solo se pueden observar registros pendientes.")
  }
  Object.assign(generacion, {
    estadoValidacion: "OBSERVADA",
    validadoPor: actor,
    fechaValidacion: ahora(),
    observacionValidacion: observacion.trim(),
  })
  auditar(actor, "OBSERVAR", "Generación", generacion.codigo, `Observó el registro: ${observacion.trim()}`)
  return esperar(vistaGeneracion(generacion))
}

export function corregirGeneracion({ idGeneracion, valores, actor }) {
  const generacion = generacionPorId(idGeneracion)
  const contenedor = vistaContenedor(contenedorPorId(generacion.idContenedor))
  const valor = Number(valores.cantidad)
  if (valor - generacion.cantidad > contenedor.disponible) {
    return fallar(
      `El contenedor ${contenedor.codigo} solo tiene ${formatoCantidad(contenedor.disponible, contenedor.unidad)} disponibles.`
    )
  }
  Object.assign(generacion, {
    cantidad: valor,
    condicionEnvase: valores.condicionEnvase,
    observaciones: valores.observaciones.trim(),
    evidencia: valores.evidencia || generacion.evidencia,
    estadoValidacion: "PENDIENTE",
  })
  registrarEvidencia(valores.evidencia, generacion.codigo, actor)
  auditar(actor, "MODIFICAR", "Generación", generacion.codigo, "Corrigió el registro observado y lo reenvió a validación")
  return esperar(vistaGeneracion(generacion))
}
