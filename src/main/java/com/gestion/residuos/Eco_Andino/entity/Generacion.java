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

    // GEN-0101, se asigna al guardar a partir del id
    @Column(unique = true, length = 20)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_area", nullable = false)
    private AreaGeneradora area;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_residuo", nullable = false)
    private Residuo residuo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contenedor", nullable = false)
    private Contenedor contenedor;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_responsable", nullable = false)
    private Usuario responsable;

    // BUEN_ESTADO / DETERIORADO / SIN_ROTULO / SIN_TAPA
    @Column(name = "condicion_envase", nullable = false, length = 20)
    private String condicionEnvase;

    @Column(name = "segregacion_correcta", nullable = false)
    private boolean segregacionCorrecta = true;

    @Column(name = "estado_validacion", nullable = false, length = 15)
    private String estadoValidacion = PENDIENTE;

    // Observaciones del generador
    @Column(length = 500)
    private String observaciones;

    // Nombre del archivo de evidencia (el archivo se guarda como Documento)
    @Column(length = 255)
    private String evidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_validado_por")
    private Usuario validadoPor;

    @Column(name = "fecha_validacion")
    private LocalDateTime fechaValidacion;

    // Motivo de la observación del supervisor
    @Column(name = "observacion_validacion", length = 500)
    private String observacionValidacion;
}
