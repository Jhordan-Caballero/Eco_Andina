package com.gestion.residuos.Eco_Andino.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Los códigos que ve el usuario se derivan del id y deben coincidir con los del frontend. */
class CodigosTest {

    @Test
    void generacion() {
        assertEquals("GEN-0101", Codigos.generacion(1));
        assertEquals("GEN-0115", Codigos.generacion(15));
    }

    @Test
    void movimiento() {
        assertEquals("MOV-0201", Codigos.movimiento(1));
    }

    @Test
    void orden() {
        assertEquals("OR-1037", Codigos.orden(1));
        assertEquals("OR-1043", Codigos.orden(7));
    }

    @Test
    void incidencia() {
        assertEquals("INC-0001", Codigos.incidencia(1));
    }
}
