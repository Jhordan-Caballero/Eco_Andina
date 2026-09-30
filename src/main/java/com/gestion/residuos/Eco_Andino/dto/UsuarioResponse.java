package com.gestion.residuos.Eco_Andino.dto;

import com.gestion.residuos.Eco_Andino.entity.Permiso;
import com.gestion.residuos.Eco_Andino.entity.Usuario;

import java.util.Comparator;
import java.util.List;

public record UsuarioResponse(
        Integer id,
        String username,
        String nombres,
        String apellidos,
        String email,
        String estado,
        List<RolResponse> roles,
        List<String> permisos) {

    // Requiere roles y permisos ya cargados (ver UsuarioRepository.findByUsernameOrEmail)
    public static UsuarioResponse from(Usuario usuario) {
        List<RolResponse> roles = usuario.getRoles().stream()
                .map(RolResponse::from)
                .sorted(Comparator.comparing(RolResponse::codigo))
                .toList();
        List<String> permisos = usuario.getRoles().stream()
                .flatMap(rol -> rol.getPermisos().stream())
                .map(Permiso::getCodigo)
                .distinct()
                .sorted()
                .toList();
        return new UsuarioResponse(usuario.getIdUsuario(), usuario.getUsername(), usuario.getNombres(),
                usuario.getApellidos(), usuario.getEmail(), usuario.getEstado(), roles, permisos);
    }
}
