import { useState } from "react"
import { useSearchParams } from "react-router-dom"
import { useQuery } from "@tanstack/react-query"
import { CalendarClock, ClipboardCheck, Hourglass, Plus, Truck } from "lucide-react"
import { EmptyRow } from "@/components/shared/EmptyState"
import { SegmentedFilter } from "@/components/shared/SegmentedFilter"
import { StatGrid, StatTile } from "@/components/shared/StatTile"
import { EstadoBadge } from "@/components/shared/StatusBadge"
import { LoadingRows, RowButton, TablePanel } from "@/components/shared/TablePanel"
import { FilterSelect, SearchInput, Toolbar } from "@/components/shared/Toolbar"
import { Button } from "@/components/ui/button"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { useAuth } from "@/features/auth/AuthContext"
import { ESTADO_RECOLECCION } from "@/lib/estados"
import { TODOS, pasaFiltro } from "@/lib/filtros"
import { cantidad, coincide, diasHasta, fecha } from "@/lib/formato"
import { AccionRecoleccion, RecepcionDialog, SalidaDialog } from "./AccionesRecoleccion"
import { ProgramarRecoleccionDialog } from "./ProgramarRecoleccionDialog"
import { RecoleccionDetalle } from "./RecoleccionDetalle"
import { listarRecolecciones, opcionesProgramacion } from "./recoleccionesApi"

