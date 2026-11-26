package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Registro de generación incluido en una orden. Cada generación pertenece a una sola orden. */
@Entity
@Table(name = "detalle_recoleccion")
@Getter
@Setter
public class DetalleRecoleccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_recoleccion", nullable = false)
    private Recoleccion recoleccion;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_generacion", nullable = false, unique = true)
    private Generacion generacion;
}
