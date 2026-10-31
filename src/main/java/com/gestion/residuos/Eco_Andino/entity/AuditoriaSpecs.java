package com.gestion.residuos.Eco_Andino.repository;

import com.gestion.residuos.Eco_Andino.entity.AccionAuditoria;
import com.gestion.residuos.Eco_Andino.entity.Auditoria;
import com.gestion.residuos.Eco_Andino.entity.Usuario;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Filtros combinables de la consulta de auditoría (CU21). Los parámetros nulos se ignoran.
 * El usuario se carga junto con la página mediante el @EntityGraph de {@link AuditoriaRepository}.
 */
public final class AuditoriaSpecs {
    private AuditoriaSpecs() {
    }

    public static Specification<Auditoria> filtrar(LocalDateTime desde, LocalDateTime hastaExclusivo,
                                                   AccionAuditoria accion, String entidad, String usuario) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (desde != null) p.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde));
            if (hastaExclusivo != null) p.add(cb.lessThan(root.get("fecha"), hastaExclusivo));
            if (accion != null) p.add(cb.equal(root.get("accion"), accion));
            if (entidad != null) p.add(cb.equal(cb.upper(root.get("entidad")), entidad.toUpperCase(Locale.ROOT)));
            if (usuario != null) {
                // Join solo para filtrar; las acciones sin usuario (sistema) no coinciden con este filtro
                Join<Auditoria, Usuario> u = root.join("usuario", JoinType.LEFT);
                String patron = "%" + escaparLike(usuario.toLowerCase(Locale.ROOT)) + "%";
                p.add(cb.or(
                        cb.like(cb.lower(u.get("username")), patron, '\\'),
                        cb.like(cb.lower(u.get("nombres")), patron, '\\'),
                        cb.like(cb.lower(u.get("apellidos")), patron, '\\')));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    // Que % y _ escritos por el usuario se busquen literalmente y no como comodines
    private static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
