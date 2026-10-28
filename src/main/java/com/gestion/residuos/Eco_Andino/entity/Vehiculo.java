package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "vehiculo")
public class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idVehiculo;

    @Column(unique = true, nullable = false, length = 15)
    private String placa;

    @Column(length = 50)
    private String tipo;

    @Column(precision = 10, scale = 2)
    private BigDecimal capacidad;

    @Column(length = 20)
    private String estado;

    @ManyToOne
    @JoinColumn(name = "id_transportista")
    private Transportista transportista;
}
