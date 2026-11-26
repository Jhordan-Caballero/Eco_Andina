package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;


public interface IncidenciaRepository extends JpaRepository<Incidencia, Integer> {

    long countByEstadoIn(Collection<String> estados);
}
