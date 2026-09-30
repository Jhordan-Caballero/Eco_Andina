import { useState } from "react"
import { NavLink, Outlet, useLocation } from "react-router-dom"
import { LogOut, Menu } from "lucide-react"
import { BrandLockup } from "@/components/brand/Logo"
import { Button } from "@/components/ui/button"
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet"
import { useAuth } from "@/features/auth/AuthContext"
import { cn } from "@/lib/utils"
import { NAV_ITEMS } from "./navigation"

function SidebarContent({ onNavigate }) {
  const { hasPermission } = useAuth()
  const items = NAV_ITEMS.filter(
    (item) => !item.permission || hasPermission(item.permission)
  )

  return (
    <div className="flex h-full flex-col bg-sidebar text-sidebar-foreground">
      <div className="px-5 py-6">
        <BrandLockup inverse />
      </div>

      <nav aria-label="Módulos" className="flex-1 space-y-1 px-3">
        {items.map(({ to, end, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            onClick={onNavigate}
            className={({ isActive }) =>
              cn(
                "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors outline-none focus-visible:ring-2 focus-visible:ring-sidebar-ring",
                isActive
                  ? "bg-sidebar-accent text-sidebar-accent-foreground"
                  : "text-sidebar-foreground/75 hover:bg-sidebar-accent/60 hover:text-sidebar-accent-foreground"
              )
            }
          >
            {({ isActive }) => (
              <>
                <Icon
                  className={cn("size-[18px]", isActive && "text-sidebar-primary")}
                />
                {label}
              </>
            )}
          </NavLink>
        ))}
      </nav>

      <p className="px-5 py-4 text-xs text-sidebar-foreground/50">
        Curso Integrador · Ingeniería de Sistemas
      </p>
    </div>
  )
}

function Topbar({ onOpenMenu }) {
  const { user, signOut } = useAuth()
  const { pathname } = useLocation()

  const current =
    NAV_ITEMS.find((item) =>
      item.end ? pathname === item.to : pathname.startsWith(item.to)
    ) ?? NAV_ITEMS[0]

  const primerNombre = user.nombres.split(" ")[0]
  const primerApellido = user.apellidos.split(" ")[0]
  const iniciales = `${primerNombre[0]}${primerApellido[0]}`.toUpperCase()
  const roles = user.roles.map((rol) => rol.nombre).join(" · ")

  return (
    <header className="sticky top-0 z-20 flex items-center gap-3 border-b bg-background/85 px-4 py-3 backdrop-blur md:px-8">
      <Button
        variant="ghost"
        size="icon"
        className="lg:hidden"
        aria-label="Abrir menú"
        onClick={onOpenMenu}
      >
        <Menu />
      </Button>

      <div className="min-w-0 flex-1">
        <h1 className="truncate font-heading text-2xl leading-none font-semibold tracking-tight md:text-[1.75rem]">
          {current.title}
        </h1>
        <p className="mt-1 hidden truncate text-sm text-muted-foreground sm:block">
          {current.subtitle}
        </p>
      </div>

      <div className="flex items-center gap-2.5">
        <span
          aria-hidden="true"
          className="flex size-9 items-center justify-center rounded-lg bg-accent text-sm font-semibold text-accent-foreground"
        >
          {iniciales}
        </span>
        <div className="hidden leading-tight sm:block">
          <div className="text-sm font-medium">
            {primerNombre} {primerApellido}
          </div>
          <div className="text-xs text-muted-foreground">{roles}</div>
        </div>
      </div>

      <Button
        variant="ghost"
        size="icon"
        aria-label="Cerrar sesión"
        title="Cerrar sesión"
        onClick={signOut}
      >
        <LogOut />
      </Button>
    </header>
  )
}

export default function AppLayout() {
  const [menuOpen, setMenuOpen] = useState(false)

  return (
    <div className="flex min-h-svh">
      <aside className="sticky top-0 hidden h-svh w-64 shrink-0 lg:block">
        <SidebarContent />
      </aside>

      <Sheet open={menuOpen} onOpenChange={setMenuOpen}>
        <SheetContent
          side="left"
          showCloseButton={false}
          className="w-72 border-0 bg-sidebar p-0 text-sidebar-foreground"
        >
          <SheetHeader className="sr-only">
            <SheetTitle>Menú</SheetTitle>
            <SheetDescription>Navegación entre los módulos del sistema</SheetDescription>
          </SheetHeader>
          <SidebarContent onNavigate={() => setMenuOpen(false)} />
        </SheetContent>
      </Sheet>

      <div className="flex min-w-0 flex-1 flex-col">
        <Topbar onOpenMenu={() => setMenuOpen(true)} />
        <main className="flex-1 p-4 md:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
