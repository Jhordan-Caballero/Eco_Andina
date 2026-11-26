package com.gestion.residuos.Eco_Andino.monitoreo;

import com.gestion.residuos.Eco_Andino.entity.Generacion;
import com.gestion.residuos.Eco_Andino.entity.Incidencia;
import com.gestion.residuos.Eco_Andino.repository.GeneracionRepository;
import com.gestion.residuos.Eco_Andino.repository.IncidenciaRepository;
import com.gestion.residuos.Eco_Andino.repository.RecoleccionRepository;
import com.gestion.residuos.Eco_Andino.service.VistaService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Métricas propias del negocio, visibles en /actuator/metrics junto a las técnicas (memoria, CPU, peticiones):
 * - ecoandina.recolecciones.activas: órdenes que aún no se cierran.
 * - ecoandina.generaciones.pendientes: registros esperando validación del supervisor (CU09).
 * - ecoandina.incidencias.abiertas: incidencias sin cerrar (CU17).
 * Se calculan al consultarlas, no en cada petición.
 */
@Component
@RequiredArgsConstructor
public class MetricasNegocio implements MeterBinder {
    private final RecoleccionRepository recoleccionRepository;
    private final GeneracionRepository generacionRepository;
    private final IncidenciaRepository incidenciaRepository;

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("ecoandina.recolecciones.activas",
                        recoleccionRepository, repo -> repo.countByEstadoIn(VistaService.ORDENES_ACTIVAS))
                .description("Órdenes de recolección que aún no se cierran")
                .register(registry);
        Gauge.builder("ecoandina.generaciones.pendientes",
                        generacionRepository, repo -> repo.countByEstadoValidacion(Generacion.PENDIENTE))
                .description("Registros de generación pendientes de validación")
                .register(registry);
        Gauge.builder("ecoandina.incidencias.abiertas",
                        incidenciaRepository,
                        repo -> repo.countByEstadoIn(List.of(Incidencia.ABIERTA, Incidencia.EN_SEGUIMIENTO)))
                .description("Incidencias abiertas o en seguimiento")
                .register(registry);
    }
}
