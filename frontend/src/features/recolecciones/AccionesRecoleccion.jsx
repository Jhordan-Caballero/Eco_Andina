import { useForm, useWatch } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { CheckCircle2, Lock, PackageCheck, Truck } from "lucide-react"
import { ConfirmAction } from "@/components/shared/ConfirmAction"
import { FieldRow, FormDialog } from "@/components/shared/FormDialog"
import {
  CheckboxField,
  FileField,
  NumberField,
  SelectField,
  TextField,
  TextareaField,
} from "@/components/shared/fields"
import { Button } from "@/components/ui/button"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import { useAuth } from "@/features/auth/AuthContext"
import { useActor } from "@/hooks/useActor"
import { useMockMutation } from "@/hooks/useMockMutation"
import { TIPO_DESTINO, TIPO_DOCUMENTO, opciones } from "@/lib/estados"
import { ahoraLocal, cantidad, fecha } from "@/lib/formato"
import {
  accionPendiente,
  aprobarRecoleccion,
  cerrarRecoleccion,
  registrarRecepcion,
  registrarSalida,
} from "./recoleccionesApi"

export function AccionRecoleccion({ orden, onDialog, size = "default" }) {
  const { hasPermission } = useAuth()
  const actor = useActor()
  const aprobar = useMockMutation(aprobarRecoleccion, {
    success: (resultado) => `${resultado.numeroOrden} aprobada y programada`,
  })
  const cerrar = useMockMutation(cerrarRecoleccion, {
    success: (resultado) => `${resultado.numeroOrden} cerrada`,
  })
  const accion = accionPendiente(orden, hasPermission)

  if (accion === "aprobar") {
    return (
      <ConfirmAction
        trigger={
          <Button size={size} disabled={aprobar.isPending}>
            <CheckCircle2 />
            Aprobar
          </Button>
        }
        title={`Aprobar ${orden.numeroOrden}`}
        description={`Se programará el retiro de ${cantidad(orden.cantidadTotal, orden.unidad)} para el ${fecha(orden.fechaProgramada)} con ${orden.transportista?.empresa} (${orden.vehiculo?.placa}).`}
        confirmLabel="Aprobar orden"
        onConfirm={() => aprobar.mutate({ idRecoleccion: orden.idRecoleccion, actor })}
      />
    )
  }
  if (accion === "salida") {
    return (
      <Button size={size} onClick={() => onDialog("salida")}>
        <Truck />
        Registrar salida
      </Button>
    )
  }
  if (accion === "recepcion") {
    return (
      <Button size={size} onClick={() => onDialog("recepcion")}>
        <PackageCheck />
        Registrar recepción
      </Button>
    )
  }
  if (accion === "cerrar") {
    const faltantes = orden.requisitosCierre.filter((item) => !item.cumple)
    if (faltantes.length > 0) {
      return (
        <Tooltip>
          <TooltipTrigger asChild>
            <span tabIndex={0} className="inline-flex rounded-lg outline-none focus-visible:ring-2 focus-visible:ring-ring">
              <Button size={size} variant="outline" disabled>
                <Lock />
                Cerrar orden
              </Button>
            </span>
          </TooltipTrigger>
          <TooltipContent className="max-w-64">
            Falta: {faltantes.map((item) => item.label.toLowerCase()).join(", ")}
          </TooltipContent>
        </Tooltip>
      )
    }
    return (
      <ConfirmAction
        trigger={
          <Button size={size} disabled={cerrar.isPending}>
            <Lock />
            Cerrar orden
          </Button>
        }
        title={`Cerrar ${orden.numeroOrden}`}
        description="Se cumplen todos los requisitos de trazabilidad. La orden quedará cerrada y ya no admitirá cambios."
        confirmLabel="Cerrar orden"
        onConfirm={() => cerrar.mutate({ idRecoleccion: orden.idRecoleccion, actor })}
      />
    )
  }
  return null
}

const fechaPasada = z
  .string()
  .min(1, "Indica la fecha y hora")
  .refine((valor) => new Date(valor) <= new Date(), "La fecha no puede ser futura")

export function SalidaDialog({ orden, onClose }) {
  const actor = useActor()
  const esquema = z
    .object({
      tipoDocumento: z.string().min(1, "Selecciona el documento de traslado"),
      numeroDocumento: z.string().trim().min(4, "Ingresa el número del documento"),
      fecha: fechaPasada,
      archivo: z.string().optional(),
      observaciones: z.string().max(255, "Máximo 255 caracteres").optional(),
    })
    .refine((valores) => !orden.peligroso || valores.tipoDocumento === "MANIFIESTO", {
      path: ["tipoDocumento"],
      message: "Los residuos peligrosos requieren manifiesto de manejo",
    })
  const form = useForm({
    resolver: zodResolver(esquema),
    defaultValues: {
      tipoDocumento: orden.peligroso ? "MANIFIESTO" : "GUIA_REMISION",
      numeroDocumento: "",
      fecha: ahoraLocal(),
      archivo: "",
      observaciones: "",
    },
  })
  const salida = useMockMutation(registrarSalida, {
    success: (resultado) => `${resultado.numeroOrden} en tránsito`,
    onSuccess: onClose,
  })

  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={`Registrar salida · ${orden.numeroOrden}`}
      description={`${cantidad(orden.cantidadTotal, orden.unidad)} de ${orden.residuos.join(", ").toLowerCase()} en ${orden.vehiculo?.placa}. Al confirmar, los contenedores se liberan.`}
      form={form}
      pending={salida.isPending}
      submitLabel="Confirmar salida"
      onSubmit={(valores) => salida.mutate({ idRecoleccion: orden.idRecoleccion, valores, actor })}
    >
      <FieldRow>
        <SelectField
          control={form.control}
          name="tipoDocumento"
          label="Documento de traslado"
          options={["MANIFIESTO", "GUIA_REMISION"].map((valor) => ({ value: valor, label: TIPO_DOCUMENTO[valor] }))}
          description={orden.peligroso ? "Obligatorio por tratarse de residuos peligrosos." : undefined}
        />
        <TextField control={form.control} name="numeroDocumento" label="Número" placeholder="Ej. MRSP-2026-0420" />
      </FieldRow>
      <TextField control={form.control} name="fecha" label="Fecha y hora de salida" type="datetime-local" max={ahoraLocal()} />
      <FileField control={form.control} name="archivo" label="Copia del documento (opcional)" accept=".pdf,image/*" />
      <TextareaField control={form.control} name="observaciones" label="Observaciones (opcional)" />
    </FormDialog>
  )
}

