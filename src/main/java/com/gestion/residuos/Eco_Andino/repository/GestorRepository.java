package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Gestor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GestorRepository extends JpaRepository<Gestor, Integer> {
    Optional<Gestor> findByAutorizacionIgnoreCase(String autorizacion);
}
