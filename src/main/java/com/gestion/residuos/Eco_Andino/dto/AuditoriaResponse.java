package com.gestion.residuos.Eco_Andino.dto;

import com.gestion.residuos.Eco_Andino.entity.AccionAuditoria;
import com.gestion.residuos.Eco_Andino.entity.Auditoria;

import java.time.LocalDateTime;

public record AuditoriaResponse(Integer id, AccionAuditoria accion, String entidad, Integer idRegistro,
                                LocalDateTime fecha, String detalle, UsuarioResumen usuario) {

    public static AuditoriaResponse from(Auditoria a) {
        return new AuditoriaResponse(a.getIdAuditoria(), a.getAccion(), a.getEntidad(), a.getIdRegistro(),
                a.getFecha(), a.getDetalle(), UsuarioResumen.from(a.getUsuario()));
    }
}
