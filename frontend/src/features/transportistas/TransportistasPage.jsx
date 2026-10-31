import { useSearchParams } from "react-router-dom"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { GestoresTab } from "./GestoresTab"
import { TransportistasTab } from "./TransportistasTab"

const PESTANAS = ["transportistas", "gestores"]

export default function TransportistasPage() {
  const [params, setParams] = useSearchParams()
  const pestana = PESTANAS.includes(params.get("tab")) ? params.get("tab") : "transportistas"

  return (
    <Tabs value={pestana} onValueChange={(valor) => setParams({ tab: valor }, { replace: true })} className="gap-4">
      <TabsList>
        <TabsTrigger value="transportistas">Transportistas (EO-RS)</TabsTrigger>
        <TabsTrigger value="gestores">Gestores autorizados</TabsTrigger>
      </TabsList>
      <TabsContent value="transportistas">
        <TransportistasTab />
      </TabsContent>
      <TabsContent value="gestores">
        <GestoresTab />
      </TabsContent>
    </Tabs>
  )
}
