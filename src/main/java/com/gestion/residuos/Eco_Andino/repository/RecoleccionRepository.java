package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Recoleccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface RecoleccionRepository extends JpaRepository<Recoleccion, Integer> {
    Optional<Recoleccion> findByNumeroOrden(String numeroOrden);

    long countByEstadoIn(Collection<String> estados);
}
