import { TableCell, TableRow } from "@/components/ui/table"
import { cn } from "@/lib/utils"

export function TablePanel({ className, children }) {
  return (
    <div
      className={cn(
        "overflow-hidden rounded-xl bg-card ring-1 ring-foreground/10 [&_td:first-child]:pl-4 [&_td:last-child]:pr-4 [&_th:first-child]:pl-4 [&_th:last-child]:pr-4",
        className
      )}
    >
      {children}
    </div>
  )
}

export function LoadingRows({ colSpan, rows = 4 }) {
  return Array.from({ length: rows }, (_, index) => (
    <TableRow key={index} className="hover:bg-transparent">
      <TableCell colSpan={colSpan}>
        <div className="h-5 animate-pulse rounded-md bg-muted" />
      </TableCell>
    </TableRow>
  ))
}

export function RowButton({ onClick, children }) {
  return (
    <button
      type="button"
      onClick={(event) => {
        event.stopPropagation()
        onClick()
      }}
      className="rounded-sm font-medium tabular-nums underline-offset-4 outline-none hover:underline focus-visible:ring-2 focus-visible:ring-ring"
    >
      {children}
    </button>
  )
}
