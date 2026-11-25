package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Bitácora de auditoría (RNF006): quién hizo qué, sobre qué registro y cuándo. */
@Entity
@Table(name = "auditoria")
@Getter
@Setter
public class Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Integer idAuditoria;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    // Nombre del usuario al momento de la acción (se conserva aunque cambie después)
    @Column(name = "nombre_usuario", nullable = false, length = 210)
    private String nombreUsuario;

    // CREAR / MODIFICAR / VALIDAR / OBSERVAR / APROBAR / RECHAZAR / REGISTRAR / CERRAR / ADJUNTAR / ...
    @Column(nullable = false, length = 20)
    private String accion;

    @Column(nullable = false, length = 40)
    private String entidad;

    @Column(length = 120)
    private String registro;

    @Column(length = 500)
    private String detalle;
}
