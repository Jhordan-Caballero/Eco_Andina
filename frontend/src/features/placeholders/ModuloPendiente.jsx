import { Construction } from "lucide-react"

export default function ModuloPendiente({ nombre }) {
  return (
    <div className="mx-auto flex max-w-md flex-col items-center gap-3 rounded-xl border border-dashed bg-card/60 px-6 py-16 text-center">
      <Construction className="size-9 text-muted-foreground" />
      <h2 className="font-heading text-2xl font-semibold">{nombre} · en desarrollo</h2>
      <p className="text-sm text-muted-foreground">
        Este módulo se implementará en las siguientes etapas del proyecto.
      </p>
    </div>
  )
}
