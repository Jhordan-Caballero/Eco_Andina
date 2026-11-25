package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ContenedorRepository extends JpaRepository<Contenedor, Integer> {
    Optional<Contenedor> findByCodigo(String codigo);
}
