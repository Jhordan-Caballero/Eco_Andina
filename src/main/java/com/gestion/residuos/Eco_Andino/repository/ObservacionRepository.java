package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Observacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ObservacionRepository extends JpaRepository<Observacion, Integer> {
    List<Observacion> findByReferenciaOrderByFechaDesc(String referencia);
}
