import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { Check, CircleAlert, FileText, Paperclip, X } from "lucide-react"
import { DetailList, DetailSection, DetailSheet, Nota } from "@/components/shared/DetailSheet"
import { EstadoBadge } from "@/components/shared/StatusBadge"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/features/auth/AuthContext"
import { AdjuntarDocumentoDialog } from "@/features/documentos/AdjuntarDocumentoDialog"
import {
  ESTADO_DOCUMENTO,
  ESTADO_INCIDENCIA,
  ESTADO_RECOLECCION,
  ESTADO_VIGENCIA,
  TIPO_DESTINO,
  TIPO_DOCUMENTO,
  TIPO_INCIDENCIA,
} from "@/lib/estados"
import { cantidad, fecha, fechaHora } from "@/lib/formato"
import { cn } from "@/lib/utils"
import { estadoVigencia } from "@/mocks/store"
import { AccionRecoleccion } from "./AccionesRecoleccion"
import { accionPendiente, detalleRecoleccion } from "./recoleccionesApi"

function pasos(orden) {
  return [
    {
      titulo: "Solicitud",
      hecho: true,
      fecha: orden.fechaSolicitud,
      detalle: `Programada por ${orden.solicitadoPor} para el ${fecha(orden.fechaProgramada)}`,
    },
    {
      titulo: "Aprobación",
      hecho: Boolean(orden.aprobacion),
      fecha: orden.aprobacion?.fecha,
      detalle: orden.aprobacion ? `Aprobada por ${orden.aprobacion.usuario}` : "Pendiente del supervisor ambiental",
    },
    {
      titulo: "Salida y traslado",
      hecho: Boolean(orden.salida),
      fecha: orden.salida?.fecha,
      detalle: orden.salida
        ? `Documento ${orden.salida.documento} · ${orden.salida.usuario}`
        : "Pendiente del transportista",
    },
    {
      titulo: "Recepción",
      hecho: Boolean(orden.recepcion),
      fecha: orden.recepcion?.fecha,
      detalle: orden.recepcion
        ? `${cantidad(orden.recepcion.cantidadRecibida, orden.unidad)} · ${orden.recepcion.conforme ? "conforme" : "no conforme"} · ${orden.recepcion.usuario}`
        : "Pendiente del gestor autorizado",
    },
    {
      titulo: "Cierre",
      hecho: Boolean(orden.cierre),
      fecha: orden.cierre?.fecha,
      detalle: orden.cierre ? `Cerrada por ${orden.cierre.usuario}` : "Requiere la documentación completa",
    },
  ]
}

function LineaDeTiempo({ orden }) {
  const lista = pasos(orden)
  const actual = lista.findIndex((paso) => !paso.hecho)
  return (
    <ol className="relative space-y-4">
      {lista.map((paso, index) => (
        <li key={paso.titulo} className="relative flex gap-3">
          {index < lista.length - 1 && (
            <span
              aria-hidden
              className={cn("absolute top-6 bottom-[-1rem] left-[0.6875rem] w-px", paso.hecho ? "bg-primary/50" : "bg-border")}
            />
          )}
          <span
            className={cn(
              "relative z-10 flex size-6 shrink-0 items-center justify-center rounded-full border text-xs",
              paso.hecho && "border-primary bg-primary text-primary-foreground",
              index === actual && "border-primary bg-card text-primary ring-3 ring-primary/15",
              !paso.hecho && index !== actual && "bg-card text-muted-foreground"
            )}
          >
            {paso.hecho ? <Check className="size-3.5" /> : index + 1}
          </span>
          <div className="min-w-0 pb-0.5">
            <p className={cn("text-sm font-medium", !paso.hecho && index !== actual && "text-muted-foreground")}>
              {paso.titulo}
              {paso.fecha && (
                <span className="ml-2 text-xs font-normal text-muted-foreground tabular-nums">{fechaHora(paso.fecha)}</span>
              )}
            </p>
            <p className="text-xs text-muted-foreground">{paso.detalle}</p>
          </div>
        </li>
      ))}
    </ol>
  )
}

function Requisitos({ requisitos }) {
  return (
    <ul className="space-y-1.5">
      {requisitos.map((item) => (
        <li key={item.clave} className="flex items-center gap-2 text-sm">
          {item.cumple ? (
            <Check className="size-4 shrink-0 text-primary" />
          ) : (
            <X className="size-4 shrink-0 text-destructive" />
          )}
          <span className={cn(!item.cumple && "text-muted-foreground")}>{item.label}</span>
          <span className="sr-only">{item.cumple ? "cumplido" : "pendiente"}</span>
        </li>
      ))}
    </ul>
  )
}

