import { format, subDays } from "date-fns"
import { CATEGORIA_RESIDUO, ESTADO_GENERACION, ESTADO_RECOLECCION, TIPO_DESTINO, TIPO_INCIDENCIA } from "@/lib/estados"
import { porcentaje } from "@/lib/formato"
import { auditar, db, esperar, vistaGeneracion, vistaRecoleccion } from "@/mocks/store"

export const PERIODOS = [
  { value: "30", label: "Últimos 30 días" },
  { value: "90", label: "Últimos 90 días" },
  { value: "TODO", label: "Todo el registro" },
]

const sumar = (lista, campo) => lista.reduce((total, item) => total + item[campo], 0)

function calcular(periodo) {
  const desde = periodo === "TODO" ? null : subDays(new Date(), Number(periodo))
  const enPeriodo = (valor) => !desde || new Date(valor) >= desde

  const generaciones = db.generaciones.filter((item) => enPeriodo(item.fecha)).map(vistaGeneracion)
  const enKg = generaciones.filter((item) => item.unidad === "kg")
  const validadas = generaciones.filter((item) => item.estadoValidacion === "VALIDADA")
  const peligrosos = enKg.filter((item) => ["PELIGROSO", "ESPECIAL"].includes(item.residuo?.categoria))
  const ordenes = db.recolecciones.filter((item) => enPeriodo(item.fechaSolicitud)).map(vistaRecoleccion)
  const conRecepcion = ordenes.filter((item) => ["RECIBIDA", "CERRADA"].includes(item.estado))
  const cerradas = ordenes.filter((item) => item.estado === "CERRADA")
  const incidencias = db.incidencias.filter((item) => enPeriodo(item.fecha))

  const porArea = db.areas
    .map((area) => ({
      clave: area.codigo,
      label: area.nombre,
      valor: sumar(enKg.filter((item) => item.idArea === area.idArea), "cantidad"),
    }))
    .filter((item) => item.valor > 0)
    .sort((a, b) => b.valor - a.valor)

  const mesActual = format(new Date(), "yyyy-MM")
  const delMes = db.generaciones.map(vistaGeneracion).filter((item) => item.fecha.slice(0, 7) === mesActual)
  const mensual = [
    ...db.historicoMensual.map((item) => ({ clave: item.mes, mes: item.mes, valor: item.kg, registros: item.registros })),
    {
      clave: mesActual,
      mes: mesActual,
      valor: sumar(delMes.filter((item) => item.unidad === "kg"), "cantidad"),
      registros: delMes.length,
      parcial: true,
    },
  ]

  const destinos = Object.keys(TIPO_DESTINO).map((tipo) => ({
    clave: tipo,
    label: TIPO_DESTINO[tipo],
    valor:
      db.destinosSemestre[tipo] +
      db.recolecciones
        .map(vistaRecoleccion)
        .filter((item) => item.destino?.tipo === tipo && item.unidad === "kg")
        .reduce((total, item) => total + item.recepcion.cantidadRecibida, 0),
  }))

  const incidenciasPorTipo = Object.keys(TIPO_INCIDENCIA)
    .map((tipo) => ({
      clave: tipo,
      label: TIPO_INCIDENCIA[tipo],
      valor: incidencias.filter((item) => item.tipo === tipo).length,
    }))
    .sort((a, b) => b.valor - a.valor)

  return {
    periodo,
    kpis: {
      totalKg: sumar(enKg, "cantidad"),
      registros: generaciones.length,
      tasaValidacion: porcentaje(validadas.length, generaciones.length),
      validadas: validadas.length,
      trazabilidad: porcentaje(cerradas.length, conRecepcion.length),
      cerradas: cerradas.length,
      conRecepcion: conRecepcion.length,
      peligrosos: porcentaje(sumar(peligrosos, "cantidad"), sumar(enKg, "cantidad")),
      incidencias: incidencias.length,
      incidenciasAbiertas: incidencias.filter((item) => item.estado !== "CERRADA").length,
    },
    porArea,
    mensual,
    destinos,
    incidenciasPorTipo,
    generaciones,
  }
}

export const generarReporte = (periodo) => esperar(calcular(periodo))

const celda = (valor) => {
  const texto = String(valor ?? "")
  return /[;"\n]/.test(texto) ? `"${texto.replaceAll('"', '""')}"` : texto
}

export function exportarReporte({ periodo, actor }) {
  const reporte = calcular(periodo)
  const etiquetaPeriodo = PERIODOS.find((item) => item.value === periodo)?.label ?? periodo
  const filas = [
    ["Reporte de gestión de residuos sólidos · EcoAndina"],
    ["Periodo", etiquetaPeriodo],
    ["Generado por", actor],
    ["Fecha", format(new Date(), "dd/MM/yyyy HH:mm")],
    [],
    ["Indicador", "Valor"],
    ["Residuos generados (kg)", reporte.kpis.totalKg],
    ["Registros de generación", reporte.kpis.registros],
    ["Tasa de validación (%)", reporte.kpis.tasaValidacion],
    ["Órdenes con trazabilidad completa (%)", reporte.kpis.trazabilidad],
    ["Residuos peligrosos (% del peso)", reporte.kpis.peligrosos],
    ["Incidencias registradas", reporte.kpis.incidencias],
    [],
    ["Código", "Fecha", "Área", "Residuo", "Categoría", "Cantidad", "Unidad", "Validación", "Contenedor", "Orden", "Estado de la orden"],
    ...reporte.generaciones.map((item) => [
      item.codigo,
      format(new Date(item.fecha), "dd/MM/yyyy HH:mm"),
      item.area?.nombre,
      item.residuo?.nombre,
      CATEGORIA_RESIDUO[item.residuo?.categoria]?.label,
      String(item.cantidad).replace(".", ","),
      item.unidad,
      ESTADO_GENERACION[item.estadoValidacion]?.label,
      item.contenedor?.codigo,
      item.recoleccion?.numeroOrden ?? "",
      item.recoleccion ? ESTADO_RECOLECCION[item.recoleccion.estado]?.label : "",
    ]),
  ]
  auditar(actor, "EXPORTAR", "Reporte", "Indicadores de gestión", `Exportó el reporte (${etiquetaPeriodo.toLowerCase()})`)
  return esperar({
    nombreArchivo: `reporte-residuos-${format(new Date(), "yyyyMMdd-HHmm")}.csv`,
    contenido: `﻿${filas.map((fila) => fila.map(celda).join(";")).join("\r\n")}`,
  })
}
