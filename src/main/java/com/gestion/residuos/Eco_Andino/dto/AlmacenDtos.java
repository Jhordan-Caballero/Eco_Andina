package com.gestion.residuos.Eco_Andino.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.ContenedorVista;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.ResiduoDto;
import com.gestion.residuos.Eco_Andino.dto.GeneracionDtos.GeneracionVista;
import com.gestion.residuos.Eco_Andino.dto.IncidenciaDtos.ObservacionDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** DTOs del almacenamiento temporal y los movimientos (CU10 y CU11). */
public final class AlmacenDtos {
    private AlmacenDtos() {
    }

    public record MovimientoDto(Integer idMovimiento, String codigo, String tipo, LocalDateTime fecha,
                                Integer idResiduo, String origen, String destino, BigDecimal cantidad,
                                BigDecimal ajuste, String referencia, String usuario, String motivo,
                                boolean afectaStock, ResiduoDto residuo) {
    }

    public record ContenedorDetalle(@JsonUnwrapped ContenedorVista contenedor, List<GeneracionVista> lotes,
                                    List<MovimientoDto> movimientos, List<ObservacionDto> observaciones) {
    }

    /** tipo TRASLADO (de idOrigen a idDestino) o PESAJE (verificación del peso de idOrigen). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MovimientoRequest(
            @NotBlank(message = "El tipo de movimiento es obligatorio") String tipo,
            @NotNull(message = "El contenedor es obligatorio") Integer idOrigen,
            Integer idDestino,
            @NotNull(message = "La cantidad es obligatoria") @DecimalMin(value = "0", message = "La cantidad no puede ser negativa") BigDecimal cantidad,
            @NotBlank(message = "El motivo es obligatorio") @Size(max = 200) String motivo) {
    }
}
