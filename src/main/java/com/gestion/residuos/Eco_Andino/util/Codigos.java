package com.gestion.residuos.Eco_Andino.util;

/** Códigos legibles de los registros, derivados de su id (GEN-0101, MOV-0201, OR-1037, INC-0001). */
public final class Codigos {
    private Codigos() {
    }

    public static String generacion(int id) {
        return String.format("GEN-%04d", 100 + id);
    }

    public static String movimiento(int id) {
        return String.format("MOV-%04d", 200 + id);
    }

    public static String orden(int id) {
        return "OR-" + (1036 + id);
    }

    public static String incidencia(int id) {
        return String.format("INC-%04d", id);
    }
}
