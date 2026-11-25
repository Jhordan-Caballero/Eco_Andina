package com.gestion.residuos.Eco_Andino.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

/** DTOs de la consulta (CU21) y configuración (CU22) de la auditoría. */
public final class AuditoriaDtos {
    private AuditoriaDtos() {
    }

    public record AuditoriaDto(Integer idAuditoria, LocalDateTime fecha, String usuario, String accion,
                               String entidad, String registro, String detalle) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Eventos(boolean validaciones, boolean modificaciones, boolean cierres, boolean accesos) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConfiguracionDto(
            @NotNull(message = "Indica los eventos a auditar") @Valid Eventos eventos,
            @NotBlank(message = "Indica la retención") @Pattern(regexp = "\\d{1,4}", message = "La retención debe ser un número de días") String retencionDias) {
    }
}
