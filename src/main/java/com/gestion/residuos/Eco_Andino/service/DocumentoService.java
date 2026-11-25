package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.DocumentoDtos.DocumentoDto;
import com.gestion.residuos.Eco_Andino.dto.DocumentoDtos.DocumentoRequest;
import com.gestion.residuos.Eco_Andino.dto.Opcion;
import com.gestion.residuos.Eco_Andino.entity.Documento;
import com.gestion.residuos.Eco_Andino.entity.Generacion;
import com.gestion.residuos.Eco_Andino.entity.Incidencia;
import com.gestion.residuos.Eco_Andino.entity.Recoleccion;
import com.gestion.residuos.Eco_Andino.exception.RecursoNoEncontradoException;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import com.gestion.residuos.Eco_Andino.repository.DocumentoRepository;
import com.gestion.residuos.Eco_Andino.repository.GeneracionRepository;
import com.gestion.residuos.Eco_Andino.repository.IncidenciaRepository;
import com.gestion.residuos.Eco_Andino.repository.RecoleccionRepository;
import com.gestion.residuos.Eco_Andino.util.Etiquetas;
import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/** Documentos y evidencias (CU16): adjuntar, consultar, validar, rechazar y descargar. */
@Service
@RequiredArgsConstructor
public class DocumentoService {
    /** Permisos con los que se revisan (validan o rechazan) documentos. */
    public static final List<String> PERMISOS_REVISION = List.of("GENERACION_VALIDAR", "RECOLECCION_PROGRAMAR");

    public record Descarga(Resource recurso, String nombreArchivo, String tipoContenido) {
    }

    private final DocumentoRepository documentoRepository;
    private final RecoleccionRepository recoleccionRepository;
    private final GeneracionRepository generacionRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final ArchivoService archivos;
    private final VistaService vistas;
    private final SesionService sesion;
    private final AuditoriaService auditoria;

    @Transactional(readOnly = true)
    public List<DocumentoDto> listar() {
        return documentoRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha", "idDocumento")).stream()
                .map(vistas::documento)
                .toList();
    }

    /** Registros a los que se puede adjuntar un documento: órdenes, generaciones e incidencias. */
    @Transactional(readOnly = true)
    public List<Opcion> referencias() {
        List<Opcion> opciones = new ArrayList<>();
        recoleccionRepository.findAll(Sort.by(Sort.Direction.DESC, "idRecoleccion"))
                .forEach(o -> opciones.add(new Opcion(o.getNumeroOrden(), o.getNumeroOrden() + " · Orden de recolección")));
        generacionRepository.findAll(Sort.by(Sort.Direction.DESC, "idGeneracion"))
                .forEach(g -> opciones.add(new Opcion(g.getCodigo(), g.getCodigo() + " · Registro de generación")));
        incidenciaRepository.findAll(Sort.by(Sort.Direction.DESC, "idIncidencia"))
                .forEach(i -> opciones.add(new Opcion(i.getCodigo(), i.getCodigo() + " · Incidencia")));
        return opciones;
    }

