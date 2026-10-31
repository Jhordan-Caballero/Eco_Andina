import { useSearchParams } from "react-router-dom"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { RolesTab } from "./RolesTab"
import { UsuariosTab } from "./UsuariosTab"

const PESTANAS = ["usuarios", "roles"]

export default function UsuariosPage() {
  const [params, setParams] = useSearchParams()
  const pestana = PESTANAS.includes(params.get("tab")) ? params.get("tab") : "usuarios"

  return (
    <Tabs value={pestana} onValueChange={(valor) => setParams({ tab: valor }, { replace: true })} className="gap-4">
      <TabsList>
        <TabsTrigger value="usuarios">Usuarios</TabsTrigger>
        <TabsTrigger value="roles">Roles y permisos</TabsTrigger>
      </TabsList>
      <TabsContent value="usuarios">
        <UsuariosTab />
      </TabsContent>
      <TabsContent value="roles">
        <RolesTab />
      </TabsContent>
    </Tabs>
  )
}
