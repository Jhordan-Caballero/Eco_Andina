import {
  differenceInCalendarDays,
  format,
  formatDistanceToNowStrict,
  parseISO,
} from "date-fns"
import { es } from "date-fns/locale"

const numeroFormato = new Intl.NumberFormat("es-PE", { maximumFractionDigits: 2 })

const aFecha = (valor) => (valor instanceof Date ? valor : parseISO(valor))

export const fecha = (valor) =>
  valor ? format(aFecha(valor), "d MMM yyyy", { locale: es }) : "—"

export const fechaHora = (valor) =>
  valor ? format(aFecha(valor), "d MMM yyyy, HH:mm", { locale: es }) : "—"

export const hora = (valor) => (valor ? format(aFecha(valor), "HH:mm") : "—")

export const relativo = (valor) =>
  valor
    ? formatDistanceToNowStrict(aFecha(valor), { locale: es, addSuffix: true })
    : "—"

export const diasDesde = (valor) =>
  differenceInCalendarDays(new Date(), aFecha(valor))

export const diasHasta = (valor) =>
  differenceInCalendarDays(aFecha(valor), new Date())

export const numero = (valor) => numeroFormato.format(valor ?? 0)

export const cantidad = (valor, unidad) => `${numero(valor)} ${unidad ?? ""}`.trim()

export const porcentaje = (parte, total) =>
  total > 0 ? Math.round((parte / total) * 100) : 0

export const ahoraLocal = () => format(new Date(), "yyyy-MM-dd'T'HH:mm")

export const hoyLocal = () => format(new Date(), "yyyy-MM-dd")

export const aIso = (valorLocal) => new Date(valorLocal).toISOString()

export const normalizar = (texto) =>
  String(texto)
    .normalize("NFD")
    .replace(/[̀-ͯ]/g, "")
    .toLowerCase()

export const coincide = (busqueda, ...campos) => {
  const consulta = normalizar(busqueda.trim())
  if (!consulta) return true
  return campos.some((campo) => campo != null && normalizar(campo).includes(consulta))
}

export const iniciales = (nombreCompleto) =>
  nombreCompleto
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((parte) => parte[0])
    .join("")
    .toUpperCase()
