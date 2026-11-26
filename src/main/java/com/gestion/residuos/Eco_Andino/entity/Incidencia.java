package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incidencia")
@Getter
@Setter
public class Incidencia {
    public static final String ABIERTA = "ABIERTA";
    public static final String EN_SEGUIMIENTO = "EN_SEGUIMIENTO";
    public static final String CERRADA = "CERRADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incidencia")
    private Integer idIncidencia;

    @Column(unique = true, length = 20)
    private String codigo;

    // DERRAME / MEZCLA / ENVASE_DANADO / RETRASO / RECHAZO / OTRO
    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_area", nullable = false)
    private AreaGeneradora area;

    // Registro relacionado (orden, contenedor o generación), opcional
    @Column(length = 30)
    private String referencia;

    // ALTA / MEDIA / BAJA
    @Column(nullable = false, length = 10)
    private String severidad;

    @Column(nullable = false, length = 15)
    private String estado = ABIERTA;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reportado_por", nullable = false)
    private Usuario reportadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable")
    private Usuario responsable;

    @Column(name = "accion_correctiva", length = 1000)
    private String accionCorrectiva;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @OneToMany(mappedBy = "incidencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha")
    private List<SeguimientoIncidencia> seguimiento = new ArrayList<>();

    public void agregarSeguimiento(Usuario usuario, String nota, LocalDateTime fecha) {
        SeguimientoIncidencia item = new SeguimientoIncidencia();
        item.setIncidencia(this);
        item.setUsuario(usuario);
        item.setNota(nota);
        item.setFecha(fecha);
        seguimiento.add(item);
    }
}
