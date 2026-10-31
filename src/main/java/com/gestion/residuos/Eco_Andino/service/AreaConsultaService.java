package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.AreaResponse;
import com.gestion.residuos.Eco_Andino.entity.Area_Generadora;
import com.gestion.residuos.Eco_Andino.repository.AreaGeneradoraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Lista las áreas activas para los selectores de incidencias y reportes. Solo lectura y sin rutas propias en
 * /api/areas, para no chocar con el CRUD de áreas (CU04) que hará otra persona del equipo.
 */
@Service
@RequiredArgsConstructor
public class AreaConsultaService {
    private final AreaGeneradoraRepository areaRepository;

    @Transactional(readOnly = true)
    public List<AreaResponse> listarActivas() {
        return areaRepository.findAll().stream()
                .filter(a -> "ACTIVA".equals(a.getEstado()))
                .sorted(Comparator.comparing(Area_Generadora::getNombre))
                .map(AreaResponse::from)
                .toList();
    }
}
