package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.*;
import com.gestion.residuos.Eco_Andino.service.CatalogoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Catálogos: residuos (CU03), áreas generadoras (CU04) y contenedores (CU05). */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogoController {
    private final CatalogoService catalogoService;

    @GetMapping("/residuos")
    @PreAuthorize("hasAuthority('CATALOGO_CONSULTAR')")
    public List<ResiduoDto> listarResiduos() {
        return catalogoService.listarResiduos();
    }

    @PostMapping("/residuos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('RESIDUO_GESTIONAR')")
    public ResiduoDto crearResiduo(@Valid @RequestBody ResiduoRequest datos) {
        return catalogoService.guardarResiduo(null, datos);
    }

    @PutMapping("/residuos/{id}")
    @PreAuthorize("hasAuthority('RESIDUO_GESTIONAR')")
    public ResiduoDto actualizarResiduo(@PathVariable Integer id, @Valid @RequestBody ResiduoRequest datos) {
        return catalogoService.guardarResiduo(id, datos);
    }

    @GetMapping("/areas")
    @PreAuthorize("hasAuthority('CATALOGO_CONSULTAR')")
    public List<AreaDto> listarAreas() {
        return catalogoService.listarAreas();
    }

    @GetMapping("/areas/responsables")
    @PreAuthorize("hasAuthority('AREA_GESTIONAR')")
    public List<String> listarResponsables() {
        return catalogoService.listarResponsables();
    }

    @PostMapping("/areas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('AREA_GESTIONAR')")
    public AreaDto crearArea(@Valid @RequestBody AreaRequest datos) {
        return catalogoService.guardarArea(null, datos);
    }

    @PutMapping("/areas/{id}")
    @PreAuthorize("hasAuthority('AREA_GESTIONAR')")
    public AreaDto actualizarArea(@PathVariable Integer id, @Valid @RequestBody AreaRequest datos) {
        return catalogoService.guardarArea(id, datos);
    }

    @PatchMapping("/areas/{id}/estado")
    @PreAuthorize("hasAuthority('AREA_GESTIONAR')")
    public AreaDto cambiarEstadoArea(@PathVariable Integer id) {
        return catalogoService.cambiarEstadoArea(id);
    }

    @GetMapping("/contenedores")
    @PreAuthorize("hasAuthority('CATALOGO_CONSULTAR')")
    public List<ContenedorVista> listarContenedores() {
        return catalogoService.listarContenedores();
    }

    @PostMapping("/contenedores")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CONTENEDOR_GESTIONAR')")
    public ContenedorVista crearContenedor(@Valid @RequestBody ContenedorRequest datos) {
        return catalogoService.guardarContenedor(null, datos);
    }

    @PutMapping("/contenedores/{id}")
    @PreAuthorize("hasAuthority('CONTENEDOR_GESTIONAR')")
    public ContenedorVista actualizarContenedor(@PathVariable Integer id, @Valid @RequestBody ContenedorRequest datos) {
        return catalogoService.guardarContenedor(id, datos);
    }
}
