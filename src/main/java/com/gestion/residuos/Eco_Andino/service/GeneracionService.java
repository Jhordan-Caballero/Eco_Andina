package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.ContenedorVista;
import com.gestion.residuos.Eco_Andino.dto.GeneracionDtos.*;
import com.gestion.residuos.Eco_Andino.entity.*;
import com.gestion.residuos.Eco_Andino.exception.RecursoNoEncontradoException;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import com.gestion.residuos.Eco_Andino.repository.GeneracionRepository;
import com.gestion.residuos.Eco_Andino.repository.MovimientoRepository;
import com.gestion.residuos.Eco_Andino.repository.ObservacionRepository;
import com.gestion.residuos.Eco_Andino.repository.ResiduoRepository;
import com.gestion.residuos.Eco_Andino.util.Codigos;
import com.gestion.residuos.Eco_Andino.util.Numeros;
import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Registro de generación y segregación (CU08), validación (CU09) y corrección de registros observados. */
@Service
@RequiredArgsConstructor
public class GeneracionService {
    private static final Set<String> CONDICIONES = Set.of("BUEN_ESTADO", "DETERIORADO", "SIN_ROTULO", "SIN_TAPA");
    private static final Set<String> CATEGORIAS_CON_EVIDENCIA = Set.of("PELIGROSO", "ESPECIAL");

    private final GeneracionRepository generacionRepository;
    private final MovimientoRepository movimientoRepository;
    private final ObservacionRepository observacionRepository;
    private final ResiduoRepository residuoRepository;
    private final CatalogoService catalogos;
    private final DocumentoService documentos;
    private final VistaService vistas;
    private final SesionService sesion;
    private final AuditoriaService auditoria;

    /** El generador ve solo sus registros; quien valida o consulta el almacén ve todos. */
    @Transactional(readOnly = true)
    public List<GeneracionVista> listar() {
        VistaService.Instantanea foto = vistas.instantanea();
        boolean veTodo = sesion.tienePermiso("GENERACION_VALIDAR") || sesion.tienePermiso("ALMACENAMIENTO_CONSULTAR");
        Integer idUsuario = veTodo ? null : sesion.usuario().getIdUsuario();
        return foto.generaciones().stream()
                .filter(g -> veTodo || g.getResponsable().getIdUsuario().equals(idUsuario))
                .sorted(Comparator.comparing(Generacion::getFecha).reversed())
                .map(g -> vistas.generacion(g, foto))
                .toList();
    }

    @Transactional(readOnly = true)
    public OpcionesRegistro opciones() {
        return new OpcionesRegistro(catalogos.listarAreasActivas(),
                residuoRepository.findAll(Sort.by("codigo")).stream().map(vistas::residuo).toList(),
                catalogos.contenedoresOperativos());
    }

    @Transactional(readOnly = true)
    public GeneracionDetalle detalle(Integer id) {
        Generacion generacion = buscar(id);
        VistaService.Instantanea foto = vistas.instantanea();
        return new GeneracionDetalle(vistas.generacion(generacion, foto),
                observacionRepository.findByReferenciaOrderByFechaDesc(generacion.getCodigo()).stream()
                        .map(vistas::observacion).toList(),
                foto.documentosDe(generacion.getCodigo()).stream().map(vistas::documento).toList());
    }

    /** CU08: registra la generación, su ingreso al contenedor y, si llega, la evidencia de segregación. */
    @Transactional
    public GeneracionVista registrar(GeneracionRequest datos, MultipartFile evidencia) {
        AreaGeneradora area = catalogos.buscarArea(datos.idArea());
        Residuo residuo = catalogos.buscarResiduo(datos.idResiduo());
        Contenedor contenedor = catalogos.buscarContenedor(datos.idContenedor());
        CatalogoService.validarValor(CONDICIONES, datos.condicionEnvase(), "La condición del envase no es válida.");

        if (!AreaGeneradora.ACTIVA.equals(area.getEstado())) {
            throw new SolicitudInvalidaException("El área " + area.getNombre() + " está inactiva.");
        }
        if (Contenedor.MANTENIMIENTO.equals(contenedor.getEstado())) {
            throw new SolicitudInvalidaException("El contenedor " + contenedor.getCodigo() + " está en mantenimiento.");
        }
        if (!contenedor.getResiduoPermitido().getIdResiduo().equals(residuo.getIdResiduo())) {
            throw new SolicitudInvalidaException("El contenedor " + contenedor.getCodigo() + " no admite "
                    + residuo.getNombre().toLowerCase(Locale.ROOT) + ".");
        }
        ContenedorVista estado = vistas.contenedor(contenedor, vistas.instantanea());
        if (Numeros.valor(datos.cantidad()) > estado.disponible()) {
            throw new SolicitudInvalidaException("El contenedor " + contenedor.getCodigo() + " solo tiene "
                    + Numeros.cantidad(estado.disponible(), residuo.getUnidad()) + " disponibles.");
        }
        boolean conEvidencia = evidencia != null && !evidencia.isEmpty();
        if (CATEGORIAS_CON_EVIDENCIA.contains(residuo.getCategoria()) && !conEvidencia) {
            throw new SolicitudInvalidaException("La evidencia es obligatoria para residuos peligrosos o especiales.");
        }
        var fecha = sesion.aLocal(datos.fecha());
        if (fecha.isAfter(sesion.ahora().plusMinutes(5))) {
            throw new SolicitudInvalidaException("La fecha de generación no puede ser futura.");
        }

        Usuario usuario = sesion.usuario();
        Generacion generacion = new Generacion();
        generacion.setArea(area);
        generacion.setResiduo(residuo);
        generacion.setContenedor(contenedor);
        generacion.setCantidad(datos.cantidad());
        generacion.setFecha(fecha);
        generacion.setResponsable(usuario);
        generacion.setCondicionEnvase(datos.condicionEnvase());
        generacion.setSegregacionCorrecta(!Boolean.FALSE.equals(datos.segregacionCorrecta()));
        generacion.setObservaciones(TextoUtil.normalizar(datos.observaciones()));
        generacion.setEstadoValidacion(Generacion.PENDIENTE);
        generacionRepository.save(generacion);
        generacion.setCodigo(Codigos.generacion(generacion.getIdGeneracion()));

        Movimiento ingreso = new Movimiento();
        ingreso.setTipo(Movimiento.INGRESO);
        ingreso.setFecha(fecha);
        ingreso.setResiduo(residuo);
        ingreso.setOrigen(area.getNombre());
        ingreso.setDestino(contenedor.getCodigo());
        ingreso.setCantidad(datos.cantidad());
        ingreso.setReferencia(generacion.getCodigo());
        ingreso.setUsuario(usuario);
        ingreso.setMotivo("Registro de generación");
        ingreso.setAfectaStock(false);
        movimientoRepository.save(ingreso);
        ingreso.setCodigo(Codigos.movimiento(ingreso.getIdMovimiento()));

        if (conEvidencia) {
            Documento documento = documentos.registrar("EVIDENCIA_FOTO", generacion.getCodigo(),
                    "Evidencia del registro " + generacion.getCodigo(), evidencia, null);
            generacion.setEvidencia(documento.getNombreArchivo());
        }

        auditoria.registrar("CREAR", "Generación", generacion.getCodigo(), "Registró "
                + Numeros.cantidad(Numeros.valor(datos.cantidad()), residuo.getUnidad()) + " de "
                + residuo.getNombre().toLowerCase(Locale.ROOT) + " en " + contenedor.getCodigo());
        return vistas.generacion(generacion, vistas.instantanea());
    }

