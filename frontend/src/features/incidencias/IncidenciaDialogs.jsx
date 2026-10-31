import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { FieldRow, FormDialog } from "@/components/shared/FormDialog"
import { FileField, SelectField, TextareaField } from "@/components/shared/fields"
import { useActor } from "@/hooks/useActor"
import { useMockMutation } from "@/hooks/useMockMutation"
import { SEVERIDAD, TIPO_INCIDENCIA, opciones } from "@/lib/estados"
import { cerrarIncidencia, registrarSeguimiento, reportarIncidencia } from "./incidenciasApi"

const esquemaReporte = z
  .object({
    tipo: z.string().min(1, "Selecciona el tipo de incidencia"),
    severidad: z.string().min(1, "Selecciona la severidad"),
    idArea: z.string().min(1, "Selecciona el área"),
    referencia: z.string().optional(),
    descripcion: z.string().trim().min(15, "Describe lo ocurrido (mínimo 15 caracteres)"),
    evidencia: z.string().optional(),
  })
  .refine((valores) => valores.severidad !== "ALTA" || valores.evidencia, {
    path: ["evidencia"],
    message: "Adjunta una evidencia para incidencias de severidad alta",
  })

export function ReportarIncidenciaDialog({ opcionesIncidencia, onClose }) {
  const actor = useActor()
  const form = useForm({
    resolver: zodResolver(esquemaReporte),
    defaultValues: { tipo: "", severidad: "MEDIA", idArea: "", referencia: "", descripcion: "", evidencia: "" },
  })
  const reportar = useMockMutation(reportarIncidencia, {
    success: (incidencia) => `Incidencia ${incidencia.codigo} reportada`,
    onSuccess: onClose,
  })
  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title="Reportar incidencia"
      description="El supervisor ambiental asignará un responsable y una acción correctiva."
      form={form}
      pending={reportar.isPending}
      submitLabel="Reportar"
      onSubmit={(valores) => reportar.mutate({ valores, actor })}
    >
      <FieldRow>
        <SelectField control={form.control} name="tipo" label="Tipo" options={opciones(TIPO_INCIDENCIA)} />
        <SelectField control={form.control} name="severidad" label="Severidad" options={opciones(SEVERIDAD)} />
      </FieldRow>
      <FieldRow>
        <SelectField
          control={form.control}
          name="idArea"
          label="Área"
          options={opcionesIncidencia.areas.map((item) => ({ value: item.idArea, label: item.nombre }))}
        />
        <SelectField
          control={form.control}
          name="referencia"
          label="Registro relacionado (opcional)"
          placeholder="Orden, contenedor o registro"
          options={opcionesIncidencia.referencias}
        />
      </FieldRow>
      <TextareaField
        control={form.control}
        name="descripcion"
        label="Descripción"
        placeholder="Qué ocurrió, dónde y qué medidas inmediatas se tomaron."
      />
      <FileField control={form.control} name="evidencia" label="Evidencia" accept="image/*,.pdf" description="Obligatoria si la severidad es alta." />
    </FormDialog>
  )
}

const esquemaSeguimiento = z.object({
  responsable: z.string().min(1, "Asigna un responsable"),
  accionCorrectiva: z.string().trim().min(10, "Describe la acción correctiva"),
  nota: z.string().trim().min(5, "Describe el avance registrado"),
})

export function SeguimientoDialog({ incidencia, responsables, onClose }) {
  const actor = useActor()
  const form = useForm({
    resolver: zodResolver(esquemaSeguimiento),
    defaultValues: {
      responsable: incidencia.responsable ?? "",
      accionCorrectiva: incidencia.accionCorrectiva ?? "",
      nota: "",
    },
  })
  const guardar = useMockMutation(registrarSeguimiento, {
    success: `Seguimiento de ${incidencia.codigo} registrado`,
    onSuccess: onClose,
  })
  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={`Seguimiento · ${incidencia.codigo}`}
      description={incidencia.descripcion}
      form={form}
      pending={guardar.isPending}
      submitLabel="Registrar seguimiento"
      onSubmit={(valores) => guardar.mutate({ idIncidencia: incidencia.idIncidencia, valores, actor })}
    >
      <SelectField
        control={form.control}
        name="responsable"
        label="Responsable"
        options={responsables.map((nombre) => ({ value: nombre, label: nombre }))}
      />
      <TextareaField control={form.control} name="accionCorrectiva" label="Acción correctiva" />
      <TextareaField control={form.control} name="nota" label="Avance" placeholder="Ej. Se capacitó al personal del turno." />
    </FormDialog>
  )
}

const esquemaCierre = z.object({
  nota: z.string().trim().min(10, "Describe el resultado de la acción correctiva"),
})

export function CerrarIncidenciaDialog({ incidencia, onClose }) {
  const actor = useActor()
  const form = useForm({ resolver: zodResolver(esquemaCierre), defaultValues: { nota: "" } })
  const cerrar = useMockMutation(cerrarIncidencia, {
    success: `${incidencia.codigo} cerrada`,
    onSuccess: onClose,
  })
  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={`Cerrar ${incidencia.codigo}`}
      description={`Acción correctiva: ${incidencia.accionCorrectiva}`}
      form={form}
      pending={cerrar.isPending}
      submitLabel="Cerrar incidencia"
      onSubmit={({ nota }) => cerrar.mutate({ idIncidencia: incidencia.idIncidencia, nota, actor })}
    >
      <TextareaField control={form.control} name="nota" label="Verificación del cierre" />
    </FormDialog>
  )
}
