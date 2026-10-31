package com.gestion.residuos.Eco_Andino.dto;

import com.gestion.residuos.Eco_Andino.entity.Severidad;
import com.gestion.residuos.Eco_Andino.entity.TipoIncidencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// CU17, paso 4 y 5: tipo, descripción, área, fecha, gravedad, responsable y (opcional) acción correctiva
public record IncidenciaRequest(
        @NotNull(message = "El tipo es obligatorio") TipoIncidencia tipo,
        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 2000, message = "La descripción admite hasta 2000 caracteres") String descripcion,
        @NotNull(message = "El área es obligatoria") Integer areaId,
        @NotNull(message = "La fecha es obligatoria") LocalDateTime fecha,
        @NotNull(message = "La gravedad es obligatoria") Severidad severidad,
        @NotNull(message = "El responsable es obligatorio") Integer responsableId,
        @Size(max = 500, message = "La acción correctiva admite hasta 500 caracteres") String accionCorrectiva) {
}
