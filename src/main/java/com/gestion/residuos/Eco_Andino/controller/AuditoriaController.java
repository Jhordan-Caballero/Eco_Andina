package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.*;
import com.gestion.residuos.Eco_Andino.entity.AccionAuditoria;
import com.gestion.residuos.Eco_Andino.service.AuditoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/** CU21 – Consultar auditoría y CU22 – Gestionar auditoría. */
@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {
    private final AuditoriaService auditoriaService;

    // ------------------------------------------------------------ CU21 (Administrador, Auditor / Gerencia)

    @GetMapping
    @PreAuthorize("hasAuthority('AUDITORIA_CONSULTAR')")
    public PaginaResponse<AuditoriaResponse> consultar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) AccionAuditoria accion,
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) String usuario,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        return auditoriaService.consultar(desde, hasta, accion, entidad, usuario, pagina, tamano);
    }

    // Se declara antes que "/{id}" para que "configuracion" no se interprete como un id
    @GetMapping("/configuracion")
    @PreAuthorize("hasAuthority('AUDITORIA_GESTIONAR')")
    public ConfiguracionAuditoriaResponse configuracion() {
        return auditoriaService.obtenerConfiguracion();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDITORIA_CONSULTAR')")
    public AuditoriaResponse detalle(@PathVariable Integer id) {
        return auditoriaService.detalle(id);
    }

    // ------------------------------------------------------------ CU22 (Administrador)

    @PutMapping("/configuracion")
    @PreAuthorize("hasAuthority('AUDITORIA_GESTIONAR')")
    public ConfiguracionAuditoriaResponse actualizarConfiguracion(
            @Valid @RequestBody ConfiguracionAuditoriaRequest request) {
        return auditoriaService.actualizarConfiguracion(request);
    }

    @PostMapping("/depuracion")
    @PreAuthorize("hasAuthority('AUDITORIA_GESTIONAR')")
    public DepuracionResponse depurar() {
        return auditoriaService.depurar();
    }
}
