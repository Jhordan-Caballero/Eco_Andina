package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "gestor")
@Getter
@Setter
public class Gestor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gestor")
    private Integer idGestor;

    @Column(nullable = false, length = 120)
    private String empresa;

    @Column(nullable = false, unique = true, length = 60)
    private String autorizacion;

    @Column(name = "proceso_destino", length = 150)
    private String procesoDestino;

    @Column(nullable = false)
    private LocalDate vigencia;
}
