package com.gestion.residuos.Eco_Andino.dto;

import com.gestion.residuos.Eco_Andino.entity.Rol;

public record RolResponse(String codigo, String nombre) {

    public static RolResponse from(Rol rol) {
        return new RolResponse(rol.getCodigo(), rol.getNombre());
    }
}
