package com.gestion.residuos.Eco_Andino.exception;

public class DemasiadosIntentosException extends RuntimeException {
    public DemasiadosIntentosException() {
        super("Demasiados intentos fallidos");
    }
}
