import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"

const TONOS = {
  neutral: "bg-secondary text-secondary-foreground",
  primary: "border-primary/40 bg-primary/5 text-primary",
  info: "bg-info/10 text-info",
  success: "bg-primary/10 text-primary",
  warning: "bg-warning/10 text-warning",
  danger: "bg-destructive/10 text-destructive",
  muted: "bg-muted text-muted-foreground",
}

export function StatusBadge({ tone = "neutral", className, children }) {
  return <Badge className={cn(TONOS[tone], className)}>{children}</Badge>
}

export function EstadoBadge({ mapa, valor, className }) {
  const item = mapa[valor]
  if (!item) return <StatusBadge className={className}>{valor ?? "—"}</StatusBadge>
  return (
    <StatusBadge tone={item.tone} className={className}>
      {item.label}
    </StatusBadge>
  )
}
