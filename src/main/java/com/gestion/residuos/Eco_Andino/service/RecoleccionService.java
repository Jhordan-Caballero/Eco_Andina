package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.GestorDto;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.TransportistaDto;
import com.gestion.residuos.Eco_Andino.dto.RecoleccionDtos.*;
import com.gestion.residuos.Eco_Andino.entity.*;
import com.gestion.residuos.Eco_Andino.exception.RecursoNoEncontradoException;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import com.gestion.residuos.Eco_Andino.repository.*;
import com.gestion.residuos.Eco_Andino.util.Codigos;
import com.gestion.residuos.Eco_Andino.util.Etiquetas;
import com.gestion.residuos.Eco_Andino.util.Numeros;
import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Órdenes de recolección: programación y aprobación (CU12), consulta (CU13), salida y traslado (CU14),
 * recepción y destino (CU15) y cierre con la regla de integridad RNF003.
 */
@Service
@RequiredArgsConstructor
public class RecoleccionService {
    private final RecoleccionRepository recoleccionRepository;
    private final GeneracionRepository generacionRepository;
    private final VehiculoRepository vehiculoRepository;
    private final MovimientoRepository movimientoRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final ObservacionRepository observacionRepository;
    private final TransportistaService transportistas;
    private final DocumentoService documentos;
    private final VistaService vistas;
    private final SesionService sesion;
    private final AuditoriaService auditoria;

    @Transactional(readOnly = true)
    public List<RecoleccionVista> listar() {
        VistaService.Instantanea foto = vistas.instantanea();
        return foto.recolecciones().stream()
                .sorted(Comparator.comparing(Recoleccion::getIdRecoleccion).reversed())
                .map(o -> vistas.recoleccion(o, foto))
                .toList();
    }

    @Transactional(readOnly = true)
    public RecoleccionDetalle detalle(Integer id) {
        Recoleccion orden = buscar(id);
        VistaService.Instantanea foto = vistas.instantanea();
        return new RecoleccionDetalle(vistas.recoleccion(orden, foto),
                observacionRepository.findByReferenciaOrderByFechaDesc(orden.getNumeroOrden()).stream()
                        .map(vistas::observacion).toList(),
                incidenciaRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha")).stream()
                        .filter(i -> orden.getNumeroOrden().equals(i.getReferencia()))
                        .map(i -> vistas.incidencia(i, foto))
                        .toList());
    }

    /** Registros validados que aún no tienen orden, con los transportistas y gestores disponibles. */
    @Transactional(readOnly = true)
    public OpcionesProgramacion opciones() {
        VistaService.Instantanea foto = vistas.instantanea();
        List<TransportistaDto> listaTransportistas = transportistas.listarTransportistas();
        List<GestorDto> gestores = transportistas.listarGestores();
        return new OpcionesProgramacion(
                foto.generaciones().stream()
                        .filter(g -> Generacion.VALIDADA.equals(g.getEstadoValidacion()) && foto.ordenDe(g).isEmpty())
                        .sorted(Comparator.comparing(Generacion::getFecha))
                        .map(g -> vistas.generacion(g, foto))
                        .toList(),
                listaTransportistas, gestores);
    }

