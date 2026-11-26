package com.gestion.residuos.Eco_Andino.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.AreaDto;
import com.gestion.residuos.Eco_Andino.dto.DocumentoDtos.DocumentoDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/** DTOs de incidencias (CU17) y observaciones operativas (CU18). */
public final class IncidenciaDtos {
    private IncidenciaDtos() {
    }

    public record SeguimientoDto(LocalDateTime fecha, String usuario, String nota) {
    }

    public record IncidenciaDto(Integer idIncidencia, String codigo, String tipo, String descripcion,
                                Integer idArea, String referencia, String severidad, String estado,
                                LocalDateTime fecha, String reportadoPor, String responsable,
                                String accionCorrectiva, List<SeguimientoDto> seguimiento,
                                LocalDateTime fechaCierre, AreaDto area, List<DocumentoDto> evidencias) {
    }

    public record ObservacionDto(Integer idObservacion, LocalDateTime fecha, String autor, String rol,
                                 String ambito, String referencia, String texto) {
    }

    public record OpcionesIncidencia(List<AreaDto> areas, List<Opcion> referencias, List<String> responsables) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IncidenciaRequest(
            @NotBlank(message = "El tipo es obligatorio") String tipo,
            @NotBlank(message = "La descripción es obligatoria") @Size(max = 1000) String descripcion,
            @NotNull(message = "El área es obligatoria") Integer idArea,
            @Size(max = 30) String referencia,
            @NotBlank(message = "La severidad es obligatoria") String severidad) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SeguimientoRequest(
            @NotBlank(message = "El responsable es obligatorio") String responsable,
            @NotBlank(message = "La acción correctiva es obligatoria") @Size(max = 1000) String accionCorrectiva,
            @NotBlank(message = "La nota de seguimiento es obligatoria") @Size(max = 500) String nota) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CierreRequest(
            @NotBlank(message = "La nota de cierre es obligatoria") @Size(max = 400) String nota) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ObservacionRequest(
            @NotBlank(message = "El ámbito es obligatorio") String ambito,
            @Size(max = 30) String referencia,
            @NotBlank(message = "La observación no puede estar vacía") @Size(max = 1000) String texto) {
    }
}