    /** CU09: el supervisor ambiental valida un registro pendiente. */
    @Transactional
    public GeneracionVista validar(Integer id) {
        Generacion generacion = pendiente(id, "validar");
        generacion.setEstadoValidacion(Generacion.VALIDADA);
        generacion.setValidadoPor(sesion.usuario());
        generacion.setFechaValidacion(sesion.ahora());
        generacion.setObservacionValidacion(null);
        auditoria.registrar("VALIDAR", "Generación", generacion.getCodigo(), "Validó el registro de generación");
        return vistas.generacion(generacion, vistas.instantanea());
    }

    /** CU09 (secuencia alternativa): el registro queda observado hasta que el generador lo corrija. */
    @Transactional
    public GeneracionVista observar(Integer id, String observacion) {
        Generacion generacion = pendiente(id, "observar");
        String texto = observacion.trim();
        generacion.setEstadoValidacion(Generacion.OBSERVADA);
        generacion.setValidadoPor(sesion.usuario());
        generacion.setFechaValidacion(sesion.ahora());
        generacion.setObservacionValidacion(texto);
        auditoria.registrar("OBSERVAR", "Generación", generacion.getCodigo(), "Observó el registro: " + texto);
        return vistas.generacion(generacion, vistas.instantanea());
    }

    /** El generador corrige un registro observado y lo reenvía a validación. */
    @Transactional
    public GeneracionVista corregir(Integer id, CorreccionRequest datos, MultipartFile evidencia) {
        Generacion generacion = buscar(id);
        if (!Generacion.OBSERVADA.equals(generacion.getEstadoValidacion())) {
            throw new SolicitudInvalidaException("Solo se pueden corregir registros observados.");
        }
        if (!generacion.getResponsable().getIdUsuario().equals(sesion.usuario().getIdUsuario())) {
            throw new SolicitudInvalidaException("Solo el responsable del registro puede corregirlo.");
        }
        CatalogoService.validarValor(CONDICIONES, datos.condicionEnvase(), "La condición del envase no es válida.");
        ContenedorVista contenedor = vistas.contenedor(generacion.getContenedor(), vistas.instantanea());
        double aumento = Numeros.valor(datos.cantidad()) - Numeros.valor(generacion.getCantidad());
        if (aumento > contenedor.disponible()) {
            throw new SolicitudInvalidaException("El contenedor " + contenedor.codigo() + " solo tiene "
                    + Numeros.cantidad(contenedor.disponible(), contenedor.unidad()) + " disponibles.");
        }

        generacion.setCantidad(datos.cantidad());
        generacion.setCondicionEnvase(datos.condicionEnvase());
        generacion.setObservaciones(TextoUtil.normalizar(datos.observaciones()));
        generacion.setEstadoValidacion(Generacion.PENDIENTE);
        if (evidencia != null && !evidencia.isEmpty()) {
            Documento documento = documentos.registrar("EVIDENCIA_FOTO", generacion.getCodigo(),
                    "Evidencia corregida del registro " + generacion.getCodigo(), evidencia, null);
            generacion.setEvidencia(documento.getNombreArchivo());
        }
        auditoria.registrar("MODIFICAR", "Generación", generacion.getCodigo(),
                "Corrigió el registro observado y lo reenvió a validación");
        return vistas.generacion(generacion, vistas.instantanea());
    }

    private Generacion pendiente(Integer id, String accion) {
        Generacion generacion = buscar(id);
        if (!Generacion.PENDIENTE.equals(generacion.getEstadoValidacion())) {
            throw new SolicitudInvalidaException("Solo se pueden " + accion + " registros pendientes.");
        }
        return generacion;
    }

    public Generacion buscar(Integer id) {
        return generacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro de generación no encontrado"));
    }
}
