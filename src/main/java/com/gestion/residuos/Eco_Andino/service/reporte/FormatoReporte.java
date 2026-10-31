package com.gestion.residuos.Eco_Andino.service.reporte;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class FormatoReporte {
    static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private FormatoReporte() {
    }

    static String fecha(LocalDateTime fecha) {
        return fecha == null ? "" : fecha.format(FECHA_HORA);
    }

    static String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }
}
