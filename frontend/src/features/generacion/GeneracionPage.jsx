import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { AlertTriangle, CheckCircle2, ClipboardList, Clock, Plus, Truck } from "lucide-react"
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
import { useActor } from "@/hooks/useActor"
import { CATEGORIA_RESIDUO, ESTADO_GENERACION } from "@/lib/estados"
import { TODOS, pasaFiltro } from "@/lib/filtros"
import { cantidad, coincide, fechaHora } from "@/lib/formato"
import { GeneracionDetalle } from "./GeneracionDetalle"
import { RegistrarGeneracionDialog } from "./RegistrarGeneracionDialog"
import { listarGeneraciones, opcionesRegistro } from "./generacionApi"

export default function GeneracionPage() {
  const { hasPermission } = useAuth()
  const actor = useActor()
  const puedeRegistrar = hasPermission("GENERACION_REGISTRAR")
  const puedeValidar = hasPermission("GENERACION_VALIDAR")
  const { data = [], isPending } = useQuery({ queryKey: ["generaciones"], queryFn: listarGeneraciones })
  const { data: opcionesFormulario } = useQuery({
    queryKey: ["generacion-opciones"],
    queryFn: opcionesRegistro,
    enabled: puedeRegistrar,
  })
  const [estado, setEstado] = useState(TODOS)
  const [busqueda, setBusqueda] = useState("")
  const [area, setArea] = useState(TODOS)
  const [registrando, setRegistrando] = useState(false)
  const [seleccion, setSeleccion] = useState(null)

  const visibles = puedeValidar ? data : data.filter((item) => item.responsable === actor)
  const contar = (valor) => visibles.filter((item) => item.estadoValidacion === valor).length
  const listosParaRecoleccion = visibles.filter(
    (item) => item.estadoValidacion === "VALIDADA" && !item.recoleccion
  ).length
  const areas = [...new Map(visibles.map((item) => [item.idArea, item.area])).values()]
  const filtrados = visibles.filter(
    (item) =>
      pasaFiltro(estado, item.estadoValidacion) &&
      pasaFiltro(area, item.idArea) &&
      coincide(busqueda, item.codigo, item.residuo?.nombre, item.contenedor?.codigo, item.responsable)
  )
  const columnas = puedeValidar ? 8 : 7

  return (
    <div className="space-y-6">
      <StatGrid>
        <StatTile
          label="Pendientes"
          value={contar("PENDIENTE")}
          hint={puedeValidar ? "Esperan tu validación" : "En revisión del supervisor"}
          tone={contar("PENDIENTE") > 0 ? "warning" : undefined}
          icon={Clock}
        />
        <StatTile
          label="Observados"
          value={contar("OBSERVADA")}
          hint="Deben corregirse y reenviarse"
          tone={contar("OBSERVADA") > 0 ? "danger" : undefined}
          icon={AlertTriangle}
        />
        <StatTile label="Validados" value={contar("VALIDADA")} hint="Aptos para almacenamiento y retiro" icon={CheckCircle2} />
        <StatTile
          label="Listos para recolección"
          value={listosParaRecoleccion}
          hint="Validados sin orden asignada"
          tone={listosParaRecoleccion > 0 ? "primary" : undefined}
          icon={Truck}
        />
      </StatGrid>

      <div className="space-y-4">
        <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <SegmentedFilter
            label="Estado de validación"
            value={estado}
            onChange={setEstado}
            options={[
              { value: TODOS, label: "Todos", count: visibles.length },
              { value: "PENDIENTE", label: "Pendientes", count: contar("PENDIENTE") },
              { value: "OBSERVADA", label: "Observados", count: contar("OBSERVADA") },
              { value: "VALIDADA", label: "Validados", count: contar("VALIDADA") },
            ]}
          />
          {puedeRegistrar && (
            <Button onClick={() => setRegistrando(true)} disabled={!opcionesFormulario}>
              <Plus />
              Registrar generación
            </Button>
          )}
        </div>

        <Toolbar>
          <SearchInput value={busqueda} onChange={setBusqueda} placeholder="Código, residuo o contenedor" />
          <FilterSelect
            label="Área"
            value={area}
            onChange={setArea}
            todos="Todas"
            options={areas.map((item) => ({ value: item.idArea, label: item.nombre }))}
          />
          {!puedeValidar && (
            <p className="text-sm text-muted-foreground">Se muestran solo los registros que realizaste.</p>
          )}
        </Toolbar>

        <TablePanel>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Código</TableHead>
                <TableHead>Fecha</TableHead>
                <TableHead>Residuo</TableHead>
                <TableHead>Área</TableHead>
                <TableHead className="text-right">Cantidad</TableHead>
                <TableHead>Contenedor</TableHead>
                {puedeValidar && <TableHead>Responsable</TableHead>}
                <TableHead>Estado</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isPending && <LoadingRows colSpan={columnas} />}
              {!isPending && filtrados.length === 0 && (
                <EmptyRow colSpan={columnas}>
                  {visibles.length === 0
                    ? "Aún no hay registros de generación."
                    : "Ningún registro coincide con los filtros."}
                </EmptyRow>
              )}
              {filtrados.map((generacion) => (
                <TableRow
                  key={generacion.idGeneracion}
                  className="cursor-pointer"
                  onClick={() => setSeleccion(generacion.idGeneracion)}
                >
                  <TableCell>
                    <RowButton onClick={() => setSeleccion(generacion.idGeneracion)}>{generacion.codigo}</RowButton>
                  </TableCell>
                  <TableCell className="text-muted-foreground tabular-nums">{fechaHora(generacion.fecha)}</TableCell>
                  <TableCell>
                    <div className="font-medium">{generacion.residuo?.nombre}</div>
                    <div className="text-xs text-muted-foreground">
                      {CATEGORIA_RESIDUO[generacion.residuo?.categoria]?.label}
                    </div>
                  </TableCell>
                  <TableCell>{generacion.area?.nombre}</TableCell>
                  <TableCell className="text-right tabular-nums">
                    {cantidad(generacion.cantidad, generacion.unidad)}
                  </TableCell>
                  <TableCell className="tabular-nums">{generacion.contenedor?.codigo}</TableCell>
                  {puedeValidar && <TableCell>{generacion.responsable}</TableCell>}
                  <TableCell>
                    <div className="flex flex-wrap items-center gap-1.5">
                      <EstadoBadge mapa={ESTADO_GENERACION} valor={generacion.estadoValidacion} />
                      {generacion.recoleccion && (
                        <span className="text-xs text-muted-foreground tabular-nums">
                          {generacion.recoleccion.numeroOrden}
                        </span>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TablePanel>
        {!isPending && filtrados.length > 0 && (
          <p className="flex items-center gap-1.5 text-xs text-muted-foreground">
            <ClipboardList className="size-3.5" />
            {filtrados.length} de {visibles.length} registros
          </p>
        )}
      </div>

      {registrando && opcionesFormulario && (
        <RegistrarGeneracionDialog opcionesRegistro={opcionesFormulario} onClose={() => setRegistrando(false)} />
      )}
      {seleccion && <GeneracionDetalle idGeneracion={seleccion} onClose={() => setSeleccion(null)} />}
    </div>
  )
}
