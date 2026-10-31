import {
  Building2,
  ChartColumn,
  FileText,
  LayoutDashboard,
  Recycle,
  ScrollText,
  Shapes,
  TriangleAlert,
  Truck,
  UsersRound,
  Warehouse,
} from "lucide-react"

export const NAV_GROUPS = [
  {
    titulo: null,
    items: [
      {
        to: "/",
        end: true,
        label: "Panel general",
        icon: LayoutDashboard,
        title: "Panel general",
        subtitle: "Trazabilidad y estado operativo de la planta",
      },
    ],
  },
  {
    titulo: "Operación",
    items: [
      {
        to: "/generacion",
        label: "Generación",
        icon: Recycle,
        permisos: ["GENERACION_REGISTRAR", "GENERACION_VALIDAR"],
        title: "Generación y segregación",
        subtitle: "Registro en el área de origen y validación ambiental",
      },
      {
        to: "/almacenamiento",
        label: "Almacenamiento",
        icon: Warehouse,
        permisos: ["ALMACENAMIENTO_CONSULTAR", "ALMACENAMIENTO_GESTIONAR"],
        title: "Almacenamiento y movimientos",
        subtitle: "Capacidad, plazos de permanencia y trazabilidad interna",
      },
      {
        to: "/recolecciones",
        label: "Recolecciones",
        icon: Truck,
        permisos: ["RECOLECCION_CONSULTAR", "RECOLECCION_PROGRAMAR", "TRASLADO_REGISTRAR", "RECEPCION_REGISTRAR"],
        title: "Órdenes de recolección",
        subtitle: "Programación, traslado, recepción y cierre",
      },
      {
        to: "/incidencias",
        label: "Incidencias",
        icon: TriangleAlert,
        permisos: ["INCIDENCIA_GESTIONAR", "OBSERVACION_REGISTRAR"],
        title: "Incidencias y observaciones",
        subtitle: "Eventos operativos y acciones correctivas",
      },
      {
        to: "/documentos",
        label: "Documentos",
        icon: FileText,
        permisos: ["DOCUMENTO_GESTIONAR", "AUDITORIA_CONSULTAR"],
        title: "Documentos y evidencias",
        subtitle: "Manifiestos, guías, actas, certificados y fotografías",
      },
    ],
  },
  {
    titulo: "Catálogos",
    items: [
      {
        to: "/catalogos",
        label: "Residuos y contenedores",
        icon: Shapes,
        permisos: ["CATALOGO_CONSULTAR"],
        title: "Catálogos",
        subtitle: "Residuos, áreas generadoras y contenedores",
      },
      {
        to: "/transportistas-gestores",
        label: "Transportistas y gestores",
        icon: Building2,
        permisos: ["TRANSPORTISTA_GESTOR_CONSULTAR", "TRANSPORTISTA_GESTIONAR", "GESTOR_GESTIONAR"],
        title: "Transportistas y gestores",
        subtitle: "Empresas operadoras autorizadas (EO-RS) y su vigencia",
      },
    ],
  },
  {
    titulo: "Control y gestión",
    items: [
      {
        to: "/reportes",
        label: "Reportes",
        icon: ChartColumn,
        permisos: ["REPORTE_GENERAR"],
        title: "Reportes e indicadores",
        subtitle: "Generación, destino y cumplimiento ambiental",
      },
      {
        to: "/usuarios",
        label: "Usuarios y roles",
        icon: UsersRound,
        permisos: ["USUARIO_GESTIONAR"],
        title: "Usuarios, roles y permisos",
        subtitle: "Cuentas de acceso y control por rol",
      },
      {
        to: "/auditoria",
        label: "Auditoría",
        icon: ScrollText,
        permisos: ["AUDITORIA_CONSULTAR", "AUDITORIA_GESTIONAR"],
        title: "Auditoría",
        subtitle: "Bitácora de acciones y configuración de eventos",
      },
    ],
  },
]

export const NAV_ITEMS = NAV_GROUPS.flatMap((grupo) => grupo.items)
