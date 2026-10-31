package com.gestion.residuos.Eco_Andino.dto;

import com.gestion.residuos.Eco_Andino.entity.ConfiguracionAuditoria;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Solo se pueden cambiar parámetros opcionales: las acciones obligatorias (RF016) no se desactivan
public record ConfiguracionAuditoriaRequest(
        @NotNull(message = "Los días de retención son obligatorios")
        @Min(value = ConfiguracionAuditoria.RETENCION_MINIMA_DIAS, message = "La retención mínima es de 30 días")
        @Max(value = ConfiguracionAuditoria.RETENCION_MAXIMA_DIAS, message = "La retención máxima es de 3650 días")
        Integer retencionDias,
        @NotNull(message = "Indica si se registran las creaciones") Boolean registrarCreaciones,
        @NotNull(message = "Indica si se registran las exportaciones") Boolean registrarExportaciones) {
}
