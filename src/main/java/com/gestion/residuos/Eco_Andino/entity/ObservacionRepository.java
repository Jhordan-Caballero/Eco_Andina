package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Observacion;
import com.gestion.residuos.Eco_Andino.entity.TipoRegistro;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ObservacionRepository extends JpaRepository<Observacion, Integer> {

    @EntityGraph(attributePaths = "usuario")
    List<Observacion> findByTipoRegistroAndIdRegistroOrderByFechaDescIdObservacionDesc(TipoRegistro tipoRegistro,
                                                                                       Integer idRegistro);

    @EntityGraph(attributePaths = "usuario")
    List<Observacion> findByTipoRegistroOrderByFechaDescIdObservacionDesc(TipoRegistro tipoRegistro, Pageable pageable);

    @EntityGraph(attributePaths = "usuario")
    List<Observacion> findAllByOrderByFechaDescIdObservacionDesc(Pageable pageable);

    // Para el reporte de observaciones (periodo [desde, hasta))
    @EntityGraph(attributePaths = "usuario")
    @Query("select o from Observacion o where o.fecha >= :desde and o.fecha < :hasta order by o.fecha desc")
    List<Observacion> findPorPeriodo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                                     Pageable pageable);
}
