package com.gestion.residuos.Eco_Andino.util;

import java.util.LinkedHashMap;
import java.util.Map;

/** Textos para mostrar de los códigos de estado y tipo (los mismos que usa el frontend en estados.js). */
public final class Etiquetas {
    private Etiquetas() {
    }

    public static final Map<String, String> TIPO_DOCUMENTO = mapa(
            "MANIFIESTO", "Manifiesto de residuos peligrosos",
            "GUIA_REMISION", "Guía de remisión",
            "ACTA_RECEPCION", "Acta de recepción",
            "CERTIFICADO", "Certificado de tratamiento o disposición",
            "EVIDENCIA_FOTO", "Fotografía de evidencia",
            "OTRO", "Otro documento");

    public static final Map<String, String> TIPO_DESTINO = mapa(
            "VALORIZACION", "Valorización",
            "TRATAMIENTO", "Tratamiento",
            "DISPOSICION", "Disposición final");

    public static final Map<String, String> TIPO_INCIDENCIA = mapa(
            "DERRAME", "Derrame",
            "MEZCLA", "Mezcla de residuos",
            "ENVASE_DANADO", "Envase dañado",
            "RETRASO", "Retraso en recolección",
            "RECHAZO", "Rechazo en recepción");

    public static final Map<String, String> AMBITO_OBSERVACION = mapa(
            "GENERACION", "Generación",
            "ALMACENAMIENTO", "Almacenamiento",
            "TRASLADO", "Traslado",
            "RECEPCION", "Recepción");

    public static final Map<String, String> CATEGORIA_RESIDUO = mapa(
            "COMUN", "Común",
            "APROVECHABLE", "Aprovechable",
            "PELIGROSO", "Peligroso",
            "ESPECIAL", "Especial / biocontaminado");

    public static final Map<String, String> ESTADO_GENERACION = mapa(
            "PENDIENTE", "Pendiente",
            "VALIDADA", "Validada",
            "OBSERVADA", "Observada");

    public static final Map<String, String> ESTADO_RECOLECCION = mapa(
            "PENDIENTE", "Pendiente",
            "PROGRAMADA", "Programada",
            "EN_TRANSITO", "En tránsito",
            "RECIBIDA", "Recibida",
            "CERRADA", "Cerrada");

    public static String de(Map<String, String> mapa, String codigo) {
        return codigo == null ? "" : mapa.getOrDefault(codigo, codigo);
    }

    private static Map<String, String> mapa(String... pares) {
        Map<String, String> mapa = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            mapa.put(pares[i], pares[i + 1]);
        }
        return Map.copyOf(mapa);
    }
}
