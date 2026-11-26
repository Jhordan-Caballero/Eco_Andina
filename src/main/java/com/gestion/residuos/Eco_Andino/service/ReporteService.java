package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.GeneracionDtos.GeneracionVista;
import com.gestion.residuos.Eco_Andino.dto.ReporteDtos.*;
import com.gestion.residuos.Eco_Andino.entity.AreaGeneradora;
import com.gestion.residuos.Eco_Andino.entity.Generacion;
import com.gestion.residuos.Eco_Andino.entity.Incidencia;
import com.gestion.residuos.Eco_Andino.entity.Recoleccion;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import com.gestion.residuos.Eco_Andino.repository.AreaGeneradoraRepository;
import com.gestion.residuos.Eco_Andino.repository.IncidenciaRepository;
import com.gestion.residuos.Eco_Andino.util.Etiquetas;
import com.gestion.residuos.Eco_Andino.util.Numeros;
import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

/** Reportes e indicadores de la gestión de residuos (CU19) y su exportación a CSV (CU20). */
@Service
@RequiredArgsConstructor
public class ReporteService {
    private static final Map<String, String> PERIODOS = Map.of(
            "30", "Últimos 30 días", "90", "Últimos 90 días", "TODO", "Todo el registro");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    private final AreaGeneradoraRepository areaRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final VistaService vistas;
    private final SesionService sesion;
    private final AuditoriaService auditoria;

    @Transactional(readOnly = true)
    public Reporte generar(String periodo) {
        if (!PERIODOS.containsKey(periodo)) {
            throw new SolicitudInvalidaException("El periodo debe ser 30, 90 o TODO.");
        }
        LocalDateTime desde = "TODO".equals(periodo) ? null : sesion.ahora().minusDays(Long.parseLong(periodo));
        Predicate<LocalDateTime> enPeriodo = fecha -> desde == null || (fecha != null && !fecha.isBefore(desde));
        VistaService.Instantanea foto = vistas.instantanea();

        List<GeneracionVista> todas = foto.generaciones().stream()
                .sorted(Comparator.comparing(Generacion::getFecha, Comparator.reverseOrder()))
                .map(g -> vistas.generacion(g, foto))
                .toList();
        List<GeneracionVista> generaciones = todas.stream().filter(g -> enPeriodo.test(g.fecha())).toList();
        List<GeneracionVista> enKg = generaciones.stream().filter(g -> "kg".equals(g.unidad())).toList();
        long validadas = generaciones.stream().filter(g -> "VALIDADA".equals(g.estadoValidacion())).count();
        double totalKg = sumar(enKg);
        double peligrososKg = sumar(enKg.stream()
                .filter(g -> List.of("PELIGROSO", "ESPECIAL").contains(g.residuo().categoria())).toList());

        List<Recoleccion> ordenes = foto.recolecciones().stream()
                .filter(o -> enPeriodo.test(o.getFechaSolicitud())).toList();
        long conRecepcion = ordenes.stream()
                .filter(o -> List.of(Recoleccion.RECIBIDA, Recoleccion.CERRADA).contains(o.getEstado())).count();
        long cerradas = ordenes.stream().filter(o -> Recoleccion.CERRADA.equals(o.getEstado())).count();

        List<Incidencia> incidencias = incidenciaRepository.findAll().stream()
                .filter(i -> enPeriodo.test(i.getFecha())).toList();

        List<Serie> porArea = new ArrayList<>();
        for (AreaGeneradora area : areaRepository.findAll(Sort.by("codigo"))) {
            double valor = sumar(enKg.stream().filter(g -> g.idArea().equals(area.getIdArea())).toList());
            if (valor > 0) {
                porArea.add(new Serie(area.getCodigo(), area.getNombre(), valor));
            }
        }
        porArea.sort(Comparator.comparingDouble(Serie::valor).reversed());

        YearMonth actual = YearMonth.from(sesion.hoy());
        List<Mensual> mensual = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth mes = actual.minusMonths(i);
            List<GeneracionVista> delMes = todas.stream().filter(g -> YearMonth.from(g.fecha()).equals(mes)).toList();
            mensual.add(new Mensual(mes.toString(), mes.toString(),
                    sumar(delMes.stream().filter(g -> "kg".equals(g.unidad())).toList()), delMes.size(), i == 0));
        }

        List<Serie> destinos = new ArrayList<>();
        Etiquetas.TIPO_DESTINO.forEach((tipo, etiqueta) -> destinos.add(new Serie(tipo, etiqueta,
                ordenes.stream()
                        .filter(o -> tipo.equals(o.getTipoDestino()) && o.getCantidadRecibida() != null)
                        .filter(o -> o.getDetalles().stream().allMatch(d -> "kg".equals(d.getGeneracion().getResiduo().getUnidad())))
                        .mapToDouble(o -> Numeros.valor(o.getCantidadRecibida()))
                        .sum())));
        destinos.sort(Comparator.comparing(Serie::clave));

