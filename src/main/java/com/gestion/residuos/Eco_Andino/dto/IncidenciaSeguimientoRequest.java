package com.gestion.residuos.Eco_Andino.dto;

import com.gestion.residuos.Eco_Andino.entity.EstadoIncidencia;
import com.gestion.residuos.Eco_Andino.entity.Severidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// CU17, paso 7: actualizar estado y acciones realizadas. El cierre tiene su propia operación.
public record IncidenciaSeguimientoRequest(
        @NotNull(message = "El estado es obligatorio") EstadoIncidencia estado,
        @NotNull(message = "La gravedad es obligatoria") Severidad severidad,
        @NotNull(message = "El responsable es obligatorio") Integer responsableId,
        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 2000, message = "La descripción admite hasta 2000 caracteres") String descripcion,
        @Size(max = 500, message = "La acción correctiva admite hasta 500 caracteres") String accionCorrectiva) {
}
