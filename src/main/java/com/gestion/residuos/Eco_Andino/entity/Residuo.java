package com.gestion.residuos.Eco_Andino.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "residuo")
public class Residuo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_residuo")
    private Integer idResiduo;

    private String codigo;
    private String nombre;
    private String categoria;
    private String peligrosidad;
    private String unidad;
    private String tratamiento;
    private String requisitos;

    // Constructores
    public Residuo() {}

    // Getters y Setters (Puedes omitirlos si usas @Data de Lombok)
    public Integer getIdResiduo() { return idResiduo; }
    public void setIdResiduo(Integer idResiduo) { this.idResiduo = idResiduo; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getPeligrosidad() { return peligrosidad; }
    public void setPeligrosidad(String peligrosidad) { this.peligrosidad = peligrosidad; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }

    public String getRequisitos() { return requisitos; }
    public void setRequisitos(String requisitos) { this.requisitos = requisitos; }
}
