package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.entity.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    public boolean autenticarUsuario(String usernameIngresado, String passwordIngresada, Usuario usuarioBD) {
        if (StringUtils.isAnyBlank(usernameIngresado, passwordIngresada)) {
            logger.warn("Intento de inicio de sesión con credenciales en blanco");
            return false;
        }

        String hashIngresado = DigestUtils.sha256Hex(passwordIngresada);
        boolean autenticado = StringUtils.equals(hashIngresado, usuarioBD.getPassword());

        if (autenticado) {
            // Log de nivel INFO para acciones exitosas
            logger.info("Usuario autenticado exitosamente: {}", usernameIngresado);
        } else {
            // Log de nivel WARN para anomalías o intentos fallidos
            logger.warn("Intento de inicio de sesión fallido para el usuario: {}", usernameIngresado);
        }

        return autenticado;
    }
}