export default function RecoleccionesPage() {
  const { hasPermission } = useAuth()
  const puedeProgramar = hasPermission("RECOLECCION_PROGRAMAR")
  const [params, setParams] = useSearchParams()
  const { data = [], isPending } = useQuery({ queryKey: ["recolecciones"], queryFn: listarRecolecciones })
  const { data: opcionesFormulario } = useQuery({
    queryKey: ["recoleccion-opciones"],
    queryFn: opcionesProgramacion,
    enabled: puedeProgramar,
  })
  const [estado, setEstado] = useState(TODOS)
  const [busqueda, setBusqueda] = useState("")
  const [transportista, setTransportista] = useState(TODOS)
  const [programando, setProgramando] = useState(false)
  const [dialogo, setDialogo] = useState(null)

  const seleccion = data.find((item) => item.numeroOrden === params.get("orden"))
  const abrir = (orden) => setParams({ orden: orden.numeroOrden }, { replace: true })
  const cerrarDetalle = () => setParams({}, { replace: true })

  const contar = (valor) => data.filter((item) => item.estado === valor).length
  const proximas = data.filter(
    (item) => item.estado === "PROGRAMADA" && diasHasta(item.fechaProgramada) >= 0 && diasHasta(item.fechaProgramada) <= 7
  ).length
  const porCerrar = data.filter((item) => item.estado === "RECIBIDA")
  const bloqueadas = porCerrar.filter((item) => item.requisitosCierre.some((requisito) => !requisito.cumple)).length
  const transportistas = [...new Map(data.map((item) => [item.idTransportista, item.transportista])).values()]
  const filtrados = data.filter(
    (item) =>
      pasaFiltro(estado, item.estado) &&
      pasaFiltro(transportista, item.idTransportista) &&
      coincide(busqueda, item.numeroOrden, item.salida?.documento, item.gestor?.empresa, ...item.residuos, ...item.areas)
  )

  return (
    <div className="space-y-6">
      <StatGrid>
        <StatTile
          label="Por aprobar"
          value={contar("PENDIENTE")}
          hint="Órdenes solicitadas sin aprobación"
          tone={contar("PENDIENTE") > 0 ? "warning" : undefined}
          icon={Hourglass}
        />
        <StatTile label="Programadas" value={contar("PROGRAMADA")} hint={`${proximas} en los próximos 7 días`} icon={CalendarClock} />
        <StatTile
          label="En tránsito"
          value={contar("EN_TRANSITO")}
          hint="Esperan confirmación del gestor"
          tone={contar("EN_TRANSITO") > 0 ? "primary" : undefined}
          icon={Truck}
        />
        <StatTile
          label="Por cerrar"
          value={porCerrar.length}
          hint={bloqueadas > 0 ? `${bloqueadas} sin documentación completa` : "Con documentación completa"}
          tone={bloqueadas > 0 ? "danger" : undefined}
          icon={ClipboardCheck}
        />
      </StatGrid>

      <div className="space-y-4">
        <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <SegmentedFilter
            label="Estado de la orden"
            value={estado}
            onChange={setEstado}
            options={[
              { value: TODOS, label: "Todas", count: data.length },
              ...Object.entries(ESTADO_RECOLECCION).map(([valor, item]) => ({
                value: valor,
                label: item.label,
                count: contar(valor),
              })),
            ]}
          />
          {puedeProgramar && (
            <Button onClick={() => setProgramando(true)} disabled={!opcionesFormulario}>
              <Plus />
              Programar recolección
            </Button>
          )}
        </div>

        <Toolbar>
          <SearchInput value={busqueda} onChange={setBusqueda} placeholder="Orden, residuo o documento" />
          <FilterSelect
            label="Transportista"
            value={transportista}
            onChange={setTransportista}
            options={transportistas.map((item) => ({ value: item.idTransportista, label: item.empresa }))}
          />
        </Toolbar>

        <TablePanel>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Orden</TableHead>
                <TableHead>Fecha programada</TableHead>
                <TableHead>Residuos</TableHead>
                <TableHead className="text-right">Cantidad</TableHead>
                <TableHead>Transporte y destino</TableHead>
                <TableHead>Estado</TableHead>
                <TableHead className="text-right">Siguiente paso</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isPending && <LoadingRows colSpan={7} />}
              {!isPending && filtrados.length === 0 && (
                <EmptyRow colSpan={7}>Ninguna orden coincide con los filtros.</EmptyRow>
              )}
              {filtrados.map((orden) => (
                <TableRow key={orden.idRecoleccion} className="cursor-pointer" onClick={() => abrir(orden)}>
                  <TableCell>
                    <RowButton onClick={() => abrir(orden)}>{orden.numeroOrden}</RowButton>
                  </TableCell>
                  <TableCell className="tabular-nums">{fecha(orden.fechaProgramada)}</TableCell>
                  <TableCell>
                    <div className="max-w-56 truncate font-medium">{orden.residuos.join(", ")}</div>
                    <div className="text-xs text-muted-foreground">{orden.areas.join(", ")}</div>
                  </TableCell>
                  <TableCell className="text-right tabular-nums">{cantidad(orden.cantidadTotal, orden.unidad)}</TableCell>
                  <TableCell>
                    <div>
                      {orden.transportista?.empresa}
                      <span className="ml-1.5 text-xs text-muted-foreground tabular-nums">{orden.vehiculo?.placa}</span>
                    </div>
                    <div className="max-w-60 truncate text-xs text-muted-foreground">{orden.gestor?.empresa}</div>
                  </TableCell>
                  <TableCell>
                    <EstadoBadge mapa={ESTADO_RECOLECCION} valor={orden.estado} />
                  </TableCell>
                  <TableCell className="text-right" onClick={(event) => event.stopPropagation()}>
                    <AccionRecoleccion orden={orden} size="sm" onDialog={(tipo) => setDialogo({ tipo, orden })} />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TablePanel>
      </div>

      {programando && opcionesFormulario && (
        <ProgramarRecoleccionDialog opcionesProgramacion={opcionesFormulario} onClose={() => setProgramando(false)} />
      )}
      {seleccion && (
        <RecoleccionDetalle
          idRecoleccion={seleccion.idRecoleccion}
          onClose={cerrarDetalle}
          onDialog={(tipo, orden) => setDialogo({ tipo, orden })}
        />
      )}
      {dialogo?.tipo === "salida" && <SalidaDialog orden={dialogo.orden} onClose={() => setDialogo(null)} />}
      {dialogo?.tipo === "recepcion" && <RecepcionDialog orden={dialogo.orden} onClose={() => setDialogo(null)} />}
    </div>
  )
}
