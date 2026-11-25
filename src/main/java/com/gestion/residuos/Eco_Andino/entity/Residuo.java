package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "residuo")
@Getter
@Setter
@NoArgsConstructor
public class Residuo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_residuo")
    private Integer idResiduo;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    // COMUN / APROVECHABLE / PELIGROSO / ESPECIAL
    @Column(nullable = false, length = 20)
    private String categoria;

    // BAJA / MEDIA / ALTA
    @Column(nullable = false, length = 10)
    private String peligrosidad;

    // kg / L / unid
    @Column(nullable = false, length = 10)
    private String unidad;

    // Días máximos que puede permanecer en el almacenamiento temporal
    @Column(name = "plazo_maximo_dias", nullable = false)
    private Integer plazoMaximoDias;

    @Column(length = 100)
    private String tratamiento;

    @Column(length = 255)
    private String requisitos;
}
