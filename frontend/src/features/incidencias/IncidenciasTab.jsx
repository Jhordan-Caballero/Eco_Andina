import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { CheckCircle2, FileText, ListPlus, Plus } from "lucide-react"
import { DetailList, DetailSection, DetailSheet, Nota } from "@/components/shared/DetailSheet"
import { EmptyRow } from "@/components/shared/EmptyState"
import { SegmentedFilter } from "@/components/shared/SegmentedFilter"
import { EstadoBadge } from "@/components/shared/StatusBadge"
import { LoadingRows, RowButton, TablePanel } from "@/components/shared/TablePanel"
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
import { ESTADO_DOCUMENTO, ESTADO_INCIDENCIA, SEVERIDAD, TIPO_INCIDENCIA, opciones } from "@/lib/estados"
import { TODOS, pasaFiltro } from "@/lib/filtros"
import { coincide, fechaHora, relativo } from "@/lib/formato"
import { CerrarIncidenciaDialog, ReportarIncidenciaDialog, SeguimientoDialog } from "./IncidenciaDialogs"
import { listarIncidencias, opcionesIncidencia } from "./incidenciasApi"

function IncidenciaDetalle({ incidencia, puedeGestionar, onClose, onDialog }) {
  const abierta = incidencia.estado !== "CERRADA"
  return (
    <DetailSheet
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={incidencia.codigo}
      subtitle={`${TIPO_INCIDENCIA[incidencia.tipo]} · ${incidencia.area?.nombre}`}
      badge={<EstadoBadge mapa={ESTADO_INCIDENCIA} valor={incidencia.estado} />}
      footer={
        puedeGestionar &&
        abierta && (
          <>
            <Button variant="outline" onClick={() => onDialog("seguimiento")}>
              <ListPlus />
              Registrar seguimiento
            </Button>
            <Button onClick={() => onDialog("cerrar")} disabled={!incidencia.responsable || !incidencia.accionCorrectiva}>
              <CheckCircle2 />
              Cerrar incidencia
            </Button>
          </>
        )
      }
    >
      <DetailSection title="Reporte">
        <Nota>{incidencia.descripcion}</Nota>
        <div className="mt-3">
          <DetailList
            items={[
              ["Severidad", <EstadoBadge key="s" mapa={SEVERIDAD} valor={incidencia.severidad} />],
              ["Registro relacionado", incidencia.referencia || "—"],
              ["Reportado por", incidencia.reportadoPor],
              ["Fecha", fechaHora(incidencia.fecha)],
            ]}
          />
        </div>
      </DetailSection>

      <DetailSection title="Acción correctiva">
        {incidencia.responsable ? (
          <DetailList
            items={[
              ["Responsable", incidencia.responsable],
              ["Acción", incidencia.accionCorrectiva],
              incidencia.fechaCierre && ["Cerrada", fechaHora(incidencia.fechaCierre)],
            ]}
          />
        ) : (
          <p className="text-sm text-muted-foreground">
            Sin responsable asignado. Registra un seguimiento para definir la acción correctiva.
          </p>
        )}
      </DetailSection>

      <DetailSection title="Seguimiento">
        {incidencia.seguimiento.length === 0 ? (
          <p className="text-sm text-muted-foreground">Aún no hay avances registrados.</p>
        ) : (
          <ol className="space-y-3 border-l pl-4">
            {[...incidencia.seguimiento].reverse().map((item, index) => (
              <li key={`${item.fecha}-${index}`} className="relative text-sm">
                <span aria-hidden className="absolute top-1.5 -left-[1.3rem] size-2 rounded-full bg-primary" />
                <p>{item.nota}</p>
                <p className="text-xs text-muted-foreground">
                  {item.usuario} · {fechaHora(item.fecha)}
                </p>
              </li>
            ))}
          </ol>
        )}
      </DetailSection>

      {incidencia.evidencias.length > 0 && (
        <DetailSection title="Evidencias">
          <ul className="space-y-2">
            {incidencia.evidencias.map((documento) => (
              <li key={documento.idDocumento} className="flex items-center gap-2 text-sm">
                <FileText className="size-4 shrink-0 text-muted-foreground" />
                <span className="min-w-0 flex-1 truncate">{documento.nombreArchivo}</span>
                <EstadoBadge mapa={ESTADO_DOCUMENTO} valor={documento.estado} />
              </li>
            ))}
          </ul>
        </DetailSection>
      )}
    </DetailSheet>
  )
}

