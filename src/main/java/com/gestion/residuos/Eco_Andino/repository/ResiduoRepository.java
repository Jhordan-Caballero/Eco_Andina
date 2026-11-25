package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Residuo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ResiduoRepository extends JpaRepository<Residuo, Integer> {
    Optional<Residuo> findByCodigo(String codigo);
}
