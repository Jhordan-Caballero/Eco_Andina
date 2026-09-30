package com.gestion.residuos.Eco_Andino.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

// errors (campo -> mensaje) solo se envía en errores de validación
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String message, Map<String, String> errors) {

    public ErrorResponse(String message) {
        this(message, null);
    }
}
