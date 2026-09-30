package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Integer> {
    Optional<Permiso> findByCodigo(String codigo);
}
