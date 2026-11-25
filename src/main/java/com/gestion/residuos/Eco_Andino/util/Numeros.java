package com.gestion.residuos.Eco_Andino.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Utilidades numéricas y de formato para cantidades. */
public final class Numeros {
    private static final DecimalFormatSymbols SIMBOLOS = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-PE"));

    private Numeros() {
    }

    public static double valor(BigDecimal numero) {
        return numero == null ? 0 : numero.doubleValue();
    }

    public static BigDecimal redondear(double numero) {
        return BigDecimal.valueOf(numero).setScale(2, RoundingMode.HALF_UP);
    }

    /** Porcentaje entero (0 si el total es 0), igual que el frontend. */
    public static int porcentaje(double parte, double total) {
        return total > 0 ? (int) Math.round(parte / total * 100) : 0;
    }

    /** "1 250,5 kg": cantidad con hasta 2 decimales y su unidad. */
    public static String cantidad(double numero, String unidad) {
        DecimalFormat formato = new DecimalFormat("#,##0.##", SIMBOLOS);
        String texto = formato.format(numero);
        return unidad == null || unidad.isBlank() ? texto : texto + " " + unidad;
    }
}
