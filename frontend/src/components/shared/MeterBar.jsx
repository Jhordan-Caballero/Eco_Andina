import { cn } from "@/lib/utils"

export function MeterBar({ value, label, className }) {
  const ancho = Math.min(100, Math.max(0, value))
  return (
    <div
      role="progressbar"
      aria-label={label}
      aria-valuenow={value}
      aria-valuemin={0}
      aria-valuemax={100}
      className={cn("h-1.5 w-full overflow-hidden rounded-full bg-muted", className)}
    >
      <div
        className={cn(
          "h-full rounded-full",
          value >= 100 ? "bg-destructive" : value >= 85 ? "bg-warning" : "bg-primary"
        )}
        style={{ width: `${ancho}%` }}
      />
    </div>
  )
}
