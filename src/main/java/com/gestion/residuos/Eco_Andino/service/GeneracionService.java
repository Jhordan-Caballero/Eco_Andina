package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.GeneracionRequest;
import com.gestion.residuos.Eco_Andino.entity.*;
import com.gestion.residuos.Eco_Andino.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeneracionService {

    private final GeneracionRepository generacionRepository;
    private final AreaGeneradoraRepository areaRepository;
    private final ResiduoRepository residuoRepository;
    private final ContenedorRepository contenedorRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Generacion registrar(GeneracionRequest request) {
        Area_Generadora area = areaRepository.findById(request.idArea())
                .orElseThrow(() -> new EntityNotFoundException("Área no encontrada"));

        Residuo residuo = residuoRepository.findById(request.idResiduo())
                .orElseThrow(() -> new EntityNotFoundException("Residuo no encontrado"));

        Contenedor contenedor = contenedorRepository.findById(request.idContenedor())
                .orElseThrow(() -> new EntityNotFoundException("Contenedor no encontrado"));

        Usuario responsable = usuarioRepository.findById(request.idResponsable())
                .orElseThrow(() -> new EntityNotFoundException("Responsable no encontrado"));

        if (contenedor.getResiduoPermitido() == null ||
                !contenedor.getResiduoPermitido().getIdResiduo()
                        .equals(residuo.getIdResiduo())) {
            throw new IllegalArgumentException(
                    "El contenedor no está habilitado para el residuo seleccionado");
        }

        Generacion generacion = new Generacion();
        generacion.setArea(area);
        generacion.setResiduo(residuo);
        generacion.setContenedor(contenedor);
        generacion.setCantidad(request.cantidad());
        generacion.setFecha(request.fecha());
        generacion.setResponsable(responsable);
        generacion.setCondicionEnvase(request.condicionEnvase());
        generacion.setEvidenciaUrl(request.evidenciaUrl());
        generacion.setEstadoValidacion(Generacion.PENDIENTE);

        return generacionRepository.save(generacion);
    }

    @Transactional(readOnly = true)
    public List<Generacion> listarPendientes() {
        return generacionRepository.findByEstadoValidacionOrderByFechaDesc(
                Generacion.PENDIENTE);
    }

    @Transactional
    public Generacion validar(Integer id) {
        Generacion generacion = buscarPorId(id);

        if (!Generacion.PENDIENTE.equals(generacion.getEstadoValidacion())) {
            throw new IllegalArgumentException(
                    "Solo se pueden validar registros pendientes");
        }

        generacion.setEstadoValidacion(Generacion.VALIDADA);
        return generacionRepository.save(generacion);
    }

    @Transactional
    public Generacion observar(Integer id, String observaciones) {
        if (observaciones == null || observaciones.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes indicar las observaciones");
        }

        Generacion generacion = buscarPorId(id);

        if (!Generacion.PENDIENTE.equals(generacion.getEstadoValidacion())) {
            throw new IllegalArgumentException(
                    "Solo se pueden observar registros pendientes");
        }

        generacion.setEstadoValidacion(Generacion.OBSERVADA);
        generacion.setObservaciones(observaciones);

        return generacionRepository.save(generacion);
    }
    @Transactional(readOnly = true)
    public List<Generacion> listarTodas() {
        return generacionRepository.findAll();
    }

    private Generacion buscarPorId(Integer id) {
        return generacionRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Generación no encontrada"));
    }
}
