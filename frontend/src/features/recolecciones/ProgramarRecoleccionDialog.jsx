import { useMemo } from "react"
import { useForm, useWatch } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { AlertTriangle } from "lucide-react"
import { FieldRow, FormDialog } from "@/components/shared/FormDialog"
import { CheckboxListField, SelectField, TextField, TextareaField } from "@/components/shared/fields"
import { useActor } from "@/hooks/useActor"
import { useMockMutation } from "@/hooks/useMockMutation"
import { cantidad, fecha, hoyLocal } from "@/lib/formato"
import { programarRecoleccion } from "./recoleccionesApi"

const crearEsquema = ({ generaciones, transportistas }) =>
  z
    .object({
      idsGeneracion: z.array(z.string()).min(1, "Selecciona al menos un registro validado"),
      fechaProgramada: z
        .string()
        .min(1, "Indica la fecha de recolección")
        .refine((valor) => valor >= hoyLocal(), "La fecha no puede ser anterior a hoy"),
      idTransportista: z.string().min(1, "Selecciona el transportista"),
      idVehiculo: z.string().min(1, "Selecciona el vehículo"),
      idGestor: z.string().min(1, "Selecciona el gestor autorizado"),
      observaciones: z.string().max(255, "Máximo 255 caracteres").optional(),
    })
    .superRefine((valores, ctx) => {
      const seleccion = generaciones.filter((item) => valores.idsGeneracion.includes(String(item.idGeneracion)))
      const unidades = new Set(seleccion.map((item) => item.unidad))
      if (unidades.size > 1) {
        ctx.addIssue({
          code: "custom",
          path: ["idsGeneracion"],
          message: "Una orden solo puede agrupar residuos con la misma unidad de medida",
        })
      }
      const vehiculo = transportistas
        .flatMap((item) => item.vehiculos)
        .find((item) => String(item.idVehiculo) === valores.idVehiculo)
      const total = seleccion.reduce((suma, item) => suma + item.cantidad, 0)
      if (vehiculo && unidades.size === 1 && !unidades.has("unid") && total > vehiculo.capacidad) {
        ctx.addIssue({
          code: "custom",
          path: ["idVehiculo"],
          message: `La carga supera la capacidad del vehículo (${cantidad(vehiculo.capacidad, "kg")})`,
        })
      }
    })

const etiquetaVigencia = (item) => {
  if (item.estadoVigencia === "VENCIDA") return " · autorización vencida"
  if (item.estadoVigencia === "POR_VENCER") return ` · vence el ${fecha(item.vigencia)}`
  return ""
}

export function ProgramarRecoleccionDialog({ opcionesProgramacion, onClose }) {
  const actor = useActor()
  const { generaciones, transportistas, gestores } = opcionesProgramacion
  const esquema = useMemo(() => crearEsquema(opcionesProgramacion), [opcionesProgramacion])
  const form = useForm({
    resolver: zodResolver(esquema),
    defaultValues: {
      idsGeneracion: [],
      fechaProgramada: "",
      idTransportista: "",
      idVehiculo: "",
      idGestor: "",
      observaciones: "",
    },
  })
  const [idsGeneracion, idTransportista, idGestor] = useWatch({
    control: form.control,
    name: ["idsGeneracion", "idTransportista", "idGestor"],
  })
  const seleccion = generaciones.filter((item) => idsGeneracion.includes(String(item.idGeneracion)))
  const unidad = seleccion[0]?.unidad
  const total = seleccion.reduce((suma, item) => suma + item.cantidad, 0)
  const peligroso = seleccion.some((item) => ["PELIGROSO", "ESPECIAL"].includes(item.residuo?.categoria))
  const transportista = transportistas.find((item) => String(item.idTransportista) === idTransportista)
  const gestor = gestores.find((item) => String(item.idGestor) === idGestor)
  const programar = useMockMutation(programarRecoleccion, {
    success: (orden) => `Orden ${orden.numeroOrden} creada; queda pendiente de aprobación`,
    onSuccess: onClose,
  })

  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title="Programar recolección"
      description="Agrupa registros validados, asigna un transportista y un gestor con autorización vigente."
      form={form}
      pending={programar.isPending}
      submitLabel="Crear orden"
      className="sm:max-w-2xl"
      onSubmit={(valores) => programar.mutate({ valores, actor })}
    >
      {generaciones.length === 0 ? (
        <p className="rounded-lg bg-muted/60 px-3 py-2 text-sm text-muted-foreground">
          No hay registros validados pendientes de recolección.
        </p>
      ) : (
        <CheckboxListField
          control={form.control}
          name="idsGeneracion"
          label="Registros a recolectar"
          options={generaciones.map((item) => ({
            value: String(item.idGeneracion),
            label: `${item.codigo} · ${item.residuo?.nombre} · ${cantidad(item.cantidad, item.unidad)}`,
            description: `${item.area?.nombre} · ${item.contenedor?.codigo} · ${fecha(item.fecha)}`,
            disabled: Boolean(unidad) && item.unidad !== unidad,
          }))}
        />
      )}
      {seleccion.length > 0 && (
        <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm">
          <span>
            Total: <span className="font-medium tabular-nums">{cantidad(total, unidad)}</span>
          </span>
          {peligroso && (
            <span className="flex items-center gap-1.5 text-warning">
              <AlertTriangle className="size-4" />
              Incluye residuos peligrosos: la salida exigirá manifiesto
            </span>
          )}
        </div>
      )}
      <TextField
        control={form.control}
        name="fechaProgramada"
        label="Fecha de recolección"
        type="date"
        min={hoyLocal()}
        className="sm:max-w-56"
      />
      <FieldRow>
        <SelectField
          control={form.control}
          name="idTransportista"
          label="Transportista"
          onValueChange={() => form.setValue("idVehiculo", "")}
          options={transportistas.map((item) => ({
            value: item.idTransportista,
            label: `${item.empresa}${etiquetaVigencia(item)}`,
            disabled: item.estadoVigencia === "VENCIDA",
          }))}
          description={transportista ? `Autorización ${transportista.autorizacion}` : undefined}
        />
        <SelectField
          control={form.control}
          name="idVehiculo"
          label="Vehículo"
          disabled={!transportista}
          placeholder={transportista ? "Selecciona el vehículo" : "Primero elige el transportista"}
          options={(transportista?.vehiculos ?? []).map((item) => ({
            value: item.idVehiculo,
            label: `${item.placa} · ${item.tipo} · ${cantidad(item.capacidad, "kg")}${item.estado === "OPERATIVO" ? "" : " · no operativo"}`,
            disabled: item.estado !== "OPERATIVO",
          }))}
        />
      </FieldRow>
      <SelectField
        control={form.control}
        name="idGestor"
        label="Gestor autorizado (destino)"
        options={gestores.map((item) => ({
          value: item.idGestor,
          label: `${item.empresa}${etiquetaVigencia(item)}`,
          disabled: item.estadoVigencia === "VENCIDA",
        }))}
        description={gestor?.procesoDestino}
      />
      <TextareaField control={form.control} name="observaciones" label="Indicaciones (opcional)" />
    </FormDialog>
  )
}
