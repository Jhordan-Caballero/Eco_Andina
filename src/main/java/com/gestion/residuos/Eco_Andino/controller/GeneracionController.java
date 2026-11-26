package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.GeneracionDtos.*;
import com.gestion.residuos.Eco_Andino.service.GeneracionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Generación y segregación (CU08) y validación (CU09). Registrar y corregir reciben multipart/form-data:
 * la parte "datos" (JSON) y, opcional, la parte "evidencia" (archivo).
 */
@RestController
@RequestMapping("/api/generaciones")
@RequiredArgsConstructor
public class GeneracionController {
    private final GeneracionService generacionService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('GENERACION_REGISTRAR', 'GENERACION_VALIDAR', 'ALMACENAMIENTO_CONSULTAR')")
    public List<GeneracionVista> listar() {
        return generacionService.listar();
    }

    @GetMapping("/opciones")
    @PreAuthorize("hasAuthority('GENERACION_REGISTRAR')")
    public OpcionesRegistro opciones() {
        return generacionService.opciones();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('GENERACION_REGISTRAR', 'GENERACION_VALIDAR', 'ALMACENAMIENTO_CONSULTAR')")
    public GeneracionDetalle detalle(@PathVariable Integer id) {
        return generacionService.detalle(id);
    }

    // CU08: registrar generación y segregación con su evidencia
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('GENERACION_REGISTRAR')")
    public GeneracionVista registrar(@Valid @RequestPart("datos") GeneracionRequest datos,
                                     @RequestPart(value = "evidencia", required = false) MultipartFile evidencia) {
        return generacionService.registrar(datos, evidencia);
    }

    // CU09: validar
    @PatchMapping("/{id}/validar")
    @PreAuthorize("hasAuthority('GENERACION_VALIDAR')")
    public GeneracionVista validar(@PathVariable Integer id) {
        return generacionService.validar(id);
    }

    // CU09: observar (validación no aprobada)
    @PatchMapping("/{id}/observar")
    @PreAuthorize("hasAuthority('GENERACION_VALIDAR')")
    public GeneracionVista observar(@PathVariable Integer id, @Valid @RequestBody ObservarRequest datos) {
        return generacionService.observar(id, datos.observacion());
    }

    // Corrección del registro observado por parte del generador
    @PutMapping(value = "/{id}/correccion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('GENERACION_REGISTRAR')")
    public GeneracionVista corregir(@PathVariable Integer id, @Valid @RequestPart("datos") CorreccionRequest datos,
                                    @RequestPart(value = "evidencia", required = false) MultipartFile evidencia) {
        return generacionService.corregir(id, datos, evidencia);
    }
}
