package com.gestion.residuos.Eco_Andino.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita los intentos fallidos de inicio de sesión por usuario (defensa contra fuerza bruta).
 * Tras N fallos dentro de la ventana, el usuario queda bloqueado un tiempo.
 * Se cuenta por nombre de usuario (también los inexistentes, para no revelar cuáles existen). No se usa la IP
 * porque tras nginx/proxy llega la del proxy, y X-Forwarded-For lo puede falsear el cliente.
 * Limitación conocida: el estado vive en memoria (se pierde al reiniciar y no se comparte entre réplicas).
 */
@Service
public class LoginAttemptService {
    private static final int MAX_ENTRADAS = 10_000;

    private record Estado(int fallos, Instant inicioVentana, Instant bloqueadoHasta) {
    }

    private final ConcurrentHashMap<String, Estado> estados = new ConcurrentHashMap<>();
    private final int maxIntentos;
    private final Duration bloqueo;

    public LoginAttemptService(@Value("${app.security.login.max-attempts:5}") int maxIntentos,
                               @Value("${app.security.login.lock-minutes:15}") long minutosBloqueo) {
        this.maxIntentos = maxIntentos;
        this.bloqueo = Duration.ofMinutes(minutosBloqueo);
    }

    public boolean estaBloqueado(String usuario) {
        String clave = clave(usuario);
        Estado estado = estados.get(clave);
        if (estado == null || estado.bloqueadoHasta() == null) {
            return false;
        }
        if (Instant.now().isBefore(estado.bloqueadoHasta())) {
            return true;
        }
        estados.remove(clave, estado); // el bloqueo ya venció
        return false;
    }

    public void registrarFallo(String usuario) {
        if (estados.size() > MAX_ENTRADAS) {
            purgarVencidos();
        }
        Instant ahora = Instant.now();
        estados.compute(clave(usuario), (k, actual) -> {
            if (actual == null || ahora.isAfter(actual.inicioVentana().plus(bloqueo))) {
                return nuevoEstado(1, ahora);
            }
            return nuevoEstado(actual.fallos() + 1, actual.inicioVentana());
        });
    }

    public void registrarExito(String usuario) {
        estados.remove(clave(usuario));
    }

    private Estado nuevoEstado(int fallos, Instant inicioVentana) {
        Instant bloqueadoHasta = fallos >= maxIntentos ? Instant.now().plus(bloqueo) : null;
        return new Estado(fallos, inicioVentana, bloqueadoHasta);
    }

    private void purgarVencidos() {
        Instant ahora = Instant.now();
        estados.entrySet().removeIf(e -> ahora.isAfter(e.getValue().inicioVentana().plus(bloqueo))
                && (e.getValue().bloqueadoHasta() == null || ahora.isAfter(e.getValue().bloqueadoHasta())));
    }

    private static String clave(String usuario) {
        String limpio = usuario == null ? "" : usuario.strip().toLowerCase(Locale.ROOT);
        return limpio.length() > 100 ? limpio.substring(0, 100) : limpio;
    }
}
