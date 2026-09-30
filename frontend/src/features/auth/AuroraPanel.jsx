import { ArrowRight } from "lucide-react"
import { cn } from "@/lib/utils"
import "./aurora.css"

const CADENA = ["Generación", "Almacenamiento", "Recolección", "Destino"]

export function AuroraPanel({ className }) {
  return (
    <div className={cn("aurora", className)}>
      <div aria-hidden="true">
        <span className="aurora__blob aurora__blob--ichu" />
        <span className="aurora__blob aurora__blob--laguna" />
        <span className="aurora__blob aurora__blob--cielo" />
        <span className="aurora__blob aurora__blob--ocre" />
        <span className="aurora__blob aurora__blob--nevado" />
        <div className="aurora__contours" />
        <div className="aurora__scrim" />
        <div className="aurora__grain" />
      </div>

      <div className="relative z-10 flex h-full w-full flex-col justify-end gap-6 p-12 text-white xl:p-16">
        <p className="text-xs font-medium tracking-[0.2em] text-white/70 uppercase">
          Trazabilidad de residuos industriales
        </p>
        <h2 className="max-w-md font-heading text-5xl leading-[1.02] font-semibold tracking-tight xl:text-6xl">
          Cada residuo, del área generadora a su destino final.
        </h2>
        <ol className="flex flex-wrap items-center gap-x-3 gap-y-2 text-sm text-white/80">
          {CADENA.map((paso, index) => (
            <li key={paso} className="flex items-center gap-3">
              {index > 0 && <ArrowRight className="size-3.5 text-white/40" />}
              {paso}
            </li>
          ))}
        </ol>
      </div>
    </div>
  )
}
