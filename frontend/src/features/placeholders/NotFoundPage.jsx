import { Link } from "react-router-dom"
import { Button } from "@/components/ui/button"

export default function NotFoundPage() {
  return (
    <div className="mx-auto flex max-w-md flex-col items-center gap-3 py-16 text-center">
      <p className="font-heading text-6xl font-semibold text-muted-foreground/60">404</p>
      <h2 className="font-heading text-2xl font-semibold">Página no encontrada</h2>
      <p className="text-sm text-muted-foreground">
        La dirección que buscas no existe o fue movida.
      </p>
      <Button asChild variant="outline" className="mt-2">
        <Link to="/">Volver al panel general</Link>
      </Button>
    </div>
  )
}
