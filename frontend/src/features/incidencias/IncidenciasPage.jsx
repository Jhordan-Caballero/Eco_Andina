import { useSearchParams } from "react-router-dom"
import { useQuery } from "@tanstack/react-query"
import { AlertOctagon, CheckCircle2, Clock, Siren } from "lucide-react"
import { StatGrid, StatTile } from "@/components/shared/StatTile"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { diasDesde } from "@/lib/formato"
import { IncidenciasTab } from "./IncidenciasTab"
import { ObservacionesTab } from "./ObservacionesTab"
import { listarIncidencias } from "./incidenciasApi"

const PESTANAS = ["incidencias", "observaciones"]

export default function IncidenciasPage() {
  const [params, setParams] = useSearchParams()
  const pestana = PESTANAS.includes(params.get("tab")) ? params.get("tab") : "incidencias"
  const { data = [] } = useQuery({ queryKey: ["incidencias"], queryFn: listarIncidencias })

  const abiertas = data.filter((item) => item.estado === "ABIERTA").length
  const seguimiento = data.filter((item) => item.estado === "EN_SEGUIMIENTO").length
  const criticas = data.filter((item) => item.estado !== "CERRADA" && item.severidad === "ALTA").length
  const cerradasMes = data.filter((item) => item.fechaCierre && diasDesde(item.fechaCierre) <= 30).length

  return (
    <div className="space-y-6">
      <StatGrid>
        <StatTile
          label="Abiertas"
          value={abiertas}
          hint="Sin responsable asignado"
          tone={abiertas > 0 ? "danger" : undefined}
          icon={Siren}
        />
        <StatTile
          label="En seguimiento"
          value={seguimiento}
          hint="Con acción correctiva en curso"
          tone={seguimiento > 0 ? "warning" : undefined}
          icon={Clock}
        />
        <StatTile label="Severidad alta" value={criticas} hint="Incidencias activas críticas" icon={AlertOctagon} />
        <StatTile label="Cerradas" value={cerradasMes} hint="En los últimos 30 días" icon={CheckCircle2} />
      </StatGrid>

      <Tabs value={pestana} onValueChange={(valor) => setParams({ tab: valor }, { replace: true })} className="gap-4">
        <TabsList>
          <TabsTrigger value="incidencias">Incidencias</TabsTrigger>
          <TabsTrigger value="observaciones">Observaciones operativas</TabsTrigger>
        </TabsList>
        <TabsContent value="incidencias">
          <IncidenciasTab />
        </TabsContent>
        <TabsContent value="observaciones">
          <ObservacionesTab />
        </TabsContent>
      </Tabs>
    </div>
  )
}
