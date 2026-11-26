package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.ReporteDtos.Exportacion;
import com.gestion.residuos.Eco_Andino.dto.ReporteDtos.Reporte;
import com.gestion.residuos.Eco_Andino.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Reportes e indicadores (CU19) y exportación (CU20). */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {
    private final ReporteService reporteService;

    @GetMapping
    @PreAuthorize("hasAuthority('REPORTE_GENERAR')")
    public Reporte generar(@RequestParam(defaultValue = "30") String periodo) {
        return reporteService.generar(periodo);
    }

    @PostMapping("/exportar")
    @PreAuthorize("hasAuthority('REPORTE_EXPORTAR')")
    public Exportacion exportar(@RequestParam(defaultValue = "30") String periodo) {
        return reporteService.exportar(periodo);
    }
}
