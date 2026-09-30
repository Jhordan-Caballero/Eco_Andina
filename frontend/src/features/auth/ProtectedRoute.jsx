import { Link, Navigate, Outlet, useLocation } from "react-router-dom"
import { LoaderCircle, ShieldAlert } from "lucide-react"
import { Button } from "@/components/ui/button"
import { useAuth } from "./AuthContext"

export function FullScreenLoader() {
  return (
    <div
      role="status"
      className="flex min-h-svh items-center justify-center gap-2 text-sm text-muted-foreground"
    >
      <LoaderCircle className="size-4 animate-spin" />
      Cargando…
    </div>
  )
}

export function ProtectedRoute() {
  const { status } = useAuth()
  const location = useLocation()

  if (status === "loading") return <FullScreenLoader />
  if (status === "anonymous") {
    return <Navigate to="/login" replace state={{ from: location }} />
  }
  return <Outlet />
}

export function RequirePermission({ permission, children }) {
  const { hasPermission } = useAuth()

  if (!hasPermission(permission)) {
    return (
      <div className="mx-auto flex max-w-md flex-col items-center gap-3 py-16 text-center">
        <ShieldAlert className="size-10 text-warning" />
        <h2 className="font-heading text-2xl font-semibold">Sin acceso a este módulo</h2>
        <p className="text-sm text-muted-foreground">
          Tu rol no incluye el permiso necesario. Si crees que es un error,
          contacta al administrador del sistema.
        </p>
        <Button asChild variant="outline" className="mt-2">
          <Link to="/">Volver al panel general</Link>
        </Button>
      </div>
    )
  }

  return children
}
