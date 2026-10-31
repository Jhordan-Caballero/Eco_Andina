import { useQuery } from "@tanstack/react-query"
import { ArrowLeftRight, Scale } from "lucide-react"
import { DetailList, DetailSection, DetailSheet, Nota } from "@/components/shared/DetailSheet"
import { MeterBar } from "@/components/shared/MeterBar"
import { EstadoBadge } from "@/components/shared/StatusBadge"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/features/auth/AuthContext"
import { ESTADO_GENERACION, SITUACION_CONTENEDOR, TIPO_MOVIMIENTO } from "@/lib/estados"
import { cantidad, fecha, fechaHora } from "@/lib/formato"
import { detalleContenedor } from "./almacenamientoApi"

const textoPlazo = (contenedor) => {
  if (contenedor.plazoRestante == null) return "Sin residuos almacenados"
  if (contenedor.plazoRestante < 0) return `Vencido hace ${Math.abs(contenedor.plazoRestante)} días`
  if (contenedor.plazoRestante === 0) return "Vence hoy"
  return `${contenedor.plazoRestante} días restantes`
}

export function ContenedorDetalle({ idContenedor, onClose, onMovimiento }) {
  const { hasPermission } = useAuth()
  const { data: contenedor } = useQuery({
    queryKey: ["contenedor", idContenedor],
    queryFn: () => detalleContenedor(idContenedor),
  })
  const puedeGestionar =
    hasPermission("ALMACENAMIENTO_GESTIONAR") && contenedor && contenedor.estado !== "MANTENIMIENTO"

  return (
    <DetailSheet
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={contenedor?.codigo ?? "Cargando…"}
      subtitle={contenedor && `${contenedor.tipo} · ${contenedor.ubicacion}`}
      badge={contenedor && <EstadoBadge mapa={SITUACION_CONTENEDOR} valor={contenedor.situacion} />}
      footer={
        puedeGestionar && (
          <>
            <Button variant="outline" onClick={() => onMovimiento({ tipo: "PESAJE", idContenedor })}>
              <Scale />
              Registrar pesaje
            </Button>
            <Button onClick={() => onMovimiento({ tipo: "TRASLADO", idContenedor })} disabled={contenedor.ocupacion <= 0}>
              <ArrowLeftRight />
              Trasladar
            </Button>
          </>
        )
      }
    >
      {contenedor && (
        <>
          <DetailSection title="Ocupación">
            <div className="space-y-2">
              <div className="flex items-baseline justify-between gap-2">
                <p className="font-heading text-3xl font-semibold tabular-nums">{contenedor.porcentaje}%</p>
                <p className="text-sm text-muted-foreground tabular-nums">
                  {cantidad(contenedor.ocupacion, contenedor.unidad)} de {cantidad(contenedor.capacidad, contenedor.unidad)}
                </p>
              </div>
              <MeterBar value={contenedor.porcentaje} label={`Ocupación de ${contenedor.codigo}`} className="h-2" />
            </div>
          </DetailSection>

          <DetailSection title="Almacenamiento temporal">
            <DetailList
              items={[
                ["Residuo", contenedor.residuo?.nombre],
                ["Disponible", cantidad(contenedor.disponible, contenedor.unidad)],
                ["Primer ingreso", contenedor.fechaIngreso ? fecha(contenedor.fechaIngreso) : "—"],
                ["Días almacenado", contenedor.fechaIngreso ? contenedor.diasAlmacenado : "—"],
                ["Plazo máximo", `${contenedor.residuo?.plazoMaximoDias} días`],
                [
                  "Plazo restante",
                  <span key="p" className={contenedor.alertaPlazo ? "font-medium text-warning" : undefined}>
                    {textoPlazo(contenedor)}
                  </span>,
                ],
              ]}
            />
            {contenedor.residuo?.requisitos && <Nota>{contenedor.residuo.requisitos}</Nota>}
          </DetailSection>

          <DetailSection title={`Lotes almacenados (${contenedor.lotes.length})`}>
            {contenedor.lotes.length === 0 ? (
              <p className="text-sm text-muted-foreground">
                {contenedor.stockInicial > 0
                  ? `Contiene ${cantidad(contenedor.stockInicial, contenedor.unidad)} de saldo inicial sin lote asociado.`
                  : "No hay lotes pendientes de retiro."}
              </p>
            ) : (
              <ul className="divide-y rounded-lg border">
                {contenedor.lotes.map((lote) => (
                  <li key={lote.idGeneracion} className="flex items-center gap-3 px-3 py-2 text-sm">
                    <div className="min-w-0 flex-1">
                      <p className="font-medium tabular-nums">{lote.codigo}</p>
                      <p className="text-xs text-muted-foreground">
                        {lote.area?.nombre} · {fecha(lote.fecha)}
                        {lote.recoleccion && ` · ${lote.recoleccion.numeroOrden}`}
                      </p>
                    </div>
                    <span className="tabular-nums">{cantidad(lote.cantidad, lote.unidad)}</span>
                    <EstadoBadge mapa={ESTADO_GENERACION} valor={lote.estadoValidacion} />
                  </li>
                ))}
              </ul>
            )}
          </DetailSection>

          <DetailSection title="Movimientos recientes">
            {contenedor.movimientos.length === 0 ? (
              <p className="text-sm text-muted-foreground">Sin movimientos registrados.</p>
            ) : (
              <ul className="space-y-2.5">
                {contenedor.movimientos.map((movimiento) => (
                  <li key={movimiento.idMovimiento} className="flex items-start gap-3 text-sm">
                    <EstadoBadge mapa={TIPO_MOVIMIENTO} valor={movimiento.tipo} className="mt-0.5" />
                    <div className="min-w-0 flex-1">
                      <p>
                        {movimiento.tipo === "PESAJE"
                          ? `Verificado: ${cantidad(movimiento.cantidad, contenedor.unidad)}`
                          : `${movimiento.origen} → ${movimiento.destino} · ${cantidad(movimiento.cantidad, contenedor.unidad)}`}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {movimiento.usuario} · {fechaHora(movimiento.fecha)}
                      </p>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </DetailSection>

          {contenedor.observaciones.length > 0 && (
            <DetailSection title="Observaciones">
              <ul className="space-y-2">
                {contenedor.observaciones.map((item) => (
                  <li key={item.idObservacion} className="text-sm">
                    <p>{item.texto}</p>
                    <p className="text-xs text-muted-foreground">
                      {item.autor} · {fechaHora(item.fecha)}
                    </p>
                  </li>
                ))}
              </ul>
            </DetailSection>
          )}
        </>
      )}
    </DetailSheet>
  )
}
