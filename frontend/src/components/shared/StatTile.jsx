import { cn } from "@/lib/utils"

const TONOS = {
  warning: "text-warning",
  danger: "text-destructive",
  primary: "text-primary",
}

export function StatTile({ label, value, hint, tone, icon: Icon }) {
  return (
    <div className="rounded-xl bg-card p-4 ring-1 ring-foreground/10">
      <div className="flex items-center justify-between gap-2">
        <p className="text-[0.68rem] font-semibold tracking-[0.14em] text-muted-foreground uppercase">
          {label}
        </p>
        {Icon && <Icon className="size-4 shrink-0 text-muted-foreground" />}
      </div>
      <p
        className={cn(
          "mt-2 font-heading text-4xl leading-none font-semibold tracking-tight",
          TONOS[tone]
        )}
      >
        {value}
      </p>
      {hint && <p className="mt-2 text-xs text-muted-foreground">{hint}</p>}
    </div>
  )
}

export function StatGrid({ children }) {
  return (
    <section aria-label="Indicadores" className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
      {children}
    </section>
  )
}
