import { useState } from "react"
import { Link } from "react-router-dom"
import { useQuery } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { CheckCircle2, FileText, MessageSquareWarning, PencilLine } from "lucide-react"
import { ConfirmAction } from "@/components/shared/ConfirmAction"
import { DetailList, DetailSection, DetailSheet, Nota } from "@/components/shared/DetailSheet"
import { FormDialog } from "@/components/shared/FormDialog"
import { FileField, NumberField, SelectField, TextareaField } from "@/components/shared/fields"
import { EstadoBadge } from "@/components/shared/StatusBadge"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/features/auth/AuthContext"
import { useActor } from "@/hooks/useActor"
import { useMockMutation } from "@/hooks/useMockMutation"
import {
  CATEGORIA_RESIDUO,
  CONDICION_ENVASE,
  ESTADO_DOCUMENTO,
  ESTADO_GENERACION,
  ESTADO_RECOLECCION,
  PELIGROSIDAD,
  etiqueta,
  opciones,
} from "@/lib/estados"
import { cantidad, fechaHora } from "@/lib/formato"
import {
  corregirGeneracion,
  detalleGeneracion,
  observarGeneracion,
  validarGeneracion,
} from "./generacionApi"

const esquemaObservacion = z.object({
  observacion: z.string().trim().min(10, "Explica qué debe corregirse (mínimo 10 caracteres)"),
})

function ObservarDialog({ generacion, onClose }) {
  const actor = useActor()
  const form = useForm({ resolver: zodResolver(esquemaObservacion), defaultValues: { observacion: "" } })
  const observar = useMockMutation(observarGeneracion, {
    success: `${generacion.codigo} observado`,
    onSuccess: onClose,
  })
  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={`Observar ${generacion.codigo}`}
      description="El generador recibirá la observación y deberá corregir el registro."
      form={form}
      pending={observar.isPending}
      submitLabel="Registrar observación"
      onSubmit={({ observacion }) => observar.mutate({ idGeneracion: generacion.idGeneracion, observacion, actor })}
    >
      <TextareaField
        control={form.control}
        name="observacion"
        label="Observación"
        placeholder="Ej. Falta el rótulo del envase y la fotografía no es legible."
      />
    </FormDialog>
  )
}

const esquemaCorreccion = z.object({
  cantidad: z.coerce.number({ error: "Ingresa la cantidad" }).positive("La cantidad debe ser mayor a 0"),
  condicionEnvase: z.string().min(1, "Indica la condición del envase"),
  evidencia: z.string().optional(),
  observaciones: z.string().trim().min(5, "Describe la corrección realizada"),
})

function CorregirDialog({ generacion, onClose }) {
  const actor = useActor()
  const form = useForm({
    resolver: zodResolver(esquemaCorreccion),
    defaultValues: {
      cantidad: generacion.cantidad,
      condicionEnvase: generacion.condicionEnvase,
      evidencia: "",
      observaciones: "",
    },
  })
  const corregir = useMockMutation(corregirGeneracion, {
    success: `${generacion.codigo} reenviado a validación`,
    onSuccess: onClose,
  })
  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={`Corregir ${generacion.codigo}`}
      description={`Observación del supervisor: ${generacion.observacionValidacion}`}
      form={form}
      pending={corregir.isPending}
      submitLabel="Reenviar a validación"
      onSubmit={(valores) => corregir.mutate({ idGeneracion: generacion.idGeneracion, valores, actor })}
    >
      <NumberField control={form.control} name="cantidad" label="Cantidad" suffix={generacion.unidad} />
      <SelectField control={form.control} name="condicionEnvase" label="Condición del envase" options={opciones(CONDICION_ENVASE)} />
      <FileField control={form.control} name="evidencia" label="Nueva evidencia (opcional)" accept="image/*,.pdf" />
      <TextareaField control={form.control} name="observaciones" label="Corrección realizada" />
    </FormDialog>
  )
}

