import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { MessageSquareText, Plus } from "lucide-react"
import { EmptyState } from "@/components/shared/EmptyState"
import { FieldRow, FormDialog } from "@/components/shared/FormDialog"
import { SelectField, TextareaField } from "@/components/shared/fields"
import { SegmentedFilter } from "@/components/shared/SegmentedFilter"
import { StatusBadge } from "@/components/shared/StatusBadge"
import { SearchInput, Toolbar } from "@/components/shared/Toolbar"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/features/auth/AuthContext"
import { useActor } from "@/hooks/useActor"
import { useMockMutation } from "@/hooks/useMockMutation"
import { AMBITO_OBSERVACION, opciones } from "@/lib/estados"
import { TODOS, pasaFiltro } from "@/lib/filtros"
import { coincide, fechaHora, iniciales } from "@/lib/formato"
import { listarObservaciones, opcionesIncidencia, registrarObservacion } from "./incidenciasApi"

const esquema = z.object({
  ambito: z.string().min(1, "Selecciona el ámbito"),
  referencia: z.string().optional(),
  texto: z.string().trim().min(10, "Escribe la observación (mínimo 10 caracteres)").max(300, "Máximo 300 caracteres"),
})

function ObservacionDialog({ referencias, onClose }) {
  const { user } = useAuth()
  const actor = useActor()
  const rol = user.roles.map((item) => item.nombre).join(" / ")
  const form = useForm({ resolver: zodResolver(esquema), defaultValues: { ambito: "", referencia: "", texto: "" } })
  const registrar = useMockMutation(registrarObservacion, { success: "Observación registrada", onSuccess: onClose })
  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title="Registrar observación"
      description="Deja constancia de hechos operativos que no llegan a ser incidencias."
      form={form}
      pending={registrar.isPending}
      submitLabel="Registrar"
      onSubmit={(valores) => registrar.mutate({ valores, actor, rol })}
    >
      <FieldRow>
        <SelectField control={form.control} name="ambito" label="Ámbito" options={opciones(AMBITO_OBSERVACION)} />
        <SelectField
          control={form.control}
          name="referencia"
          label="Registro relacionado (opcional)"
          placeholder="Orden, contenedor o registro"
          options={referencias}
        />
      </FieldRow>
      <TextareaField control={form.control} name="texto" label="Observación" rows={4} />
    </FormDialog>
  )
}

export function ObservacionesTab() {
  const { hasPermission } = useAuth()
  const puedeRegistrar = hasPermission("OBSERVACION_REGISTRAR")
  const { data = [], isPending } = useQuery({ queryKey: ["observaciones"], queryFn: listarObservaciones })
  const { data: opcionesFormulario } = useQuery({ queryKey: ["incidencia-opciones"], queryFn: opcionesIncidencia })
  const [ambito, setAmbito] = useState(TODOS)
  const [busqueda, setBusqueda] = useState("")
  const [registrando, setRegistrando] = useState(false)

  const filtrados = data.filter(
    (item) => pasaFiltro(ambito, item.ambito) && coincide(busqueda, item.texto, item.autor, item.referencia)
  )

  return (
    <div className="space-y-4">
      <SegmentedFilter
        label="Ámbito"
        value={ambito}
        onChange={setAmbito}
        options={[
          { value: TODOS, label: "Todas", count: data.length },
          ...Object.entries(AMBITO_OBSERVACION).map(([valor, label]) => ({
            value: valor,
            label,
            count: data.filter((item) => item.ambito === valor).length,
          })),
        ]}
      />
      <Toolbar
        actions={
          puedeRegistrar && (
            <Button onClick={() => setRegistrando(true)} disabled={!opcionesFormulario}>
              <Plus />
              Registrar observación
            </Button>
          )
        }
      >
        <SearchInput value={busqueda} onChange={setBusqueda} placeholder="Texto, autor o registro" />
      </Toolbar>

      {isPending ? (
        <div className="space-y-3">
          {[0, 1, 2].map((index) => (
            <div key={index} className="h-24 animate-pulse rounded-xl bg-muted" />
          ))}
        </div>
      ) : filtrados.length === 0 ? (
        <div className="rounded-xl bg-card ring-1 ring-foreground/10">
          <EmptyState icon={MessageSquareText}>No hay observaciones que coincidan con los filtros.</EmptyState>
        </div>
      ) : (
        <ul className="divide-y rounded-xl bg-card ring-1 ring-foreground/10">
          {filtrados.map((item) => (
            <li key={item.idObservacion} className="flex gap-3 p-4">
              <span className="flex size-9 shrink-0 items-center justify-center rounded-full bg-secondary text-xs font-semibold text-secondary-foreground">
                {iniciales(item.autor)}
              </span>
              <div className="min-w-0 flex-1 space-y-1">
                <div className="flex flex-wrap items-center gap-x-2 gap-y-1 text-sm">
                  <span className="font-medium">{item.autor}</span>
                  <span className="text-xs text-muted-foreground">{item.rol}</span>
                  <span className="text-xs text-muted-foreground tabular-nums">· {fechaHora(item.fecha)}</span>
                </div>
                <p className="text-sm">{item.texto}</p>
                <div className="flex flex-wrap gap-1.5 pt-0.5">
                  <StatusBadge>{AMBITO_OBSERVACION[item.ambito]}</StatusBadge>
                  {item.referencia && <StatusBadge tone="muted">{item.referencia}</StatusBadge>}
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}

      {registrando && opcionesFormulario && (
        <ObservacionDialog referencias={opcionesFormulario.referencias} onClose={() => setRegistrando(false)} />
      )}
    </div>
  )
}
