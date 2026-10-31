package com.gestion.residuos.Eco_Andino.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Página de resultados con una forma JSON estable (no se expone PageImpl directamente). */
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long total, int totalPaginas) {

    public static <E, T> PaginaResponse<T> de(Page<E> page, Function<E, T> mapper) {
        return new PaginaResponse<>(page.getContent().stream().map(mapper).toList(), page.getNumber(),
                page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
