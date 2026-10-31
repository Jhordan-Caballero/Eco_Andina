import { useState } from "react"
import { useSearchParams } from "react-router-dom"
import { useQuery } from "@tanstack/react-query"
import { Save } from "lucide-react"
import { EmptyRow } from "@/components/shared/EmptyState"
import { EstadoBadge } from "@/components/shared/StatusBadge"
import { LoadingRows, TablePanel } from "@/components/shared/TablePanel"
import { FilterSelect, SearchInput, Toolbar } from "@/components/shared/Toolbar"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Switch } from "@/components/ui/switch"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { useAuth } from "@/features/auth/AuthContext"
import { useActor } from "@/hooks/useActor"
import { useMockMutation } from "@/hooks/useMockMutation"
import { ACCION_AUDITORIA, opciones } from "@/lib/estados"
import { TODOS, pasaFiltro } from "@/lib/filtros"
import { coincide, diasDesde, fechaHora } from "@/lib/formato"
import { guardarConfiguracion, listarAuditoria, obtenerConfiguracion } from "./auditoriaApi"

const RANGOS = [
  { value: "0", label: "Hoy" },
  { value: "7", label: "Últimos 7 días" },
  { value: "30", label: "Últimos 30 días" },
]

const POR_PAGINA = 25

function Bitacora() {
  const { data = [], isPending } = useQuery({ queryKey: ["auditoria"], queryFn: listarAuditoria })
  const [busqueda, setBusqueda] = useState("")
  const [usuario, setUsuario] = useState(TODOS)
  const [entidad, setEntidad] = useState(TODOS)
  const [accion, setAccion] = useState(TODOS)
  const [rango, setRango] = useState(TODOS)
  const [limite, setLimite] = useState(POR_PAGINA)

  const usuarios = [...new Set(data.map((item) => item.usuario))].sort()
  const entidades = [...new Set(data.map((item) => item.entidad))].sort()
  const filtrados = data.filter(
    (item) =>
      pasaFiltro(usuario, item.usuario) &&
      pasaFiltro(entidad, item.entidad) &&
      pasaFiltro(accion, item.accion) &&
      (rango === TODOS || diasDesde(item.fecha) <= Number(rango)) &&
      coincide(busqueda, item.registro, item.detalle)
  )
  const visibles = filtrados.slice(0, limite)

  return (
    <div className="space-y-4">
      <Toolbar>
        <SearchInput value={busqueda} onChange={setBusqueda} placeholder="Registro o detalle" />
        <FilterSelect label="Periodo" value={rango} onChange={setRango} options={RANGOS} todos="Todo" />
        <FilterSelect label="Usuario" value={usuario} onChange={setUsuario} options={usuarios.map((item) => ({ value: item, label: item }))} />
        <FilterSelect label="Módulo" value={entidad} onChange={setEntidad} options={entidades.map((item) => ({ value: item, label: item }))} />
        <FilterSelect label="Acción" value={accion} onChange={setAccion} options={opciones(ACCION_AUDITORIA)} todos="Todas" />
      </Toolbar>

      <TablePanel>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Fecha</TableHead>
              <TableHead>Usuario</TableHead>
              <TableHead>Acción</TableHead>
              <TableHead>Módulo</TableHead>
              <TableHead>Registro</TableHead>
              <TableHead>Detalle</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isPending && <LoadingRows colSpan={6} rows={8} />}
            {!isPending && filtrados.length === 0 && (
              <EmptyRow colSpan={6}>No hay eventos que coincidan con los filtros.</EmptyRow>
            )}
            {visibles.map((item) => (
              <TableRow key={item.idAuditoria}>
                <TableCell className="text-muted-foreground tabular-nums">{fechaHora(item.fecha)}</TableCell>
                <TableCell>{item.usuario}</TableCell>
                <TableCell>
                  <EstadoBadge mapa={ACCION_AUDITORIA} valor={item.accion} />
                </TableCell>
                <TableCell>{item.entidad}</TableCell>
                <TableCell className="max-w-48 truncate font-medium tabular-nums">{item.registro}</TableCell>
                <TableCell className="min-w-64 whitespace-normal text-muted-foreground">{item.detalle}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TablePanel>
      <div className="flex items-center justify-between gap-3 text-xs text-muted-foreground">
        <span>
          {visibles.length} de {filtrados.length} eventos
        </span>
        {filtrados.length > limite && (
          <Button variant="outline" size="sm" onClick={() => setLimite((valor) => valor + POR_PAGINA)}>
            Mostrar más
          </Button>
        )}
      </div>
    </div>
  )
}

const EVENTOS = [
  { clave: "validaciones", label: "Validaciones y aprobaciones", descripcion: "Validar, observar, aprobar y rechazar registros o documentos." },
  { clave: "modificaciones", label: "Creación y modificación", descripcion: "Altas, ediciones, adjuntos, movimientos y exportaciones." },
  { clave: "cierres", label: "Cierres", descripcion: "Cierre de órdenes de recolección e incidencias." },
  { clave: "accesos", label: "Inicios de sesión", descripcion: "Cada ingreso de un usuario al sistema." },
]

const RETENCION = ["90", "180", "365", "730"]

function Configuracion({ configuracion }) {
  const actor = useActor()
  const [valores, setValores] = useState(configuracion)
  const guardar = useMockMutation(guardarConfiguracion, { success: "Configuración de auditoría guardada" })
  const sinCambios =
    valores.retencionDias === configuracion.retencionDias &&
    EVENTOS.every(({ clave }) => valores.eventos[clave] === configuracion.eventos[clave])

  return (
    <div className="max-w-2xl space-y-4">
      <section className="rounded-xl bg-card ring-1 ring-foreground/10">
        <header className="border-b p-4">
          <h3 className="font-heading text-lg font-semibold">Eventos auditados</h3>
          <p className="text-xs text-muted-foreground">
            Los cambios de esta configuración siempre quedan registrados en la bitácora.
          </p>
        </header>
        <ul className="divide-y">
          {EVENTOS.map((evento) => (
            <li key={evento.clave} className="flex items-center justify-between gap-4 p-4">
              <div>
                <Label htmlFor={`evento-${evento.clave}`}>{evento.label}</Label>
                <p className="text-xs text-muted-foreground">{evento.descripcion}</p>
              </div>
              <Switch
                id={`evento-${evento.clave}`}
                checked={valores.eventos[evento.clave]}
                onCheckedChange={(marcado) =>
                  setValores((previo) => ({ ...previo, eventos: { ...previo.eventos, [evento.clave]: marcado } }))
                }
              />
            </li>
          ))}
        </ul>
      </section>

      <section className="flex flex-col gap-3 rounded-xl bg-card p-4 ring-1 ring-foreground/10 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <Label htmlFor="retencion">Retención de la bitácora</Label>
          <p className="text-xs text-muted-foreground">Tiempo que se conservan los eventos antes de archivarse.</p>
        </div>
        <Select
          value={valores.retencionDias}
          onValueChange={(valor) => setValores((previo) => ({ ...previo, retencionDias: valor }))}
        >
          <SelectTrigger id="retencion" className="w-full sm:w-40">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {RETENCION.map((dias) => (
              <SelectItem key={dias} value={dias}>
                {dias} días
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </section>

      <div className="flex justify-end gap-2">
        <Button variant="outline" onClick={() => setValores(configuracion)} disabled={sinCambios}>
          Descartar
        </Button>
        <Button onClick={() => guardar.mutate({ valores, actor })} disabled={sinCambios || guardar.isPending}>
          <Save />
          Guardar configuración
        </Button>
      </div>
    </div>
  )
}

export default function AuditoriaPage() {
  const { hasPermission } = useAuth()
  const puedeGestionar = hasPermission("AUDITORIA_GESTIONAR")
  const [params, setParams] = useSearchParams()
  const pestana = puedeGestionar && params.get("tab") === "configuracion" ? "configuracion" : "bitacora"
  const { data: configuracion } = useQuery({
    queryKey: ["auditoria-configuracion"],
    queryFn: obtenerConfiguracion,
    enabled: puedeGestionar,
  })

  if (!puedeGestionar) return <Bitacora />

  return (
    <Tabs value={pestana} onValueChange={(valor) => setParams({ tab: valor }, { replace: true })} className="gap-4">
      <TabsList>
        <TabsTrigger value="bitacora">Bitácora</TabsTrigger>
        <TabsTrigger value="configuracion">Configuración</TabsTrigger>
      </TabsList>
      <TabsContent value="bitacora">
        <Bitacora />
      </TabsContent>
      <TabsContent value="configuracion">
        {configuracion && (
          <Configuracion
            key={`${configuracion.retencionDias}-${Object.values(configuracion.eventos).join("")}`}
            configuracion={configuracion}
          />
        )}
      </TabsContent>
    </Tabs>
  )
}
