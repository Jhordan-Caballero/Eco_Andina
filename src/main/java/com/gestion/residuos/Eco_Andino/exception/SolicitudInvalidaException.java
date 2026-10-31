package com.gestion.residuos.Eco_Andino.exception;

/** Datos válidos en formato pero que incumplen una regla de negocio (HTTP 400). */
public class SolicitudInvalidaException extends RuntimeException {
    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
