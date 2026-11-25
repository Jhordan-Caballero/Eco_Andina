package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.AuditoriaDtos.AuditoriaDto;
import com.gestion.residuos.Eco_Andino.dto.AuditoriaDtos.ConfiguracionDto;
import com.gestion.residuos.Eco_Andino.service.AuditoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Consulta (CU21) y gestión (CU22) de la auditoría. */
@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {
    private static final String CONSULTAR = "hasAnyAuthority('AUDITORIA_CONSULTAR', 'AUDITORIA_GESTIONAR')";

    private final AuditoriaService auditoriaService;

    @GetMapping
    @PreAuthorize(CONSULTAR)
    public List<AuditoriaDto> listar() {
        return auditoriaService.listar();
    }

    @GetMapping("/configuracion")
    @PreAuthorize(CONSULTAR)
    public ConfiguracionDto configuracion() {
        return auditoriaService.configuracion();
    }

    @PutMapping("/configuracion")
    @PreAuthorize("hasAuthority('AUDITORIA_GESTIONAR')")
    public ConfiguracionDto guardarConfiguracion(@Valid @RequestBody ConfiguracionDto datos) {
        return auditoriaService.guardarConfiguracion(datos);
    }
}
