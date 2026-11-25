package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.IncidenciaDtos.*;
import com.gestion.residuos.Eco_Andino.dto.Opcion;
import com.gestion.residuos.Eco_Andino.entity.*;
import com.gestion.residuos.Eco_Andino.exception.RecursoNoEncontradoException;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import com.gestion.residuos.Eco_Andino.repository.*;
import com.gestion.residuos.Eco_Andino.util.Codigos;
import com.gestion.residuos.Eco_Andino.util.Etiquetas;
import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Incidencias ambientales y acciones correctivas (CU17) y observaciones operativas (CU18). */
@Service
@RequiredArgsConstructor
public class IncidenciaService {
    private static final Map<String, Integer> ORDEN_ESTADO = Map.of(
            Incidencia.ABIERTA, 0, Incidencia.EN_SEGUIMIENTO, 1, Incidencia.CERRADA, 2);
    private static final Set<String> SEVERIDADES = Set.of("ALTA", "MEDIA", "BAJA");

    private final IncidenciaRepository incidenciaRepository;
    private final ObservacionRepository observacionRepository;
    private final RecoleccionRepository recoleccionRepository;
    private final ContenedorRepository contenedorRepository;
    private final GeneracionRepository generacionRepository;
    private final CatalogoService catalogos;
    private final DocumentoService documentos;
    private final VistaService vistas;
    private final SesionService sesion;
    private final AuditoriaService auditoria;

    /** Primero las abiertas, luego en seguimiento y al final las cerradas; dentro de cada grupo, las más recientes. */
    @Transactional(readOnly = true)
    public List<IncidenciaDto> listar() {
        VistaService.Instantanea foto = vistas.instantanea();
        return incidenciaRepository.findAll().stream()
                .sorted(Comparator.comparing((Incidencia i) -> ORDEN_ESTADO.getOrDefault(i.getEstado(), 9))
                        .thenComparing(Incidencia::getFecha, Comparator.reverseOrder()))
                .map(i -> vistas.incidencia(i, foto))
                .toList();
    }

    @Transactional(readOnly = true)
    public OpcionesIncidencia opciones() {
        List<Opcion> referencias = new ArrayList<>();
        recoleccionRepository.findAll(Sort.by(Sort.Direction.DESC, "idRecoleccion"))
                .forEach(o -> referencias.add(new Opcion(o.getNumeroOrden(), o.getNumeroOrden() + " · Orden de recolección")));
        contenedorRepository.findAll(Sort.by("codigo"))
                .forEach(c -> referencias.add(new Opcion(c.getCodigo(), c.getCodigo() + " · Contenedor")));
        generacionRepository.findAll(Sort.by(Sort.Direction.DESC, "idGeneracion"))
                .forEach(g -> referencias.add(new Opcion(g.getCodigo(), g.getCodigo() + " · Registro de generación")));
        return new OpcionesIncidencia(catalogos.listarAreasActivas(), referencias, catalogos.listarResponsables());
    }

    @Transactional
    public IncidenciaDto reportar(IncidenciaRequest datos, MultipartFile evidencia) {
        if (!Etiquetas.TIPO_INCIDENCIA.containsKey(datos.tipo())) {
            throw new SolicitudInvalidaException("El tipo de incidencia no es válido.");
        }
        CatalogoService.validarValor(SEVERIDADES, datos.severidad(), "La severidad debe ser ALTA, MEDIA o BAJA.");
        boolean conEvidencia = evidencia != null && !evidencia.isEmpty();
        if ("ALTA".equals(datos.severidad()) && !conEvidencia) {
            throw new SolicitudInvalidaException("Adjunta una evidencia para incidencias de severidad alta.");
        }

        Incidencia incidencia = new Incidencia();
        incidencia.setTipo(datos.tipo());
        incidencia.setDescripcion(datos.descripcion().trim());
        incidencia.setArea(catalogos.buscarArea(datos.idArea()));
        incidencia.setReferencia(TextoUtil.normalizarCodigo(datos.referencia()));
        incidencia.setSeveridad(datos.severidad());
        incidencia.setEstado(Incidencia.ABIERTA);
        incidencia.setFecha(sesion.ahora());
        incidencia.setReportadoPor(sesion.usuario());
        incidenciaRepository.save(incidencia);
        incidencia.setCodigo(Codigos.incidencia(incidencia.getIdIncidencia()));

        if (conEvidencia) {
            documentos.registrar("EVIDENCIA_FOTO", incidencia.getCodigo(), "Evidencia de la incidencia", evidencia, null);
        }
        auditoria.registrar("CREAR", "Incidencia", incidencia.getCodigo(),
                "Reportó: " + Etiquetas.de(Etiquetas.TIPO_INCIDENCIA, incidencia.getTipo()).toLowerCase());
        return vistas.incidencia(incidencia, vistas.instantanea());
    }

