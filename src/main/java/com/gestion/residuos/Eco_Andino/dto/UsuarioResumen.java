package com.gestion.residuos.Eco_Andino.dto;

import com.gestion.residuos.Eco_Andino.entity.Usuario;

public record UsuarioResumen(Integer id, String username, String nombreCompleto) {

    public static UsuarioResumen from(Usuario u) {
        return u == null ? null : new UsuarioResumen(u.getIdUsuario(), u.getUsername(),
                u.getNombres() + " " + u.getApellidos());
    }
}
