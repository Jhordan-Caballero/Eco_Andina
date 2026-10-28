package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "generacion")
@Getter
@Setter
@NoArgsConstructor
public class Generacion {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String VALIDADA = "VALIDADA";
    public static final String OBSERVADA = "OBSERVADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_generacion")
    private Integer idGeneracion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_area", nullable = false)
    private Area_Generadora area;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_residuo", nullable = false)
    private Residuo residuo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contenedor", nullable = false)
    private Contenedor contenedor;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal cantidad;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_responsable", nullable = false)
    private Usuario responsable;

    @Column(name = "condicion_envase", length = 50)
    private String condicionEnvase;

    @Column(name = "estado_validacion", nullable = false, length = 15)
    private String estadoValidacion = PENDIENTE;

    @Column(length = 255)
    private String observaciones;

    @Column(name = "evidencia_url", length = 255)
    private String evidenciaUrl;
}
