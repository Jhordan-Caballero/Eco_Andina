import { Link } from "react-router-dom"
import { EstadoBadge } from "@/components/shared/StatusBadge"
import {
  Card,
  CardAction,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { useAuth } from "@/features/auth/AuthContext"
import { ESTADO_RECOLECCION } from "@/lib/estados"

const PERMISOS_RECOLECCION = ["RECOLECCION_CONSULTAR", "RECOLECCION_PROGRAMAR", "TRASLADO_REGISTRAR", "RECEPCION_REGISTRAR"]

export function OrdenesRecientes({ ordenes }) {
  const { hasAnyPermission } = useAuth()
  const puedeVer = hasAnyPermission(PERMISOS_RECOLECCION)

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">Órdenes recientes</CardTitle>
        {puedeVer && (
          <CardAction>
            <Link
              to="/recolecciones"
              className="text-sm font-medium text-primary underline-offset-4 hover:underline"
            >
              Ver todas
            </Link>
          </CardAction>
        )}
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Orden</TableHead>
              <TableHead>Área</TableHead>
              <TableHead>Residuo</TableHead>
              <TableHead>Transportista</TableHead>
              <TableHead>Estado</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {ordenes.map((orden) => (
              <TableRow key={orden.codigo}>
                <TableCell className="font-medium tabular-nums">
                  {puedeVer ? (
                    <Link
                      to={`/recolecciones?orden=${orden.codigo}`}
                      className="underline-offset-4 hover:underline"
                    >
                      {orden.codigo}
                    </Link>
                  ) : (
                    orden.codigo
                  )}
                </TableCell>
                <TableCell>{orden.area}</TableCell>
                <TableCell>
                  <div>{orden.residuo}</div>
                  <div className="text-xs text-muted-foreground tabular-nums">
                    {orden.cantidad}
                  </div>
                </TableCell>
                <TableCell>{orden.transportista}</TableCell>
                <TableCell>
                  <EstadoBadge mapa={ESTADO_RECOLECCION} valor={orden.estado} />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  )
}
