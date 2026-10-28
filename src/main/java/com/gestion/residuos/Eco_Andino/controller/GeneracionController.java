package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.GeneracionRequest;
import com.gestion.residuos.Eco_Andino.entity.Generacion;
import com.gestion.residuos.Eco_Andino.service.GeneracionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/generaciones")
@RequiredArgsConstructor
public class GeneracionController {

    private final GeneracionService generacionService;

    // CU08: Registrar una generación de residuos
    @PostMapping
    @PreAuthorize("hasAuthority('GENERACION_REGISTRAR')")
    public Generacion registrar(
            @Valid @RequestBody GeneracionRequest request) {
        return generacionService.registrar(request);
    }

    // Consultar todas las generaciones
    @GetMapping
    @PreAuthorize("hasAnyAuthority('GENERACION_REGISTRAR', 'GENERACION_VALIDAR')")
    public List<Generacion> listarTodas() {
        return generacionService.listarTodas();
    }

    // CU09: Consultar generaciones pendientes de validación
    @GetMapping("/pendientes")
    @PreAuthorize("hasAuthority('GENERACION_VALIDAR')")
    public List<Generacion> listarPendientes() {
        return generacionService.listarPendientes();
    }

    // CU09: Validar una generación
    @PatchMapping("/{id}/validar")
    @PreAuthorize("hasAuthority('GENERACION_VALIDAR')")
    public Generacion validar(@PathVariable Integer id) {
        return generacionService.validar(id);
    }

    // CU09: Observar una generación
    @PatchMapping("/{id}/observar")
    @PreAuthorize("hasAuthority('GENERACION_VALIDAR')")
    public Generacion observar(
            @PathVariable Integer id,
            @RequestBody ObservacionRequest request) {
        return generacionService.observar(id, request.observaciones());
    }

    public record ObservacionRequest(String observaciones) {}
}
