package com.gestion.residuos.Eco_Andino.mantenimiento;

import com.gestion.residuos.Eco_Andino.mantenimiento.MantenimientoService.VerificacionArchivos;
import com.gestion.residuos.Eco_Andino.mantenimiento.MantenimientoService.Vigencia;
import com.gestion.residuos.Eco_Andino.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Ejecución manual de las tareas de mantenimiento (las mismas que corren solas por horario).
 * Útil después de restaurar un backup o para comprobar el sistema antes de una auditoría externa.
 */
@RestController
@RequestMapping("/api/mantenimiento")
@RequiredArgsConstructor
public class MantenimientoController {
    private final MantenimientoService mantenimientoService;
    private final AuditoriaService auditoriaService;

    @PostMapping("/depuracion-auditoria")
    @PreAuthorize("hasAuthority('AUDITORIA_GESTIONAR')")
    public Map<String, Integer> depurarAuditoria() {
        return Map.of("eliminados", auditoriaService.depurarManual());
    }

    @GetMapping("/archivos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public VerificacionArchivos verificarArchivos() {
        return mantenimientoService.verificarArchivos();
    }

    @GetMapping("/vigencias")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasAuthority('TRANSPORTISTA_GESTOR_CONSULTAR')")
    public List<Vigencia> vigencias() {
        return mantenimientoService.revisarVigencias();
    }
}
