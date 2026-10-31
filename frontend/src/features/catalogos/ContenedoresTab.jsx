import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { useForm, useWatch } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { Pencil, Plus } from "lucide-react"
import { EmptyRow } from "@/components/shared/EmptyState"
import { FieldRow, FormDialog } from "@/components/shared/FormDialog"
import { MeterBar } from "@/components/shared/MeterBar"
import { NumberField, SelectField, TextField } from "@/components/shared/fields"
import { EstadoBadge } from "@/components/shared/StatusBadge"
import { LoadingRows, TablePanel } from "@/components/shared/TablePanel"
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
import { useMockMutation } from "@/hooks/useMockMutation"
import { SITUACION_CONTENEDOR, opciones } from "@/lib/estados"
import { TODOS, pasaFiltro } from "@/lib/filtros"
import { cantidad, coincide } from "@/lib/formato"
import { guardarContenedor, listarContenedores, listarResiduos } from "./catalogosApi"

const ESTADOS_EDITABLES = [
  { value: "DISPONIBLE", label: "Operativo" },
  { value: "MANTENIMIENTO", label: "En mantenimiento" },
]

const esquema = z.object({
  codigo: z.string().trim().min(1, "Ingresa el código").max(20, "Máximo 20 caracteres"),
  tipo: z.string().trim().min(3, "Describe el tipo de contenedor"),
  idResiduo: z.string().min(1, "Selecciona el residuo permitido"),
  capacidad: z.coerce.number({ error: "Ingresa la capacidad" }).positive("La capacidad debe ser mayor a 0"),
  ubicacion: z.string().trim().min(3, "Indica la ubicación"),
  estado: z.string().min(1, "Selecciona el estado"),
})

function ContenedorDialog({ contenedor, residuos, siguienteCodigo, onClose }) {
  const actor = useActor()
  const form = useForm({
    resolver: zodResolver(esquema),
    defaultValues: {
      codigo: contenedor.codigo ?? siguienteCodigo,
      tipo: contenedor.tipo ?? "",
      idResiduo: contenedor.idResiduo ? String(contenedor.idResiduo) : "",
      capacidad: contenedor.capacidad ?? "",
      ubicacion: contenedor.ubicacion ?? "",
      estado: contenedor.estado ?? "DISPONIBLE",
    },
  })
  const idResiduo = useWatch({ control: form.control, name: "idResiduo" })
  const unidad = residuos.find((item) => String(item.idResiduo) === idResiduo)?.unidad
  const guardar = useMockMutation(guardarContenedor, {
    success: contenedor.idContenedor ? "Contenedor actualizado" : "Contenedor registrado",
    onSuccess: onClose,
  })

  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={contenedor.idContenedor ? `Editar ${contenedor.codigo}` : "Nuevo contenedor"}
      description="Cada contenedor admite un solo tipo de residuo y su capacidad se mide en la unidad de ese residuo."
      form={form}
      pending={guardar.isPending}
      onSubmit={(valores) =>
        guardar.mutate({ valores: { ...valores, idContenedor: contenedor.idContenedor }, actor })
      }
    >
      <FieldRow>
        <TextField control={form.control} name="codigo" label="Código" />
        <SelectField control={form.control} name="estado" label="Estado" options={ESTADOS_EDITABLES} />
      </FieldRow>
      <TextField control={form.control} name="tipo" label="Tipo" placeholder="Ej. Cilindro metálico 200 L" />
      <SelectField
        control={form.control}
        name="idResiduo"
        label="Residuo permitido"
        options={residuos.map((item) => ({ value: item.idResiduo, label: `${item.codigo} · ${item.nombre}` }))}
      />
      <FieldRow>
        <NumberField control={form.control} name="capacidad" label="Capacidad" suffix={unidad} />
        <TextField control={form.control} name="ubicacion" label="Ubicación" placeholder="Ej. Patio · zona A" />
      </FieldRow>
    </FormDialog>
  )
}

