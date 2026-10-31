import { Link } from "react-router-dom"
import { ChevronRight, CircleCheck } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { useAuth } from "@/features/auth/AuthContext"

const TAREAS = [
  { clave: "porValidar", permiso: "GENERACION_VALIDAR", to: "/generacion", texto: ["registro por validar", "registros por validar"] },
  { clave: "porCorregir", permiso: "GENERACION_REGISTRAR", to: "/generacion", texto: ["registro observado por corregir", "registros observados por corregir"] },
  { clave: "porAprobar", permiso: "RECOLECCION_PROGRAMAR", to: "/recolecciones", texto: ["orden por aprobar", "órdenes por aprobar"] },
  { clave: "porSalida", permiso: "TRASLADO_REGISTRAR", to: "/recolecciones", texto: ["orden lista para salida", "órdenes listas para salida"] },
  { clave: "porRecibir", permiso: "RECEPCION_REGISTRAR", to: "/recolecciones", texto: ["orden en tránsito por recibir", "órdenes en tránsito por recibir"] },
  { clave: "porCerrar", permiso: "RECOLECCION_PROGRAMAR", to: "/recolecciones", texto: ["orden recibida por cerrar", "órdenes recibidas por cerrar"] },
  { clave: "documentosPorRevisar", permiso: "GENERACION_VALIDAR", to: "/documentos", texto: ["documento por revisar", "documentos por revisar"] },
  { clave: "incidenciasSinResponsable", permiso: "INCIDENCIA_GESTIONAR", to: "/incidencias", texto: ["incidencia sin responsable", "incidencias sin responsable"] },
]

export function MisPendientes({ pendientes }) {
  const { hasPermission } = useAuth()
  const tareas = TAREAS.filter((tarea) => hasPermission(tarea.permiso) && pendientes[tarea.clave] > 0)
  const aplica = TAREAS.some((tarea) => hasPermission(tarea.permiso))

  if (!aplica) return null

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">Mis pendientes</CardTitle>
      </CardHeader>
      <CardContent>
        {tareas.length === 0 ? (
          <p className="flex items-center gap-2 text-sm text-muted-foreground">
            <CircleCheck className="size-4 text-primary" />
            No tienes tareas pendientes.
          </p>
        ) : (
          <ul className="-mx-2">
            {tareas.map((tarea) => {
              const valor = pendientes[tarea.clave]
              return (
                <li key={tarea.clave}>
                  <Link
                    to={tarea.to}
                    className="flex items-center gap-3 rounded-lg px-2 py-2 text-sm outline-none hover:bg-muted focus-visible:ring-2 focus-visible:ring-ring"
                  >
                    <span className="flex min-w-7 items-center justify-center rounded-md bg-primary/10 px-1.5 py-0.5 font-heading text-base font-semibold text-primary tabular-nums">
                      {valor}
                    </span>
                    <span className="flex-1">{tarea.texto[valor === 1 ? 0 : 1]}</span>
                    <ChevronRight className="size-4 text-muted-foreground" />
                  </Link>
                </li>
              )
            })}
          </ul>
        )}
      </CardContent>
    </Card>
  )
}
