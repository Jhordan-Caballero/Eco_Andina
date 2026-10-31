import { useState } from "react"
import { ChartColumnBig, Table2 } from "lucide-react"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import { porcentaje } from "@/lib/formato"
import { cn } from "@/lib/utils"

const SERIES = ["var(--chart-1)", "var(--chart-2)", "var(--chart-3)", "var(--chart-4)", "var(--chart-5)"]

const marca =
  "outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-1 focus-visible:ring-offset-card"

function pasoRedondo(valor) {
  if (valor <= 0) return 1
  const potencia = 10 ** Math.floor(Math.log10(valor))
  return [1, 2, 2.5, 5, 10].map((factor) => factor * potencia).find((paso) => paso >= valor)
}

export function ChartCard({ title, description, table, children, className }) {
  const [vista, setVista] = useState("grafico")
  return (
    <section className={cn("flex flex-col rounded-xl bg-card p-4 ring-1 ring-foreground/10", className)}>
      <header className="mb-4 flex items-start justify-between gap-3">
        <div className="min-w-0">
          <h3 className="font-heading text-lg leading-tight font-semibold">{title}</h3>
          {description && <p className="mt-0.5 text-xs text-muted-foreground">{description}</p>}
        </div>
        <Tabs value={vista} onValueChange={setVista} aria-label={`Vista de ${title}`}>
          <TabsList>
            <TabsTrigger value="grafico" aria-label="Ver gráfico" className="px-2">
              <ChartColumnBig />
            </TabsTrigger>
            <TabsTrigger value="tabla" aria-label="Ver tabla" className="px-2">
              <Table2 />
            </TabsTrigger>
          </TabsList>
        </Tabs>
      </header>
      <div className="flex-1">{vista === "grafico" ? children : table}</div>
    </section>
  )
}

