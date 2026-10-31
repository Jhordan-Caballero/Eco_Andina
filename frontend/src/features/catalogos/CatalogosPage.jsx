import { useSearchParams } from "react-router-dom"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { AreasTab } from "./AreasTab"
import { ContenedoresTab } from "./ContenedoresTab"
import { ResiduosTab } from "./ResiduosTab"

const PESTANAS = ["residuos", "areas", "contenedores"]

export default function CatalogosPage() {
  const [params, setParams] = useSearchParams()
  const pestana = PESTANAS.includes(params.get("tab")) ? params.get("tab") : "residuos"

  return (
    <Tabs value={pestana} onValueChange={(valor) => setParams({ tab: valor }, { replace: true })} className="gap-4">
      <TabsList>
        <TabsTrigger value="residuos">Residuos</TabsTrigger>
        <TabsTrigger value="areas">Áreas generadoras</TabsTrigger>
        <TabsTrigger value="contenedores">Contenedores</TabsTrigger>
      </TabsList>
      <TabsContent value="residuos">
        <ResiduosTab />
      </TabsContent>
      <TabsContent value="areas">
        <AreasTab />
      </TabsContent>
      <TabsContent value="contenedores">
        <ContenedoresTab />
      </TabsContent>
    </Tabs>
  )
}
