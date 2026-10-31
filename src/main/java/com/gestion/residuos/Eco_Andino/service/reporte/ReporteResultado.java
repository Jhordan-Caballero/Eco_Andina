package com.gestion.residuos.Eco_Andino.service.reporte;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Reporte ya calculado: tabla de datos + indicadores. Es lo que se muestra (CU19) y lo que se exporta (CU20). */
public record ReporteResultado(String codigo, String nombre, LocalDateTime generadoEn, Map<String, String> filtros,
                               List<String> columnas, List<List<String>> filas, List<Indicador> indicadores) {
}