export function ContenedoresTab() {
  const { hasPermission } = useAuth()
  const puedeGestionar = hasPermission("CONTENEDOR_GESTIONAR")
  const { data = [], isPending } = useQuery({ queryKey: ["contenedores"], queryFn: listarContenedores })
  const { data: residuos = [] } = useQuery({ queryKey: ["residuos"], queryFn: listarResiduos })
  const [busqueda, setBusqueda] = useState("")
  const [situacion, setSituacion] = useState(TODOS)
  const [residuo, setResiduo] = useState(TODOS)
  const [editando, setEditando] = useState(null)

  const filtrados = data.filter(
    (item) =>
      coincide(busqueda, item.codigo, item.tipo, item.ubicacion, item.residuo?.nombre) &&
      pasaFiltro(situacion, item.situacion) &&
      pasaFiltro(residuo, item.idResiduo)
  )

  return (
    <div className="space-y-4">
      <Toolbar
        actions={
          puedeGestionar && (
            <Button onClick={() => setEditando({})}>
              <Plus />
              Nuevo contenedor
            </Button>
          )
        }
      >
        <SearchInput value={busqueda} onChange={setBusqueda} placeholder="Código, tipo o ubicación" />
        <FilterSelect label="Situación" value={situacion} onChange={setSituacion} options={opciones(SITUACION_CONTENEDOR)} todos="Todas" />
        <FilterSelect
          label="Residuo"
          value={residuo}
          onChange={setResiduo}
          options={residuos.map((item) => ({ value: item.idResiduo, label: item.nombre }))}
        />
      </Toolbar>

      <TablePanel>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Código</TableHead>
              <TableHead>Tipo y ubicación</TableHead>
              <TableHead>Residuo permitido</TableHead>
              <TableHead className="w-56">Ocupación</TableHead>
              <TableHead>Situación</TableHead>
              {puedeGestionar && <TableHead className="text-right">Acciones</TableHead>}
            </TableRow>
          </TableHeader>
          <TableBody>
            {isPending && <LoadingRows colSpan={6} />}
            {!isPending && filtrados.length === 0 && (
              <EmptyRow colSpan={6}>Ningún contenedor coincide con los filtros.</EmptyRow>
            )}
            {filtrados.map((contenedor) => (
              <TableRow key={contenedor.idContenedor}>
                <TableCell className="font-medium tabular-nums">{contenedor.codigo}</TableCell>
                <TableCell>
                  <div className="font-medium">{contenedor.tipo}</div>
                  <div className="text-xs text-muted-foreground">{contenedor.ubicacion}</div>
                </TableCell>
                <TableCell>{contenedor.residuo?.nombre}</TableCell>
                <TableCell>
                  <div className="flex items-center gap-2">
                    <MeterBar value={contenedor.porcentaje} label={`Ocupación de ${contenedor.codigo}`} />
                    <span className="w-10 text-right text-xs text-muted-foreground tabular-nums">
                      {contenedor.porcentaje}%
                    </span>
                  </div>
                  <div className="mt-1 text-xs text-muted-foreground tabular-nums">
                    {cantidad(contenedor.ocupacion, contenedor.unidad)} de {cantidad(contenedor.capacidad, contenedor.unidad)}
                  </div>
                </TableCell>
                <TableCell>
                  <EstadoBadge mapa={SITUACION_CONTENEDOR} valor={contenedor.situacion} />
                </TableCell>
                {puedeGestionar && (
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" onClick={() => setEditando(contenedor)}>
                      <Pencil />
                      Editar
                    </Button>
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TablePanel>

      {editando && (
        <ContenedorDialog
          key={editando.idContenedor ?? "nuevo"}
          contenedor={editando}
          residuos={residuos}
          siguienteCodigo={`CT-${101 + data.length}`}
          onClose={() => setEditando(null)}
        />
      )}
    </div>
  )
}
