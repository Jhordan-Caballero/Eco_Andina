import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { Pencil, Plus, Power } from "lucide-react"
import { ConfirmAction } from "@/components/shared/ConfirmAction"
import { EmptyRow } from "@/components/shared/EmptyState"
import { FieldRow, FormDialog } from "@/components/shared/FormDialog"
import { SelectField, TextField } from "@/components/shared/fields"
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
import { ESTADO_AREA, opciones } from "@/lib/estados"
import { TODOS, pasaFiltro } from "@/lib/filtros"
import { coincide } from "@/lib/formato"
import { cambiarEstadoArea, guardarArea, listarAreas, listarResponsables } from "./catalogosApi"

const esquema = z.object({
  codigo: z.string().trim().min(1, "Ingresa el código").max(20, "Máximo 20 caracteres"),
  nombre: z.string().trim().min(3, "Ingresa el nombre del área"),
  proceso: z.string().trim().min(3, "Describe el proceso del área"),
  responsable: z.string().min(1, "Selecciona un responsable"),
  estado: z.string().min(1, "Selecciona el estado"),
})

function AreaDialog({ area, onClose }) {
  const actor = useActor()
  const { data: responsables = [] } = useQuery({ queryKey: ["responsables"], queryFn: listarResponsables })
  const form = useForm({
    resolver: zodResolver(esquema),
    defaultValues: {
      codigo: area.codigo ?? "AR-",
      nombre: area.nombre ?? "",
      proceso: area.proceso ?? "",
      responsable: area.responsable ?? "",
      estado: area.estado ?? "ACTIVA",
    },
  })
  const guardar = useMockMutation(guardarArea, {
    success: area.idArea ? "Área actualizada" : "Área registrada",
    onSuccess: onClose,
  })

  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={area.idArea ? `Editar ${area.nombre}` : "Nueva área generadora"}
      description="Las áreas activas aparecen al registrar la generación de residuos."
      form={form}
      pending={guardar.isPending}
      onSubmit={(valores) => guardar.mutate({ valores: { ...valores, idArea: area.idArea }, actor })}
    >
      <FieldRow>
        <TextField control={form.control} name="codigo" label="Código" />
        <SelectField control={form.control} name="estado" label="Estado" options={opciones(ESTADO_AREA)} />
      </FieldRow>
      <TextField control={form.control} name="nombre" label="Nombre del área" placeholder="Ej. Pintura" />
      <TextField control={form.control} name="proceso" label="Proceso" placeholder="Ej. Pintado de piezas" />
      <SelectField
        control={form.control}
        name="responsable"
        label="Responsable"
        options={responsables.map((nombre) => ({ value: nombre, label: nombre }))}
      />
    </FormDialog>
  )
}

export function AreasTab() {
  const { hasPermission } = useAuth()
  const actor = useActor()
  const puedeGestionar = hasPermission("AREA_GESTIONAR")
  const { data = [], isPending } = useQuery({ queryKey: ["areas"], queryFn: listarAreas })
  const [busqueda, setBusqueda] = useState("")
  const [estado, setEstado] = useState(TODOS)
  const [editando, setEditando] = useState(null)
  const cambiarEstado = useMockMutation(cambiarEstadoArea, {
    success: (area) => `Área ${area.estado === "ACTIVA" ? "activada" : "desactivada"}`,
  })

  const filtradas = data.filter(
    (item) =>
      coincide(busqueda, item.codigo, item.nombre, item.proceso, item.responsable) &&
      pasaFiltro(estado, item.estado)
  )

  return (
    <div className="space-y-4">
      <Toolbar
        actions={
          puedeGestionar && (
            <Button onClick={() => setEditando({})}>
              <Plus />
              Nueva área
            </Button>
          )
        }
      >
        <SearchInput value={busqueda} onChange={setBusqueda} placeholder="Código, área o responsable" />
        <FilterSelect label="Estado" value={estado} onChange={setEstado} options={opciones(ESTADO_AREA)} />
      </Toolbar>

      <TablePanel>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Código</TableHead>
              <TableHead>Área</TableHead>
              <TableHead>Responsable</TableHead>
              <TableHead className="text-right">Registros (30 días)</TableHead>
              <TableHead>Estado</TableHead>
              {puedeGestionar && <TableHead className="text-right">Acciones</TableHead>}
            </TableRow>
          </TableHeader>
          <TableBody>
            {isPending && <LoadingRows colSpan={6} />}
            {!isPending && filtradas.length === 0 && (
              <EmptyRow colSpan={6}>Ninguna área coincide con los filtros.</EmptyRow>
            )}
            {filtradas.map((area) => (
              <TableRow key={area.idArea}>
                <TableCell className="font-medium tabular-nums">{area.codigo}</TableCell>
                <TableCell>
                  <div className="font-medium">{area.nombre}</div>
                  <div className="text-xs text-muted-foreground">{area.proceso}</div>
                </TableCell>
                <TableCell>{area.responsable}</TableCell>
                <TableCell className="text-right tabular-nums">{area.registros30}</TableCell>
                <TableCell>
                  <EstadoBadge mapa={ESTADO_AREA} valor={area.estado} />
                </TableCell>
                {puedeGestionar && (
                  <TableCell>
                    <div className="flex justify-end gap-1">
                      <Button variant="ghost" size="sm" onClick={() => setEditando(area)}>
                        <Pencil />
                        Editar
                      </Button>
                      <ConfirmAction
                        trigger={
                          <Button variant="ghost" size="sm">
                            <Power />
                            {area.estado === "ACTIVA" ? "Desactivar" : "Activar"}
                          </Button>
                        }
                        title={`${area.estado === "ACTIVA" ? "Desactivar" : "Activar"} ${area.nombre}`}
                        description={
                          area.estado === "ACTIVA"
                            ? "El área dejará de aparecer al registrar nuevas generaciones. Sus registros históricos se conservan."
                            : "El área volverá a estar disponible para registrar generaciones."
                        }
                        confirmLabel={area.estado === "ACTIVA" ? "Desactivar" : "Activar"}
                        destructive={area.estado === "ACTIVA"}
                        onConfirm={() => cambiarEstado.mutate({ idArea: area.idArea, actor })}
                      />
                    </div>
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TablePanel>

      {editando && (
        <AreaDialog key={editando.idArea ?? "nueva"} area={editando} onClose={() => setEditando(null)} />
      )}
    </div>
  )
}
