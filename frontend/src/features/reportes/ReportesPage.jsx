import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { format, parseISO } from "date-fns"
import { es } from "date-fns/locale"
import { Download, LoaderCircle, Route, Scale, ShieldCheck, Siren } from "lucide-react"
import { SegmentedFilter } from "@/components/shared/SegmentedFilter"
import { StatGrid, StatTile } from "@/components/shared/StatTile"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/features/auth/AuthContext"
import { useActor } from "@/hooks/useActor"
import { useMockMutation } from "@/hooks/useMockMutation"
import { cantidad, numero, porcentaje } from "@/lib/formato"
import { BarList, ChartCard, ColumnChart, DataTable, ShareBar } from "./charts"
import { PERIODOS, exportarReporte, generarReporte } from "./reportesApi"

const kg = (valor) => cantidad(valor, "kg")
const etiquetaMes = (item) => format(parseISO(`${item.mes}-01`), "MMM yy", { locale: es })

function descargar({ nombreArchivo, contenido }) {
  const url = URL.createObjectURL(new Blob([contenido], { type: "text/csv;charset=utf-8" }))
  const enlace = document.createElement("a")
  enlace.href = url
  enlace.download = nombreArchivo
  enlace.click()
  URL.revokeObjectURL(url)
}

export default function ReportesPage() {
  const { hasPermission } = useAuth()
  const actor = useActor()
  const [periodo, setPeriodo] = useState("30")
  const { data: reporte, isPending } = useQuery({
    queryKey: ["reporte", periodo],
    queryFn: () => generarReporte(periodo),
    placeholderData: (anterior) => anterior,
  })
  const exportar = useMockMutation(exportarReporte, {
    success: (archivo) => `Se descargó ${archivo.nombreArchivo}`,
    onSuccess: descargar,
  })

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <SegmentedFilter label="Periodo" value={periodo} onChange={setPeriodo} options={PERIODOS} />
        {hasPermission("REPORTE_EXPORTAR") && (
          <Button variant="outline" onClick={() => exportar.mutate({ periodo, actor })} disabled={!reporte || exportar.isPending}>
            {exportar.isPending ? <LoaderCircle className="animate-spin" /> : <Download />}
            Exportar CSV
          </Button>
        )}
      </div>

      {isPending || !reporte ? (
        <div className="space-y-4" role="status" aria-label="Generando reporte">
          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            {[0, 1, 2, 3].map((index) => (
              <div key={index} className="h-32 animate-pulse rounded-xl bg-muted" />
            ))}
          </div>
          <div className="grid gap-4 lg:grid-cols-2">
            {[0, 1].map((index) => (
              <div key={index} className="h-80 animate-pulse rounded-xl bg-muted" />
            ))}
          </div>
        </div>
      ) : (
        <>
          <StatGrid>
            <StatTile
              label="Residuos generados"
              value={kg(reporte.kpis.totalKg)}
              hint={`${reporte.kpis.registros} registros · ${reporte.kpis.peligrosos}% peligrosos`}
              icon={Scale}
            />
            <StatTile
              label="Tasa de validación"
              value={`${reporte.kpis.tasaValidacion}%`}
              hint={`${reporte.kpis.validadas} de ${reporte.kpis.registros} registros validados`}
              icon={ShieldCheck}
            />
            <StatTile
              label="Trazabilidad completa"
              value={`${reporte.kpis.trazabilidad}%`}
              hint={`${reporte.kpis.cerradas} de ${reporte.kpis.conRecepcion} órdenes recibidas están cerradas`}
              tone={reporte.kpis.trazabilidad < 80 ? "warning" : "primary"}
              icon={Route}
            />
            <StatTile
              label="Incidencias"
              value={reporte.kpis.incidencias}
              hint={`${reporte.kpis.incidenciasAbiertas} siguen abiertas o en seguimiento`}
              tone={reporte.kpis.incidenciasAbiertas > 0 ? "danger" : undefined}
              icon={Siren}
            />
          </StatGrid>

          <div className="grid items-start gap-4 lg:grid-cols-2">
            <ChartCard
              title="Generación por área"
              description="Residuos medidos en kg dentro del periodo"
              table={
                <DataTable
                  columnas={["Área", "Cantidad", "Participación"]}
                  filas={reporte.porArea.map((item) => [
                    item.label,
                    kg(item.valor),
                    `${porcentaje(item.valor, reporte.kpis.totalKg)}%`,
                  ])}
                />
              }
            >
              <BarList datos={reporte.porArea} formato={kg} />
            </ChartCard>

            <ChartCard
              title="Evolución mensual"
              description="Kilogramos registrados en los últimos seis meses"
              table={
                <DataTable
                  columnas={["Mes", "Cantidad", "Registros"]}
                  filas={reporte.mensual.map((item) => [
                    `${etiquetaMes(item)}${item.parcial ? " (en curso)" : ""}`,
                    kg(item.valor),
                    item.registros,
                  ])}
                />
              }
            >
              <ColumnChart
                datos={reporte.mensual}
                formato={kg}
                formatoEje={(valor) => numero(valor)}
                etiqueta={etiquetaMes}
              />
            </ChartCard>

            <ChartCard
              title="Destino de los residuos"
              description="Kilogramos entregados a gestores en el semestre"
              table={
                <DataTable
                  columnas={["Destino", "Cantidad", "Participación"]}
                  filas={reporte.destinos.map((item) => [
                    item.label,
                    kg(item.valor),
                    `${porcentaje(item.valor, reporte.destinos.reduce((total, destino) => total + destino.valor, 0))}%`,
                  ])}
                />
              }
            >
              <ShareBar datos={reporte.destinos} formato={kg} />
            </ChartCard>

            <ChartCard
              title="Incidencias por tipo"
              description="Reportes registrados en el periodo"
              table={
                <DataTable
                  columnas={["Tipo", "Incidencias"]}
                  filas={reporte.incidenciasPorTipo.map((item) => [item.label, item.valor])}
                />
              }
            >
              <BarList
                datos={reporte.incidenciasPorTipo}
                formato={(valor) => numero(valor)}
                vacio="No se registraron incidencias en el periodo."
              />
            </ChartCard>
          </div>
        </>
      )}
    </div>
  )
}
