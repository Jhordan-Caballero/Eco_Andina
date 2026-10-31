package com.gestion.residuos.Eco_Andino.service.reporte;

import java.util.List;

/** Tipo de reporte que se muestra en el módulo. {@code disponible=false}: depende de un módulo aún sin implementar. */
public record ReporteTipo(String codigo, String nombre, String descripcion, boolean disponible, String nota,
                          List<String> filtros) {
}
