package com.gestion.residuos.Eco_Andino.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.AreaDto;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.ContenedorBasico;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.ContenedorVista;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.ResiduoDto;
import com.gestion.residuos.Eco_Andino.dto.DocumentoDtos.DocumentoDto;
import com.gestion.residuos.Eco_Andino.dto.IncidenciaDtos.ObservacionDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

/** DTOs de generación y segregación (CU08) y su validación (CU09). */
public final class GeneracionDtos {
    private GeneracionDtos() {
    }

    public record RecoleccionMini(Integer idRecoleccion, String numeroOrden, String estado) {
    }

    public record GeneracionVista(Integer idGeneracion, String codigo, Integer idArea, Integer idResiduo,
                                  Integer idContenedor, BigDecimal cantidad, LocalDateTime fecha, String responsable,
                                  String condicionEnvase, boolean segregacionCorrecta, String estadoValidacion,
                                  String observaciones, String evidencia, String validadoPor,
                                  LocalDateTime fechaValidacion, String observacionValidacion, AreaDto area,
                                  ResiduoDto residuo, String unidad, ContenedorBasico contenedor,
                                  RecoleccionMini recoleccion) {
    }

    public record GeneracionDetalle(@JsonUnwrapped GeneracionVista generacion,
                                    List<ObservacionDto> observacionesOperativas,
                                    List<DocumentoDto> documentos) {
    }

    public record OpcionesRegistro(List<AreaDto> areas, List<ResiduoDto> residuos,
                                   List<ContenedorVista> contenedores) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeneracionRequest(
            @NotNull(message = "El área es obligatoria") Integer idArea,
            @NotNull(message = "El residuo es obligatorio") Integer idResiduo,
            @NotNull(message = "El contenedor es obligatorio") Integer idContenedor,
            @NotNull(message = "La cantidad es obligatoria") @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0") BigDecimal cantidad,
            @NotNull(message = "La fecha es obligatoria") OffsetDateTime fecha,
            @NotBlank(message = "La condición del envase es obligatoria") String condicionEnvase,
            Boolean segregacionCorrecta,
            @Size(max = 500) String observaciones) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ObservarRequest(
            @NotBlank(message = "Debes indicar la observación") @Size(max = 500) String observacion) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CorreccionRequest(
            @NotNull(message = "La cantidad es obligatoria") @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0") BigDecimal cantidad,
            @NotBlank(message = "La condición del envase es obligatoria") String condicionEnvase,
            @Size(max = 500) String observaciones) {
    }
}