    /** CU12: crea la orden en estado PENDIENTE hasta que se apruebe. */
    @Transactional
    public RecoleccionVista programar(ProgramarRequest datos) {
        VistaService.Instantanea foto = vistas.instantanea();
        List<Generacion> generaciones = datos.idsGeneracion().stream().distinct()
                .map(id -> generacionRepository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Registro de generación no encontrado")))
                .toList();
        if (generaciones.stream().anyMatch(g -> !Generacion.VALIDADA.equals(g.getEstadoValidacion())
                || foto.ordenDe(g).isPresent())) {
            throw new SolicitudInvalidaException("Solo se pueden programar registros validados que no tengan otra orden.");
        }
        if (datos.fechaProgramada().isBefore(sesion.hoy())) {
            throw new SolicitudInvalidaException("La fecha programada no puede ser anterior a hoy.");
        }
        Transportista transportista = transportistas.buscarTransportista(datos.idTransportista());
        Vehiculo vehiculo = vehiculoRepository.findById(datos.idVehiculo())
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado"));
        Gestor gestor = transportistas.buscarGestor(datos.idGestor());
        validarAsignacion(transportista, vehiculo, gestor);

        Recoleccion orden = new Recoleccion();
        orden.setEstado(Recoleccion.PENDIENTE);
        orden.setFechaSolicitud(sesion.ahora());
        orden.setSolicitadoPor(sesion.usuario());
        orden.setFechaProgramada(datos.fechaProgramada());
        orden.setTransportista(transportista);
        orden.setVehiculo(vehiculo);
        orden.setGestor(gestor);
        orden.setObservaciones(TextoUtil.normalizar(datos.observaciones()));
        generaciones.forEach(orden::agregarDetalle);
        recoleccionRepository.save(orden);
        orden.setNumeroOrden(Codigos.orden(orden.getIdRecoleccion()));

        RecoleccionVista vista = vistas.recoleccion(orden, vistas.instantanea());
        auditoria.registrar("CREAR", "Recolección", orden.getNumeroOrden(), "Programó la recolección de "
                + Numeros.cantidad(vista.cantidadTotal(), vista.unidad()) + " de "
                + String.join(", ", vista.residuos()).toLowerCase(Locale.ROOT));
        return vista;
    }

    /** CU12: aprobar la orden la deja PROGRAMADA para el transportista. */
    @Transactional
    public RecoleccionVista aprobar(Integer id) {
        Recoleccion orden = enEstado(id, Recoleccion.PENDIENTE, "aprobar");
        validarAsignacion(orden.getTransportista(), orden.getVehiculo(), orden.getGestor());
        orden.setEstado(Recoleccion.PROGRAMADA);
        orden.setFechaAprobacion(sesion.ahora());
        orden.setAprobadoPor(sesion.usuario());
        auditoria.registrar("APROBAR", "Recolección", orden.getNumeroOrden(), "Aprobó la orden de recolección");
        return vistas.recoleccion(orden, vistas.instantanea());
    }

    /** CU14: el transportista registra la salida con su documento de traslado; los residuos dejan la planta. */
    @Transactional
    public RecoleccionVista registrarSalida(Integer id, SalidaRequest datos, MultipartFile archivo) {
        Recoleccion orden = enEstado(id, Recoleccion.PROGRAMADA, "registrar la salida");
        RecoleccionVista vista = vistas.recoleccion(orden, vistas.instantanea());
        String tipoDocumento = datos.tipoDocumento();
        if (!List.of("MANIFIESTO", "GUIA_REMISION").contains(tipoDocumento)) {
            throw new SolicitudInvalidaException("El documento de traslado debe ser un manifiesto o una guía de remisión.");
        }
        if (vista.peligroso() && !"MANIFIESTO".equals(tipoDocumento)) {
            throw new SolicitudInvalidaException("Los residuos peligrosos requieren manifiesto de manejo.");
        }
        String numero = datos.numeroDocumento().trim().toUpperCase(Locale.ROOT);
        Usuario usuario = sesion.usuario();

        orden.setEstado(Recoleccion.EN_TRANSITO);
        orden.setFechaSalida(sesion.aLocal(datos.fecha()));
        orden.setDocumentoSalida(numero);
        orden.setSalidaPor(usuario);
        orden.setObservacionesSalida(TextoUtil.normalizar(datos.observaciones()));

        String nombrePorDefecto = ("MANIFIESTO".equals(tipoDocumento) ? "manifiesto-" : "guia-") + numero + ".pdf";
        documentos.registrar(tipoDocumento, orden.getNumeroOrden(), "Documento de traslado " + numero, archivo,
                nombrePorDefecto);

        String unidadTransporte = orden.getTransportista().getEmpresa() + " · " + orden.getVehiculo().getPlaca();
        for (DetalleRecoleccion detalle : orden.getDetalles()) {
            Generacion generacion = detalle.getGeneracion();
            Movimiento salida = new Movimiento();
            salida.setTipo(Movimiento.SALIDA);
            salida.setFecha(orden.getFechaSalida());
            salida.setResiduo(generacion.getResiduo());
            salida.setOrigen(generacion.getContenedor().getCodigo());
            salida.setDestino(unidadTransporte);
            salida.setCantidad(generacion.getCantidad());
            salida.setReferencia(orden.getNumeroOrden());
            salida.setUsuario(usuario);
            salida.setMotivo("Salida por recolección");
            salida.setAfectaStock(false);
            movimientoRepository.save(salida);
            salida.setCodigo(Codigos.movimiento(salida.getIdMovimiento()));
        }

        auditoria.registrar("REGISTRAR", "Recolección", orden.getNumeroOrden(),
                "Registró la salida con el documento " + numero);
        return vistas.recoleccion(orden, vistas.instantanea());
    }

    /** CU15: el gestor confirma la recepción, la cantidad recibida y el destino. */
    @Transactional
    public RecoleccionVista registrarRecepcion(Integer id, RecepcionRequest datos, MultipartFile acta,
                                               MultipartFile certificado) {
        Recoleccion orden = enEstado(id, Recoleccion.EN_TRANSITO, "registrar la recepción");
        if (!Etiquetas.TIPO_DESTINO.containsKey(datos.tipoDestino())) {
            throw new SolicitudInvalidaException("El tipo de destino no es válido.");
        }
        boolean conforme = !Boolean.FALSE.equals(datos.conforme());
        String observaciones = TextoUtil.normalizar(datos.observaciones());
        if (!conforme && observaciones == null) {
            throw new SolicitudInvalidaException("Describe el motivo de la recepción no conforme.");
        }
        RecoleccionVista vista = vistas.recoleccion(orden, vistas.instantanea());
        Usuario usuario = sesion.usuario();

        orden.setEstado(Recoleccion.RECIBIDA);
        orden.setFechaRecepcion(sesion.aLocal(datos.fecha()));
        orden.setCantidadRecibida(datos.cantidadRecibida());
        orden.setRecepcionConforme(conforme);
        orden.setRecibidoPor(usuario);
        orden.setObservacionesRecepcion(observaciones);
        orden.setTipoDestino(datos.tipoDestino());
        orden.setTratamientoDestino(datos.tratamiento().trim());

        if (acta != null && !acta.isEmpty()) {
            documentos.registrar("ACTA_RECEPCION", orden.getNumeroOrden(), "Acta de recepción firmada por el gestor",
                    acta, null);
        }
        if (certificado != null && !certificado.isEmpty()) {
            documentos.registrar("CERTIFICADO", orden.getNumeroOrden(), "Certificado de "
                    + Etiquetas.de(Etiquetas.TIPO_DESTINO, datos.tipoDestino()).toLowerCase(Locale.ROOT), certificado, null);
        }
        if (!conforme) {
            Incidencia incidencia = new Incidencia();
            incidencia.setTipo("RECHAZO");
            incidencia.setDescripcion("Recepción no conforme de la orden " + orden.getNumeroOrden() + ": " + observaciones);
            incidencia.setArea(orden.getDetalles().get(0).getGeneracion().getArea());
            incidencia.setReferencia(orden.getNumeroOrden());
            incidencia.setSeveridad("MEDIA");
            incidencia.setEstado(Incidencia.ABIERTA);
            incidencia.setFecha(sesion.ahora());
            incidencia.setReportadoPor(usuario);
            incidenciaRepository.save(incidencia);
            incidencia.setCodigo(Codigos.incidencia(incidencia.getIdIncidencia()));
            auditoria.registrar("CREAR", "Incidencia", incidencia.getCodigo(), "Reportó: rechazo en recepción");
        }

        auditoria.registrar("REGISTRAR", "Recolección", orden.getNumeroOrden(), "Registró la recepción de "
                + Numeros.cantidad(Numeros.valor(datos.cantidadRecibida()), vista.unidad()) + " con destino "
                + Etiquetas.de(Etiquetas.TIPO_DESTINO, datos.tipoDestino()).toLowerCase(Locale.ROOT));
        return vistas.recoleccion(orden, vistas.instantanea());
    }

    /** Cierre con la regla de integridad RNF003: deben cumplirse todos los requisitos. */
    @Transactional
    public RecoleccionVista cerrar(Integer id) {
        Recoleccion orden = enEstado(id, Recoleccion.RECIBIDA, "cerrar");
        VistaService.Instantanea foto = vistas.instantanea();
        List<String> faltantes = vistas.requisitosCierre(orden, foto.documentosDe(orden.getNumeroOrden())).stream()
                .filter(r -> !r.cumple())
                .map(r -> StringUtils.uncapitalize(r.label()))
                .toList();
        if (!faltantes.isEmpty()) {
            throw new SolicitudInvalidaException("Falta: " + String.join(", ", faltantes) + ".");
        }
        orden.setEstado(Recoleccion.CERRADA);
        orden.setFechaCierre(sesion.ahora());
        orden.setCerradoPor(sesion.usuario());
        auditoria.registrar("CERRAR", "Recolección", orden.getNumeroOrden(), "Cerró la orden con la documentación completa");
        return vistas.recoleccion(orden, vistas.instantanea());
    }

    private void validarAsignacion(Transportista transportista, Vehiculo vehiculo, Gestor gestor) {
        if ("VENCIDA".equals(vistas.estadoVigencia(transportista.getVigencia()))) {
            throw new SolicitudInvalidaException("La autorización del transportista está vencida.");
        }
        if (!Objects.equals(vehiculo.getTransportista().getIdTransportista(), transportista.getIdTransportista())) {
            throw new SolicitudInvalidaException("El vehículo no pertenece al transportista seleccionado.");
        }
        if (!Vehiculo.OPERATIVO.equals(vehiculo.getEstado())) {
            throw new SolicitudInvalidaException("El vehículo " + vehiculo.getPlaca() + " no está operativo.");
        }
        if ("VENCIDA".equals(vistas.estadoVigencia(gestor.getVigencia()))) {
            throw new SolicitudInvalidaException("La autorización del gestor está vencida.");
        }
    }

    private Recoleccion enEstado(Integer id, String estado, String accion) {
        Recoleccion orden = buscar(id);
        if (!estado.equals(orden.getEstado())) {
            throw new SolicitudInvalidaException("No se puede " + accion + ": la orden " + orden.getNumeroOrden()
                    + " no está en el estado requerido.");
        }
        return orden;
    }

    private Recoleccion buscar(Integer id) {
        return recoleccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden de recolección no encontrada"));
    }
}
