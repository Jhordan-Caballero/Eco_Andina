package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "permiso")
@Getter
@Setter
// Se compara por código: los permisos viven en un Set y pueden llegar de sesiones distintas
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Permiso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Integer idPermiso;

    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true, length = 60)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 200)
    private String descripcion;
}
