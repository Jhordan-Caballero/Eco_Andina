package com.gestion.residuos.Eco_Andino.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** DTOs de los catálogos: residuos, áreas, contenedores, transportistas, vehículos y gestores (CU03–CU07). */
public final class CatalogoDtos {
    private CatalogoDtos() {
    }

    public record ResiduoDto(Integer idResiduo, String codigo, String nombre, String categoria, String peligrosidad,
                             String unidad, Integer plazoMaximoDias, String tratamiento, String requisitos) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResiduoRequest(
            @NotBlank(message = "El código es obligatorio") @Size(max = 20) String codigo,
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
            @NotBlank(message = "La categoría es obligatoria") String categoria,
            @NotBlank(message = "La peligrosidad es obligatoria") String peligrosidad,
            @NotBlank(message = "La unidad es obligatoria") String unidad,
            @NotNull(message = "El plazo máximo es obligatorio") @Min(value = 1, message = "El plazo debe ser de al menos 1 día") Integer plazoMaximoDias,
            @Size(max = 100) String tratamiento,
            @Size(max = 255) String requisitos) {
    }

    public record AreaDto(Integer idArea, String codigo, String nombre, String proceso, String responsable,
                          String estado, Long registros30) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AreaRequest(
            @NotBlank(message = "El código es obligatorio") @Size(max = 20) String codigo,
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
            @Size(max = 100) String proceso,
            String responsable,
            @NotBlank(message = "El estado es obligatorio") String estado) {
    }

    /** Contenedor tal como está guardado. */
    public record ContenedorBasico(Integer idContenedor, String codigo, String tipo, BigDecimal capacidad,
                                   Integer idResiduo, String ubicacion, String estado, BigDecimal stockInicial,
                                   LocalDateTime fechaStock) {
    }

    /** Contenedor con su ocupación, plazos y alertas calculados (almacenamiento temporal). */
    public record ContenedorVista(Integer idContenedor, String codigo, String tipo, BigDecimal capacidad,
                                  Integer idResiduo, String ubicacion, String estado, BigDecimal stockInicial,
                                  LocalDateTime fechaStock, ResiduoDto residuo, String unidad, double ocupacion,
                                  double disponible, int porcentaje, LocalDateTime fechaIngreso, long diasAlmacenado,
                                  Long plazoRestante, String situacion, boolean alertaCapacidad, boolean alertaPlazo) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContenedorRequest(
            @NotBlank(message = "El código es obligatorio") @Size(max = 20) String codigo,
            @NotBlank(message = "El tipo es obligatorio") @Size(max = 50) String tipo,
            @NotNull(message = "La capacidad es obligatoria") @DecimalMin(value = "0.01", message = "La capacidad debe ser mayor a 0") BigDecimal capacidad,
            @NotNull(message = "El residuo permitido es obligatorio") Integer idResiduo,
            @Size(max = 100) String ubicacion,
            @NotBlank(message = "El estado es obligatorio") String estado) {
    }

    public record VehiculoDto(Integer idVehiculo, String placa, String tipo, BigDecimal capacidad, String estado) {
    }

    public record TransportistaDto(Integer idTransportista, String empresa, String autorizacion, String contacto,
                                   LocalDate vigencia, List<VehiculoDto> vehiculos, String estadoVigencia,
                                   long ordenesActivas, long ordenesCerradas) {
    }

    public record GestorDto(Integer idGestor, String empresa, String autorizacion, String procesoDestino,
                            LocalDate vigencia, String estadoVigencia, long ordenesActivas, long ordenesCerradas) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TransportistaRequest(
            @NotBlank(message = "La empresa es obligatoria") @Size(max = 120) String empresa,
            @NotBlank(message = "La autorización es obligatoria") @Size(max = 60) String autorizacion,
            @Size(max = 120) String contacto,
            @NotNull(message = "La vigencia es obligatoria") LocalDate vigencia) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VehiculoRequest(
            @NotBlank(message = "La placa es obligatoria") @Size(max = 15) String placa,
            @Size(max = 50) String tipo,
            @DecimalMin(value = "0", message = "La capacidad no puede ser negativa") BigDecimal capacidad,
            @NotBlank(message = "El estado es obligatorio") String estado) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GestorRequest(
            @NotBlank(message = "La empresa es obligatoria") @Size(max = 120) String empresa,
            @NotBlank(message = "La autorización es obligatoria") @Size(max = 60) String autorizacion,
            @Size(max = 150) String procesoDestino,
            @NotNull(message = "La vigencia es obligatoria") LocalDate vigencia) {
    }
}
