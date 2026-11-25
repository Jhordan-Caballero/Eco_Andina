package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Transportista;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransportistaRepository extends JpaRepository<Transportista, Integer> {
    Optional<Transportista> findByAutorizacionIgnoreCase(String autorizacion);
}
