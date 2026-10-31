package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.entity.AccionAuditoria;
import com.gestion.residuos.Eco_Andino.exception.ConflictoException;
import com.gestion.residuos.Eco_Andino.exception.ExportacionException;
import com.gestion.residuos.Eco_Andino.exception.RecursoNoEncontradoException;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import com.gestion.residuos.Eco_Andino.security.UsuarioActual;
import com.gestion.residuos.Eco_Andino.service.reporte.*;
import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * CU19 – Generar reportes e indicadores y CU20 – Exportar reportes.
 * Los reportes son plugins ({@link ReporteGenerador}); aquí se generan, se validan los filtros y se exportan a
 * Excel (Apache POI) o CSV (Commons CSV) con una huella SHA-256 (Commons Codec).
 */
@Service
public class ReporteService {
    public static final String TIPO_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String TIPO_CSV = "text/csv; charset=UTF-8";

    private static final DateTimeFormatter FECHA_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
    private static final DateTimeFormatter FECHA_HOJA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public enum Formato {
        XLSX("xlsx", TIPO_XLSX), CSV("csv", TIPO_CSV);

        final String extension;
        final String contentType;

        Formato(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        public static Formato de(String valor) {
            for (Formato f : values()) {
                if (f.extension.equalsIgnoreCase(valor)) return f;
            }
            throw new SolicitudInvalidaException("Formato no soportado. Usa xlsx o csv");
        }
    }

    public record Archivo(String nombre, String contentType, byte[] datos, String sha256) {
    }

    // Reportes del informe (RF015) que dependen de módulos de otros casos de uso, todavía sin implementar
    private static final List<ReporteTipo> PENDIENTES = List.of(
            pendiente("GENERACION", "Cantidades generadas", "Residuos generados por área, tipo y periodo.", "CU08 (registro de generación)"),
            pendiente("ALMACENAMIENTO", "Almacenamiento temporal", "Ocupación de contenedores y permanencia de residuos.", "CU10 (almacenamiento y movimientos)"),
            pendiente("RECOLECCIONES", "Recolecciones", "Órdenes de recolección programadas y ejecutadas.", "CU12 y CU14 (recolección y traslado)"),
            pendiente("DESTINOS", "Destinos finales", "Cantidades recibidas, tratamiento y disposición final.", "CU15 (recepción y destino)"),
            pendiente("CUMPLIMIENTO", "Cumplimiento de procedimientos", "Validaciones y plazos cumplidos.", "CU09 (validación de generación)"));

    private static ReporteTipo pendiente(String codigo, String nombre, String descripcion, String depende) {
        return new ReporteTipo(codigo, nombre, descripcion, false, "Disponible cuando se implemente " + depende,
                List.of("periodo", "area"));
    }

    private final Map<String, ReporteGenerador> generadores;
    private final UsuarioActual usuarioActual;
    private final AuditoriaService auditoriaService;
    private final Clock clock;

    public ReporteService(List<ReporteGenerador> generadores, UsuarioActual usuarioActual,
                          AuditoriaService auditoriaService, Clock clock) {
        this.generadores = generadores.stream().collect(
                Collectors.toMap(ReporteGenerador::codigo, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        this.usuarioActual = usuarioActual;
        this.auditoriaService = auditoriaService;
        this.clock = clock;
    }

    // ---------------------------------------------------------------- CU19

    public List<ReporteTipo> tipos() {
        List<ReporteTipo> tipos = new ArrayList<>();
        generadores.values().forEach(g -> tipos.add(
                new ReporteTipo(g.codigo(), g.nombre(), g.descripcion(), true, null, g.filtros())));
        tipos.addAll(PENDIENTES);
        return tipos;
    }

    public ReporteResultado generar(String codigo, FiltroReporte filtro) {
        validar(filtro);
        ReporteGenerador generador = generadores.get(codigo == null ? "" : codigo.toUpperCase(Locale.ROOT));
        if (generador == null) {
            boolean pendiente = PENDIENTES.stream().anyMatch(t -> t.codigo().equalsIgnoreCase(codigo));
            if (pendiente) {
                throw new ConflictoException("Este reporte aún no está disponible: depende de un módulo en desarrollo");
            }
            throw new RecursoNoEncontradoException("No existe el reporte " + TextoUtil.paraLog(codigo));
        }
        return generador.generar(filtro, LocalDateTime.now(clock));
    }

    // ---------------------------------------------------------------- CU20

    // Transacción de escritura: el registro de auditoría de la exportación se guarda en la misma
    @Transactional
    public Archivo exportar(String codigo, FiltroReporte filtro, Formato formato) {
        ReporteResultado reporte = generar(codigo, filtro);
        // Alternativa 1 del CU20
        if (reporte.filas().isEmpty()) {
            throw new RecursoNoEncontradoException("No hay datos disponibles para exportar con los filtros seleccionados");
        }
        byte[] datos = formato == Formato.XLSX
                ? generarXlsx(reporte, usuarioActual.username())
                : generarCsv(reporte);
        String nombre = TextoUtil.nombreArchivoSeguro("%s_%s.%s".formatted(reporte.codigo().toLowerCase(Locale.ROOT),
                reporte.generadoEn().format(FECHA_ARCHIVO), formato.extension));
        Archivo archivo = new Archivo(nombre, formato.contentType, datos, DigestUtils.sha256Hex(datos));

        auditoriaService.registrar(AccionAuditoria.EXPORTAR, "REPORTE", null,
                "%s en %s, %d filas, sha256=%s".formatted(reporte.codigo(), formato.extension, reporte.filas().size(),
                        archivo.sha256()));
        return archivo;
    }

    // ---------------------------------------------------------------- generación de archivos (visibles para pruebas)

    static void validar(FiltroReporte filtro) {
        if (filtro.desde() != null && filtro.hasta() != null && filtro.desde().isAfter(filtro.hasta())) {
            throw new SolicitudInvalidaException("La fecha inicial no puede ser posterior a la final");
        }
    }

    static byte[] generarXlsx(ReporteResultado reporte, String usuario) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Font negrita = libro.createFont();
            negrita.setBold(true);
            CellStyle encabezado = libro.createCellStyle();
            encabezado.setFont(negrita);
            encabezado.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Sheet hoja = libro.createSheet("Reporte");
            Row cabecera = hoja.createRow(0);
            for (int i = 0; i < reporte.columnas().size(); i++) {
                Cell celda = cabecera.createCell(i);
                celda.setCellValue(reporte.columnas().get(i));
                celda.setCellStyle(encabezado);
                // Anchos fijos: autoSizeColumn necesita fuentes del sistema y falla en servidores sin ellas
                hoja.setColumnWidth(i, 24 * 256);
            }
            int fila = 1;
            for (List<String> datos : reporte.filas()) {
                Row row = hoja.createRow(fila++);
                for (int i = 0; i < datos.size(); i++) {
                    row.createCell(i).setCellValue(TextoUtil.neutralizarFormula(datos.get(i)));
                }
            }
            hoja.createFreezePane(0, 1);
            hoja.setAutoFilter(new CellRangeAddress(0, 0, 0, reporte.columnas().size() - 1));

            Sheet indicadores = libro.createSheet("Indicadores");
            int n = 0;
            for (Indicador ind : reporte.indicadores()) {
                Row row = indicadores.createRow(n++);
                Cell nombre = row.createCell(0);
                nombre.setCellValue(ind.nombre());
                nombre.setCellStyle(encabezado);
                row.createCell(1).setCellValue(TextoUtil.neutralizarFormula(ind.valor()));
            }
            indicadores.setColumnWidth(0, 34 * 256);
            indicadores.setColumnWidth(1, 40 * 256);

            Sheet info = libro.createSheet("Información");
            List<String[]> meta = new ArrayList<>(List.of(
                    new String[]{"Reporte", reporte.nombre()},
                    new String[]{"Generado", reporte.generadoEn().format(FECHA_HOJA)},
                    new String[]{"Generado por", TextoUtil.neutralizarFormula(usuario)},
                    new String[]{"Registros", String.valueOf(reporte.filas().size())}));
            reporte.filtros().forEach((k, v) -> meta.add(new String[]{"Filtro: " + k, TextoUtil.neutralizarFormula(v)}));
            for (int i = 0; i < meta.size(); i++) {
                Row row = info.createRow(i);
                Cell clave = row.createCell(0);
                clave.setCellValue(meta.get(i)[0]);
                clave.setCellStyle(encabezado);
                row.createCell(1).setCellValue(meta.get(i)[1]);
            }
            info.setColumnWidth(0, 22 * 256);
            info.setColumnWidth(1, 40 * 256);

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new ExportacionException("No se pudo generar el archivo", e);
        }
    }

    static byte[] generarCsv(ReporteResultado reporte) {
        CSVFormat formato = CSVFormat.DEFAULT.builder().setHeader(reporte.columnas().toArray(new String[0])).get();
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            // BOM UTF-8: sin él Excel muestra mal las tildes al abrir el CSV
            salida.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            try (CSVPrinter impresor = new CSVPrinter(new OutputStreamWriter(salida, StandardCharsets.UTF_8), formato)) {
                for (List<String> fila : reporte.filas()) {
                    impresor.printRecord(fila.stream().map(TextoUtil::neutralizarFormula).toList());
                }
            }
            return salida.toByteArray();
        } catch (IOException e) {
            throw new ExportacionException("No se pudo generar el archivo", e);
        }
    }
}
