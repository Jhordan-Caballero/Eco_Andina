import { Checkbox } from "@/components/ui/checkbox"
import {
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import { cn } from "@/lib/utils"

export function TextField({ control, name, label, description, className, ...inputProps }) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem className={className}>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            <Input {...inputProps} {...field} value={field.value ?? ""} />
          </FormControl>
          {description && <FormDescription>{description}</FormDescription>}
          <FormMessage />
        </FormItem>
      )}
    />
  )
}

export function NumberField({ control, name, label, description, suffix, className, ...inputProps }) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem className={className}>
          <FormLabel>{label}</FormLabel>
          <div className="relative">
            <FormControl>
              <Input
                type="number"
                inputMode="decimal"
                step="any"
                className={cn(suffix && "pr-14")}
                {...inputProps}
                {...field}
                value={field.value ?? ""}
              />
            </FormControl>
            {suffix && (
              <span className="pointer-events-none absolute top-1/2 right-3 -translate-y-1/2 text-sm text-muted-foreground">
                {suffix}
              </span>
            )}
          </div>
          {description && <FormDescription>{description}</FormDescription>}
          <FormMessage />
        </FormItem>
      )}
    />
  )
}

export function SelectField({
  control,
  name,
  label,
  description,
  placeholder = "Selecciona una opción",
  options,
  disabled,
  className,
  onValueChange,
}) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem className={className}>
          <FormLabel>{label}</FormLabel>
          <Select
            value={field.value ? String(field.value) : ""}
            onValueChange={(valor) => {
              field.onChange(valor)
              onValueChange?.(valor)
            }}
            disabled={disabled}
          >
            <FormControl>
              <SelectTrigger className="w-full" onBlur={field.onBlur}>
                <SelectValue placeholder={placeholder} />
              </SelectTrigger>
            </FormControl>
            <SelectContent>
              {options.map((option) => (
                <SelectItem key={option.value} value={String(option.value)} disabled={option.disabled}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {description && <FormDescription>{description}</FormDescription>}
          <FormMessage />
        </FormItem>
      )}
    />
  )
}

export function TextareaField({ control, name, label, description, className, ...props }) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem className={className}>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            <Textarea rows={3} {...props} {...field} value={field.value ?? ""} />
          </FormControl>
          {description && <FormDescription>{description}</FormDescription>}
          <FormMessage />
        </FormItem>
      )}
    />
  )
}

export function FileField({ control, name, label, description, accept, className }) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem className={className}>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            <Input
              type="file"
              accept={accept}
              name={field.name}
              onBlur={field.onBlur}
              onChange={(event) => field.onChange(event.target.files?.[0]?.name ?? "")}
              className="cursor-pointer file:mr-3 file:cursor-pointer"
            />
          </FormControl>
          {description && <FormDescription>{description}</FormDescription>}
          <FormMessage />
        </FormItem>
      )}
    />
  )
}

export function CheckboxField({ control, name, label, description, className }) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem className={cn("flex items-start gap-3", className)}>
          <FormControl>
            <Checkbox
              checked={Boolean(field.value)}
              onCheckedChange={(valor) => field.onChange(valor === true)}
              className="mt-0.5"
            />
          </FormControl>
          <div className="grid gap-1">
            <FormLabel className="font-normal">{label}</FormLabel>
            {description && <FormDescription>{description}</FormDescription>}
          </div>
        </FormItem>
      )}
    />
  )
}

export function CheckboxListField({ control, name, label, description, options, className }) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => {
        const seleccion = field.value ?? []
        const alternar = (valor, marcado) =>
          field.onChange(marcado ? [...seleccion, valor] : seleccion.filter((item) => item !== valor))
        return (
          <FormItem className={className}>
            <FormLabel>{label}</FormLabel>
            {description && <FormDescription>{description}</FormDescription>}
            <div className="grid gap-2 rounded-lg border p-3">
              {options.map((option) => (
                <label
                  key={option.value}
                  className={cn(
                    "flex cursor-pointer items-start gap-3 text-sm",
                    option.disabled && "cursor-not-allowed opacity-50"
                  )}
                >
                  <Checkbox
                    checked={seleccion.includes(option.value)}
                    disabled={option.disabled}
                    onCheckedChange={(valor) => alternar(option.value, valor === true)}
                    className="mt-0.5"
                  />
                  <span className="grid gap-0.5">
                    <span className="font-medium">{option.label}</span>
                    {option.description && (
                      <span className="text-xs text-muted-foreground">{option.description}</span>
                    )}
                  </span>
                </label>
              ))}
            </div>
            <FormMessage />
          </FormItem>
        )
      }}
    />
  )
}
