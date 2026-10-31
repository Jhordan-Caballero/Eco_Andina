import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { Check, FileText, Paperclip, X } from "lucide-react"
import { ConfirmAction } from "@/components/shared/ConfirmAction"
import { EmptyRow } from "@/components/shared/EmptyState"
import { FormDialog } from "@/components/shared/FormDialog"
import { TextareaField } from "@/components/shared/fields"
import { SegmentedFilter } from "@/components/shared/SegmentedFilter"
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
import { ESTADO_DOCUMENTO, TIPO_DOCUMENTO, opciones } from "@/lib/estados"
import { TODOS, pasaFiltro } from "@/lib/filtros"
import { coincide, fechaHora } from "@/lib/formato"
import { AdjuntarDocumentoDialog } from "./AdjuntarDocumentoDialog"
import {
  PERMISOS_REVISION,
  listarDocumentos,
  rechazarDocumento,
  referenciasDisponibles,
  validarDocumento,
} from "./documentosApi"

const esquemaRechazo = z.object({
  motivo: z.string().trim().min(10, "Indica qué debe corregirse (mínimo 10 caracteres)"),
})

function RechazarDialog({ documento, onClose }) {
  const actor = useActor()
  const form = useForm({ resolver: zodResolver(esquemaRechazo), defaultValues: { motivo: "" } })
  const rechazar = useMockMutation(rechazarDocumento, {
    success: `${documento.nombreArchivo} rechazado`,
    onSuccess: onClose,
  })
  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title="Rechazar documento"
      description={`${documento.nombreArchivo} · ${documento.referencia}`}
      form={form}
      pending={rechazar.isPending}
      submitLabel="Rechazar"
      onSubmit={({ motivo }) => rechazar.mutate({ idDocumento: documento.idDocumento, motivo, actor })}
    >
      <TextareaField control={form.control} name="motivo" label="Motivo del rechazo" />
    </FormDialog>
  )
}

function AccionesRevision({ documento, onRechazar }) {
  const actor = useActor()
  const validar = useMockMutation(validarDocumento, { success: `${documento.nombreArchivo} validado` })
  return (
    <div className="flex justify-end gap-1">
      <Button variant="ghost" size="sm" onClick={onRechazar}>
        <X />
        Rechazar
      </Button>
      <ConfirmAction
        trigger={
          <Button variant="outline" size="sm" disabled={validar.isPending}>
            <Check />
            Validar
          </Button>
        }
        title="Validar documento"
        description={`Confirmas que ${documento.nombreArchivo} es legible y corresponde a ${documento.referencia}.`}
        confirmLabel="Validar"
        onConfirm={() => validar.mutate({ idDocumento: documento.idDocumento, actor })}
      />
    </div>
  )
}

export default function DocumentosPage() {
  const { hasPermission, hasAnyPermission } = useAuth()
  const puedeAdjuntar = hasPermission("DOCUMENTO_GESTIONAR")
  const puedeRevisar = hasAnyPermission(PERMISOS_REVISION) && puedeAdjuntar
  const { data = [], isPending } = useQuery({ queryKey: ["documentos"], queryFn: listarDocumentos })
  const { data: referencias } = useQuery({
    queryKey: ["documento-referencias"],
    queryFn: referenciasDisponibles,
    enabled: puedeAdjuntar,
  })
  const [estado, setEstado] = useState(TODOS)
  const [tipo, setTipo] = useState(TODOS)
  const [busqueda, setBusqueda] = useState("")
  const [adjuntando, setAdjuntando] = useState(false)
  const [rechazando, setRechazando] = useState(null)
  const columnas = puedeRevisar ? 6 : 5

  const contar = (valor) => data.filter((item) => item.estado === valor).length
  const filtrados = data.filter(
    (item) =>
      pasaFiltro(estado, item.estado) &&
      pasaFiltro(tipo, item.tipo) &&
      coincide(busqueda, item.nombreArchivo, item.referencia, item.descripcion, item.subidoPor)
  )

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
        <SegmentedFilter
          label="Estado del documento"
          value={estado}
          onChange={setEstado}
          options={[
            { value: TODOS, label: "Todos", count: data.length },
            ...Object.entries(ESTADO_DOCUMENTO).map(([valor, item]) => ({
              value: valor,
              label: valor === "PENDIENTE" ? "Por revisar" : `${item.label}s`,
              count: contar(valor),
            })),
          ]}
        />
        {puedeAdjuntar && (
          <Button onClick={() => setAdjuntando(true)} disabled={!referencias}>
            <Paperclip />
            Adjuntar documento
          </Button>
        )}
      </div>
      <Toolbar>
        <SearchInput value={busqueda} onChange={setBusqueda} placeholder="Archivo, registro o usuario" />
        <FilterSelect label="Tipo" value={tipo} onChange={setTipo} options={opciones(TIPO_DOCUMENTO)} />
      </Toolbar>

      <TablePanel>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Documento</TableHead>
              <TableHead>Tipo</TableHead>
              <TableHead>Registro</TableHead>
              <TableHead>Subido por</TableHead>
              <TableHead>Estado</TableHead>
              {puedeRevisar && <TableHead className="text-right">Revisión</TableHead>}
            </TableRow>
          </TableHeader>
          <TableBody>
            {isPending && <LoadingRows colSpan={columnas} />}
            {!isPending && filtrados.length === 0 && (
              <EmptyRow colSpan={columnas}>Ningún documento coincide con los filtros.</EmptyRow>
            )}
            {filtrados.map((documento) => (
              <TableRow key={documento.idDocumento}>
                <TableCell>
                  <div className="flex items-start gap-2">
                    <FileText className="mt-0.5 size-4 shrink-0 text-muted-foreground" />
                    <div className="min-w-0">
                      <div className="max-w-64 truncate font-medium">{documento.nombreArchivo}</div>
                      <div className="max-w-64 truncate text-xs text-muted-foreground">{documento.descripcion}</div>
                    </div>
                  </div>
                </TableCell>
                <TableCell className="max-w-48 truncate">{TIPO_DOCUMENTO[documento.tipo]}</TableCell>
                <TableCell className="tabular-nums">{documento.referencia}</TableCell>
                <TableCell>
                  <div>{documento.subidoPor}</div>
                  <div className="text-xs text-muted-foreground tabular-nums">{fechaHora(documento.fecha)}</div>
                </TableCell>
                <TableCell>
                  <EstadoBadge mapa={ESTADO_DOCUMENTO} valor={documento.estado} />
                  {documento.motivoRechazo && (
                    <div className="mt-1 max-w-56 text-xs whitespace-normal text-destructive">{documento.motivoRechazo}</div>
                  )}
                </TableCell>
                {puedeRevisar && (
                  <TableCell className="text-right">
                    {documento.estado === "PENDIENTE" ? (
                      <AccionesRevision documento={documento} onRechazar={() => setRechazando(documento)} />
                    ) : (
                      <span className="text-xs text-muted-foreground">Revisado</span>
                    )}
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TablePanel>

      {adjuntando && referencias && (
        <AdjuntarDocumentoDialog referencias={referencias} onClose={() => setAdjuntando(false)} />
      )}
      {rechazando && <RechazarDialog documento={rechazando} onClose={() => setRechazando(null)} />}
    </div>
  )
}
