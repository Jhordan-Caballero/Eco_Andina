package com.gestion.residuos.Eco_Andino.dto;

import com.gestion.residuos.Eco_Andino.entity.Area_Generadora;

public record AreaResponse(Integer id, String codigo, String nombre, String proceso) {

    public static AreaResponse from(Area_Generadora a) {
        return new AreaResponse(a.getIdArea(), a.getCodigo(), a.getNombre(), a.getProceso());
    }
}
