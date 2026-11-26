package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.AlmacenDtos.ContenedorDetalle;
import com.gestion.residuos.Eco_Andino.dto.AlmacenDtos.MovimientoDto;
import com.gestion.residuos.Eco_Andino.dto.AlmacenDtos.MovimientoRequest;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.ContenedorVista;
import com.gestion.residuos.Eco_Andino.service.AlmacenamientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Almacenamiento temporal: consulta (CU11) y movimientos internos (CU10). */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AlmacenamientoController {
    private static final String CONSULTAR = "hasAnyAuthority('ALMACENAMIENTO_CONSULTAR', 'ALMACENAMIENTO_GESTIONAR')";

    private final AlmacenamientoService almacenamientoService;

    @GetMapping("/almacenamiento")
    @PreAuthorize(CONSULTAR)
    public List<ContenedorVista> listar() {
        return almacenamientoService.listar();
    }

    @GetMapping("/almacenamiento/{idContenedor}")
    @PreAuthorize(CONSULTAR)
    public ContenedorDetalle detalle(@PathVariable Integer idContenedor) {
        return almacenamientoService.detalle(idContenedor);
    }

    @GetMapping("/movimientos")
    @PreAuthorize(CONSULTAR)
    public List<MovimientoDto> movimientos() {
        return almacenamientoService.movimientos();
    }

    @PostMapping("/movimientos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ALMACENAMIENTO_GESTIONAR')")
    public MovimientoDto registrar(@Valid @RequestBody MovimientoRequest datos) {
        return almacenamientoService.registrar(datos);
    }
}
