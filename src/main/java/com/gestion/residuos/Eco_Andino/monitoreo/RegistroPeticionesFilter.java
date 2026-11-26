package com.gestion.residuos.Eco_Andino.monitoreo;

import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Monitoreo de rendimiento: escribe una línea por cada petición a la API en logs/ecoandina-peticiones.log
 * con el método, la ruta, el código de respuesta, el tiempo que tardó y la IP de origen.
 * Las peticiones lentas (más del umbral configurado) y las que terminan en error 5xx se registran como WARN,
 * así también aparecen en logs/ecoandina-alertas.log.
 * Se ejecuta antes que Spring Security para registrar también los rechazos (401, 403, 429).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RegistroPeticionesFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RegistroPeticionesFilter.class);

    private final long umbralLentaMs;

    public RegistroPeticionesFilter(@Value("${app.monitoreo.peticion-lenta-ms:1000}") long umbralLentaMs) {
        this.umbralLentaMs = umbralLentaMs;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Solo la API; los chequeos de salud se repiten cada pocos segundos y solo harían ruido
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long inicio = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            int estado = response.getStatus();
            String linea = String.format("%s %s %d %dms ip=%s", request.getMethod(),
                    TextoUtil.paraLog(request.getRequestURI()), estado, ms, request.getRemoteAddr());
            if (estado >= 500) {
                log.warn("ERROR_SERVIDOR {}", linea);
            } else if (ms > umbralLentaMs) {
                log.warn("PETICION_LENTA {}", linea);
            } else {
                log.info(linea);
            }
        }
    }
}
