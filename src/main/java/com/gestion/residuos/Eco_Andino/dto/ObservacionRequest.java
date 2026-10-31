package com.gestion.residuos.Eco_Andino.dto;

import com.gestion.residuos.Eco_Andino.entity.TipoRegistro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ObservacionRequest(
        @NotNull(message = "El tipo de registro es obligatorio") TipoRegistro tipoRegistro,
        @NotNull(message = "El registro es obligatorio")
        @Positive(message = "El número de registro debe ser mayor que cero") Integer idRegistro,
        @NotBlank(message = "Ingresa la descripción de la observación")
        @Size(max = 500, message = "La observación admite hasta 500 caracteres") String descripcion) {
}
