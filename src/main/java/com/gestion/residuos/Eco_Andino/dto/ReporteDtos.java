package com.gestion.residuos.Eco_Andino.dto;

import com.gestion.residuos.Eco_Andino.dto.GeneracionDtos.GeneracionVista;

import java.util.List;

/** DTOs de reportes e indicadores (CU19) y su exportación (CU20). */
public final class ReporteDtos {
    private ReporteDtos() {
    }

    public record Kpis(double totalKg, long registros, int tasaValidacion, long validadas, int trazabilidad,
                       long cerradas, long conRecepcion, int peligrosos, long incidencias,
                       long incidenciasAbiertas) {
    }

    public record Serie(String clave, String label, double valor) {
    }

    public record Mensual(String clave, String mes, double valor, long registros, boolean parcial) {
    }

    public record Reporte(String periodo, Kpis kpis, List<Serie> porArea, List<Mensual> mensual,
                          List<Serie> destinos, List<Serie> incidenciasPorTipo,
                          List<GeneracionVista> generaciones) {
    }

    public record Exportacion(String nombreArchivo, String contenido) {
    }
}
