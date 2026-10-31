package com.gestion.residuos.Eco_Andino.dto;

import com.gestion.residuos.Eco_Andino.entity.Observacion;
import com.gestion.residuos.Eco_Andino.entity.TipoRegistro;

import java.time.LocalDateTime;

public record ObservacionResponse(Integer id, TipoRegistro tipoRegistro, Integer idRegistro, String descripcion,
                                  LocalDateTime fecha, UsuarioResumen autor) {

    public static ObservacionResponse from(Observacion o) {
        return new ObservacionResponse(o.getIdObservacion(), o.getTipoRegistro(), o.getIdRegistro(),
                o.getDescripcion(), o.getFecha(), UsuarioResumen.from(o.getUsuario()));
    }
}
