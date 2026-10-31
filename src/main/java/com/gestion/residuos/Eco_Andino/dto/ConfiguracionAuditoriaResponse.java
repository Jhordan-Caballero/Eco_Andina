package com.gestion.residuos.Eco_Andino.dto;

import com.gestion.residuos.Eco_Andino.entity.ConfiguracionAuditoria;

import java.time.LocalDateTime;
import java.util.List;

public record ConfiguracionAuditoriaResponse(
        int retencionDias,
        boolean registrarCreaciones,
        boolean registrarExportaciones,
        List<String> accionesObligatorias,
        UsuarioResumen actualizadoPor,
        LocalDateTime fechaActualizacion) {

    public static ConfiguracionAuditoriaResponse from(ConfiguracionAuditoria c) {
        return new ConfiguracionAuditoriaResponse(c.getRetencionDias(), c.isRegistrarCreaciones(),
                c.isRegistrarExportaciones(), List.of("MODIFICAR", "VALIDAR", "CERRAR", "CONFIGURAR", "DEPURAR"),
                UsuarioResumen.from(c.getActualizadoPor()), c.getFechaActualizacion());
    }
}
