import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { FormDialog } from "@/components/shared/FormDialog"
import { FileField, SelectField, TextareaField } from "@/components/shared/fields"
import { useActor } from "@/hooks/useActor"
import { useMockMutation } from "@/hooks/useMockMutation"
import { TIPO_DOCUMENTO, opciones } from "@/lib/estados"
import { adjuntarDocumento } from "./documentosApi"

const esquema = z.object({
  tipo: z.string().min(1, "Selecciona el tipo de documento"),
  referencia: z.string().min(1, "Selecciona el registro relacionado"),
  archivo: z.any().refine((valor) => valor instanceof File, "Selecciona el archivo"),
  descripcion: z.string().max(200, "Máximo 200 caracteres").optional(),
})

export function AdjuntarDocumentoDialog({ referencia, referencias = [], tipoInicial = "", onClose }) {
  const actor = useActor()
  const form = useForm({
    resolver: zodResolver(esquema),
    defaultValues: { tipo: tipoInicial, referencia: referencia ?? "", archivo: null, descripcion: "" },
  })
  const adjuntar = useMockMutation(adjuntarDocumento, {
    success: (documento) => `${documento.nombreArchivo} adjuntado a ${documento.referencia}`,
    onSuccess: onClose,
  })

  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title="Adjuntar documento"
      description={
        referencia
          ? `El documento quedará vinculado a ${referencia} y pendiente de revisión.`
          : "El documento quedará pendiente de revisión por el supervisor ambiental."
      }
      form={form}
      pending={adjuntar.isPending}
      submitLabel="Adjuntar"
      onSubmit={(valores) => adjuntar.mutate({ valores, actor })}
    >
      <SelectField control={form.control} name="tipo" label="Tipo de documento" options={opciones(TIPO_DOCUMENTO)} />
      {!referencia && (
        <SelectField control={form.control} name="referencia" label="Registro relacionado" options={referencias} />
      )}
      <FileField
        control={form.control}
        name="archivo"
        label="Archivo"
        accept=".pdf,image/*"
        description="PDF, JPG, PNG o WEBP de hasta 10 MB."
      />
      <TextareaField control={form.control} name="descripcion" label="Descripción (opcional)" />
    </FormDialog>
  )
}
