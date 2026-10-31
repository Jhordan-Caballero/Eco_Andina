package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Incidencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Integer>, JpaSpecificationExecutor<Incidencia> {

    // Trae área y responsable en la misma consulta (la sesión ya no está abierta al mapear la respuesta)
    @EntityGraph(attributePaths = {"area", "responsable"})
    Optional<Incidencia> findWithDetalleByIdIncidencia(Integer idIncidencia);
}
