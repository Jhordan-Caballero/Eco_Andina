package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.*;
import com.gestion.residuos.Eco_Andino.entity.EstadoIncidencia;
import com.gestion.residuos.Eco_Andino.entity.Severidad;
import com.gestion.residuos.Eco_Andino.entity.TipoIncidencia;
import com.gestion.residuos.Eco_Andino.service.AreaConsultaService;
import com.gestion.residuos.Eco_Andino.service.IncidenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** CU17 – Gestionar incidencias y acciones correctivas (Supervisor ambiental). */
@RestController
@RequestMapping("/api/incidencias")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('INCIDENCIA_GESTIONAR')")
public class IncidenciaController {
    private final IncidenciaService incidenciaService;
    private final AreaConsultaService areaConsultaService;

    @GetMapping
    public PaginaResponse<IncidenciaResponse> listar(
            @RequestParam(required = false) EstadoIncidencia estado,
            @RequestParam(required = false) Severidad severidad,
            @RequestParam(required = false) TipoIncidencia tipo,
            @RequestParam(required = false) Integer areaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        return incidenciaService.listar(estado, severidad, tipo, areaId, desde, hasta, pagina, tamano);
    }

    // Áreas activas para el selector (ruta propia: /api/areas queda libre para el CU04)
    @GetMapping("/areas")
    public List<AreaResponse> areas() {
        return areaConsultaService.listarActivas();
    }

    @GetMapping("/responsables")
    public List<UsuarioResumen> responsables() {
        return incidenciaService.responsablesDisponibles();
    }

    @GetMapping("/{id}")
    public IncidenciaResponse obtener(@PathVariable Integer id) {
        return incidenciaService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidenciaResponse crear(@Valid @RequestBody IncidenciaRequest request) {
        return incidenciaService.crear(request);
    }

    @PutMapping("/{id}")
    public IncidenciaResponse seguimiento(@PathVariable Integer id,
                                          @Valid @RequestBody IncidenciaSeguimientoRequest request) {
        return incidenciaService.registrarSeguimiento(id, request);
    }

    @PostMapping("/{id}/cierre")
    public IncidenciaResponse cerrar(@PathVariable Integer id,
                                     @Valid @RequestBody(required = false) IncidenciaCierreRequest request) {
        return incidenciaService.cerrar(id, request);
    }
}
