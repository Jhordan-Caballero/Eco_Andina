import { useState } from "react"
import { useSearchParams } from "react-router-dom"
import { useQuery } from "@tanstack/react-query"
import { AlertTriangle, ArrowLeftRight, Clock, Gauge, Package, Plus } from "lucide-react"
import { EmptyState } from "@/components/shared/EmptyState"
import { MeterBar } from "@/components/shared/MeterBar"
import { StatGrid, StatTile } from "@/components/shared/StatTile"
import { EstadoBadge } from "@/components/shared/StatusBadge"
import { FilterSelect, SearchInput, Toolbar } from "@/components/shared/Toolbar"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Switch } from "@/components/ui/switch"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { useAuth } from "@/features/auth/AuthContext"
import { SITUACION_CONTENEDOR, opciones } from "@/lib/estados"
import { TODOS, pasaFiltro } from "@/lib/filtros"
import { cantidad, coincide, diasDesde } from "@/lib/formato"
import { ContenedorDetalle } from "./ContenedorDetalle"
import { MovimientoDialog } from "./MovimientoDialog"
import { MovimientosTab } from "./MovimientosTab"
import { listarAlmacenamiento, listarMovimientos } from "./almacenamientoApi"

const PESTANAS = ["contenedores", "movimientos"]

function EstadoPlazo({ contenedor }) {
  if (contenedor.estado === "MANTENIMIENTO") {
    return <p className="text-xs text-muted-foreground">Fuera de servicio</p>
  }
  if (contenedor.alertaPlazo) {
    return (
      <p className="flex items-center gap-1.5 text-xs font-medium text-warning">
        <Clock className="size-3.5" />
        {contenedor.plazoRestante < 0
          ? `Plazo vencido hace ${Math.abs(contenedor.plazoRestante)} días`
          : `Plazo: ${contenedor.plazoRestante} días restantes`}
      </p>
    )
  }
  if (!contenedor.fechaIngreso) {
    return <p className="text-xs text-muted-foreground">Sin residuos almacenados</p>
  }
  return (
    <p className="text-xs text-muted-foreground">
      {contenedor.diasAlmacenado} de {contenedor.residuo?.plazoMaximoDias} días de plazo
    </p>
  )
}

function ContenedorCard({ contenedor, onSelect }) {
  return (
    <button
      type="button"
      onClick={onSelect}
      aria-label={`${contenedor.codigo}, ${contenedor.residuo?.nombre}, ${contenedor.porcentaje}% de ocupación`}
      className="flex flex-col gap-3 rounded-xl bg-card p-4 text-left ring-1 ring-foreground/10 transition-shadow outline-none hover:ring-foreground/25 focus-visible:ring-2 focus-visible:ring-ring"
    >
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0">
          <p className="font-heading text-lg leading-tight font-semibold tabular-nums">{contenedor.codigo}</p>
          <p className="truncate text-xs text-muted-foreground">{contenedor.ubicacion}</p>
        </div>
        <EstadoBadge mapa={SITUACION_CONTENEDOR} valor={contenedor.situacion} />
      </div>
      <div className="min-w-0">
        <p className="truncate text-sm font-medium">{contenedor.residuo?.nombre}</p>
        <p className="truncate text-xs text-muted-foreground">{contenedor.tipo}</p>
      </div>
      <div className="space-y-1.5">
        <div className="flex items-baseline justify-between gap-2 text-xs text-muted-foreground tabular-nums">
          <span>
            {cantidad(contenedor.ocupacion, contenedor.unidad)} de {cantidad(contenedor.capacidad, contenedor.unidad)}
          </span>
          <span className="font-medium text-foreground">{contenedor.porcentaje}%</span>
        </div>
        <MeterBar value={contenedor.porcentaje} label={`Ocupación de ${contenedor.codigo}`} />
      </div>
      <div className="mt-auto space-y-1">
        {contenedor.alertaCapacidad && (
          <p className="flex items-center gap-1.5 text-xs font-medium text-warning">
            <AlertTriangle className="size-3.5" />
            {contenedor.porcentaje >= 100 ? "Contenedor lleno" : "Cerca de su capacidad máxima"}
          </p>
        )}
        <EstadoPlazo contenedor={contenedor} />
      </div>
    </button>
  )
}

