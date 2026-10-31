package com.gestion.residuos.Eco_Andino.service.reporte;

import java.time.LocalDate;

/** Filtros que el usuario puede aplicar a un reporte; cada reporte usa solo los que soporta. */
public record FiltroReporte(LocalDate desde, LocalDate hasta, Integer areaId, String estado) {
}
