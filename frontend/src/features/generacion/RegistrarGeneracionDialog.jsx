import { useMemo } from "react"
import { useForm, useWatch } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { FieldRow, FormDialog } from "@/components/shared/FormDialog"
import {
  CheckboxField,
  FileField,
  NumberField,
  SelectField,
  TextField,
  TextareaField,
} from "@/components/shared/fields"
import { useActor } from "@/hooks/useActor"
import { useMockMutation } from "@/hooks/useMockMutation"
import { CONDICION_ENVASE, opciones } from "@/lib/estados"
import { ahoraLocal, cantidad } from "@/lib/formato"
import { registrarGeneracion } from "./generacionApi"

const crearEsquema = ({ contenedores, residuos }) =>
  z
    .object({
      idArea: z.string().min(1, "Selecciona el área de origen"),
      idResiduo: z.string().min(1, "Selecciona el residuo"),
      idContenedor: z.string().min(1, "Selecciona el contenedor"),
      cantidad: z.coerce.number({ error: "Ingresa la cantidad" }).positive("La cantidad debe ser mayor a 0"),
      fecha: z
        .string()
        .min(1, "Indica la fecha y hora")
        .refine((valor) => new Date(valor) <= new Date(), "La fecha no puede ser futura"),
      condicionEnvase: z.string().min(1, "Indica la condición del envase"),
      segregacionCorrecta: z.boolean(),
      evidencia: z.string().optional(),
      observaciones: z.string().max(255, "Máximo 255 caracteres").optional(),
    })
    .superRefine((valores, ctx) => {
      const contenedor = contenedores.find((item) => String(item.idContenedor) === valores.idContenedor)
      if (contenedor && valores.cantidad > contenedor.disponible) {
        ctx.addIssue({
          code: "custom",
          path: ["cantidad"],
          message: `Supera lo disponible en ${contenedor.codigo} (${cantidad(contenedor.disponible, contenedor.unidad)})`,
        })
      }
      const residuo = residuos.find((item) => String(item.idResiduo) === valores.idResiduo)
      if (residuo && ["PELIGROSO", "ESPECIAL"].includes(residuo.categoria) && !valores.evidencia) {
        ctx.addIssue({
          code: "custom",
          path: ["evidencia"],
          message: "La evidencia es obligatoria para residuos peligrosos o especiales",
        })
      }
    })

export function RegistrarGeneracionDialog({ opcionesRegistro, onClose }) {
  const actor = useActor()
  const { areas, residuos, contenedores } = opcionesRegistro
  const esquema = useMemo(() => crearEsquema(opcionesRegistro), [opcionesRegistro])
  const form = useForm({
    resolver: zodResolver(esquema),
    defaultValues: {
      idArea: "",
      idResiduo: "",
      idContenedor: "",
      cantidad: "",
      fecha: ahoraLocal(),
      condicionEnvase: "BUEN_ESTADO",
      segregacionCorrecta: true,
      evidencia: "",
      observaciones: "",
    },
  })
  const [idResiduo, idContenedor] = useWatch({ control: form.control, name: ["idResiduo", "idContenedor"] })
  const residuo = residuos.find((item) => String(item.idResiduo) === idResiduo)
  const contenedor = contenedores.find((item) => String(item.idContenedor) === idContenedor)
  const contenedoresDelResiduo = contenedores.filter((item) => String(item.idResiduo) === idResiduo)
  const registrar = useMockMutation(registrarGeneracion, {
    success: (registro) => `Generación ${registro.codigo} registrada y enviada a validación`,
    onSuccess: onClose,
  })

  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title="Registrar generación"
      description="El registro queda pendiente hasta que el supervisor ambiental lo valide."
      form={form}
      pending={registrar.isPending}
      submitLabel="Registrar"
      onSubmit={(valores) => registrar.mutate({ valores, actor })}
    >
      <FieldRow>
        <SelectField
          control={form.control}
          name="idArea"
          label="Área de origen"
          options={areas.map((item) => ({ value: item.idArea, label: item.nombre }))}
        />
        <TextField control={form.control} name="fecha" label="Fecha y hora" type="datetime-local" max={ahoraLocal()} />
      </FieldRow>
      <SelectField
        control={form.control}
        name="idResiduo"
        label="Residuo"
        options={residuos.map((item) => ({ value: item.idResiduo, label: `${item.nombre} · ${item.unidad}` }))}
        onValueChange={() => form.setValue("idContenedor", "")}
        description={residuo ? `Requisito: ${residuo.requisitos}` : undefined}
      />
      <FieldRow>
        <SelectField
          control={form.control}
          name="idContenedor"
          label="Contenedor"
          disabled={!residuo}
          placeholder={residuo ? "Selecciona el contenedor" : "Primero elige el residuo"}
          options={contenedoresDelResiduo.map((item) => ({
            value: item.idContenedor,
            label: `${item.codigo} · ${cantidad(item.disponible, item.unidad)} libres`,
            disabled: item.disponible <= 0,
          }))}
          description={contenedor ? contenedor.ubicacion : undefined}
        />
        <NumberField control={form.control} name="cantidad" label="Cantidad" suffix={residuo?.unidad} min="0" />
      </FieldRow>
      <SelectField
        control={form.control}
        name="condicionEnvase"
        label="Condición del envase"
        options={opciones(CONDICION_ENVASE)}
      />
      <CheckboxField
        control={form.control}
        name="segregacionCorrecta"
        label="El residuo se segregó según su categoría"
        description="Desmarca si encontraste otros residuos mezclados."
      />
      <FileField
        control={form.control}
        name="evidencia"
        label="Evidencia"
        accept="image/*,.pdf"
        description="Fotografía del residuo y su rótulo. Obligatoria para residuos peligrosos."
      />
      <TextareaField control={form.control} name="observaciones" label="Observaciones (opcional)" />
    </FormDialog>
  )
}