export function DataTable({ columnas, filas }) {
  return (
    <table className="w-full text-sm">
      <thead>
        <tr className="border-b text-left text-xs text-muted-foreground">
          {columnas.map((columna, index) => (
            <th key={columna} className={cn("py-1.5 font-medium", index > 0 && "text-right")}>
              {columna}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {filas.map((fila) => (
          <tr key={fila[0]} className="border-b last:border-0">
            {fila.map((celda, index) => (
              <td key={index} className={cn("py-1.5", index > 0 && "text-right tabular-nums")}>
                {celda}
              </td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  )
}

export function BarList({ datos, formato, color = SERIES[0], vacio = "Sin datos en el periodo." }) {
  const maximo = Math.max(0, ...datos.map((item) => item.valor))
  if (datos.length === 0 || maximo === 0) {
    return <p className="py-8 text-center text-sm text-muted-foreground">{vacio}</p>
  }
  return (
    <ul className="space-y-1">
      {datos.map((item) => (
        <li key={item.clave}>
          <Tooltip>
            <TooltipTrigger asChild>
              <div
                tabIndex={0}
                className={cn(
                  "grid grid-cols-[minmax(0,11rem)_minmax(0,1fr)_auto] items-center gap-3 rounded-md px-1 py-1 hover:bg-muted/60",
                  marca
                )}
              >
                <span className="truncate text-sm">{item.label}</span>
                <span className="flex h-5 items-center border-l border-foreground/25">
                  <span
                    className="block h-full rounded-r-[4px]"
                    style={{
                      width: item.valor > 0 ? `max(2px, ${(item.valor / maximo) * 100}%)` : 0,
                      backgroundColor: color,
                    }}
                  />
                </span>
                <span className="min-w-14 text-right text-sm text-muted-foreground tabular-nums">{formato(item.valor)}</span>
              </div>
            </TooltipTrigger>
            <TooltipContent side="top">
              {item.label}: {formato(item.valor)}
            </TooltipContent>
          </Tooltip>
        </li>
      ))}
    </ul>
  )
}

export function ColumnChart({ datos, formato, formatoEje, etiqueta, color = SERIES[0] }) {
  const maximo = pasoRedondo(Math.max(0, ...datos.map((item) => item.valor)) / 4) * 4
  const marcas = [0, 0.25, 0.5, 0.75, 1]
  const textura = `repeating-linear-gradient(135deg, ${color} 0 3px, transparent 3px 6px)`
  const hayParcial = datos.some((item) => item.parcial)

  return (
    <div className="space-y-3">
      <div className="grid grid-cols-[auto_minmax(0,1fr)] gap-x-2">
        <div className="relative h-52 w-10" aria-hidden>
          {marcas.map((fraccion) => (
            <span
              key={fraccion}
              className="absolute right-0 translate-y-1/2 text-[0.68rem] text-muted-foreground tabular-nums"
              style={{ bottom: `${fraccion * 100}%` }}
            >
              {formatoEje(fraccion * maximo)}
            </span>
          ))}
        </div>
        <div className="relative h-52 border-b border-foreground/30">
          {marcas.slice(1).map((fraccion) => (
            <span
              key={fraccion}
              aria-hidden
              className="absolute inset-x-0 border-t border-foreground/8"
              style={{ bottom: `${fraccion * 100}%` }}
            />
          ))}
          <div className="relative flex h-full items-end">
            {datos.map((item) => (
              <Tooltip key={item.clave}>
                <TooltipTrigger asChild>
                  <div
                    tabIndex={0}
                    className={cn("flex h-full flex-1 items-end justify-center rounded-t-md hover:bg-muted/50", marca)}
                  >
                    <span
                      className="block w-6 rounded-t-[4px]"
                      style={{
                        height: `${(item.valor / maximo) * 100}%`,
                        background: item.parcial ? textura : color,
                        boxShadow: item.parcial ? `inset 0 0 0 1.5px ${color}` : undefined,
                      }}
                    />
                  </div>
                </TooltipTrigger>
                <TooltipContent side="top">
                  {etiqueta(item)}: {formato(item.valor)}
                  {item.parcial && " (mes en curso)"}
                </TooltipContent>
              </Tooltip>
            ))}
          </div>
        </div>
        <span />
        <div className="flex pt-1.5">
          {datos.map((item) => (
            <span key={item.clave} className="flex-1 text-center text-xs text-muted-foreground capitalize">
              {etiqueta(item)}
            </span>
          ))}
        </div>
      </div>
      {hayParcial && (
        <p className="flex items-center gap-2 text-xs text-muted-foreground">
          <span aria-hidden className="size-3 rounded-[3px]" style={{ background: textura, boxShadow: `inset 0 0 0 1.5px ${color}` }} />
          Mes en curso, dato parcial
        </p>
      )}
    </div>
  )
}

export function ShareBar({ datos, formato, colores = SERIES }) {
  const total = datos.reduce((suma, item) => suma + item.valor, 0)
  return (
    <div className="space-y-4">
      <div className="flex h-6 w-full gap-0.5">
        {datos.map((item, index) => (
          <Tooltip key={item.clave}>
            <TooltipTrigger asChild>
              <span
                tabIndex={0}
                className={cn("h-full min-w-1 first:rounded-l-[4px] last:rounded-r-[4px]", marca)}
                style={{ flex: `${item.valor} 1 0%`, backgroundColor: colores[index] }}
              />
            </TooltipTrigger>
            <TooltipContent side="top">
              {item.label}: {formato(item.valor)} ({porcentaje(item.valor, total)}%)
            </TooltipContent>
          </Tooltip>
        ))}
      </div>
      <ul className="grid gap-3 sm:grid-cols-3">
        {datos.map((item, index) => (
          <li key={item.clave} className="flex items-start gap-2">
            <span aria-hidden className="mt-1 size-2.5 shrink-0 rounded-[3px]" style={{ backgroundColor: colores[index] }} />
            <div className="min-w-0">
              <p className="text-sm font-medium">{item.label}</p>
              <p className="text-xs text-muted-foreground tabular-nums">
                {formato(item.valor)} · {porcentaje(item.valor, total)}%
              </p>
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}
