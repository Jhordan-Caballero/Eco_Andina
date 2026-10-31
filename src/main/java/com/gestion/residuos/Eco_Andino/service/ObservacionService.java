package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.ObservacionRequest;
import com.gestion.residuos.Eco_Andino.dto.ObservacionResponse;
import com.gestion.residuos.Eco_Andino.entity.AccionAuditoria;
import com.gestion.residuos.Eco_Andino.entity.Observacion;
import com.gestion.residuos.Eco_Andino.entity.TipoRegistro;
import com.gestion.residuos.Eco_Andino.repository.ObservacionRepository;
import com.gestion.residuos.Eco_Andino.repository.UsuarioRepository;
import com.gestion.residuos.Eco_Andino.security.UsuarioActual;
import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** CU18 – Registrar observaciones operativas. */
@Service
@RequiredArgsConstructor
public class ObservacionService {
    private static final int LIMITE_LISTADO = 100;

    private final ObservacionRepository observacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActual usuarioActual;
    private final AuditoriaService auditoriaService;
    private final Clock clock;

    @Transactional
    public ObservacionResponse registrar(ObservacionRequest request) {
        // Alternativa 2 del CU: el permiso depende del tipo de registro (Generador, Almacenero, Transportista o
        // Gestor autorizado); tener otro permiso de observación no habilita registrar sobre ese registro
        TipoRegistro tipo = request.tipoRegistro();
        if (!usuarioActual.tienePermiso(tipo.permisoRequerido())) {
            throw new AccessDeniedException("Sin permiso para registrar observaciones de " + tipo);
        }
        Integer uid = usuarioActual.id();
        if (uid == null) {
            throw new AccessDeniedException("No se pudo identificar al usuario");
        }

        Observacion observacion = new Observacion();
        observacion.setTipoRegistro(tipo);
        observacion.setIdRegistro(request.idRegistro());
        observacion.setDescripcion(TextoUtil.normalizar(request.descripcion()));
        observacion.setFecha(LocalDateTime.now(clock));
        observacion.setUsuario(usuarioRepository.getReferenceById(uid));
        Observacion guardada = observacionRepository.save(observacion);

        auditoriaService.registrar(AccionAuditoria.CREAR, "OBSERVACION", guardada.getIdObservacion(),
                "%s #%d".formatted(tipo, request.idRegistro()));
        return ObservacionResponse.from(guardada);
    }

    @Transactional(readOnly = true)
    public List<ObservacionResponse> listar(TipoRegistro tipo, Integer idRegistro) {
        List<Observacion> resultado;
        if (tipo != null && idRegistro != null) {
            resultado = observacionRepository
                    .findByTipoRegistroAndIdRegistroOrderByFechaDescIdObservacionDesc(tipo, idRegistro);
        } else if (tipo != null) {
            resultado = observacionRepository.findByTipoRegistroOrderByFechaDescIdObservacionDesc(tipo,
                    PageRequest.of(0, LIMITE_LISTADO));
        } else {
            resultado = observacionRepository.findAllByOrderByFechaDescIdObservacionDesc(
                    PageRequest.of(0, LIMITE_LISTADO));
        }
        return resultado.stream().map(ObservacionResponse::from).toList();
    }
}
