package com.gestion.residuos.Eco_Andino.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.GestorDto;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.TransportistaDto;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.VehiculoDto;
import com.gestion.residuos.Eco_Andino.dto.DocumentoDtos.DocumentoDto;
import com.gestion.residuos.Eco_Andino.dto.GeneracionDtos.GeneracionVista;
import com.gestion.residuos.Eco_Andino.dto.IncidenciaDtos.IncidenciaDto;
import com.gestion.residuos.Eco_Andino.dto.IncidenciaDtos.ObservacionDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

/** DTOs de las órdenes de recolección: programación (CU12), consulta (CU13), salida (CU14) y recepción (CU15). */
public final class RecoleccionDtos {
    private RecoleccionDtos() {
    }

    public record Aprobacion(LocalDateTime fecha, String usuario) {
    }

    public record Salida(LocalDateTime fecha, String documento, String usuario, String observaciones) {
    }

    public record Recepcion(LocalDateTime fecha, BigDecimal cantidadRecibida, Boolean conforme, String usuario,
                            String observaciones) {
    }

    public record Destino(String tipo, String tratamiento) {
    }

    public record Cierre(LocalDateTime fecha, String usuario) {
    }

    public record Requisito(String clave, String label, boolean cumple) {
    }

    public record RecoleccionVista(Integer idRecoleccion, String numeroOrden, String estado,
                                   LocalDateTime fechaSolicitud, String solicitadoPor, LocalDate fechaProgramada,
                                   Integer idTransportista, Integer idVehiculo, Integer idGestor,
                                   String observaciones, Aprobacion aprobacion, Salida salida, Recepcion recepcion,
                                   Destino destino, Cierre cierre, List<GeneracionVista> detalles,
                                   List<String> residuos, List<String> areas, double cantidadTotal, String unidad,
                                   boolean peligroso, TransportistaDto transportista, VehiculoDto vehiculo,
                                   GestorDto gestor, List<DocumentoDto> documentos,
                                   List<Requisito> requisitosCierre) {
    }

    public record RecoleccionDetalle(@JsonUnwrapped RecoleccionVista orden,
                                     List<ObservacionDto> observacionesOperativas,
                                     List<IncidenciaDto> incidencias) {
    }

    public record OpcionesProgramacion(List<GeneracionVista> generaciones, List<TransportistaDto> transportistas,
                                       List<GestorDto> gestores) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProgramarRequest(
            @NotEmpty(message = "Selecciona al menos un registro de generación") List<Integer> idsGeneracion,
            @NotNull(message = "La fecha programada es obligatoria") LocalDate fechaProgramada,
            @NotNull(message = "El transportista es obligatorio") Integer idTransportista,
            @NotNull(message = "El vehículo es obligatorio") Integer idVehiculo,
            @NotNull(message = "El gestor es obligatorio") Integer idGestor,
            @Size(max = 500) String observaciones) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SalidaRequest(
            @NotNull(message = "La fecha de salida es obligatoria") OffsetDateTime fecha,
            @NotBlank(message = "El tipo de documento es obligatorio") String tipoDocumento,
            @NotBlank(message = "El número de documento es obligatorio") @Size(max = 40) String numeroDocumento,
            @Size(max = 500) String observaciones) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RecepcionRequest(
            @NotNull(message = "La fecha de recepción es obligatoria") OffsetDateTime fecha,
            @NotNull(message = "La cantidad recibida es obligatoria") @DecimalMin(value = "0.01", message = "La cantidad recibida debe ser mayor a 0") BigDecimal cantidadRecibida,
            Boolean conforme,
            @NotBlank(message = "El tipo de destino es obligatorio") String tipoDestino,
            @NotBlank(message = "El tratamiento es obligatorio") @Size(max = 150) String tratamiento,
            @Size(max = 500) String observaciones) {
    }
}
