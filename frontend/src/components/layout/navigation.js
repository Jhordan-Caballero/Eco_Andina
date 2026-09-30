import { LayoutDashboard, Shapes, Truck, Users, Warehouse } from "lucide-react"

// `permission`: código de permiso que el backend entrega en `user.permisos`.
export const NAV_ITEMS = [
  {
    to: "/",
    end: true,
    label: "Panel general",
    icon: LayoutDashboard,
    title: "Panel general",
    subtitle: "Trazabilidad y estado operativo de la planta",
  },
  {
    to: "/catalogos",
    label: "Catálogos",
    icon: Shapes,
    permission: "CATALOGO_CONSULTAR",
    title: "Catálogos",
    subtitle: "Residuos, áreas y contenedores registrados",
  },
  {
    to: "/almacenamiento",
    label: "Almacenamiento",
    icon: Warehouse,
    permission: "ALMACENAMIENTO_CONSULTAR",
    title: "Almacenamiento y movimientos",
    subtitle: "Capacidad de contenedores y trazabilidad de movimientos",
  },
  {
    to: "/recolecciones",
    label: "Recolecciones",
    icon: Truck,
    permission: "RECOLECCION_CONSULTAR",
    title: "Órdenes de recolección",
    subtitle: "Programación, transporte y recepción de residuos",
  },
  {
    to: "/transportistas-gestores",
    label: "Transportistas y gestores",
    icon: Users,
    permission: "TRANSPORTISTA_GESTOR_CONSULTAR",
    title: "Transportistas y gestores",
    subtitle: "Empresas autorizadas para transporte y disposición final",
  },
]
