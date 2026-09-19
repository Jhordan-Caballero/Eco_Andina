package com.gestion.residuos.Eco_Andino.repository;



import com.gestion.residuos.Eco_Andino.entity.Residuo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface ResiduoRepository extends JpaRepository<Residuo, Integer> {
}
