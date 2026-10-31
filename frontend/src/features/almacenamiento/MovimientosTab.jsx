import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { ArrowRight } from "lucide-react"
import { EmptyRow } from "@/components/shared/EmptyState"
import { SegmentedFilter } from "@/components/shared/SegmentedFilter"
import { EstadoBadge } from "@/components/shared/StatusBadge"
import { LoadingRows, TablePanel } from "@/components/shared/TablePanel"
import { SearchInput, Toolbar } from "@/components/shared/Toolbar"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { TIPO_MOVIMIENTO } from "@/lib/estados"
import { TODOS, pasaFiltro } from "@/lib/filtros"
import { cantidad, coincide, fechaHora, numero } from "@/lib/formato"
import { listarMovimientos } from "./almacenamientoApi"

export function MovimientosTab({ actions }) {
  const { data = [], isPending } = useQuery({ queryKey: ["movimientos"], queryFn: listarMovimientos })
  const [tipo, setTipo] = useState(TODOS)
  const [busqueda, setBusqueda] = useState("")
  const contar = (valor) => data.filter((item) => item.tipo === valor).length
  const filtrados = data.filter(
    (item) =>
      pasaFiltro(tipo, item.tipo) &&
      coincide(busqueda, item.codigo, item.origen, item.destino, item.referencia, item.residuo?.nombre, item.usuario)
  )

  return (
    <div className="space-y-4">
      <SegmentedFilter
        label="Tipo de movimiento"
        value={tipo}
        onChange={setTipo}
        options={[
          { value: TODOS, label: "Todos", count: data.length },
          ...Object.entries(TIPO_MOVIMIENTO).map(([valor, item]) => ({
            value: valor,
            label: item.label,
            count: contar(valor),
          })),
        ]}
      />
      <Toolbar actions={actions}>
        <SearchInput value={busqueda} onChange={setBusqueda} placeholder="Contenedor, referencia o usuario" />
      </Toolbar>
      <TablePanel>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Código</TableHead>
              <TableHead>Fecha</TableHead>
              <TableHead>Tipo</TableHead>
              <TableHead>Residuo</TableHead>
              <TableHead>Origen y destino</TableHead>
              <TableHead className="text-right">Cantidad</TableHead>
              <TableHead>Referencia</TableHead>
              <TableHead>Registrado por</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isPending && <LoadingRows colSpan={8} />}
            {!isPending && filtrados.length === 0 && (
              <EmptyRow colSpan={8}>Ningún movimiento coincide con los filtros.</EmptyRow>
            )}
            {filtrados.map((movimiento) => (
              <TableRow key={movimiento.idMovimiento}>
                <TableCell className="font-medium tabular-nums">{movimiento.codigo}</TableCell>
                <TableCell className="text-muted-foreground tabular-nums">{fechaHora(movimiento.fecha)}</TableCell>
                <TableCell>
                  <EstadoBadge mapa={TIPO_MOVIMIENTO} valor={movimiento.tipo} />
                </TableCell>
                <TableCell>{movimiento.residuo?.nombre}</TableCell>
                <TableCell>
                  {movimiento.tipo === "PESAJE" ? (
                    movimiento.destino
                  ) : (
                    <span className="inline-flex items-center gap-1.5">
                      {movimiento.origen}
                      <ArrowRight className="size-3.5 text-muted-foreground" />
                      {movimiento.destino}
                    </span>
                  )}
                  <div className="text-xs text-muted-foreground">{movimiento.motivo}</div>
                </TableCell>
                <TableCell className="text-right tabular-nums">
                  {cantidad(movimiento.cantidad, movimiento.residuo?.unidad)}
                  {movimiento.ajuste != null && movimiento.ajuste !== 0 && (
                    <div className="text-xs text-muted-foreground">
                      ajuste {movimiento.ajuste > 0 ? "+" : "−"}
                      {numero(Math.abs(movimiento.ajuste))}
                    </div>
                  )}
                </TableCell>
                <TableCell className="tabular-nums">{movimiento.referencia}</TableCell>
                <TableCell>{movimiento.usuario}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TablePanel>
    </div>
  )
}
