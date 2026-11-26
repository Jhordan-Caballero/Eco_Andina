package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Documento o evidencia (CU16). Se asocia al registro por su código (OR-1040, GEN-0112, INC-0031), así un
 * mismo módulo sirve para órdenes, generaciones e incidencias.
 */
@Entity
@Table(name = "documento")
@Getter
@Setter
public class Documento {
    public static final String PENDIENTE = "PENDIENTE";
    public static final String VALIDADO = "VALIDADO";
    public static final String RECHAZADO = "RECHAZADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Integer idDocumento;

    // MANIFIESTO / GUIA_REMISION / ACTA_RECEPCION / CERTIFICADO / EVIDENCIA_FOTO / OTRO
    @Column(nullable = false, length = 20)
    private String tipo;

    // Nombre original del archivo (solo para mostrar)
    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    // Nombre con el que se guardó en disco (aleatorio); null si solo se registró la referencia
    @Column(name = "ruta_archivo", length = 100)
    private String rutaArchivo;

    @Column(name = "tipo_contenido", length = 100)
    private String tipoContenido;

    @Column(nullable = false, length = 30)
    private String referencia;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_subido_por", nullable = false)
    private Usuario subidoPor;

    @Column(nullable = false, length = 15)
    private String estado = PENDIENTE;

    @Column(name = "motivo_rechazo", length = 255)
    private String motivoRechazo;
}
