import { Fragment } from "react"
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetFooter,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet"

export function DetailSheet({ open, onOpenChange, title, subtitle, badge, footer, children }) {
  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-full gap-0 data-[side=right]:sm:max-w-xl">
        <SheetHeader className="border-b pr-12">
          <div className="flex flex-wrap items-center gap-2">
            <SheetTitle className="font-heading text-2xl leading-none">{title}</SheetTitle>
            {badge}
          </div>
          {subtitle && <SheetDescription>{subtitle}</SheetDescription>}
        </SheetHeader>
        <div className="flex-1 space-y-6 overflow-y-auto p-4">{children}</div>
        {footer && (
          <SheetFooter className="flex-row flex-wrap justify-end border-t">{footer}</SheetFooter>
        )}
      </SheetContent>
    </Sheet>
  )
}

export function DetailSection({ title, action, children }) {
  return (
    <section>
      <div className="mb-2.5 flex items-center justify-between gap-2">
        <h3 className="text-[0.68rem] font-semibold tracking-[0.14em] text-muted-foreground uppercase">
          {title}
        </h3>
        {action}
      </div>
      {children}
    </section>
  )
}

export function DetailList({ items }) {
  return (
    <dl className="grid grid-cols-[minmax(7.5rem,auto)_minmax(0,1fr)] gap-x-4 gap-y-2 text-sm">
      {items.filter(Boolean).map(([termino, valor]) => (
        <Fragment key={termino}>
          <dt className="text-muted-foreground">{termino}</dt>
          <dd className="min-w-0 break-words">{valor ?? "—"}</dd>
        </Fragment>
      ))}
    </dl>
  )
}

export function Nota({ children }) {
  return (
    <p className="rounded-lg bg-muted/60 px-3 py-2 text-sm whitespace-pre-line">{children}</p>
  )
}
