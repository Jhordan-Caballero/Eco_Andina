package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.AreaGeneradora;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AreaGeneradoraRepository extends JpaRepository<AreaGeneradora, Integer> {
    Optional<AreaGeneradora> findByCodigo(String codigo);
}
