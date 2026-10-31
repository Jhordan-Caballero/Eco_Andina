package com.gestion.residuos.Eco_Andino.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    // Un único reloj para toda la aplicación: las fechas se guardan en la hora de la planta (no en UTC del
    // contenedor) y las pruebas pueden reemplazarlo por un reloj fijo
    @Bean
    public Clock clock(@Value("${app.timezone:America/Lima}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
