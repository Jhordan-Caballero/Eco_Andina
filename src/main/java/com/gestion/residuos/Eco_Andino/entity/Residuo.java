package com.gestion.residuos.Eco_Andino.entity;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "residuo")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Residuo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_residuo")

    private Integer idResiduo;
    private String codigo;
    private String nombre;
    private String categoria;
    private String peligrosidad;
    private String unidad;
    private String tratamiento;
    private String requisitos;
}