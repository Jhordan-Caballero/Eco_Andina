package com.gestion.residuos.Eco_Andino.service.reporte;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Punto de extensión de los reportes. Para sumar uno nuevo (por ejemplo, cantidades generadas cuando exista el
 * CU08) basta una clase @Component que implemente esta interfaz: aparece sola en el catálogo, se puede generar
 * y se puede exportar, sin tocar el controlador ni el servicio.
 */
public interface ReporteGenerador {
    String codigo();

    String nombre();

    String descripcion();

    /** Filtros que entiende: "periodo", "area", "estado". */
    List<String> filtros();

    ReporteResultado generar(FiltroReporte filtro, LocalDateTime ahora);
}
