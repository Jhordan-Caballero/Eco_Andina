// Datos simulados tomados de los mockups del equipo; se reemplazan por la API cuando exista.
export const resumenMock = {
  kpis: {
    trazabilidad: {
      valor: 92,
      descripcion: "Residuos con seguimiento completo generación → destino",
    },
    ordenesActivas: {
      valor: 3,
      descripcion: "Pendientes, programadas o en tránsito",
    },
    incidenciasAbiertas: {
      valor: 2,
      descripcion: "Registradas en el periodo, en revisión",
    },
    alertasCapacidad: {
      valor: 1,
      descripcion: "Contenedores sobre 85% de ocupación",
    },
  },
  ordenesRecientes: [
    {
      codigo: "OR-1042",
      area: "Mantenimiento",
      residuo: "Aceite usado",
      cantidad: "40 L",
      transportista: "Transportes Andina SAC",
      estado: "Pendiente",
    },
    {
      codigo: "OR-1041",
      area: "Producción",
      residuo: "Chatarra metálica",
      cantidad: "120 kg",
      transportista: "EcoTrans Perú EIRL",
      estado: "Programada",
    },
    {
      codigo: "OR-1040",
      area: "Producción",
      residuo: "Envases con residuo químico",
      cantidad: "18 unid",
      transportista: "Transportes Andina SAC",
      estado: "En tránsito",
    },
    {
      codigo: "OR-1039",
      area: "Almacén",
      residuo: "Guantes y EPP contaminado",
      cantidad: "60 kg",
      transportista: "Recolectora Sur SAC",
      estado: "Recibida",
    },
    {
      codigo: "OR-1038",
      area: "Oficinas",
      residuo: "Papel y cartón",
      cantidad: "35 kg",
      transportista: "EcoTrans Perú EIRL",
      estado: "Cerrada",
    },
  ],
  alertas: [
    {
      id: "capacidad-ct-104",
      tipo: "capacidad",
      titulo: "CT-104 · Mantenimiento",
      detalle: "Cilindro 200 L · Peligrosos",
      ocupacion: 92,
    },
    {
      id: "plazo-res-001",
      tipo: "plazo",
      titulo: "Aceite usado · CT-101",
      detalle: "Plazo máximo de almacenamiento vence en 3 días",
    },
  ],
}
