import { normalizar } from "@/lib/formato"
import {
  auditar,
  db,
  esperar,
  estadoVigencia,
  fallar,
  gestorPorId,
  siguienteId,
  transportistaPorId,
} from "@/mocks/store"

const ACTIVAS = ["PENDIENTE", "PROGRAMADA", "EN_TRANSITO", "RECIBIDA"]

const resumenOrdenes = (campo, id) => {
  const ordenes = db.recolecciones.filter((item) => item[campo] === id)
  return {
    ordenesActivas: ordenes.filter((item) => ACTIVAS.includes(item.estado)).length,
    ordenesCerradas: ordenes.filter((item) => item.estado === "CERRADA").length,
  }
}

export const listarTransportistas = () =>
  esperar(
    db.transportistas.map((item) => ({
      ...item,
      estadoVigencia: estadoVigencia(item.vigencia),
      ...resumenOrdenes("idTransportista", item.idTransportista),
    }))
  )

export const listarGestores = () =>
  esperar(
    db.gestores.map((item) => ({
      ...item,
      estadoVigencia: estadoVigencia(item.vigencia),
      ...resumenOrdenes("idGestor", item.idGestor),
    }))
  )

const autorizacionDuplicada = (lista, campo, valores) =>
  lista.some(
    (item) =>
      normalizar(item.autorizacion) === normalizar(valores.autorizacion.trim()) &&
      item[campo] !== Number(valores[campo])
  )

export function guardarTransportista({ valores, actor }) {
  if (autorizacionDuplicada(db.transportistas, "idTransportista", valores)) {
    return fallar(`Ya existe un transportista con la autorización ${valores.autorizacion}.`)
  }
  const datos = {
    empresa: valores.empresa.trim(),
    autorizacion: valores.autorizacion.trim().toUpperCase(),
    contacto: valores.contacto.trim(),
    vigencia: valores.vigencia,
  }
  if (valores.idTransportista) {
    const transportista = transportistaPorId(valores.idTransportista)
    Object.assign(transportista, datos)
    auditar(actor, "MODIFICAR", "Transportista", transportista.empresa, "Actualizó los datos y la vigencia de la autorización")
    return esperar(transportista)
  }
  const transportista = { idTransportista: siguienteId(db.transportistas, "idTransportista"), ...datos, vehiculos: [] }
  db.transportistas.push(transportista)
  auditar(actor, "CREAR", "Transportista", transportista.empresa, `Registró la EO-RS ${transportista.autorizacion}`)
  return esperar(transportista)
}

export function guardarVehiculo({ idTransportista, valores, actor }) {
  const transportista = transportistaPorId(idTransportista)
  const placa = valores.placa.trim().toUpperCase()
  const vehiculos = db.transportistas.flatMap((item) => item.vehiculos)
  if (vehiculos.some((item) => item.placa === placa && item.idVehiculo !== Number(valores.idVehiculo))) {
    return fallar(`La placa ${placa} ya está registrada.`)
  }
  const datos = {
    placa,
    tipo: valores.tipo.trim(),
    capacidad: Number(valores.capacidad),
    estado: valores.estado,
  }
  if (valores.idVehiculo) {
    const enUso = db.recolecciones.some(
      (item) => item.idVehiculo === Number(valores.idVehiculo) && ["PROGRAMADA", "EN_TRANSITO"].includes(item.estado)
    )
    if (enUso && datos.estado !== "OPERATIVO") {
      return fallar(`${placa} tiene una recolección programada o en tránsito.`)
    }
    const vehiculo = transportista.vehiculos.find((item) => item.idVehiculo === Number(valores.idVehiculo))
    Object.assign(vehiculo, datos)
    auditar(actor, "MODIFICAR", "Transportista", transportista.empresa, `Actualizó el vehículo ${placa}`)
    return esperar(vehiculo)
  }
  const vehiculo = { idVehiculo: siguienteId(vehiculos, "idVehiculo"), ...datos }
  transportista.vehiculos.push(vehiculo)
  auditar(actor, "CREAR", "Transportista", transportista.empresa, `Registró el vehículo ${placa}`)
  return esperar(vehiculo)
}

export function guardarGestor({ valores, actor }) {
  if (autorizacionDuplicada(db.gestores, "idGestor", valores)) {
    return fallar(`Ya existe un gestor con la autorización ${valores.autorizacion}.`)
  }
  const datos = {
    empresa: valores.empresa.trim(),
    autorizacion: valores.autorizacion.trim().toUpperCase(),
    procesoDestino: valores.procesoDestino.trim(),
    vigencia: valores.vigencia,
  }
  if (valores.idGestor) {
    const gestor = gestorPorId(valores.idGestor)
    Object.assign(gestor, datos)
    auditar(actor, "MODIFICAR", "Gestor", gestor.empresa, "Actualizó los datos y la vigencia de la autorización")
    return esperar(gestor)
  }
  const gestor = { idGestor: siguienteId(db.gestores, "idGestor"), ...datos }
  db.gestores.push(gestor)
  auditar(actor, "CREAR", "Gestor", gestor.empresa, `Registró la EO-RS ${gestor.autorizacion}`)
  return esperar(gestor)
}
