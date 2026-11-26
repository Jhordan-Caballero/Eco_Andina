package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocumentoRepository extends JpaRepository<Documento, Integer> {
    List<Documento> findByReferenciaOrderByFechaDesc(String referencia);

    /** Nombres en disco de todos los archivos adjuntos registrados (para verificar la carpeta de archivos). */
    @Query("select d.rutaArchivo from Documento d where d.rutaArchivo is not null")
    List<String> findRutasArchivo();
}