function ContenedoresPanel({ contenedores, isPending, actions, onSelect }) {
  const [busqueda, setBusqueda] = useState("")
  const [situacion, setSituacion] = useState(TODOS)
  const [soloAlertas, setSoloAlertas] = useState(false)
  const filtrados = contenedores.filter(
    (item) =>
      pasaFiltro(situacion, item.situacion) &&
      (!soloAlertas || item.alertaCapacidad || item.alertaPlazo) &&
      coincide(busqueda, item.codigo, item.residuo?.nombre, item.ubicacion, item.tipo)
  )

  return (
    <div className="space-y-4">
      <Toolbar actions={actions}>
        <SearchInput value={busqueda} onChange={setBusqueda} placeholder="Contenedor, residuo o ubicación" />
        <FilterSelect
          label="Situación"
          value={situacion}
          onChange={setSituacion}
          options={opciones(SITUACION_CONTENEDOR)}
          todos="Todas"
        />
        <div className="flex items-center gap-2 px-1">
          <Switch id="solo-alertas" checked={soloAlertas} onCheckedChange={setSoloAlertas} />
          <Label htmlFor="solo-alertas" className="font-normal">
            Solo con alertas
          </Label>
        </div>
      </Toolbar>
      {isPending ? (
        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 6 }, (_, index) => (
            <div key={index} className="h-48 animate-pulse rounded-xl bg-muted" />
          ))}
        </div>
      ) : filtrados.length === 0 ? (
        <div className="rounded-xl bg-card ring-1 ring-foreground/10">
          <EmptyState icon={Package}>
            {soloAlertas ? "No hay contenedores con alertas activas." : "Ningún contenedor coincide con los filtros."}
          </EmptyState>
        </div>
      ) : (
        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
          {filtrados.map((contenedor) => (
            <ContenedorCard
              key={contenedor.idContenedor}
              contenedor={contenedor}
              onSelect={() => onSelect(contenedor.idContenedor)}
            />
          ))}
        </div>
      )}
    </div>
  )
}

export default function AlmacenamientoPage() {
  const { hasPermission } = useAuth()
  const puedeGestionar = hasPermission("ALMACENAMIENTO_GESTIONAR")
  const [params, setParams] = useSearchParams()
  const pestana = PESTANAS.includes(params.get("tab")) ? params.get("tab") : "contenedores"
  const { data: contenedores = [], isPending } = useQuery({
    queryKey: ["almacenamiento"],
    queryFn: listarAlmacenamiento,
  })
  const { data: movimientos = [] } = useQuery({ queryKey: ["movimientos"], queryFn: listarMovimientos })
  const [seleccion, setSeleccion] = useState(null)
  const [movimiento, setMovimiento] = useState(null)

  const operativos = contenedores.filter((item) => item.estado !== "MANTENIMIENTO")
  const ocupacionPromedio = operativos.length
    ? Math.round(operativos.reduce((total, item) => total + item.porcentaje, 0) / operativos.length)
    : 0
  const alertasCapacidad = contenedores.filter((item) => item.alertaCapacidad).length
  const alertasPlazo = contenedores.filter((item) => item.alertaPlazo).length
  const movimientosSemana = movimientos.filter((item) => diasDesde(item.fecha) <= 7).length

  const botonMovimiento = puedeGestionar && (
    <Button onClick={() => setMovimiento({})} disabled={isPending}>
      <Plus />
      Registrar movimiento
    </Button>
  )

  return (
    <div className="space-y-6">
      <StatGrid>
        <StatTile
          label="Ocupación promedio"
          value={`${ocupacionPromedio}%`}
          hint={`${operativos.length} contenedores operativos`}
          icon={Gauge}
        />
        <StatTile
          label="Alertas de capacidad"
          value={alertasCapacidad}
          hint="Contenedores al 85% o más"
          tone={alertasCapacidad > 0 ? "warning" : undefined}
          icon={AlertTriangle}
        />
        <StatTile
          label="Plazos por vencer"
          value={alertasPlazo}
          hint="5 días o menos de almacenamiento permitido"
          tone={alertasPlazo > 0 ? "danger" : undefined}
          icon={Clock}
        />
        <StatTile
          label="Movimientos"
          value={movimientosSemana}
          hint="Registrados en los últimos 7 días"
          icon={ArrowLeftRight}
        />
      </StatGrid>

      <Tabs value={pestana} onValueChange={(valor) => setParams({ tab: valor }, { replace: true })} className="gap-4">
        <TabsList>
          <TabsTrigger value="contenedores">Contenedores</TabsTrigger>
          <TabsTrigger value="movimientos">Movimientos</TabsTrigger>
        </TabsList>
        <TabsContent value="contenedores">
          <ContenedoresPanel
            contenedores={contenedores}
            isPending={isPending}
            actions={botonMovimiento}
            onSelect={setSeleccion}
          />
        </TabsContent>
        <TabsContent value="movimientos">
          <MovimientosTab actions={botonMovimiento} />
        </TabsContent>
      </Tabs>

      {seleccion && (
        <ContenedorDetalle
          idContenedor={seleccion}
          onClose={() => setSeleccion(null)}
          onMovimiento={(inicial) => {
            setSeleccion(null)
            setMovimiento(inicial)
          }}
        />
      )}
      {movimiento && (
        <MovimientoDialog contenedores={contenedores} inicial={movimiento} onClose={() => setMovimiento(null)} />
      )}
    </div>
  )
}
