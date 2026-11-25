package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.*;
import com.gestion.residuos.Eco_Andino.service.TransportistaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Transportistas y vehículos (CU06) y gestores autorizados (CU07). */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransportistaController {
    private static final String CONSULTAR =
            "hasAnyAuthority('TRANSPORTISTA_GESTOR_CONSULTAR', 'TRANSPORTISTA_GESTIONAR', 'GESTOR_GESTIONAR')";

    private final TransportistaService transportistaService;

    @GetMapping("/transportistas")
    @PreAuthorize(CONSULTAR)
    public List<TransportistaDto> listarTransportistas() {
        return transportistaService.listarTransportistas();
    }

    @PostMapping("/transportistas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TRANSPORTISTA_GESTIONAR')")
    public TransportistaDto crearTransportista(@Valid @RequestBody TransportistaRequest datos) {
        return transportistaService.guardarTransportista(null, datos);
    }

    @PutMapping("/transportistas/{id}")
    @PreAuthorize("hasAuthority('TRANSPORTISTA_GESTIONAR')")
    public TransportistaDto actualizarTransportista(@PathVariable Integer id, @Valid @RequestBody TransportistaRequest datos) {
        return transportistaService.guardarTransportista(id, datos);
    }

    @PostMapping("/transportistas/{id}/vehiculos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TRANSPORTISTA_GESTIONAR')")
    public VehiculoDto crearVehiculo(@PathVariable Integer id, @Valid @RequestBody VehiculoRequest datos) {
        return transportistaService.guardarVehiculo(id, null, datos);
    }

    @PutMapping("/transportistas/{id}/vehiculos/{idVehiculo}")
    @PreAuthorize("hasAuthority('TRANSPORTISTA_GESTIONAR')")
    public VehiculoDto actualizarVehiculo(@PathVariable Integer id, @PathVariable Integer idVehiculo,
                                          @Valid @RequestBody VehiculoRequest datos) {
        return transportistaService.guardarVehiculo(id, idVehiculo, datos);
    }

    @GetMapping("/gestores")
    @PreAuthorize(CONSULTAR)
    public List<GestorDto> listarGestores() {
        return transportistaService.listarGestores();
    }

    @PostMapping("/gestores")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('GESTOR_GESTIONAR')")
    public GestorDto crearGestor(@Valid @RequestBody GestorRequest datos) {
        return transportistaService.guardarGestor(null, datos);
    }

    @PutMapping("/gestores/{id}")
    @PreAuthorize("hasAuthority('GESTOR_GESTIONAR')")
    public GestorDto actualizarGestor(@PathVariable Integer id, @Valid @RequestBody GestorRequest datos) {
        return transportistaService.guardarGestor(id, datos);
    }
}
