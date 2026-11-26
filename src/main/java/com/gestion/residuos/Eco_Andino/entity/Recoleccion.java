package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Orden de recolección. Ciclo: PENDIENTE -> PROGRAMADA (aprobación) -> EN_TRANSITO (salida)
 * -> RECIBIDA (recepción y destino) -> CERRADA. La recepción y el destino viven en la misma tabla
 * (decisión 5 del modelo entidad-relación).
 */
@Entity
@Table(name = "recoleccion")
@Getter
@Setter
public class Recoleccion {
    public static final String PENDIENTE = "PENDIENTE";
    public static final String PROGRAMADA = "PROGRAMADA";
    public static final String EN_TRANSITO = "EN_TRANSITO";
    public static final String RECIBIDA = "RECIBIDA";
    public static final String CERRADA = "CERRADA";

    /** Estados en los que el residuo ya salió de la planta (deja de ocupar el contenedor). */
    public static final List<String> RETIRADA = List.of(EN_TRANSITO, RECIBIDA, CERRADA);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recoleccion")
    private Integer idRecoleccion;

    @Column(name = "numero_orden", unique = true, length = 20)
    private String numeroOrden;

    @Column(nullable = false, length = 15)
    private String estado = PENDIENTE;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitado_por", nullable = false)
    private Usuario solicitadoPor;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDate fechaProgramada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_transportista", nullable = false)
    private Transportista transportista;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_gestor", nullable = false)
    private Gestor gestor;

    @Column(length = 500)
    private String observaciones;

    @OneToMany(mappedBy = "recoleccion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("idDetalle")
    private List<DetalleRecoleccion> detalles = new ArrayList<>();

    // Aprobación (CU12)
    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aprobado_por")
    private Usuario aprobadoPor;

    // Salida y traslado (CU14)
    @Column(name = "fecha_salida")
    private LocalDateTime fechaSalida;

    @Column(name = "documento_salida", length = 40)
    private String documentoSalida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_salida_por")
    private Usuario salidaPor;

    @Column(name = "observaciones_salida", length = 500)
    private String observacionesSalida;

    // Recepción y destino (CU15)
    @Column(name = "fecha_recepcion")
    private LocalDateTime fechaRecepcion;

    @Column(name = "cantidad_recibida", precision = 10, scale = 2)
    private BigDecimal cantidadRecibida;

    @Column(name = "recepcion_conforme")
    private Boolean recepcionConforme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recibido_por")
    private Usuario recibidoPor;

    @Column(name = "observaciones_recepcion", length = 500)
    private String observacionesRecepcion;

    // VALORIZACION / TRATAMIENTO / DISPOSICION
    @Column(name = "tipo_destino", length = 20)
    private String tipoDestino;

    @Column(name = "tratamiento_destino", length = 150)
    private String tratamientoDestino;

    // Cierre
    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cerrado_por")
    private Usuario cerradoPor;

    public void agregarDetalle(Generacion generacion) {
        DetalleRecoleccion detalle = new DetalleRecoleccion();
        detalle.setRecoleccion(this);
        detalle.setGeneracion(generacion);
        detalles.add(detalle);
    }
}
