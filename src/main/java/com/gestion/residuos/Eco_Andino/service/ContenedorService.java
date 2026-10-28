package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.entity.Contenedor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

public class ContenedorService {

    public void registrarContenedor(Contenedor contenedor) {
        if (StringUtils.isBlank(contenedor.getCodigo())) {
            // Genera un código alfanumérico commons-lang3
            String codigoAutogenerado = "CONT-" + RandomStringUtils.randomAlphanumeric(8).toUpperCase();
            contenedor.setCodigo(codigoAutogenerado);
        }
        //Más código...
    }
}