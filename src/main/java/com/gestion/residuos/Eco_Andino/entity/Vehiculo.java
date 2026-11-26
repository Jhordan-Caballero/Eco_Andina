package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "vehiculo")
@Getter
@Setter
public class Vehiculo {
    public static final String OPERATIVO = "OPERATIVO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Integer idVehiculo;

    @Column(nullable = false, unique = true, length = 15)
    private String placa;

    @Column(length = 50)
    private String tipo;

    @Column(precision = 10, scale = 2)
    private BigDecimal capacidad;

    // OPERATIVO / EN_MANTENIMIENTO / INACTIVO
    @Column(nullable = false, length = 20)
    private String estado = OPERATIVO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_transportista", nullable = false)
    private Transportista transportista;
}