    /** Adjuntar desde el módulo de documentos: el archivo es obligatorio y la orden no debe estar cerrada. */
    @Transactional
    public DocumentoDto adjuntar(DocumentoRequest datos, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new SolicitudInvalidaException("Selecciona el archivo.");
        }
        String referencia = TextoUtil.normalizarCodigo(datos.referencia());
        validarReferencia(referencia);
        recoleccionRepository.findByNumeroOrden(referencia)
                .filter(o -> Recoleccion.CERRADA.equals(o.getEstado()))
                .ifPresent(o -> {
                    throw new SolicitudInvalidaException("La orden " + o.getNumeroOrden() + " ya está cerrada.");
                });
        return vistas.documento(registrar(datos.tipo(), referencia, datos.descripcion(), archivo, null));
    }

    /**
     * Registra un documento asociado a un registro. Si no llega archivo, se guarda solo la referencia con el
     * nombre indicado (ej. el número del manifiesto al registrar la salida).
     */
    @Transactional
    public Documento registrar(String tipo, String referencia, String descripcion, MultipartFile archivo,
                               String nombreSinArchivo) {
        if (!Etiquetas.TIPO_DOCUMENTO.containsKey(tipo)) {
            throw new SolicitudInvalidaException("El tipo de documento no es válido.");
        }
        Documento documento = new Documento();
        documento.setTipo(tipo);
        documento.setReferencia(referencia);
        documento.setDescripcion(StringUtils.defaultIfBlank(StringUtils.trimToNull(descripcion),
                Etiquetas.TIPO_DOCUMENTO.get(tipo)));
        documento.setFecha(sesion.ahora());
        documento.setSubidoPor(sesion.usuario());
        documento.setEstado(Documento.PENDIENTE);
        if (archivo != null && !archivo.isEmpty()) {
            ArchivoService.Guardado guardado = archivos.guardar(archivo);
            documento.setNombreArchivo(TextoUtil.nombreArchivoSeguro(archivo.getOriginalFilename()));
            documento.setRutaArchivo(guardado.nombreAlmacenado());
            documento.setTipoContenido(guardado.tipoContenido());
        } else {
            documento.setNombreArchivo(TextoUtil.nombreArchivoSeguro(nombreSinArchivo));
        }
        documentoRepository.save(documento);
        auditoria.registrar("ADJUNTAR", "Documento", documento.getNombreArchivo(),
                "Adjuntó " + StringUtils.uncapitalize(Etiquetas.TIPO_DOCUMENTO.get(tipo)) + " a " + referencia);
        return documento;
    }

    @Transactional
    public DocumentoDto validar(Integer id) {
        Documento documento = pendiente(id);
        documento.setEstado(Documento.VALIDADO);
        documento.setMotivoRechazo(null);
        auditoria.registrar("VALIDAR", "Documento", documento.getNombreArchivo(),
                "Validó el documento de " + documento.getReferencia());
        return vistas.documento(documento);
    }

    @Transactional
    public DocumentoDto rechazar(Integer id, String motivo) {
        Documento documento = pendiente(id);
        documento.setEstado(Documento.RECHAZADO);
        documento.setMotivoRechazo(motivo.trim());
        auditoria.registrar("RECHAZAR", "Documento", documento.getNombreArchivo(),
                "Rechazó el documento: " + motivo.trim());
        return vistas.documento(documento);
    }

    @Transactional(readOnly = true)
    public Descarga descargar(Integer id) {
        if (!sesion.tienePermiso("DOCUMENTO_GESTIONAR") && !sesion.tienePermiso("AUDITORIA_CONSULTAR")) {
            throw new AccessDeniedException("Sin permiso para descargar documentos");
        }
        Documento documento = buscar(id);
        if (documento.getRutaArchivo() == null) {
            throw new RecursoNoEncontradoException("Este documento solo tiene registrada la referencia, sin archivo adjunto.");
        }
        return new Descarga(archivos.cargar(documento.getRutaArchivo()), documento.getNombreArchivo(),
                documento.getTipoContenido());
    }

    private void validarReferencia(String referencia) {
        boolean existe = recoleccionRepository.findByNumeroOrden(referencia).isPresent()
                || generacionRepository.findAll().stream().map(Generacion::getCodigo).anyMatch(referencia::equals)
                || incidenciaRepository.findAll().stream().map(Incidencia::getCodigo).anyMatch(referencia::equals);
        if (!existe) {
            throw new SolicitudInvalidaException("La referencia " + referencia + " no corresponde a ningún registro.");
        }
    }

    private Documento pendiente(Integer id) {
        Documento documento = buscar(id);
        if (!Documento.PENDIENTE.equals(documento.getEstado())) {
            throw new SolicitudInvalidaException("El documento ya fue revisado.");
        }
        return documento;
    }

    private Documento buscar(Integer id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Documento no encontrado"));
    }
}