    @Transactional
    public IncidenciaDto registrarSeguimiento(Integer id, SeguimientoRequest datos) {
        Incidencia incidencia = buscar(id);
        if (Incidencia.CERRADA.equals(incidencia.getEstado())) {
            throw new SolicitudInvalidaException("La incidencia ya está cerrada.");
        }
        String nota = datos.nota().trim();
        incidencia.setResponsable(catalogos.buscarResponsable(datos.responsable()));
        incidencia.setAccionCorrectiva(datos.accionCorrectiva().trim());
        incidencia.setEstado(Incidencia.EN_SEGUIMIENTO);
        incidencia.agregarSeguimiento(sesion.usuario(), nota, sesion.ahora());
        auditoria.registrar("MODIFICAR", "Incidencia", incidencia.getCodigo(), "Registró seguimiento: " + nota);
        return vistas.incidencia(incidencia, vistas.instantanea());
    }

    /** No se cierra sin responsable ni acción correctiva (CU17, secuencia alternativa 2). */
    @Transactional
    public IncidenciaDto cerrar(Integer id, String nota) {
        Incidencia incidencia = buscar(id);
        if (Incidencia.CERRADA.equals(incidencia.getEstado())) {
            throw new SolicitudInvalidaException("La incidencia ya está cerrada.");
        }
        if (incidencia.getResponsable() == null || StringUtils.isBlank(incidencia.getAccionCorrectiva())) {
            throw new SolicitudInvalidaException("Asigna un responsable y una acción correctiva antes de cerrar.");
        }
        String texto = nota.trim();
        incidencia.setEstado(Incidencia.CERRADA);
        incidencia.setFechaCierre(sesion.ahora());
        incidencia.agregarSeguimiento(sesion.usuario(), "Incidencia cerrada: " + texto, incidencia.getFechaCierre());
        auditoria.registrar("CERRAR", "Incidencia", incidencia.getCodigo(), "Cerró la incidencia: " + texto);
        return vistas.incidencia(incidencia, vistas.instantanea());
    }

    // ---------------------------------------------------------------- observaciones (CU18)

    @Transactional(readOnly = true)
    public List<ObservacionDto> listarObservaciones() {
        return observacionRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha", "idObservacion")).stream()
                .map(vistas::observacion)
                .toList();
    }

    @Transactional
    public ObservacionDto registrarObservacion(ObservacionRequest datos) {
        if (!Etiquetas.AMBITO_OBSERVACION.containsKey(datos.ambito())) {
            throw new SolicitudInvalidaException("El ámbito de la observación no es válido.");
        }
        Usuario usuario = sesion.usuario();
        Observacion observacion = new Observacion();
        observacion.setFecha(sesion.ahora());
        observacion.setAutor(usuario);
        observacion.setRol(sesion.nombreRol(usuario, "OBSERVACION_REGISTRAR"));
        observacion.setAmbito(datos.ambito());
        observacion.setReferencia(TextoUtil.normalizarCodigo(datos.referencia()));
        observacion.setTexto(datos.texto().trim());
        observacionRepository.save(observacion);

        String ambito = Etiquetas.de(Etiquetas.AMBITO_OBSERVACION, datos.ambito());
        auditoria.registrar("REGISTRAR", "Observación",
                StringUtils.defaultIfBlank(observacion.getReferencia(), ambito),
                "Registró una observación de " + ambito.toLowerCase());
        return vistas.observacion(observacion);
    }

    private Incidencia buscar(Integer id) {
        return incidenciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Incidencia no encontrada"));
    }
}
