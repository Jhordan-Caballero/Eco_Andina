import { useMemo } from "react"
import { useForm, useWatch } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { FieldRow, FormDialog } from "@/components/shared/FormDialog"
import { NumberField, SelectField, TextareaField } from "@/components/shared/fields"
import { useActor } from "@/hooks/useActor"
import { useMockMutation } from "@/hooks/useMockMutation"
import { cantidad } from "@/lib/formato"
import { registrarMovimiento } from "./almacenamientoApi"

const TIPOS = [
  { value: "TRASLADO", label: "Traslado interno entre contenedores" },
  { value: "PESAJE", label: "Pesaje de verificación" },
]

const crearEsquema = (contenedores) =>
  z
    .object({
      tipo: z.enum(["TRASLADO", "PESAJE"]),
      idOrigen: z.string().min(1, "Selecciona el contenedor"),
      idDestino: z.string().optional(),
      cantidad: z.coerce.number({ error: "Ingresa la cantidad" }).min(0, "No puede ser negativa"),
      motivo: z.string().trim().min(5, "Indica el motivo del movimiento"),
    })
    .superRefine((valores, ctx) => {
      const buscar = (id) => contenedores.find((item) => String(item.idContenedor) === id)
      const origen = buscar(valores.idOrigen)
      if (!origen) return
      if (valores.tipo === "PESAJE") {
        if (valores.cantidad > origen.capacidad) {
          ctx.addIssue({
            code: "custom",
            path: ["cantidad"],
            message: `Supera la capacidad de ${origen.codigo} (${cantidad(origen.capacidad, origen.unidad)})`,
          })
        }
        return
      }
      const destino = buscar(valores.idDestino)
      if (!destino) {
        ctx.addIssue({ code: "custom", path: ["idDestino"], message: "Selecciona el contenedor de destino" })
        return
      }
      if (valores.cantidad <= 0) {
        ctx.addIssue({ code: "custom", path: ["cantidad"], message: "La cantidad debe ser mayor a 0" })
      } else if (valores.cantidad > origen.ocupacion) {
        ctx.addIssue({
          code: "custom",
          path: ["cantidad"],
          message: `${origen.codigo} solo contiene ${cantidad(origen.ocupacion, origen.unidad)}`,
        })
      } else if (valores.cantidad > destino.disponible) {
        ctx.addIssue({
          code: "custom",
          path: ["cantidad"],
          message: `${destino.codigo} solo admite ${cantidad(destino.disponible, destino.unidad)} más`,
        })
      }
    })

export function MovimientoDialog({ contenedores, inicial = {}, onClose }) {
  const actor = useActor()
  const esquema = useMemo(() => crearEsquema(contenedores), [contenedores])
  const form = useForm({
    resolver: zodResolver(esquema),
    defaultValues: {
      tipo: inicial.tipo ?? "TRASLADO",
      idOrigen: inicial.idContenedor ? String(inicial.idContenedor) : "",
      idDestino: "",
      cantidad: "",
      motivo: "",
    },
  })
  const [tipo, idOrigen] = useWatch({ control: form.control, name: ["tipo", "idOrigen"] })
  const origen = contenedores.find((item) => String(item.idContenedor) === idOrigen)
  const esTraslado = tipo === "TRASLADO"
  const opcionesOrigen = contenedores.filter((item) =>
    esTraslado ? item.ocupacion > 0 : item.estado !== "MANTENIMIENTO"
  )
  const opcionesDestino = contenedores.filter(
    (item) =>
      origen &&
      item.idContenedor !== origen.idContenedor &&
      item.idResiduo === origen.idResiduo &&
      item.estado !== "MANTENIMIENTO"
  )
  const registrar = useMockMutation(registrarMovimiento, {
    success: (movimiento) => `Movimiento ${movimiento.codigo} registrado`,
    onSuccess: onClose,
  })

  const descripcionOrigen = origen
    ? `${origen.residuo?.nombre} · contiene ${cantidad(origen.ocupacion, origen.unidad)} de ${cantidad(origen.capacidad, origen.unidad)}`
    : undefined

  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title="Registrar movimiento"
      description="Los traslados y pesajes actualizan la ocupación de los contenedores y quedan en el historial."
      form={form}
      pending={registrar.isPending}
      submitLabel="Registrar movimiento"
      onSubmit={(valores) => registrar.mutate({ valores, actor })}
    >
      <SelectField
        control={form.control}
        name="tipo"
        label="Tipo de movimiento"
        options={TIPOS}
        onValueChange={() => {
          form.setValue("idOrigen", "")
          form.setValue("idDestino", "")
        }}
      />
      {esTraslado ? (
        <FieldRow>
          <SelectField
            control={form.control}
            name="idOrigen"
            label="Contenedor de origen"
            options={opcionesOrigen.map((item) => ({ value: item.idContenedor, label: `${item.codigo} · ${item.residuo?.nombre}` }))}
            onValueChange={() => form.setValue("idDestino", "")}
            description={descripcionOrigen}
          />
          <SelectField
            control={form.control}
            name="idDestino"
            label="Contenedor de destino"
            disabled={!origen}
            placeholder={origen ? "Selecciona el destino" : "Primero elige el origen"}
            options={opcionesDestino.map((item) => ({
              value: item.idContenedor,
              label: `${item.codigo} · ${cantidad(item.disponible, item.unidad)} libres`,
              disabled: item.disponible <= 0,
            }))}
            description={origen && opcionesDestino.length === 0 ? "No hay otro contenedor operativo para este residuo." : undefined}
          />
        </FieldRow>
      ) : (
        <SelectField
          control={form.control}
          name="idOrigen"
          label="Contenedor"
          options={opcionesOrigen.map((item) => ({ value: item.idContenedor, label: `${item.codigo} · ${item.residuo?.nombre}` }))}
          description={
            origen
              ? `Registrado en sistema: ${cantidad(origen.ocupacion, origen.unidad)} de ${cantidad(origen.capacidad, origen.unidad)}`
              : undefined
          }
        />
      )}
      <NumberField
        control={form.control}
        name="cantidad"
        label={esTraslado ? "Cantidad a trasladar" : "Peso o volumen verificado"}
        suffix={origen?.unidad}
        min="0"
        description={esTraslado ? undefined : "La diferencia con lo registrado se guarda como ajuste."}
      />
      <TextareaField
        control={form.control}
        name="motivo"
        label="Motivo"
        placeholder={esTraslado ? "Ej. Contenedor en mantenimiento" : "Ej. Pesaje de verificación semanal"}
      />
    </FormDialog>
  )
}
