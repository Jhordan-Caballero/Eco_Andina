package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "gestor")
public class Gestor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idGestor;

    @Column(nullable = false, length = 100)
    private String empresa;

    @Column(length = 50)
    private String autorizacion;

    @Column(length = 100)
    private String procesoDestino;

    private LocalDate vigencia;
}