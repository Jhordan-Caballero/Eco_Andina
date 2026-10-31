package com.gestion.residuos.Eco_Andino.service.reporte;

import com.gestion.residuos.Eco_Andino.entity.Residuo;
import com.gestion.residuos.Eco_Andino.repository.ResiduoRepository;
import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.gestion.residuos.Eco_Andino.service.reporte.FormatoReporte.texto;

/** Catálogo de residuos con su clasificación. No usa filtros. */
@Component
@RequiredArgsConstructor
public class ReporteCatalogoResiduos implements ReporteGenerador {
    private final ResiduoRepository residuoRepository;

    @Override
    public String codigo() {
        return "CATALOGO_RESIDUOS";
    }

    @Override
    public String nombre() {
        return "Catálogo de residuos";
    }

    @Override
    public String descripcion() {
        return "Residuos registrados con su categoría, peligrosidad, unidad y tratamiento.";
    }

    @Override
    public List<String> filtros() {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteResultado generar(FiltroReporte filtro, LocalDateTime ahora) {
        List<Residuo> residuos = residuoRepository.findAll(Sort.by("codigo"));
        List<List<String>> filas = new ArrayList<>();
        for (Residuo r : residuos) {
            filas.add(List.of(r.getCodigo(), r.getNombre(), TextoUtil.etiqueta(r.getCategoria()),
                    TextoUtil.etiqueta(r.getPeligrosidad()), texto(r.getUnidad()), texto(r.getTratamiento()),
                    texto(r.getRequisitos())));
        }
        List<Indicador> indicadores = new ArrayList<>();
        indicadores.add(new Indicador("Total de residuos", String.valueOf(residuos.size())));
        Map<String, Long> porCategoria = residuos.stream().collect(
                Collectors.groupingBy(r -> TextoUtil.etiqueta(r.getCategoria()), TreeMap::new, Collectors.counting()));
        porCategoria.forEach((categoria, total) ->
                indicadores.add(new Indicador("Categoría " + categoria, String.valueOf(total))));

        return new ReporteResultado(codigo(), nombre(), ahora, new LinkedHashMap<>(),
                List.of("Código", "Nombre", "Categoría", "Peligrosidad", "Unidad", "Tratamiento", "Requisitos"),
                filas, indicadores);
    }
}
