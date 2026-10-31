package com.gestion.residuos.Eco_Andino.service.reporte;

import com.gestion.residuos.Eco_Andino.entity.Observacion;
import com.gestion.residuos.Eco_Andino.entity.TipoRegistro;
import com.gestion.residuos.Eco_Andino.repository.ObservacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.gestion.residuos.Eco_Andino.service.reporte.FormatoReporte.fecha;

/** Observaciones operativas registradas por los actores, por periodo. */
@Component
@RequiredArgsConstructor
public class ReporteObservaciones implements ReporteGenerador {
    private final ObservacionRepository observacionRepository;

    @Override
    public String codigo() {
        return "OBSERVACIONES";
    }

    @Override
    public String nombre() {
        return "Observaciones operativas";
    }

    @Override
    public String descripcion() {
        return "Observaciones sobre generación, almacenamiento, traslado y recepción.";
    }

    @Override
    public List<String> filtros() {
        return List.of("periodo");
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteResultado generar(FiltroReporte filtro, LocalDateTime ahora) {
        LocalDateTime desde = filtro.desde() == null ? LocalDate.of(2000, 1, 1).atStartOfDay()
                : filtro.desde().atStartOfDay();
        LocalDateTime hasta = filtro.hasta() == null ? LocalDate.of(9999, 12, 31).atStartOfDay()
                : filtro.hasta().plusDays(1).atStartOfDay();
        List<Observacion> observaciones = observacionRepository.findPorPeriodo(desde, hasta,
                PageRequest.of(0, ReporteIncidencias.MAX_FILAS));

        List<List<String>> filas = new ArrayList<>();
        for (Observacion o : observaciones) {
            filas.add(List.of(fecha(o.getFecha()), o.getTipoRegistro().name(), String.valueOf(o.getIdRegistro()),
                    o.getUsuario().getNombres() + " " + o.getUsuario().getApellidos(), o.getDescripcion()));
        }
        List<Indicador> indicadores = new ArrayList<>();
        indicadores.add(new Indicador("Total de observaciones", String.valueOf(observaciones.size())));
        for (TipoRegistro tipo : TipoRegistro.values()) {
            indicadores.add(new Indicador("Sobre " + tipo.name().toLowerCase(),
                    String.valueOf(observaciones.stream().filter(o -> o.getTipoRegistro() == tipo).count())));
        }

        Map<String, String> aplicados = new LinkedHashMap<>();
        if (filtro.desde() != null) aplicados.put("Desde", filtro.desde().toString());
        if (filtro.hasta() != null) aplicados.put("Hasta", filtro.hasta().toString());
        return new ReporteResultado(codigo(), nombre(), ahora, aplicados,
                List.of("Fecha", "Tipo de registro", "N.º de registro", "Autor", "Observación"), filas, indicadores);
    }
}
