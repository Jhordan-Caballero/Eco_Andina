package com.gestion.residuos.Eco_Andino.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Protección contra fuerza bruta en el inicio de sesión (RNF de seguridad, CU01). */
class LoginAttemptServiceTest {
    private LoginAttemptService servicio;

    @BeforeEach
    void preparar() {
        servicio = new LoginAttemptService(5, 15);
    }

    @Test
    @DisplayName("Con menos de 5 fallos el usuario puede seguir intentando")
    void noBloqueaAntesDelLimite() {
        for (int i = 0; i < 4; i++) {
            servicio.registrarFallo("operario");
        }
        assertFalse(servicio.estaBloqueado("operario"));
    }

    @Test
    @DisplayName("Al quinto fallo el usuario queda bloqueado")
    void bloqueaAlLlegarAlLimite() {
        for (int i = 0; i < 5; i++) {
            servicio.registrarFallo("operario");
        }
        assertTrue(servicio.estaBloqueado("operario"));
    }

    @Test
    @DisplayName("El bloqueo no distingue mayúsculas ni espacios en el usuario")
    void normalizaElUsuario() {
        for (int i = 0; i < 5; i++) {
            servicio.registrarFallo(i % 2 == 0 ? "Operario" : "  OPERARIO ");
        }
        assertTrue(servicio.estaBloqueado("operario"));
    }

    @Test
    @DisplayName("Un inicio de sesión correcto reinicia el contador")
    void exitoReiniciaContador() {
        for (int i = 0; i < 4; i++) {
            servicio.registrarFallo("operario");
        }
        servicio.registrarExito("operario");
        servicio.registrarFallo("operario");
        assertFalse(servicio.estaBloqueado("operario"));
    }

    @Test
    @DisplayName("Bloquear a un usuario no afecta a los demás")
    void usuariosIndependientes() {
        for (int i = 0; i < 5; i++) {
            servicio.registrarFallo("operario");
        }
        assertFalse(servicio.estaBloqueado("supervisor"));
    }
}
