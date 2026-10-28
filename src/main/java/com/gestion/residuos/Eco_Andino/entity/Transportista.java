package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "transportista")
public class Transportista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTransportista;

    @Column(nullable = false, length = 100)
    private String empresa;

    @Column(length = 50)
    private String autorizacion;

    @Column(length = 100)
    private String contacto;

    private LocalDate vigencia;

    @OneToMany(mappedBy = "transportista")
    private List<Vehiculo> vehiculos;
}