export function IncidenciasTab() {
  const { hasPermission, hasAnyPermission } = useAuth()
  const puedeGestionar = hasPermission("INCIDENCIA_GESTIONAR")
  const puedeReportar = hasAnyPermission(["INCIDENCIA_GESTIONAR", "OBSERVACION_REGISTRAR"])
  const { data = [], isPending } = useQuery({ queryKey: ["incidencias"], queryFn: listarIncidencias })
  const { data: opcionesFormulario } = useQuery({ queryKey: ["incidencia-opciones"], queryFn: opcionesIncidencia })
  const [estado, setEstado] = useState(TODOS)
  const [tipo, setTipo] = useState(TODOS)
  const [severidad, setSeveridad] = useState(TODOS)
  const [busqueda, setBusqueda] = useState("")
  const [reportando, setReportando] = useState(false)
  const [idSeleccion, setIdSeleccion] = useState(null)
  const [dialogo, setDialogo] = useState(null)

  const seleccion = data.find((item) => item.idIncidencia === idSeleccion)
  const contar = (valor) => data.filter((item) => item.estado === valor).length
  const filtrados = data.filter(
    (item) =>
      pasaFiltro(estado, item.estado) &&
      pasaFiltro(tipo, item.tipo) &&
      pasaFiltro(severidad, item.severidad) &&
      coincide(busqueda, item.codigo, item.descripcion, item.referencia, item.responsable, item.area?.nombre)
  )

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
        <SegmentedFilter
          label="Estado de la incidencia"
          value={estado}
          onChange={setEstado}
          options={[
            { value: TODOS, label: "Todas", count: data.length },
            ...Object.entries(ESTADO_INCIDENCIA).map(([valor, item]) => ({
              value: valor,
              label: item.label,
              count: contar(valor),
            })),
          ]}
        />
        {puedeReportar && (
          <Button onClick={() => setReportando(true)} disabled={!opcionesFormulario}>
            <Plus />
            Reportar incidencia
          </Button>
        )}
      </div>
      <Toolbar>
        <SearchInput value={busqueda} onChange={setBusqueda} placeholder="Código, descripción o responsable" />
        <FilterSelect label="Tipo" value={tipo} onChange={setTipo} options={opciones(TIPO_INCIDENCIA)} />
        <FilterSelect label="Severidad" value={severidad} onChange={setSeveridad} options={opciones(SEVERIDAD)} todos="Todas" />
      </Toolbar>

      <TablePanel>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Código</TableHead>
              <TableHead>Tipo</TableHead>
              <TableHead>Severidad</TableHead>
              <TableHead>Área y registro</TableHead>
              <TableHead>Responsable</TableHead>
              <TableHead>Reportada</TableHead>
              <TableHead>Estado</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isPending && <LoadingRows colSpan={7} />}
            {!isPending && filtrados.length === 0 && (
              <EmptyRow colSpan={7}>Ninguna incidencia coincide con los filtros.</EmptyRow>
            )}
            {filtrados.map((incidencia) => (
              <TableRow
                key={incidencia.idIncidencia}
                className="cursor-pointer"
                onClick={() => setIdSeleccion(incidencia.idIncidencia)}
              >
                <TableCell>
                  <RowButton onClick={() => setIdSeleccion(incidencia.idIncidencia)}>{incidencia.codigo}</RowButton>
                </TableCell>
                <TableCell>
                  <div className="font-medium">{TIPO_INCIDENCIA[incidencia.tipo]}</div>
                  <div className="max-w-64 truncate text-xs text-muted-foreground">{incidencia.descripcion}</div>
                </TableCell>
                <TableCell>
                  <EstadoBadge mapa={SEVERIDAD} valor={incidencia.severidad} />
                </TableCell>
                <TableCell>
                  <div>{incidencia.area?.nombre}</div>
                  <div className="text-xs text-muted-foreground tabular-nums">{incidencia.referencia || "—"}</div>
                </TableCell>
                <TableCell className={incidencia.responsable ? undefined : "text-muted-foreground"}>
                  {incidencia.responsable ?? "Sin asignar"}
                </TableCell>
                <TableCell className="text-muted-foreground">{relativo(incidencia.fecha)}</TableCell>
                <TableCell>
                  <EstadoBadge mapa={ESTADO_INCIDENCIA} valor={incidencia.estado} />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TablePanel>

      {reportando && opcionesFormulario && (
        <ReportarIncidenciaDialog opcionesIncidencia={opcionesFormulario} onClose={() => setReportando(false)} />
      )}
      {seleccion && (
        <IncidenciaDetalle
          incidencia={seleccion}
          puedeGestionar={puedeGestionar}
          onClose={() => setIdSeleccion(null)}
          onDialog={setDialogo}
        />
      )}
      {dialogo === "seguimiento" && seleccion && opcionesFormulario && (
        <SeguimientoDialog
          incidencia={seleccion}
          responsables={opcionesFormulario.responsables}
          onClose={() => setDialogo(null)}
        />
      )}
      {dialogo === "cerrar" && seleccion && (
        <CerrarIncidenciaDialog incidencia={seleccion} onClose={() => setDialogo(null)} />
      )}
    </div>
  )
}
