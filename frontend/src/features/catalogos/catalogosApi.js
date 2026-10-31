import { diasDesde } from "@/lib/formato"
import {
  auditar,
  db,
  esperar,
  fallar,
  residuoPorId,
  siguienteId,
  vistaContenedor,
} from "@/mocks/store"

export const listarResiduos = () => esperar(db.residuos)

export const listarAreas = () =>
  esperar(
    db.areas.map((area) => {
      const recientes = db.generaciones.filter(
        (item) => item.idArea === area.idArea && diasDesde(item.fecha) <= 30
      )
      return { ...area, registros30: recientes.length }
    })
  )

export const listarContenedores = () => esperar(db.contenedores.map(vistaContenedor))

const normalizarCodigo = (valor) => valor.trim().toUpperCase()

export function guardarResiduo({ valores, actor }) {
  const codigo = normalizarCodigo(valores.codigo)
  const duplicado = db.residuos.find(
    (item) => item.codigo === codigo && item.idResiduo !== valores.idResiduo
  )
  if (duplicado) return fallar(`Ya existe un residuo con el código ${codigo}.`)

  const datos = {
    codigo,
    nombre: valores.nombre.trim(),
    categoria: valores.categoria,
    peligrosidad: valores.peligrosidad,
    unidad: valores.unidad,
    plazoMaximoDias: Number(valores.plazoMaximoDias),
    tratamiento: valores.tratamiento.trim(),
    requisitos: valores.requisitos.trim(),
  }

  if (valores.idResiduo) {
    const residuo = residuoPorId(valores.idResiduo)
    Object.assign(residuo, datos)
    auditar(actor, "MODIFICAR", "Residuo", codigo, `Actualizó el residuo ${datos.nombre}`)
    return esperar(residuo)
  }

  const residuo = { idResiduo: siguienteId(db.residuos, "idResiduo"), ...datos }
  db.residuos.push(residuo)
  auditar(actor, "CREAR", "Residuo", codigo, `Registró el residuo ${datos.nombre}`)
  return esperar(residuo)
}

export function guardarArea({ valores, actor }) {
  const codigo = normalizarCodigo(valores.codigo)
  const duplicado = db.areas.find((item) => item.codigo === codigo && item.idArea !== valores.idArea)
  if (duplicado) return fallar(`Ya existe un área con el código ${codigo}.`)

  const datos = {
    codigo,
    nombre: valores.nombre.trim(),
    proceso: valores.proceso.trim(),
    responsable: valores.responsable,
    estado: valores.estado,
  }

  if (valores.idArea) {
    const area = db.areas.find((item) => item.idArea === valores.idArea)
    Object.assign(area, datos)
    auditar(actor, "MODIFICAR", "Área generadora", codigo, `Actualizó el área ${datos.nombre}`)
    return esperar(area)
  }

  const area = { idArea: siguienteId(db.areas, "idArea"), ...datos }
  db.areas.push(area)
  auditar(actor, "CREAR", "Área generadora", codigo, `Registró el área ${datos.nombre}`)
  return esperar(area)
}

export function cambiarEstadoArea({ idArea, actor }) {
  const area = db.areas.find((item) => item.idArea === idArea)
  area.estado = area.estado === "ACTIVA" ? "INACTIVA" : "ACTIVA"
  const activa = area.estado === "ACTIVA"
  auditar(
    actor,
    activa ? "ACTIVAR" : "DESACTIVAR",
    "Área generadora",
    area.codigo,
    `${activa ? "Activó" : "Desactivó"} el área ${area.nombre}`
  )
  return esperar(area)
}

export function guardarContenedor({ valores, actor }) {
  const codigo = normalizarCodigo(valores.codigo)
  const duplicado = db.contenedores.find(
    (item) => item.codigo === codigo && item.idContenedor !== valores.idContenedor
  )
  if (duplicado) return fallar(`Ya existe un contenedor con el código ${codigo}.`)

  const datos = {
    codigo,
    tipo: valores.tipo.trim(),
    capacidad: Number(valores.capacidad),
    idResiduo: Number(valores.idResiduo),
    ubicacion: valores.ubicacion.trim(),
    estado: valores.estado,
  }

  if (valores.idContenedor) {
    const contenedor = db.contenedores.find((item) => item.idContenedor === valores.idContenedor)
    const ocupacion = vistaContenedor(contenedor).ocupacion
    if (datos.capacidad < ocupacion) {
      return fallar(`La capacidad no puede ser menor a la ocupación actual (${ocupacion}).`)
    }
    if (datos.idResiduo !== contenedor.idResiduo && ocupacion > 0) {
      return fallar("No se puede cambiar el residuo permitido de un contenedor con residuos almacenados.")
    }
    Object.assign(contenedor, datos)
    auditar(actor, "MODIFICAR", "Contenedor", codigo, `Actualizó el contenedor ${codigo}`)
    return esperar(contenedor)
  }

  const contenedor = {
    idContenedor: siguienteId(db.contenedores, "idContenedor"),
    ...datos,
    stockInicial: 0,
    fechaStock: null,
  }
  db.contenedores.push(contenedor)
  auditar(actor, "CREAR", "Contenedor", codigo, `Registró el contenedor ${codigo} (${datos.tipo})`)
  return esperar(contenedor)
}

export const listarResponsables = () =>
  esperar(
    db.usuarios
      .filter((item) => item.estado === "ACTIVO")
      .map((item) => `${item.nombres} ${item.apellidos}`)
  )
