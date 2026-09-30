package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rol")
@Getter
@Setter
// Se compara por código: los roles viven en un Set y pueden llegar de sesiones distintas
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer idRol;

    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "rol_permiso",
            joinColumns = @JoinColumn(name = "id_rol"),
            inverseJoinColumns = @JoinColumn(name = "id_permiso"))
    private Set<Permiso> permisos = new HashSet<>();
}
