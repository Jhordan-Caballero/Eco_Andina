package com.gestion.residuos.Eco_Andino.monitoreo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Datos de la aplicación en /actuator/info: nombre, zona horaria, versión de Java y tiempo encendida.
 * La versión y la fecha de compilación las agrega Maven (build-info) en la sección "build".
 */
@Component
public class EcoAndinaInfoContributor implements InfoContributor {
    private final String zonaHoraria;

    public EcoAndinaInfoContributor(@Value("${app.timezone:America/Lima}") String zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Duration encendida = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("nombre", "EcoAndina - Gestión de residuos industriales");
        app.put("zonaHoraria", zonaHoraria);
        app.put("java", System.getProperty("java.version"));
        app.put("encendidaDesde", String.format("%dd %dh %dm", encendida.toDays(), encendida.toHoursPart(),
                encendida.toMinutesPart()));
        builder.withDetail("aplicacion", app);
    }
}
