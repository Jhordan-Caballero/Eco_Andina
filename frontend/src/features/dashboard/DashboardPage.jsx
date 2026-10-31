import { useQuery } from "@tanstack/react-query"
import { Info } from "lucide-react"
import { AlertasAlmacenamiento } from "./AlertasAlmacenamiento"
import { fetchResumen } from "./dashboardApi"
import { useActor } from "@/hooks/useActor"
import { KpiCard } from "./KpiCard"
import { MisPendientes } from "./MisPendientes"
import { OrdenesRecientes } from "./OrdenesRecientes"

function Skeleton({ className }) {
  return <div className={`animate-pulse rounded-xl bg-muted ${className}`} />
}

function DashboardSkeleton() {
  return (
    <div className="space-y-6" role="status" aria-label="Cargando panel">
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {[0, 1, 2, 3].map((i) => (
          <Skeleton key={i} className="h-40" />
        ))}
      </div>
      <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_22rem]">
        <Skeleton className="h-80" />
        <Skeleton className="h-80" />
      </div>
    </div>
  )
}

export default function DashboardPage() {
  const actor = useActor()
  const { data, isPending, isError } = useQuery({
    queryKey: ["dashboard", "resumen", actor],
    queryFn: () => fetchResumen(actor),
  })

  if (isPending) return <DashboardSkeleton />
  if (isError) {
    return (
      <p role="alert" className="text-sm text-destructive">
        No se pudo cargar el panel. Intenta nuevamente.
      </p>
    )
  }

  const { kpis, ordenesRecientes, alertas, pendientes } = data

  return (
    <div className="space-y-6">
      <p className="flex items-center gap-2 text-xs text-muted-foreground">
        <Info className="size-3.5 shrink-0" />
        Datos de demostración: los módulos trabajan con datos simulados en el navegador y se reinician al recargar la página.
      </p>

      <section
        aria-label="Indicadores"
        className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4"
      >
        <KpiCard
          label="Trazabilidad"
          value={kpis.trazabilidad.valor}
          suffix="%"
          progress={kpis.trazabilidad.valor}
          description={kpis.trazabilidad.descripcion}
        />
        <KpiCard
          label="Órdenes activas"
          value={kpis.ordenesActivas.valor}
          description={kpis.ordenesActivas.descripcion}
        />
        <KpiCard
          label="Incidencias abiertas"
          value={kpis.incidenciasAbiertas.valor}
          description={kpis.incidenciasAbiertas.descripcion}
        />
        <KpiCard
          label="Alertas de capacidad"
          value={kpis.alertasCapacidad.valor}
          tone="warning"
          description={kpis.alertasCapacidad.descripcion}
        />
      </section>

      <div className="grid items-start gap-4 2xl:grid-cols-[minmax(0,1fr)_24rem]">
        <OrdenesRecientes ordenes={ordenesRecientes} />
        <div className="grid items-start gap-4 lg:grid-cols-2 2xl:grid-cols-1">
          <MisPendientes pendientes={pendientes} />
          <AlertasAlmacenamiento alertas={alertas} />
        </div>
      </div>
    </div>
  )
}