export function GeneracionDetalle({ idGeneracion, onClose }) {
  const { hasPermission } = useAuth()
  const actor = useActor()
  const [dialogo, setDialogo] = useState(null)
  const { data: generacion } = useQuery({
    queryKey: ["generacion", idGeneracion],
    queryFn: () => detalleGeneracion(idGeneracion),
  })
  const validar = useMockMutation(validarGeneracion, {
    success: (registro) => `${registro.codigo} validado`,
  })

  const puedeValidar = hasPermission("GENERACION_VALIDAR") && generacion?.estadoValidacion === "PENDIENTE"
  const puedeCorregir =
    hasPermission("GENERACION_REGISTRAR") &&
    generacion?.estadoValidacion === "OBSERVADA" &&
    generacion?.responsable === actor

  const footer = generacion && (puedeValidar || puedeCorregir) && (
    <>
      {puedeValidar && (
        <>
          <Button variant="outline" onClick={() => setDialogo("observar")}>
            <MessageSquareWarning />
            Observar
          </Button>
          <ConfirmAction
            trigger={
              <Button>
                <CheckCircle2 />
                Validar
              </Button>
            }
            title={`Validar ${generacion.codigo}`}
            description={`Confirmas que ${cantidad(generacion.cantidad, generacion.unidad)} de ${generacion.residuo?.nombre.toLowerCase()} cumplen la categoría, la peligrosidad y las condiciones de almacenamiento.`}
            confirmLabel="Validar registro"
            onConfirm={() => validar.mutate({ idGeneracion: generacion.idGeneracion, actor })}
          />
        </>
      )}
      {puedeCorregir && (
        <Button onClick={() => setDialogo("corregir")}>
          <PencilLine />
          Corregir y reenviar
        </Button>
      )}
    </>
  )

  return (
    <DetailSheet
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={generacion?.codigo ?? "Cargando…"}
      subtitle={generacion && `${generacion.residuo?.nombre} · ${generacion.area?.nombre}`}
      badge={generacion && <EstadoBadge mapa={ESTADO_GENERACION} valor={generacion.estadoValidacion} />}
      footer={footer}
    >
      {generacion && (
        <>
          <DetailSection title="Registro">
            <DetailList
              items={[
                ["Residuo", generacion.residuo?.nombre],
                ["Categoría", <EstadoBadge key="c" mapa={CATEGORIA_RESIDUO} valor={generacion.residuo?.categoria} />],
                ["Peligrosidad", PELIGROSIDAD[generacion.residuo?.peligrosidad]],
                ["Cantidad", cantidad(generacion.cantidad, generacion.unidad)],
                ["Área de origen", generacion.area?.nombre],
                ["Contenedor", `${generacion.contenedor?.codigo} · ${generacion.contenedor?.ubicacion}`],
                ["Fecha", fechaHora(generacion.fecha)],
                ["Responsable", generacion.responsable],
                ["Condición del envase", etiqueta(CONDICION_ENVASE, generacion.condicionEnvase)],
                ["Segregación", generacion.segregacionCorrecta ? "Correcta" : "Con residuos mezclados"],
              ]}
            />
            {generacion.observaciones && <Nota>{generacion.observaciones}</Nota>}
          </DetailSection>

          <DetailSection title="Validación">
            {generacion.estadoValidacion === "PENDIENTE" ? (
              <p className="text-sm text-muted-foreground">Pendiente de revisión por el supervisor ambiental.</p>
            ) : (
              <DetailList
                items={[
                  ["Revisado por", generacion.validadoPor],
                  ["Fecha", fechaHora(generacion.fechaValidacion)],
                  generacion.observacionValidacion && ["Observación", generacion.observacionValidacion],
                ]}
              />
            )}
          </DetailSection>

          <DetailSection title="Recolección">
            {generacion.recoleccion ? (
              <div className="flex items-center gap-2 text-sm">
                <Link
                  to={`/recolecciones?orden=${generacion.recoleccion.numeroOrden}`}
                  className="font-medium text-primary underline-offset-4 hover:underline"
                >
                  {generacion.recoleccion.numeroOrden}
                </Link>
                <EstadoBadge mapa={ESTADO_RECOLECCION} valor={generacion.recoleccion.estado} />
              </div>
            ) : (
              <p className="text-sm text-muted-foreground">
                {generacion.estadoValidacion === "VALIDADA"
                  ? "Disponible para programar una recolección."
                  : "Se podrá programar cuando el registro esté validado."}
              </p>
            )}
          </DetailSection>

          <DetailSection title="Evidencias">
            {generacion.documentos.length === 0 ? (
              <p className="text-sm text-muted-foreground">Sin evidencias adjuntas.</p>
            ) : (
              <ul className="space-y-2">
                {generacion.documentos.map((documento) => (
                  <li key={documento.idDocumento} className="flex items-center gap-2 text-sm">
                    <FileText className="size-4 shrink-0 text-muted-foreground" />
                    <span className="min-w-0 flex-1 truncate">{documento.nombreArchivo}</span>
                    <EstadoBadge mapa={ESTADO_DOCUMENTO} valor={documento.estado} />
                  </li>
                ))}
              </ul>
            )}
          </DetailSection>

          {generacion.observacionesOperativas.length > 0 && (
            <DetailSection title="Observaciones operativas">
              <ul className="space-y-2">
                {generacion.observacionesOperativas.map((item) => (
                  <li key={item.idObservacion} className="text-sm">
                    <p>{item.texto}</p>
                    <p className="text-xs text-muted-foreground">
                      {item.autor} · {fechaHora(item.fecha)}
                    </p>
                  </li>
                ))}
              </ul>
            </DetailSection>
          )}
        </>
      )}

      {dialogo === "observar" && <ObservarDialog generacion={generacion} onClose={() => setDialogo(null)} />}
      {dialogo === "corregir" && <CorregirDialog generacion={generacion} onClose={() => setDialogo(null)} />}
    </DetailSheet>
  )
}
