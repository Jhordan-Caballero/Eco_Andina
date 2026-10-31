import { EstadoBadge } from "@/components/shared/StatusBadge"
import { ESTADO_VIGENCIA } from "@/lib/estados"
import { diasHasta, fecha } from "@/lib/formato"

export function Vigencia({ valor, estado }) {
  const dias = diasHasta(valor)
  let detalle = `hasta ${fecha(valor)}`
  if (estado === "VENCIDA") detalle = `venció el ${fecha(valor)}`
  if (estado === "POR_VENCER") detalle = `vence en ${dias} ${dias === 1 ? "día" : "días"}`
  return (
    <span className="inline-flex flex-wrap items-center gap-x-2 gap-y-1">
      <EstadoBadge mapa={ESTADO_VIGENCIA} valor={estado} />
      <span className="text-xs text-muted-foreground tabular-nums">{detalle}</span>
    </span>
  )
}
