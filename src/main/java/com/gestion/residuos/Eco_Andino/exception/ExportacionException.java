package com.gestion.residuos.Eco_Andino.exception;

public class ExportacionException extends RuntimeException {
    public ExportacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
