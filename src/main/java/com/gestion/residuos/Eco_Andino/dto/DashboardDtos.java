package com.gestion.residuos.Eco_Andino.dto;

import java.util.List;

/** DTOs del panel general. */
public final class DashboardDtos {
    private DashboardDtos() {
    }

    public record Kpi(long valor, String descripcion) {
    }

    public record Kpis(Kpi trazabilidad, Kpi ordenesActivas, Kpi incidenciasAbiertas, Kpi alertasCapacidad) {
    }

    public record OrdenReciente(String codigo, String area, String residuo, String cantidad, String transportista,
                                String estado) {
    }

    public record Alerta(String id, String tipo, String titulo, String detalle, Integer ocupacion) {
    }

    public record Pendientes(long porValidar, long porCorregir, long porAprobar, long porSalida, long porRecibir,
                             long porCerrar, long documentosPorRevisar, long incidenciasSinResponsable) {
    }

    public record Resumen(Kpis kpis, List<OrdenReciente> ordenesRecientes, List<Alerta> alertas,
                          Pendientes pendientes) {
    }
}
