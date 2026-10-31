import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"

export function SegmentedFilter({ value, onChange, options, label }) {
  return (
    <Tabs value={value} onValueChange={onChange} aria-label={label} className="max-w-full">
      <TabsList className="h-auto max-w-full flex-wrap justify-start">
        {options.map((option) => (
          <TabsTrigger key={option.value} value={option.value} className="flex-none px-2.5">
            {option.label}
            {option.count != null && (
              <span className="rounded-full bg-foreground/8 px-1.5 text-xs tabular-nums">
                {option.count}
              </span>
            )}
          </TabsTrigger>
        ))}
      </TabsList>
    </Tabs>
  )
}
