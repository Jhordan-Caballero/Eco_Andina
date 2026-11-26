package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Generacion;
import org.springframework.data.jpa.repository.JpaRepository;


public interface GeneracionRepository extends JpaRepository<Generacion, Integer> {

    long countByEstadoValidacion(String estadoValidacion);
}
