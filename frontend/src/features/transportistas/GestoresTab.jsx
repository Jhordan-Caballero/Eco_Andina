import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { Pencil, Plus } from "lucide-react"
import { EmptyRow } from "@/components/shared/EmptyState"
import { FieldRow, FormDialog } from "@/components/shared/FormDialog"
import { TextField, TextareaField } from "@/components/shared/fields"
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
import { ESTADO_VIGENCIA, opciones } from "@/lib/estados"
import { TODOS, pasaFiltro } from "@/lib/filtros"
import { coincide } from "@/lib/formato"
import { guardarGestor, listarGestores } from "./transportistasApi"
import { Vigencia } from "./Vigencia"

const esquema = z.object({
  empresa: z.string().trim().min(3, "Ingresa la razón social"),
  autorizacion: z.string().trim().min(6, "Ingresa el número de registro EO-RS"),
  procesoDestino: z.string().trim().min(5, "Describe el proceso de tratamiento o disposición"),
  vigencia: z.string().min(1, "Indica la fecha de vencimiento"),
})

function GestorDialog({ gestor, onClose }) {
  const actor = useActor()
  const form = useForm({
    resolver: zodResolver(esquema),
    defaultValues: {
      empresa: gestor.empresa ?? "",
      autorizacion: gestor.autorizacion ?? "",
      procesoDestino: gestor.procesoDestino ?? "",
      vigencia: gestor.vigencia ?? "",
    },
  })
  const guardar = useMockMutation(guardarGestor, {
    success: gestor.idGestor ? "Gestor actualizado" : "Gestor registrado",
    onSuccess: onClose,
  })
  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={gestor.idGestor ? `Editar ${gestor.empresa}` : "Nuevo gestor autorizado"}
      description="Empresa operadora que recibe los residuos para su valorización, tratamiento o disposición final."
      form={form}
      pending={guardar.isPending}
      onSubmit={(valores) => guardar.mutate({ valores: { ...valores, idGestor: gestor.idGestor }, actor })}
    >
      <TextField control={form.control} name="empresa" label="Razón social" />
      <FieldRow>
        <TextField control={form.control} name="autorizacion" label="Registro EO-RS" placeholder="EO-RS-00000-2026-MINAM" />
        <TextField control={form.control} name="vigencia" label="Vigencia hasta" type="date" />
      </FieldRow>
      <TextareaField control={form.control} name="procesoDestino" label="Proceso de destino" />
    </FormDialog>
  )
}

export function GestoresTab() {
  const { hasPermission } = useAuth()
  const puedeGestionar = hasPermission("GESTOR_GESTIONAR")
  const { data = [], isPending } = useQuery({ queryKey: ["gestores"], queryFn: listarGestores })
  const [busqueda, setBusqueda] = useState("")
  const [vigencia, setVigencia] = useState(TODOS)
  const [editando, setEditando] = useState(null)
  const columnas = puedeGestionar ? 5 : 4

  const filtrados = data.filter(
    (item) =>
      pasaFiltro(vigencia, item.estadoVigencia) &&
      coincide(busqueda, item.empresa, item.autorizacion, item.procesoDestino)
  )

  return (
    <div className="space-y-4">
      <Toolbar
        actions={
          puedeGestionar && (
            <Button onClick={() => setEditando({})}>
              <Plus />
              Nuevo gestor
            </Button>
          )
        }
      >
        <SearchInput value={busqueda} onChange={setBusqueda} placeholder="Empresa, EO-RS o proceso" />
        <FilterSelect label="Vigencia" value={vigencia} onChange={setVigencia} options={opciones(ESTADO_VIGENCIA)} todos="Todas" />
      </Toolbar>

      <TablePanel>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Empresa</TableHead>
              <TableHead>Proceso de destino</TableHead>
              <TableHead>Autorización</TableHead>
              <TableHead className="text-right">Órdenes</TableHead>
              {puedeGestionar && <TableHead className="text-right">Acciones</TableHead>}
            </TableRow>
          </TableHeader>
          <TableBody>
            {isPending && <LoadingRows colSpan={columnas} />}
            {!isPending && filtrados.length === 0 && (
              <EmptyRow colSpan={columnas}>Ningún gestor coincide con los filtros.</EmptyRow>
            )}
            {filtrados.map((gestor) => (
              <TableRow key={gestor.idGestor}>
                <TableCell>
                  <div className="font-medium">{gestor.empresa}</div>
                  <div className="text-xs text-muted-foreground tabular-nums">{gestor.autorizacion}</div>
                </TableCell>
                <TableCell className="max-w-72 whitespace-normal">{gestor.procesoDestino}</TableCell>
                <TableCell>
                  <Vigencia valor={gestor.vigencia} estado={gestor.estadoVigencia} />
                </TableCell>
                <TableCell className="text-right tabular-nums">
                  {gestor.ordenesActivas + gestor.ordenesCerradas}
                  <div className="text-xs text-muted-foreground">{gestor.ordenesActivas} activas</div>
                </TableCell>
                {puedeGestionar && (
                  <TableCell className="text-right">
                    <Button variant="ghost" size="sm" onClick={() => setEditando(gestor)}>
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

      {editando && <GestorDialog key={editando.idGestor ?? "nuevo"} gestor={editando} onClose={() => setEditando(null)} />}
    </div>
  )
}
