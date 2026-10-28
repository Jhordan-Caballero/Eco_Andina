package com.gestion.residuos.Eco_Andino.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GeneracionRequest(
        @NotNull Integer idArea,
        @NotNull Integer idResiduo,
        @NotNull Integer idContenedor,
        @NotNull @DecimalMin(value = "0.01") BigDecimal cantidad,
        @NotNull LocalDateTime fecha,
        @NotNull Integer idResponsable,
        @NotBlank String condicionEnvase,
        String evidenciaUrl
) {
}
