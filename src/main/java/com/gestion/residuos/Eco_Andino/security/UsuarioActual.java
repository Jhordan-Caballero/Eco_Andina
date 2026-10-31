package com.gestion.residuos.Eco_Andino.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** Datos del usuario autenticado en la petición actual (salen del JWT: claim "uid" y subject). */
@Component
public class UsuarioActual {

    public String username() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "sistema" : auth.getName();
    }

    /** Id del usuario o null si la acción no la hace una persona autenticada. */
    public Integer id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken token && token.getToken().getClaim("uid") instanceof Number uid) {
            return uid.intValue();
        }
        return null;
    }

    public boolean tienePermiso(String permiso) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(permiso));
    }
}
