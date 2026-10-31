package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.entity.Contenedor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

public class ContenedorService {
    private static final Logger logger = LoggerFactory.getLogger(ContenedorService.class);

    public void registrarContenedor(Contenedor contenedor) {
        logger.info("Iniciando registro de nuevo contenedor de tipo: {}", contenedor.getTipo());

        if (StringUtils.isBlank(contenedor.getCodigo())) {
            String codigoGenerado = "CONT-" + RandomStringUtils.randomAlphanumeric(8).toUpperCase();
            contenedor.setCodigo(codigoGenerado);

            // Log de nivel DEBUG o INFO para trazabilidad de datos autogenerados
            logger.info("Se autogeneró el código {} para el contenedor", codigoGenerado);
        }

        try {
            // ... lógica para guardar en base de datos
            logger.info("Contenedor {} registrado correctamente", contenedor.getCodigo());
        } catch (Exception e) {
            // Log de nivel ERROR capturando la traza de la excepción
            logger.error("Error al registrar el contenedor en la base de datos", e);
            throw e;
        }
    }
}