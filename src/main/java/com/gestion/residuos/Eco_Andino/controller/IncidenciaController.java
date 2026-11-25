package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.IncidenciaDtos.*;
import com.gestion.residuos.Eco_Andino.service.IncidenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Incidencias y acciones correctivas (CU17) y observaciones operativas (CU18). */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class IncidenciaController {
    private static final String VER = "hasAnyAuthority('INCIDENCIA_GESTIONAR', 'OBSERVACION_REGISTRAR')";

    private final IncidenciaService incidenciaService;

    @GetMapping("/incidencias")
    @PreAuthorize(VER)
    public List<IncidenciaDto> listar() {
        return incidenciaService.listar();
    }

    @GetMapping("/incidencias/opciones")
    @PreAuthorize(VER)
    public OpcionesIncidencia opciones() {
        return incidenciaService.opciones();
    }

    @PostMapping(value = "/incidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(VER)
    public IncidenciaDto reportar(@Valid @RequestPart("datos") IncidenciaRequest datos,
                                  @RequestPart(value = "evidencia", required = false) MultipartFile evidencia) {
        return incidenciaService.reportar(datos, evidencia);
    }

    @PostMapping("/incidencias/{id}/seguimiento")
    @PreAuthorize("hasAuthority('INCIDENCIA_GESTIONAR')")
    public IncidenciaDto seguimiento(@PathVariable Integer id, @Valid @RequestBody SeguimientoRequest datos) {
        return incidenciaService.registrarSeguimiento(id, datos);
    }

    @PatchMapping("/incidencias/{id}/cerrar")
    @PreAuthorize("hasAuthority('INCIDENCIA_GESTIONAR')")
    public IncidenciaDto cerrar(@PathVariable Integer id, @Valid @RequestBody CierreRequest datos) {
        return incidenciaService.cerrar(id, datos.nota());
    }

    @GetMapping("/observaciones")
    @PreAuthorize(VER)
    public List<ObservacionDto> listarObservaciones() {
        return incidenciaService.listarObservaciones();
    }

    @PostMapping("/observaciones")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('OBSERVACION_REGISTRAR')")
    public ObservacionDto registrarObservacion(@Valid @RequestBody ObservacionRequest datos) {
        return incidenciaService.registrarObservacion(datos);
    }
}
