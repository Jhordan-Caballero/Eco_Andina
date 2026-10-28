package com.gestion.residuos.Eco_Andino.entity;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "residuo")
public class Residuo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idResiduo;

    @Column(unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 50)
    private String categoria;

    @Column(length = 50)
    private String peligrosidad;

    @Column(length = 20)
    private String unidad;

    @Column(length = 100)
    private String tratamiento;

    @Column(length = 255)
    private String requisitos;
}