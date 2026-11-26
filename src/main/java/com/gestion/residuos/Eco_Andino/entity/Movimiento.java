package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento")
@Getter
@Setter
@NoArgsConstructor
public class Movimiento {
    public static final String INGRESO = "INGRESO";
    public static final String SALIDA = "SALIDA";
    public static final String TRASLADO = "TRASLADO";
    public static final String PESAJE = "PESAJE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Integer idMovimiento;

    @Column(unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 15)
    private String tipo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_residuo", nullable = false)
    private Residuo residuo;

    // Código de contenedor o texto (área, unidad de transporte)
    @Column(nullable = false, length = 120)
    private String origen;

    @Column(nullable = false, length = 120)
    private String destino;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    // Solo en pesajes: diferencia entre el peso verificado y la ocupación calculada
    @Column(precision = 10, scale = 2)
    private BigDecimal ajuste;

    // Registro que originó el movimiento (GEN-0101, OR-1040, CT-104)
    @Column(length = 30)
    private String referencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(length = 200)
    private String motivo;

    // true en traslados y pesajes: modifican la ocupación calculada del contenedor
    @Column(name = "afecta_stock", nullable = false)
    private boolean afectaStock;
}
