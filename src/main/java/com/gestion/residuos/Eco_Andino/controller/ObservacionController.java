package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.ObservacionRequest;
import com.gestion.residuos.Eco_Andino.dto.ObservacionResponse;
import com.gestion.residuos.Eco_Andino.entity.TipoRegistro;
import com.gestion.residuos.Eco_Andino.service.ObservacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CU18 – Registrar observaciones operativas. */
@RestController
@RequestMapping("/api/observaciones")
@RequiredArgsConstructor
public class ObservacionController {
    private final ObservacionService observacionService;

    // Consultar las observaciones de un registro (paso 2); también las ven quienes consultan esas operaciones
    @GetMapping
    @PreAuthorize("hasAnyAuthority('OBSERVACION_REGISTRAR','ALMACENAMIENTO_CONSULTAR','RECOLECCION_CONSULTAR',"
            + "'INCIDENCIA_GESTIONAR','AUDITORIA_CONSULTAR','REPORTE_GENERAR')")
    public List<ObservacionResponse> listar(@RequestParam(required = false) TipoRegistro tipo,
                                            @RequestParam(required = false) Integer idRegistro) {
        return observacionService.listar(tipo, idRegistro);
    }

    // El permiso concreto depende del tipo de registro y se valida en el servicio
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('OBSERVACION_REGISTRAR')")
    public ObservacionResponse registrar(@Valid @RequestBody ObservacionRequest request) {
        return observacionService.registrar(request);
    }
}
