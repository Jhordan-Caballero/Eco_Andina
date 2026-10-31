package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.EstadoIncidencia;
import com.gestion.residuos.Eco_Andino.entity.Incidencia;
import com.gestion.residuos.Eco_Andino.entity.Severidad;
import com.gestion.residuos.Eco_Andino.entity.TipoIncidencia;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Filtros combinables del listado de incidencias. Los parámetros nulos se ignoran. */
public final class IncidenciaSpecs {
    private IncidenciaSpecs() {
    }

    /** {@code hastaExclusivo}: se excluye ese instante (el filtro "hasta el día X" pasa el día X+1 a las 00:00). */
    public static Specification<Incidencia> filtrar(EstadoIncidencia estado, Severidad severidad, TipoIncidencia tipo,
                                                    Integer areaId, LocalDateTime desde, LocalDateTime hastaExclusivo) {
        return (root, query, cb) -> {
            // En la consulta de conteo (paginación) no se hace fetch
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("area");
                root.fetch("responsable");
            }
            List<Predicate> p = new ArrayList<>();
            if (estado != null) p.add(cb.equal(root.get("estado"), estado));
            if (severidad != null) p.add(cb.equal(root.get("severidad"), severidad));
            if (tipo != null) p.add(cb.equal(root.get("tipo"), tipo));
            if (areaId != null) p.add(cb.equal(root.get("area").get("idArea"), areaId));
            if (desde != null) p.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde));
            if (hastaExclusivo != null) p.add(cb.lessThan(root.get("fecha"), hastaExclusivo));
            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
