package com.gestion.residuos.Eco_Andino.config;

import com.gestion.residuos.Eco_Andino.entity.Area_Generadora;
import com.gestion.residuos.Eco_Andino.repository.AreaGeneradoraRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Áreas de ejemplo para poder probar incidencias en desarrollo. Está APAGADO por defecto para no chocar con el
 * seed o el CRUD de áreas (CU04): actívalo con app.seed.areas-ejemplo=true y solo actúa si la tabla está vacía.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.areas-ejemplo", havingValue = "true")
public class AreaSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AreaSeeder.class);

    private final AreaGeneradoraRepository areaRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (areaRepository.count() > 0) {
            return;
        }
        String[][] areas = {
                {"AR-001", "Producción", "Fabricación y ensamblaje"},
                {"AR-002", "Mantenimiento", "Mantenimiento mecánico y eléctrico"},
                {"AR-003", "Almacén central", "Recepción y despacho de materiales"},
                {"AR-004", "Laboratorio de calidad", "Control y ensayos"},
                {"AR-005", "Planta de tratamiento", "Tratamiento de efluentes"}};
        for (String[] datos : areas) {
            Area_Generadora area = new Area_Generadora();
            area.setCodigo(datos[0]);
            area.setNombre(datos[1]);
            area.setProceso(datos[2]);
            areaRepository.save(area);
        }
        log.info("Seed: {} áreas de ejemplo creadas", areas.length);
    }
}