const destinoSugerido = (orden) => {
  const categoria = orden.detalles[0]?.residuo?.categoria
  if (categoria === "APROVECHABLE") return "VALORIZACION"
  if (categoria === "COMUN") return "DISPOSICION"
  return "TRATAMIENTO"
}

export function RecepcionDialog({ orden, onClose }) {
  const actor = useActor()
  const esquema = z
    .object({
      fecha: fechaPasada,
      cantidadRecibida: z.coerce.number({ error: "Ingresa la cantidad" }).positive("La cantidad debe ser mayor a 0"),
      conforme: z.boolean(),
      tipoDestino: z.string().min(1, "Selecciona el destino"),
      tratamiento: z.string().trim().min(3, "Describe el tratamiento o disposición"),
      acta: z.string().optional(),
      certificado: z.string().optional(),
      observaciones: z.string().max(255, "Máximo 255 caracteres").optional(),
    })
    .superRefine((valores, ctx) => {
      const nota = valores.observaciones?.trim()
      if (!valores.conforme && !nota) {
        ctx.addIssue({ code: "custom", path: ["observaciones"], message: "Describe el motivo de la no conformidad" })
      }
      const diferencia = Math.abs(valores.cantidadRecibida - orden.cantidadTotal) / orden.cantidadTotal
      if (valores.conforme && diferencia > 0.05 && !nota) {
        ctx.addIssue({
          code: "custom",
          path: ["observaciones"],
          message: `Explica la diferencia con lo despachado (${cantidad(orden.cantidadTotal, orden.unidad)})`,
        })
      }
    })
  const form = useForm({
    resolver: zodResolver(esquema),
    defaultValues: {
      fecha: ahoraLocal(),
      cantidadRecibida: orden.cantidadTotal,
      conforme: true,
      tipoDestino: destinoSugerido(orden),
      tratamiento: orden.detalles[0]?.residuo?.tratamiento ?? "",
      acta: "",
      certificado: "",
      observaciones: "",
    },
  })
  const conforme = useWatch({ control: form.control, name: "conforme" })
  const recepcion = useMockMutation(registrarRecepcion, {
    success: (resultado) =>
      resultado.recepcion.conforme
        ? `Recepción de ${resultado.numeroOrden} registrada`
        : `Recepción registrada y se abrió una incidencia por rechazo`,
    onSuccess: onClose,
  })

  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={`Registrar recepción · ${orden.numeroOrden}`}
      description={`Despachado: ${cantidad(orden.cantidadTotal, orden.unidad)} con el documento ${orden.salida?.documento}.`}
      form={form}
      pending={recepcion.isPending}
      submitLabel="Registrar recepción"
      onSubmit={(valores) => recepcion.mutate({ idRecoleccion: orden.idRecoleccion, valores, actor })}
    >
      <FieldRow>
        <TextField control={form.control} name="fecha" label="Fecha y hora" type="datetime-local" max={ahoraLocal()} />
        <NumberField control={form.control} name="cantidadRecibida" label="Cantidad recibida" suffix={orden.unidad} min="0" />
      </FieldRow>
      <CheckboxField
        control={form.control}
        name="conforme"
        label="Recepción conforme"
        description="Desmarca si los residuos llegaron mezclados, sin rótulo o con envases dañados. Se abrirá una incidencia."
      />
      <FieldRow>
        <SelectField control={form.control} name="tipoDestino" label="Destino" options={opciones(TIPO_DESTINO)} />
        <TextField control={form.control} name="tratamiento" label="Tratamiento o proceso" />
      </FieldRow>
      <FieldRow>
        <FileField control={form.control} name="acta" label="Acta de recepción (opcional)" accept=".pdf,image/*" />
        <FileField
          control={form.control}
          name="certificado"
          label="Certificado (opcional)"
          accept=".pdf"
          description="Necesario para cerrar la orden. Puedes adjuntarlo después."
        />
      </FieldRow>
      <TextareaField
        control={form.control}
        name="observaciones"
        label={conforme ? "Observaciones (opcional)" : "Motivo de la no conformidad"}
      />
    </FormDialog>
  )
}
