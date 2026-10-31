package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.*;
import com.gestion.residuos.Eco_Andino.entity.*;
import com.gestion.residuos.Eco_Andino.exception.ConflictoException;
import com.gestion.residuos.Eco_Andino.exception.RecursoNoEncontradoException;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import com.gestion.residuos.Eco_Andino.repository.AreaGeneradoraRepository;
import com.gestion.residuos.Eco_Andino.repository.IncidenciaRepository;
import com.gestion.residuos.Eco_Andino.repository.IncidenciaSpecs;
import com.gestion.residuos.Eco_Andino.repository.UsuarioRepository;
import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** CU17 – Gestionar incidencias y acciones correctivas. */
@Service
@RequiredArgsConstructor
public class IncidenciaService {
    static final int ACCION_MINIMA_CIERRE = 10;
    private static final int TAMANO_MAXIMO = 100;

    private final IncidenciaRepository incidenciaRepository;
    private final AreaGeneradoraRepository areaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final Clock clock;

    // ---------------------------------------------------------------- consulta (pasos 2 y 3)

    @Transactional(readOnly = true)
    public PaginaResponse<IncidenciaResponse> listar(EstadoIncidencia estado, Severidad severidad, TipoIncidencia tipo,
                                                     Integer areaId, LocalDate desde, LocalDate hasta,
                                                     int pagina, int tamano) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new SolicitudInvalidaException("La fecha inicial no puede ser posterior a la final");
        }
        var spec = IncidenciaSpecs.filtrar(estado, severidad, tipo, areaId,
                desde == null ? null : desde.atStartOfDay(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay());
        var orden = Sort.by(Sort.Direction.DESC, "fecha", "idIncidencia");
        var page = incidenciaRepository.findAll(spec,
                PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 1), TAMANO_MAXIMO), orden));
        return PaginaResponse.de(page, IncidenciaResponse::from);
    }

    @Transactional(readOnly = true)
    public IncidenciaResponse obtener(Integer id) {
        return IncidenciaResponse.from(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResumen> responsablesDisponibles() {
        return usuarioRepository.findByEstadoOrderByApellidosAscNombresAsc(Usuario.ESTADO_ACTIVO).stream()
                .map(UsuarioResumen::from).toList();
    }

    // ---------------------------------------------------------------- registro (pasos 4 a 6)

    @Transactional
    public IncidenciaResponse crear(IncidenciaRequest request) {
        Area_Generadora area = areaActiva(request.areaId());
        Usuario responsable = responsableActivo(request.responsableId());
        validarFecha(request.fecha());

        Incidencia incidencia = new Incidencia();
        incidencia.setTipo(request.tipo());
        incidencia.setDescripcion(TextoUtil.normalizar(request.descripcion()));
        incidencia.setArea(area);
        incidencia.setFecha(request.fecha());
        incidencia.setSeveridad(request.severidad());
        incidencia.setResponsable(responsable);
        incidencia.setEstado(EstadoIncidencia.ABIERTA);
        incidencia.setAccionCorrectiva(TextoUtil.normalizar(request.accionCorrectiva()));
        Incidencia guardada = incidenciaRepository.save(incidencia);

        auditoriaService.registrar(AccionAuditoria.CREAR, "INCIDENCIA", guardada.getIdIncidencia(),
                "%s, gravedad %s, área %s, responsable %s".formatted(guardada.getTipo(), guardada.getSeveridad(),
                        area.getNombre(), responsable.getUsername()));
        return IncidenciaResponse.from(guardada);
    }

    // ---------------------------------------------------------------- seguimiento (paso 7)

    @Transactional
    public IncidenciaResponse registrarSeguimiento(Integer id, IncidenciaSeguimientoRequest request) {
        Incidencia incidencia = buscar(id);
        if (incidencia.estaCerrada()) {
            throw new ConflictoException("La incidencia ya está cerrada y no se puede modificar");
        }
        if (request.estado() == EstadoIncidencia.CERRADA) {
            throw new SolicitudInvalidaException("Para cerrar la incidencia usa la opción de cierre");
        }
        Usuario responsable = incidencia.getResponsable().getIdUsuario().equals(request.responsableId())
                ? incidencia.getResponsable() : responsableActivo(request.responsableId());

        List<String> cambios = new ArrayList<>();
        registrarCambio(cambios, "estado", incidencia.getEstado(), request.estado());
        registrarCambio(cambios, "gravedad", incidencia.getSeveridad(), request.severidad());
        registrarCambio(cambios, "responsable", incidencia.getResponsable().getUsername(), responsable.getUsername());
        String descripcion = TextoUtil.normalizar(request.descripcion());
        String accion = TextoUtil.normalizar(request.accionCorrectiva());
        if (!Objects.equals(incidencia.getDescripcion(), descripcion)) cambios.add("descripción actualizada");
        if (!Objects.equals(incidencia.getAccionCorrectiva(), accion)) cambios.add("acción correctiva actualizada");

        incidencia.setEstado(request.estado());
        incidencia.setSeveridad(request.severidad());
        incidencia.setResponsable(responsable);
        incidencia.setDescripcion(descripcion);
        incidencia.setAccionCorrectiva(accion);
        incidenciaRepository.save(incidencia);

        if (!cambios.isEmpty()) {
            auditoriaService.registrar(AccionAuditoria.MODIFICAR, "INCIDENCIA", id, String.join("; ", cambios));
        }
        return IncidenciaResponse.from(incidencia);
    }

    // ---------------------------------------------------------------- cierre (pasos 8 y 9)

    @Transactional
    public IncidenciaResponse cerrar(Integer id, IncidenciaCierreRequest request) {
        Incidencia incidencia = buscar(id);
        if (incidencia.estaCerrada()) {
            throw new ConflictoException("La incidencia ya está cerrada");
        }
        String accion = request != null && TextoUtil.normalizar(request.accionCorrectiva()) != null
                ? TextoUtil.normalizar(request.accionCorrectiva())
                : incidencia.getAccionCorrectiva();
        // Alternativa 2 del CU: sin información suficiente no se puede cerrar
        if (accion == null || accion.length() < ACCION_MINIMA_CIERRE) {
            throw new SolicitudInvalidaException(
                    "No se puede cerrar: registra la acción correctiva realizada (mínimo %d caracteres)"
                            .formatted(ACCION_MINIMA_CIERRE));
        }
        incidencia.setAccionCorrectiva(accion);
        incidencia.setEstado(EstadoIncidencia.CERRADA);
        incidencia.setFechaCierre(LocalDateTime.now(clock));
        incidenciaRepository.save(incidencia);

        auditoriaService.registrar(AccionAuditoria.CERRAR, "INCIDENCIA", id, "Acción correctiva: " + accion);
        return IncidenciaResponse.from(incidencia);
    }

    // ---------------------------------------------------------------- apoyo

    private Incidencia buscar(Integer id) {
        return incidenciaRepository.findWithDetalleByIdIncidencia(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la incidencia " + id));
    }

    private Area_Generadora areaActiva(Integer id) {
        Area_Generadora area = areaRepository.findById(id)
                .orElseThrow(() -> new SolicitudInvalidaException("El área seleccionada no existe"));
        if (!"ACTIVA".equals(area.getEstado())) {
            throw new SolicitudInvalidaException("El área seleccionada está inactiva");
        }
        return area;
    }

    private Usuario responsableActivo(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new SolicitudInvalidaException("El responsable seleccionado no existe"));
        if (!usuario.isActivo()) {
            throw new SolicitudInvalidaException("El responsable seleccionado está desactivado");
        }
        return usuario;
    }

    // Se tolera un pequeño adelanto del reloj del cliente
    private void validarFecha(LocalDateTime fecha) {
        if (fecha.isAfter(LocalDateTime.now(clock).plusMinutes(5))) {
            throw new SolicitudInvalidaException("La fecha de la incidencia no puede ser futura");
        }
    }

    private static void registrarCambio(List<String> cambios, String campo, Object antes, Object despues) {
        if (!Objects.equals(antes, despues)) {
            cambios.add("%s: %s → %s".formatted(campo, antes, despues));
        }
    }
}
