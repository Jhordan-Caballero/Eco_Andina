package com.gestion.residuos.Eco_Andino.service.reporte;

import com.gestion.residuos.Eco_Andino.entity.EstadoIncidencia;
import com.gestion.residuos.Eco_Andino.entity.Incidencia;
import com.gestion.residuos.Eco_Andino.entity.Severidad;
import com.gestion.residuos.Eco_Andino.repository.IncidenciaRepository;
import com.gestion.residuos.Eco_Andino.repository.IncidenciaSpecs;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.gestion.residuos.Eco_Andino.service.reporte.FormatoReporte.fecha;
import static com.gestion.residuos.Eco_Andino.service.reporte.FormatoReporte.texto;

/** Reporte de incidencias por periodo, área y estado, con indicadores de cierre. */
@Component
@RequiredArgsConstructor
public class ReporteIncidencias implements ReporteGenerador {
    static final int MAX_FILAS = 5000;

    private final IncidenciaRepository incidenciaRepository;

    @Override
    public String codigo() {
        return "INCIDENCIAS";
    }

    @Override
    public String nombre() {
        return "Incidencias y acciones correctivas";
    }

    @Override
    public String descripcion() {
        return "Incidencias registradas, su gravedad, responsable, estado y tiempo de cierre.";
    }

    @Override
    public List<String> filtros() {
        return List.of("periodo", "area", "estado");
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteResultado generar(FiltroReporte filtro, LocalDateTime ahora) {
        EstadoIncidencia estado = parseEstado(filtro.estado());
        var spec = IncidenciaSpecs.filtrar(estado, null, null, filtro.areaId(),
                filtro.desde() == null ? null : filtro.desde().atStartOfDay(),
                filtro.hasta() == null ? null : filtro.hasta().plusDays(1).atStartOfDay());
        var page = incidenciaRepository.findAll(spec,
                PageRequest.of(0, MAX_FILAS, Sort.by(Sort.Direction.DESC, "fecha", "idIncidencia")));
        List<Incidencia> incidencias = page.getContent();

        List<List<String>> filas = new ArrayList<>();
        for (Incidencia i : incidencias) {
            filas.add(List.of(String.valueOf(i.getIdIncidencia()), fecha(i.getFecha()), i.getTipo().name(),
                    i.getArea().getNombre(), i.getSeveridad().name(),
                    i.getResponsable().getNombres() + " " + i.getResponsable().getApellidos(),
                    i.getEstado().name(), fecha(i.getFechaCierre()), texto(i.getAccionCorrectiva())));
        }

        List<Indicador> indicadores = new ArrayList<>();
        indicadores.add(new Indicador("Total de incidencias", String.valueOf(page.getTotalElements())));
        for (EstadoIncidencia e : EstadoIncidencia.values()) {
            indicadores.add(new Indicador("Incidencias " + e.name().toLowerCase().replace('_', ' '),
                    String.valueOf(incidencias.stream().filter(i -> i.getEstado() == e).count())));
        }
        indicadores.add(new Indicador("Gravedad alta",
                String.valueOf(incidencias.stream().filter(i -> i.getSeveridad() == Severidad.ALTA).count())));
        long cerradas = incidencias.stream().filter(Incidencia::estaCerrada).count();
        if (!incidencias.isEmpty()) {
            indicadores.add(new Indicador("Porcentaje cerradas", "%.1f%%".formatted(100.0 * cerradas / incidencias.size())));
        }
        incidencias.stream().filter(i -> i.estaCerrada() && i.getFechaCierre() != null)
                .mapToLong(i -> Duration.between(i.getFecha(), i.getFechaCierre()).toMinutes())
                .average()
                .ifPresent(min -> indicadores.add(new Indicador("Tiempo promedio de cierre",
                        "%.1f horas".formatted(min / 60.0))));
        if (page.getTotalElements() > MAX_FILAS) {
            indicadores.add(new Indicador("Aviso",
                    "Se muestran solo los primeros %d de %d registros; acota el periodo".formatted(MAX_FILAS,
                            page.getTotalElements())));
        }

        Map<String, String> aplicados = new LinkedHashMap<>();
        if (filtro.desde() != null) aplicados.put("Desde", filtro.desde().toString());
        if (filtro.hasta() != null) aplicados.put("Hasta", filtro.hasta().toString());
        if (filtro.areaId() != null) aplicados.put("Área", incidencias.isEmpty() ? "#" + filtro.areaId()
                : incidencias.get(0).getArea().getNombre());
        if (estado != null) aplicados.put("Estado", estado.name());

        return new ReporteResultado(codigo(), nombre(), ahora, aplicados,
                List.of("N.º", "Fecha", "Tipo", "Área", "Gravedad", "Responsable", "Estado", "Cierre",
                        "Acción correctiva"),
                filas, indicadores);
    }

    private static EstadoIncidencia parseEstado(String valor) {
        String limpio = TextoUtil.normalizar(valor);
        if (limpio == null) {
            return null;
        }
        try {
            return EstadoIncidencia.valueOf(limpio.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SolicitudInvalidaException("El estado indicado no es válido");
        }
    }
}
