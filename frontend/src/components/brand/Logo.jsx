import { useId } from "react"
import { cn } from "@/lib/utils"

export function LogoMark({ className }) {
  const gradientId = `ea-${useId().replace(/:/g, "")}`

  return (
    <svg
      viewBox="0 0 48 48"
      role="img"
      aria-label="EcoAndina"
      className={className}
    >
      <defs>
        <linearGradient
          id={gradientId}
          x1="4"
          y1="2"
          x2="44"
          y2="46"
          gradientUnits="userSpaceOnUse"
        >
          <stop offset="0" stopColor="#2f9e66" />
          <stop offset="1" stopColor="#17908a" />
        </linearGradient>
      </defs>
      <rect width="48" height="48" rx="12" fill={`url(#${gradientId})`} />
      <path
        d="M37.93 14.25A17 17 0 1 1 19.6 7.58"
        fill="none"
        stroke="#fff"
        strokeOpacity=".92"
        strokeWidth="2.6"
        strokeLinecap="round"
      />
      <path
        d="M-3.4-3.8 4.2 0-3.4 3.8Z"
        fill="#f0be5a"
        transform="translate(19.6 7.58) rotate(-15)"
      />
      <path d="M12 31.5 20 16l5.6 9.2 4.4-4.7 6 11Z" fill="#fff" />
    </svg>
  )
}

export function BrandLockup({ inverse = false, className }) {
  return (
    <div className={cn("flex items-center gap-3", className)}>
      <LogoMark className="size-9 shrink-0" />
      <div className="leading-none">
        <div
          className={cn(
            "font-heading text-[1.4rem] font-semibold tracking-tight",
            inverse ? "text-white" : "text-foreground"
          )}
        >
          EcoAndina
        </div>
        <div
          className={cn(
            "mt-1 text-[0.62rem] font-medium tracking-[0.16em] uppercase",
            inverse ? "text-white/60" : "text-muted-foreground"
          )}
        >
          Gestión de residuos
        </div>
      </div>
    </div>
  )
}
