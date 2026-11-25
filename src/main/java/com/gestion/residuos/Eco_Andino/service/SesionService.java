package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.entity.Rol;
import com.gestion.residuos.Eco_Andino.entity.Usuario;
import com.gestion.residuos.Eco_Andino.repository.UsuarioRepository;
import com.gestion.residuos.Eco_Andino.security.UsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Comparator;

/**
 * Usuario autenticado y hora oficial de la planta. El responsable de cada registro se toma siempre del token,
 * nunca del cuerpo de la petición: así nadie puede registrar a nombre de otra persona.
 */
@Service
@RequiredArgsConstructor
public class SesionService {
    private final UsuarioActual usuarioActual;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    public Usuario usuario() {
        Integer id = usuarioActual.id();
        if (id != null) {
            return usuarioRepository.findById(id)
                    .orElseThrow(() -> new AccessDeniedException("La sesión ya no es válida"));
        }
        return usuarioRepository.findByUsernameOrEmail(usuarioActual.username())
                .orElseThrow(() -> new AccessDeniedException("La sesión ya no es válida"));
    }

    public boolean tienePermiso(String permiso) {
        return usuarioActual.tienePermiso(permiso);
    }

    /** Nombre del primer rol (por id) del usuario que tenga el permiso indicado; si ninguno, el primero. */
    public String nombreRol(Usuario usuario, String permiso) {
        return usuario.getRoles().stream()
                .sorted(Comparator.comparing(Rol::getIdRol))
                .filter(rol -> rol.getPermisos().stream().anyMatch(p -> p.getCodigo().equals(permiso)))
                .map(Rol::getNombre)
                .findFirst()
                .orElseGet(() -> usuario.getRoles().stream().sorted(Comparator.comparing(Rol::getIdRol))
                        .map(Rol::getNombre).findFirst().orElse(""));
    }

    public LocalDateTime ahora() {
        return LocalDateTime.now(clock);
    }

    public LocalDate hoy() {
        return LocalDate.now(clock);
    }

    /** Convierte la fecha enviada por el navegador (con zona) a la hora de la planta. */
    public LocalDateTime aLocal(OffsetDateTime fecha) {
        return fecha.atZoneSameInstant(clock.getZone()).toLocalDateTime();
    }
}
