package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contenedor")
@Getter
@Setter
@NoArgsConstructor
public class Contenedor {
    public static final String DISPONIBLE = "DISPONIBLE";
    public static final String MANTENIMIENTO = "MANTENIMIENTO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contenedor")
    private Integer idContenedor;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(length = 50)
    private String tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidad;

    @Column(length = 100)
    private String ubicacion;

    // Estado administrativo: DISPONIBLE o MANTENIMIENTO. La situación (en uso, lleno) se calcula.
    @Column(nullable = false, length = 15)
    private String estado = DISPONIBLE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_residuo_permitido", nullable = false)
    private Residuo residuoPermitido;

    // Cantidad que ya contenía al empezar a usar el sistema (carga inicial)
    @Column(name = "stock_inicial", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockInicial = BigDecimal.ZERO;

    @Column(name = "fecha_stock")
    private LocalDateTime fechaStock;
}
