import { cn } from "@/lib/utils"

function CornerMarks() {
  const mark =
    "pointer-events-none absolute text-[11px] leading-none text-muted-foreground/40 select-none"
  return (
    <span aria-hidden="true">
      <span className={cn(mark, "top-1.5 left-2")}>+</span>
      <span className={cn(mark, "top-1.5 right-2")}>+</span>
      <span className={cn(mark, "bottom-1.5 left-2")}>+</span>
      <span className={cn(mark, "right-2 bottom-1.5")}>+</span>
    </span>
  )
}

export function KpiCard({ label, value, suffix, description, tone, progress }) {
  return (
    <div className="relative rounded-xl bg-card p-5 ring-1 ring-foreground/10">
      <CornerMarks />
      <p className="text-[0.68rem] font-semibold tracking-[0.14em] text-muted-foreground uppercase">
        {label}
      </p>
      <p
        className={cn(
          "mt-3 font-heading text-5xl leading-none font-semibold tracking-tight tabular-nums",
          tone === "warning" && "text-warning"
        )}
      >
        {value}
        {suffix && <span className="text-3xl">{suffix}</span>}
      </p>
      {progress != null && (
        <div
          role="progressbar"
          aria-label={label}
          aria-valuenow={progress}
          aria-valuemin={0}
          aria-valuemax={100}
          className="mt-3 h-1.5 overflow-hidden rounded-full bg-muted"
        >
          <div
            className="h-full rounded-full bg-primary"
            style={{ width: `${progress}%` }}
          />
        </div>
      )}
      <p className="mt-3 text-sm text-muted-foreground">{description}</p>
    </div>
  )
}