        List<Serie> incidenciasPorTipo = new ArrayList<>();
        Etiquetas.TIPO_INCIDENCIA.forEach((tipo, etiqueta) -> incidenciasPorTipo.add(new Serie(tipo, etiqueta,
                incidencias.stream().filter(i -> tipo.equals(i.getTipo())).count())));
        incidenciasPorTipo.sort(Comparator.comparingDouble(Serie::valor).reversed());

        Kpis kpis = new Kpis(totalKg, generaciones.size(), Numeros.porcentaje(validadas, generaciones.size()), validadas,
                Numeros.porcentaje(cerradas, conRecepcion), cerradas, conRecepcion,
                Numeros.porcentaje(peligrososKg, totalKg), incidencias.size(),
                incidencias.stream().filter(i -> !Incidencia.CERRADA.equals(i.getEstado())).count());
        return new Reporte(periodo, kpis, porArea, mensual, destinos, incidenciasPorTipo, generaciones);
    }

    /** CU20: exporta los indicadores y el detalle a CSV (separado por punto y coma, abre bien en Excel). */
    @Transactional
    public Exportacion exportar(String periodo) {
        Reporte reporte = generar(periodo);
        String etiquetaPeriodo = PERIODOS.get(periodo);
        LocalDateTime ahora = sesion.ahora();
        StringWriter salida = new StringWriter();
        CSVFormat formato = CSVFormat.DEFAULT.builder().setDelimiter(';').setRecordSeparator("\r\n").build();
        try (CSVPrinter csv = new CSVPrinter(salida, formato)) {
            csv.printRecord("Reporte de gestión de residuos sólidos · EcoAndina");
            csv.printRecord("Periodo", etiquetaPeriodo);
            csv.printRecord("Generado por", sesion.usuario().getNombreCompleto());
            csv.printRecord("Fecha", ahora.format(FECHA_HORA));
            csv.println();
            csv.printRecord("Indicador", "Valor");
            csv.printRecord("Residuos generados (kg)", decimal(reporte.kpis().totalKg()));
            csv.printRecord("Registros de generación", reporte.kpis().registros());
            csv.printRecord("Tasa de validación (%)", reporte.kpis().tasaValidacion());
            csv.printRecord("Órdenes con trazabilidad completa (%)", reporte.kpis().trazabilidad());
            csv.printRecord("Residuos peligrosos (% del peso)", reporte.kpis().peligrosos());
            csv.printRecord("Incidencias registradas", reporte.kpis().incidencias());
            csv.println();
            csv.printRecord("Código", "Fecha", "Área", "Residuo", "Categoría", "Cantidad", "Unidad", "Validación",
                    "Contenedor", "Orden", "Estado de la orden");
            for (GeneracionVista g : reporte.generaciones()) {
                csv.printRecord(g.codigo(), g.fecha().format(FECHA_HORA), celda(g.area().nombre()),
                        celda(g.residuo().nombre()), Etiquetas.de(Etiquetas.CATEGORIA_RESIDUO, g.residuo().categoria()),
                        decimal(Numeros.valor(g.cantidad())), g.unidad(),
                        Etiquetas.de(Etiquetas.ESTADO_GENERACION, g.estadoValidacion()), g.contenedor().codigo(),
                        g.recoleccion() == null ? "" : g.recoleccion().numeroOrden(),
                        g.recoleccion() == null ? "" : Etiquetas.de(Etiquetas.ESTADO_RECOLECCION, g.recoleccion().estado()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el reporte", e);
        }
        auditoria.registrar("EXPORTAR", "Reporte", "Indicadores de gestión",
                "Exportó el reporte (" + etiquetaPeriodo.toLowerCase(Locale.ROOT) + ")");
        // El BOM hace que Excel reconozca las tildes (UTF-8)
        return new Exportacion("reporte-residuos-" + ahora.format(ARCHIVO) + ".csv", "﻿" + salida);
    }

    private static double sumar(List<GeneracionVista> lista) {
        return Math.round(lista.stream().mapToDouble(g -> Numeros.valor(g.cantidad())).sum() * 100) / 100.0;
    }

    private static String decimal(double valor) {
        return Numeros.cantidad(valor, null).replace(" ", "").replace(" ", "");
    }

    private static String celda(String valor) {
        return TextoUtil.neutralizarFormula(valor);
    }
}
