import { Link } from "react-router-dom"
import { Badge } from "@/components/ui/badge"
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
import { cn } from "@/lib/utils"

const ESTADO_STYLES = {
  Pendiente: "bg-secondary text-secondary-foreground",
  Programada: "border-primary/40 bg-primary/5 text-primary",
  "En tránsito": "bg-info/10 text-info",
  Recibida: "bg-primary/10 text-primary",
  Cerrada: "bg-muted text-muted-foreground",
}

export function EstadoBadge({ estado }) {
  return <Badge className={cn(ESTADO_STYLES[estado])}>{estado}</Badge>
}

export function OrdenesRecientes({ ordenes }) {
  const { hasPermission } = useAuth()

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">Órdenes recientes</CardTitle>
        {hasPermission("RECOLECCION_CONSULTAR") && (
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
                <TableCell className="font-medium tabular-nums">{orden.codigo}</TableCell>
                <TableCell>{orden.area}</TableCell>
                <TableCell>
                  <div>{orden.residuo}</div>
                  <div className="text-xs text-muted-foreground tabular-nums">
                    {orden.cantidad}
                  </div>
                </TableCell>
                <TableCell>{orden.transportista}</TableCell>
                <TableCell>
                  <EstadoBadge estado={orden.estado} />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  )
}
