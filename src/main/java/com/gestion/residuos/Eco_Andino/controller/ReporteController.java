package com.gestion.residuos.Eco_Andino.controller;

import com.gestion.residuos.Eco_Andino.dto.AreaResponse;
import com.gestion.residuos.Eco_Andino.service.AreaConsultaService;
import com.gestion.residuos.Eco_Andino.service.ReporteService;
import com.gestion.residuos.Eco_Andino.service.ReporteService.Archivo;
import com.gestion.residuos.Eco_Andino.service.reporte.FiltroReporte;
import com.gestion.residuos.Eco_Andino.service.reporte.ReporteResultado;
import com.gestion.residuos.Eco_Andino.service.reporte.ReporteTipo;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** CU19 (consultar) y CU20 (exportar). Cada operación exige su propio permiso. */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {
    private final ReporteService reporteService;
    private final AreaConsultaService areaConsultaService;

    // Paso 2 del CU19: tipos de reporte disponibles
    @GetMapping
    @PreAuthorize("hasAuthority('REPORTE_GENERAR')")
    public List<ReporteTipo> tipos() {
        return reporteService.tipos();
    }

    // Áreas activas para el filtro de área
    @GetMapping("/areas")
    @PreAuthorize("hasAuthority('REPORTE_GENERAR')")
    public List<AreaResponse> areas() {
        return areaConsultaService.listarActivas();
    }

    // Pasos 3 a 7 del CU19
    @GetMapping("/{codigo}")
    @PreAuthorize("hasAuthority('REPORTE_GENERAR')")
    public ReporteResultado generar(@PathVariable String codigo,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                    @RequestParam(required = false) Integer areaId,
                                    @RequestParam(required = false) String estado) {
        return reporteService.generar(codigo, new FiltroReporte(desde, hasta, areaId, estado));
    }

    // CU20: exige además haber podido generar el reporte (el filtro se vuelve a aplicar al exportar)
    @GetMapping("/{codigo}/export")
    @PreAuthorize("hasAuthority('REPORTE_EXPORTAR')")
    public ResponseEntity<byte[]> exportar(@PathVariable String codigo,
                                           @RequestParam(defaultValue = "xlsx") String formato,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                           @RequestParam(required = false) Integer areaId,
                                           @RequestParam(required = false) String estado) {
        Archivo archivo = reporteService.exportar(codigo, new FiltroReporte(desde, hasta, areaId, estado),
                ReporteService.Formato.de(formato));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo.nombre()).build().toString())
                // Huella para verificar la integridad del archivo descargado
                .header("X-Content-SHA256", archivo.sha256())
                .cacheControl(CacheControl.noStore())
                .body(archivo.datos());
    }
}
