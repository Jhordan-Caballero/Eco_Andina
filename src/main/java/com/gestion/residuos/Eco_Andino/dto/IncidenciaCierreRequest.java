package com.gestion.residuos.Eco_Andino.dto;

import jakarta.validation.constraints.Size;

// CU17, paso 8: la acción correctiva final puede enviarse aquí o haberse registrado antes
public record IncidenciaCierreRequest(
        @Size(max = 500, message = "La acción correctiva admite hasta 500 caracteres") String accionCorrectiva) {
}
