package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;


public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {

    /** Borra en una sola sentencia los registros anteriores a la fecha límite (depuración por retención). */
    @Modifying
    @Query("delete from Auditoria a where a.fecha < :limite")
    int eliminarAnterioresA(@Param("limite") LocalDateTime limite);
}
