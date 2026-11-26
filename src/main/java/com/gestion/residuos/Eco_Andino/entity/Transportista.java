package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Sin @Data: con la relación bidireccional, el hashCode/toString de Lombok entra en un ciclo infinito
@Entity
@Table(name = "transportista")
@Getter
@Setter
public class Transportista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transportista")
    private Integer idTransportista;

    @Column(nullable = false, length = 120)
    private String empresa;

    // Registro EO-RS del MINAM
    @Column(nullable = false, unique = true, length = 60)
    private String autorizacion;

    @Column(length = 120)
    private String contacto;

    @Column(nullable = false)
    private LocalDate vigencia;

    @OneToMany(mappedBy = "transportista", cascade = CascadeType.ALL)
    @OrderBy("idVehiculo")
    private List<Vehiculo> vehiculos = new ArrayList<>();
}
