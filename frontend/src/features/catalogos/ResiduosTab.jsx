import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { Pencil, Plus } from "lucide-react"
import { EmptyRow } from "@/components/shared/EmptyState"
import { FieldRow, FormDialog } from "@/components/shared/FormDialog"
import { NumberField, SelectField, TextField, TextareaField } from "@/components/shared/fields"
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
import { CATEGORIA_RESIDUO, PELIGROSIDAD, opciones } from "@/lib/estados"
import { TODOS, pasaFiltro } from "@/lib/filtros"
import { coincide } from "@/lib/formato"
import { guardarResiduo, listarResiduos } from "./catalogosApi"

const UNIDADES = [
  { value: "kg", label: "Kilogramos (kg)" },
  { value: "L", label: "Litros (L)" },
  { value: "unid", label: "Unidades (unid)" },
  { value: "m3", label: "Metros cúbicos (m3)" },
]

const esquema = z.object({
  codigo: z.string().trim().min(1, "Ingresa el código").max(20, "Máximo 20 caracteres"),
  nombre: z.string().trim().min(3, "Ingresa el nombre del residuo"),
  categoria: z.string().min(1, "Selecciona la categoría"),
  peligrosidad: z.string().min(1, "Selecciona la peligrosidad"),
  unidad: z.string().min(1, "Selecciona la unidad"),
  plazoMaximoDias: z.coerce
    .number({ error: "Ingresa un número de días" })
    .int("Debe ser un número entero")
    .min(1, "Debe ser al menos 1 día")
    .max(365, "Máximo 365 días"),
  tratamiento: z.string().trim().min(3, "Indica el tratamiento o destino"),
  requisitos: z.string().trim().min(3, "Indica los requisitos de almacenamiento"),
})

function ResiduoDialog({ residuo, siguienteCodigo, onClose }) {
  const actor = useActor()
  const form = useForm({
    resolver: zodResolver(esquema),
    defaultValues: {
      codigo: residuo.codigo ?? siguienteCodigo,
      nombre: residuo.nombre ?? "",
      categoria: residuo.categoria ?? "",
      peligrosidad: residuo.peligrosidad ?? "",
      unidad: residuo.unidad ?? "kg",
      plazoMaximoDias: residuo.plazoMaximoDias ?? 30,
      tratamiento: residuo.tratamiento ?? "",
      requisitos: residuo.requisitos ?? "",
    },
  })
  const guardar = useMockMutation(guardarResiduo, {
    success: residuo.idResiduo ? "Residuo actualizado" : "Residuo registrado",
    onSuccess: onClose,
  })

  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={residuo.idResiduo ? `Editar ${residuo.codigo}` : "Nuevo residuo"}
      description="Datos del catálogo usados al registrar la generación y programar recolecciones."
      form={form}
      pending={guardar.isPending}
      onSubmit={(valores) => guardar.mutate({ valores: { ...valores, idResiduo: residuo.idResiduo }, actor })}
    >
      <FieldRow>
        <TextField control={form.control} name="codigo" label="Código" placeholder="RES-012" />
        <SelectField control={form.control} name="unidad" label="Unidad de medida" options={UNIDADES} />
      </FieldRow>
      <TextField control={form.control} name="nombre" label="Nombre" placeholder="Ej. Baterías usadas" />
      <FieldRow>
        <SelectField control={form.control} name="categoria" label="Categoría" options={opciones(CATEGORIA_RESIDUO)} />
        <SelectField control={form.control} name="peligrosidad" label="Peligrosidad" options={opciones(PELIGROSIDAD)} />
      </FieldRow>
      <NumberField
        control={form.control}
        name="plazoMaximoDias"
        label="Plazo máximo de almacenamiento"
        suffix="días"
        description="Se usa para alertar cuando un residuo se acerca a su fecha máxima en planta."
      />
      <TextField control={form.control} name="tratamiento" label="Tratamiento o destino" placeholder="Ej. Reciclaje" />
      <TextareaField control={form.control} name="requisitos" label="Requisitos de almacenamiento" />
    </FormDialog>
  )
}

export function ResiduosTab() {
  const { hasPermission } = useAuth()
  const puedeGestionar = hasPermission("RESIDUO_GESTIONAR")
  const { data = [], isPending } = useQuery({ queryKey: ["residuos"], queryFn: listarResiduos })
  const [busqueda, setBusqueda] = useState("")
  const [categoria, setCategoria] = useState(TODOS)
  const [peligrosidad, setPeligrosidad] = useState(TODOS)
  const [editando, setEditando] = useState(null)

  const filtrados = data.filter(
    (item) =>
      coincide(busqueda, item.codigo, item.nombre, item.tratamiento) &&
      pasaFiltro(categoria, item.categoria) &&
      pasaFiltro(peligrosidad, item.peligrosidad)
  )
  const siguienteCodigo = `RES-${String(data.length + 1).padStart(3, "0")}`

  return (
    <div className="space-y-4">
      <Toolbar
        actions={
          puedeGestionar && (
            <Button onClick={() => setEditando({})}>
              <Plus />
              Nuevo residuo
            </Button>
          )
        }
      >
        <SearchInput value={busqueda} onChange={setBusqueda} placeholder="Código, nombre o tratamiento" />
        <FilterSelect label="Categoría" value={categoria} onChange={setCategoria} options={opciones(CATEGORIA_RESIDUO)} todos="Todas" />
        <FilterSelect label="Peligrosidad" value={peligrosidad} onChange={setPeligrosidad} options={opciones(PELIGROSIDAD)} todos="Todas" />
      </Toolbar>

      <TablePanel>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Código</TableHead>
              <TableHead>Residuo</TableHead>
              <TableHead>Categoría</TableHead>
              <TableHead>Peligrosidad</TableHead>
              <TableHead>Unidad</TableHead>
              <TableHead>Plazo máx.</TableHead>
              <TableHead>Tratamiento</TableHead>
              {puedeGestionar && <TableHead className="text-right">Acciones</TableHead>}
            </TableRow>
          </TableHeader>
          <TableBody>
            {isPending && <LoadingRows colSpan={8} />}
            {!isPending && filtrados.length === 0 && (
              <EmptyRow colSpan={8}>Ningún residuo coincide con los filtros.</EmptyRow>
            )}
            {filtrados.map((residuo) => (
              <TableRow key={residuo.idResiduo}>
                <TableCell className="font-medium tabular-nums">{residuo.codigo}</TableCell>
                <TableCell className="max-w-72 whitespace-normal">
                  <div className="font-medium">{residuo.nombre}</div>
                  <div className="text-xs text-muted-foreground">{residuo.requisitos}</div>
                </TableCell>
                <TableCell>
                  <EstadoBadge mapa={CATEGORIA_RESIDUO} valor={residuo.categoria} />
                </TableCell>
                <TableCell>{PELIGROSIDAD[residuo.peligrosidad]}</TableCell>
                <TableCell>{residuo.unidad}</TableCell>
                <TableCell className="tabular-nums">{residuo.plazoMaximoDias} días</TableCell>
                <TableCell className="max-w-56 whitespace-normal">{residuo.tratamiento}</TableCell>
                {puedeGestionar && (
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" onClick={() => setEditando(residuo)}>
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
        <ResiduoDialog
          key={editando.idResiduo ?? "nuevo"}
          residuo={editando}
          siguienteCodigo={siguienteCodigo}
          onClose={() => setEditando(null)}
        />
      )}
    </div>
  )
}
