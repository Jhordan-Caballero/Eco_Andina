package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Observación operativa (CU18) del generador, almacenero, transportista o gestor. */
@Entity
@Table(name = "observacion")
@Getter
@Setter
public class Observacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_observacion")
    private Integer idObservacion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_autor", nullable = false)
    private Usuario autor;

    // Rol con el que se registró (nombre para mostrar)
    @Column(nullable = false, length = 60)
    private String rol;

    // GENERACION / ALMACENAMIENTO / TRASLADO / RECEPCION
    @Column(nullable = false, length = 20)
    private String ambito;

    @Column(length = 30)
    private String referencia;

    @Column(nullable = false, length = 1000)
    private String texto;
}
