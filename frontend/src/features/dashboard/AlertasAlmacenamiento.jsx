import { Clock, TriangleAlert } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

const ICONOS = {
  capacidad: TriangleAlert,
  plazo: Clock,
}

export function AlertasAlmacenamiento({ alertas }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">Alertas de almacenamiento</CardTitle>
      </CardHeader>
      <CardContent>
        <ul>
          {alertas.map((alerta) => {
            const Icono = ICONOS[alerta.tipo]
            return (
              <li
                key={alerta.id}
                className="flex gap-3 border-t py-3 first:border-t-0 first:pt-0"
              >
                <span className="mt-0.5 flex size-8 shrink-0 items-center justify-center rounded-lg bg-warning/10 text-warning">
                  <Icono className="size-4" />
                </span>
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-medium">{alerta.titulo}</p>
                  <p className="text-xs text-muted-foreground">{alerta.detalle}</p>
                  {alerta.ocupacion != null && (
                    <div className="mt-2 flex items-center gap-2">
                      <div
                        role="progressbar"
                        aria-label={`Ocupación de ${alerta.titulo}`}
                        aria-valuenow={alerta.ocupacion}
                        aria-valuemin={0}
                        aria-valuemax={100}
                        className="h-1.5 flex-1 overflow-hidden rounded-full bg-muted"
                      >
                        <div
                          className="h-full rounded-full bg-warning"
                          style={{ width: `${alerta.ocupacion}%` }}
                        />
                      </div>
                      <span className="text-xs text-muted-foreground tabular-nums">
                        {alerta.ocupacion}%
                      </span>
                    </div>
                  )}
                </div>
              </li>
            )
          })}
        </ul>
      </CardContent>
    </Card>
  )
}
