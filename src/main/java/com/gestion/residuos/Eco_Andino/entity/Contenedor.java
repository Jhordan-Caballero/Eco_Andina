package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "contenedor")
@Getter
@Setter
@NoArgsConstructor
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contenedor")
    private Integer idContenedor;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(length = 50)
    private String tipo;

    @Column(precision = 8, scale = 2)
    private BigDecimal capacidad;

    @Column(length = 100)
    private String ubicacion;

    @Column(nullable = false, length = 15)
    private String estado = "DISPONIBLE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_residuo_permitido")
    private Residuo residuoPermitido;
}
