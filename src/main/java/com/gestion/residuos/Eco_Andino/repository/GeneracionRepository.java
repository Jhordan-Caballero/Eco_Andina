package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Generacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneracionRepository extends JpaRepository<Generacion, Integer> {

    List<Generacion> findByEstadoValidacion(String estadoValidacion);

    List<Generacion> findByEstadoValidacionOrderByFechaDesc(String estadoValidacion);
}
