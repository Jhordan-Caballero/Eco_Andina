import { Inbox } from "lucide-react"
import { TableCell, TableRow } from "@/components/ui/table"

export function EmptyState({ icon: Icon = Inbox, title = "Sin resultados", children }) {
  return (
    <div className="flex flex-col items-center gap-2 px-4 py-10 text-center">
      <Icon className="size-8 text-muted-foreground/60" />
      <p className="font-medium">{title}</p>
      {children && <p className="max-w-sm text-sm text-muted-foreground">{children}</p>}
    </div>
  )
}

export function EmptyRow({ colSpan, title, children }) {
  return (
    <TableRow className="hover:bg-transparent">
      <TableCell colSpan={colSpan} className="whitespace-normal">
        <EmptyState title={title}>{children}</EmptyState>
      </TableCell>
    </TableRow>
  )
}
