package com.gestion.residuos.Eco_Andino.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** DTOs de documentos y evidencias (CU16). */
public final class DocumentoDtos {
    private DocumentoDtos() {
    }

    public record DocumentoDto(Integer idDocumento, String tipo, String nombreArchivo, String referencia,
                               String descripcion, LocalDateTime fecha, String subidoPor, String estado,
                               String motivoRechazo, boolean tieneArchivo) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DocumentoRequest(
            @NotBlank(message = "El tipo de documento es obligatorio") String tipo,
            @NotBlank(message = "La referencia es obligatoria") @Size(max = 30) String referencia,
            @Size(max = 255) String descripcion) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RechazoRequest(
            @NotBlank(message = "Indica el motivo del rechazo") @Size(max = 255) String motivo) {
    }
}
