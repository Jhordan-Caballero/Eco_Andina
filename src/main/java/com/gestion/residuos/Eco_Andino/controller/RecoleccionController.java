package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.RecoleccionDtos.*;
import com.gestion.residuos.Eco_Andino.service.RecoleccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Órdenes de recolección: CU12 (programar y aprobar), CU13 (consultar), CU14 (salida), CU15 (recepción). */
@RestController
@RequestMapping("/api/recolecciones")
@RequiredArgsConstructor
public class RecoleccionController {
    private static final String CONSULTAR = "hasAnyAuthority('RECOLECCION_CONSULTAR', 'RECOLECCION_PROGRAMAR', "
            + "'TRASLADO_REGISTRAR', 'RECEPCION_REGISTRAR')";

    private final RecoleccionService recoleccionService;

    @GetMapping
    @PreAuthorize(CONSULTAR)
    public List<RecoleccionVista> listar() {
        return recoleccionService.listar();
    }

    @GetMapping("/opciones")
    @PreAuthorize("hasAuthority('RECOLECCION_PROGRAMAR')")
    public OpcionesProgramacion opciones() {
        return recoleccionService.opciones();
    }

    @GetMapping("/{id}")
    @PreAuthorize(CONSULTAR)
    public RecoleccionDetalle detalle(@PathVariable Integer id) {
        return recoleccionService.detalle(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('RECOLECCION_PROGRAMAR')")
    public RecoleccionVista programar(@Valid @RequestBody ProgramarRequest datos) {
        return recoleccionService.programar(datos);
    }

    @PatchMapping("/{id}/aprobar")
    @PreAuthorize("hasAuthority('RECOLECCION_PROGRAMAR')")
    public RecoleccionVista aprobar(@PathVariable Integer id) {
        return recoleccionService.aprobar(id);
    }

    // CU14: datos (JSON) + archivo opcional con la copia del manifiesto o guía
    @PostMapping(value = "/{id}/salida", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('TRASLADO_REGISTRAR')")
    public RecoleccionVista registrarSalida(@PathVariable Integer id, @Valid @RequestPart("datos") SalidaRequest datos,
                                            @RequestPart(value = "archivo", required = false) MultipartFile archivo) {
        return recoleccionService.registrarSalida(id, datos, archivo);
    }

    // CU15: datos (JSON) + acta y certificado opcionales
    @PostMapping(value = "/{id}/recepcion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('RECEPCION_REGISTRAR')")
    public RecoleccionVista registrarRecepcion(@PathVariable Integer id,
                                               @Valid @RequestPart("datos") RecepcionRequest datos,
                                               @RequestPart(value = "acta", required = false) MultipartFile acta,
                                               @RequestPart(value = "certificado", required = false) MultipartFile certificado) {
        return recoleccionService.registrarRecepcion(id, datos, acta, certificado);
    }

    @PatchMapping("/{id}/cerrar")
    @PreAuthorize("hasAuthority('RECOLECCION_PROGRAMAR')")
    public RecoleccionVista cerrar(@PathVariable Integer id) {
        return recoleccionService.cerrar(id);
    }
}
