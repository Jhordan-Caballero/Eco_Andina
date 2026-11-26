package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Configuración de la auditoría (CU22). Siempre hay una sola fila, con id 1. */
@Entity
@Table(name = "configuracion_auditoria")
@Getter
@Setter
public class ConfiguracionAuditoria {
    public static final int ID = 1;

    @Id
    @Column(name = "id_configuracion")
    private Integer idConfiguracion = ID;

    @Column(nullable = false)
    private boolean validaciones = true;

    @Column(nullable = false)
    private boolean modificaciones = true;

    @Column(nullable = false)
    private boolean cierres = true;

    @Column(nullable = false)
    private boolean accesos = false;

    @Column(name = "retencion_dias", nullable = false)
    private Integer retencionDias = 365;
}