export function RecoleccionDetalle({ idRecoleccion, onClose, onDialog }) {
  const { hasPermission } = useAuth()
  const [adjuntando, setAdjuntando] = useState(false)
  const { data: orden } = useQuery({
    queryKey: ["recoleccion", idRecoleccion],
    queryFn: () => detalleRecoleccion(idRecoleccion),
  })
  const puedeAdjuntar = hasPermission("DOCUMENTO_GESTIONAR") && orden && orden.estado !== "CERRADA"
  const faltantes = orden?.requisitosCierre.filter((item) => !item.cumple) ?? []
  const accion = orden && accionPendiente(orden, hasPermission) && (
    <AccionRecoleccion orden={orden} onDialog={(tipo) => onDialog(tipo, orden)} />
  )

  return (
    <DetailSheet
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={orden?.numeroOrden ?? "Cargando…"}
      subtitle={orden && `${orden.residuos.join(", ")} · ${orden.areas.join(", ")}`}
      badge={orden && <EstadoBadge mapa={ESTADO_RECOLECCION} valor={orden.estado} />}
      footer={accion}
    >
      {orden && (
        <>
          <DetailSection title="Seguimiento">
            <LineaDeTiempo orden={orden} />
          </DetailSection>

          <DetailSection title={`Residuos (${cantidad(orden.cantidadTotal, orden.unidad)})`}>
            <ul className="divide-y rounded-lg border">
              {orden.detalles.map((detalle) => (
                <li key={detalle.idGeneracion} className="flex items-center gap-3 px-3 py-2 text-sm">
                  <div className="min-w-0 flex-1">
                    <p className="font-medium">
                      <span className="tabular-nums">{detalle.codigo}</span> · {detalle.residuo?.nombre}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {detalle.area?.nombre} · {detalle.contenedor?.codigo} · {fecha(detalle.fecha)}
                    </p>
                  </div>
                  <span className="tabular-nums">{cantidad(detalle.cantidad, detalle.unidad)}</span>
                </li>
              ))}
            </ul>
            {orden.observaciones && <Nota>{orden.observaciones}</Nota>}
          </DetailSection>

          <DetailSection title="Transporte y destino">
            <DetailList
              items={[
                [
                  "Transportista",
                  <span key="t" className="inline-flex flex-wrap items-center gap-1.5">
                    {orden.transportista?.empresa}
                    <EstadoBadge mapa={ESTADO_VIGENCIA} valor={estadoVigencia(orden.transportista?.vigencia)} />
                  </span>,
                ],
                ["Vehículo", orden.vehiculo && `${orden.vehiculo.placa} · ${orden.vehiculo.tipo}`],
                [
                  "Gestor",
                  <span key="g" className="inline-flex flex-wrap items-center gap-1.5">
                    {orden.gestor?.empresa}
                    <EstadoBadge mapa={ESTADO_VIGENCIA} valor={estadoVigencia(orden.gestor?.vigencia)} />
                  </span>,
                ],
                ["Autorización", orden.gestor?.autorizacion],
                orden.destino && ["Destino", `${TIPO_DESTINO[orden.destino.tipo]} · ${orden.destino.tratamiento}`],
                orden.recepcion && [
                  "Recibido",
                  `${cantidad(orden.recepcion.cantidadRecibida, orden.unidad)} de ${cantidad(orden.cantidadTotal, orden.unidad)} despachados`,
                ],
              ]}
            />
            {orden.recepcion?.observaciones && <Nota>{orden.recepcion.observaciones}</Nota>}
          </DetailSection>

          <DetailSection
            title={`Documentos (${orden.documentos.length})`}
            action={
              puedeAdjuntar && (
                <Button variant="ghost" size="sm" onClick={() => setAdjuntando(true)}>
                  <Paperclip />
                  Adjuntar
                </Button>
              )
            }
          >
            {orden.documentos.length === 0 ? (
              <p className="text-sm text-muted-foreground">Aún no hay documentos vinculados.</p>
            ) : (
              <ul className="space-y-2">
                {orden.documentos.map((documento) => (
                  <li key={documento.idDocumento} className="flex items-start gap-2 text-sm">
                    <FileText className="mt-0.5 size-4 shrink-0 text-muted-foreground" />
                    <div className="min-w-0 flex-1">
                      <p className="truncate">{documento.nombreArchivo}</p>
                      <p className="text-xs text-muted-foreground">
                        {TIPO_DOCUMENTO[documento.tipo]} · {documento.subidoPor}
                      </p>
                      {documento.motivoRechazo && (
                        <p className="text-xs text-destructive">{documento.motivoRechazo}</p>
                      )}
                    </div>
                    <EstadoBadge mapa={ESTADO_DOCUMENTO} valor={documento.estado} />
                  </li>
                ))}
              </ul>
            )}
          </DetailSection>

          {["RECIBIDA", "CERRADA"].includes(orden.estado) && (
            <DetailSection title="Requisitos de cierre">
              <Requisitos requisitos={orden.requisitosCierre} />
              {orden.estado === "RECIBIDA" && faltantes.length > 0 && (
                <p className="mt-3 flex items-start gap-2 text-xs text-muted-foreground">
                  <CircleAlert className="mt-px size-3.5 shrink-0" />
                  La orden no puede cerrarse hasta completar los requisitos marcados.
                </p>
              )}
            </DetailSection>
          )}

          {(orden.incidencias.length > 0 || orden.observacionesOperativas.length > 0) && (
            <DetailSection title="Incidencias y observaciones">
              <ul className="space-y-2.5">
                {orden.incidencias.map((item) => (
                  <li key={item.idIncidencia} className="text-sm">
                    <p className="flex flex-wrap items-center gap-1.5 font-medium">
                      {item.codigo} · {TIPO_INCIDENCIA[item.tipo]}
                      <EstadoBadge mapa={ESTADO_INCIDENCIA} valor={item.estado} />
                    </p>
                    <p className="text-xs text-muted-foreground">{item.descripcion}</p>
                  </li>
                ))}
                {orden.observacionesOperativas.map((item) => (
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

      {adjuntando && (
        <AdjuntarDocumentoDialog
          referencia={orden.numeroOrden}
          tipoInicial={orden.estado === "RECIBIDA" ? "CERTIFICADO" : ""}
          onClose={() => setAdjuntando(false)}
        />
      )}
    </DetailSheet>
  )
}
