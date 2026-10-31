package com.gestion.residuos.Eco_Andino.dto;

import com.gestion.residuos.Eco_Andino.entity.EstadoIncidencia;
import com.gestion.residuos.Eco_Andino.entity.Incidencia;
import com.gestion.residuos.Eco_Andino.entity.Severidad;
import com.gestion.residuos.Eco_Andino.entity.TipoIncidencia;

import java.time.LocalDateTime;

public record IncidenciaResponse(
        Integer id,
        TipoIncidencia tipo,
        String descripcion,
        AreaResponse area,
        LocalDateTime fecha,
        Severidad severidad,
        UsuarioResumen responsable,
        EstadoIncidencia estado,
        String accionCorrectiva,
        LocalDateTime fechaCierre) {

    // Requiere area y responsable ya cargados (ver IncidenciaRepository)
    public static IncidenciaResponse from(Incidencia i) {
        return new IncidenciaResponse(i.getIdIncidencia(), i.getTipo(), i.getDescripcion(),
                AreaResponse.from(i.getArea()), i.getFecha(), i.getSeveridad(),
                UsuarioResumen.from(i.getResponsable()), i.getEstado(), i.getAccionCorrectiva(), i.getFechaCierre());
    }
}
