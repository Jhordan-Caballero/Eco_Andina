package com.gestion.residuos.Eco_Andino.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.Locale;

/**
 * Utilidades de texto basadas en Apache Commons (lang3, text, io).
 * Centraliza la normalización de entradas y las defensas contra inyección en logs y hojas de cálculo.
 */
public final class TextoUtil {
    private static final int MAX_LOG = 100;

    private TextoUtil() {
    }

    /** Quita espacios sobrantes; devuelve null si queda vacío (commons-lang3). */
    public static String normalizar(String valor) {
        return StringUtils.trimToNull(valor);
    }

    /** Código en mayúsculas y sin espacios alrededor: "res-001 " -> "RES-001". */
    public static String normalizarCodigo(String valor) {
        String limpio = StringUtils.trimToNull(valor);
        return limpio == null ? null : limpio.toUpperCase(Locale.ROOT);
    }

    /** "BIO_CONTAMINADO" -> "Bio Contaminado" (commons-text). Para mostrar en reportes. */
    public static String etiqueta(String valor) {
        if (StringUtils.isBlank(valor)) {
            return "";
        }
        return WordUtils.capitalizeFully(valor.trim().replace('_', ' '));
    }

    /**
     * Evita la inyección de fórmulas (CSV/Excel injection): una celda que empieza con = + - @ se ejecutaría
     * como fórmula al abrir el archivo. Se antepone un apóstrofo para que se muestre como texto.
     */
    public static String neutralizarFormula(String valor) {
        if (StringUtils.isEmpty(valor)) {
            return "";
        }
        char primero = valor.charAt(0);
        if (primero == '=' || primero == '+' || primero == '-' || primero == '@' || primero == '\t' || primero == '\r') {
            return "'" + valor;
        }
        return valor;
    }

    /** Evita la inyección de logs: elimina saltos de línea y caracteres de control, y limita el largo. */
    public static String paraLog(String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = valor.replaceAll("[\\p{Cntrl}]", "_");
        return StringUtils.abbreviate(limpio, MAX_LOG);
    }

    /** Nombre de archivo seguro para la cabecera Content-Disposition (commons-io: descarta rutas). */
    public static String nombreArchivoSeguro(String nombre) {
        String soloNombre = FilenameUtils.getName(StringUtils.defaultString(nombre));
        String seguro = soloNombre.replaceAll("[^A-Za-z0-9._-]", "_");
        return StringUtils.defaultIfBlank(seguro, "reporte");
    }
}
