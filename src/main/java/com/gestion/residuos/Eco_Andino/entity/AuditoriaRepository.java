package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer>, JpaSpecificationExecutor<Auditoria> {

    // Trae el usuario en la misma consulta (evita una consulta extra por fila). La consulta de conteo de la
    // paginación lo ignora automáticamente.
    @Override
    @EntityGraph(attributePaths = "usuario")
    Page<Auditoria> findAll(Specification<Auditoria> spec, Pageable pageable);

    // Depuración por retención: un solo DELETE, sin cargar los registros en memoria
    @Modifying
    @Query("delete from Auditoria a where a.fecha < :limite")
    int eliminarAnterioresA(@Param("limite") LocalDateTime limite);
}
