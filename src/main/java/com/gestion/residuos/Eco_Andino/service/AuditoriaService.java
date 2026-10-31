package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.*;
import com.gestion.residuos.Eco_Andino.entity.AccionAuditoria;
import com.gestion.residuos.Eco_Andino.entity.Auditoria;
import com.gestion.residuos.Eco_Andino.entity.ConfiguracionAuditoria;
import com.gestion.residuos.Eco_Andino.exception.RecursoNoEncontradoException;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import com.gestion.residuos.Eco_Andino.repository.AuditoriaRepository;
import com.gestion.residuos.Eco_Andino.repository.AuditoriaSpecs;
import com.gestion.residuos.Eco_Andino.repository.ConfiguracionAuditoriaRepository;
import com.gestion.residuos.Eco_Andino.repository.UsuarioRepository;
import com.gestion.residuos.Eco_Andino.security.UsuarioActual;
import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Auditoría de la aplicación (RF016, CU21 y CU22).
 * {@link #registrar} lo llaman los servicios de negocio dentro de su propia transacción: si la operación falla y
 * se revierte, su registro de auditoría también. Por eso quien llama debe ser @Transactional de escritura
 * (en una transacción de solo lectura el INSERT no se llegaría a ejecutar).
 */
@Service
@RequiredArgsConstructor
public class AuditoriaService {
    // Además de la tabla, cada evento va al archivo auditoria.log (ver logback-spring.xml)
    private static final Logger AUDITORIA = LoggerFactory.getLogger("auditoria");
    private static final int TAMANO_MAXIMO = 100;

    private final AuditoriaRepository auditoriaRepository;
    private final ConfiguracionAuditoriaRepository configuracionRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActual usuarioActual;
    private final Clock clock;

    // ---------------------------------------------------------------- registro (RF016)

    @Transactional
    public void registrar(AccionAuditoria accion, String entidad, Integer idRegistro, String detalle) {
        AUDITORIA.info("{} entidad={} id={} usuario={} detalle={}", accion, entidad, idRegistro,
                TextoUtil.paraLog(usuarioActual.username()), TextoUtil.paraLog(detalle));

        if (!configuracionActual().debeRegistrar(accion)) {
            return;
        }
        Auditoria registro = new Auditoria();
        registro.setAccion(accion);
        registro.setEntidad(entidad);
        registro.setIdRegistro(idRegistro);
        registro.setFecha(LocalDateTime.now(clock));
        registro.setDetalle(StringUtils.abbreviate(detalle, 500));
        Integer uid = usuarioActual.id();
        registro.setUsuario(uid == null ? null : usuarioRepository.getReferenceById(uid));
        auditoriaRepository.save(registro);
    }

    // ---------------------------------------------------------------- consulta (CU21)

    @Transactional(readOnly = true)
    public PaginaResponse<AuditoriaResponse> consultar(LocalDate desde, LocalDate hasta, AccionAuditoria accion,
                                                       String entidad, String usuario, int pagina, int tamano) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new SolicitudInvalidaException("La fecha inicial no puede ser posterior a la final");
        }
        var spec = AuditoriaSpecs.filtrar(
                desde == null ? null : desde.atStartOfDay(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay(),
                accion, TextoUtil.normalizar(entidad), TextoUtil.normalizar(usuario));
        var orden = Sort.by(Sort.Direction.DESC, "fecha", "idAuditoria");
        var page = auditoriaRepository.findAll(spec,
                PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 1), TAMANO_MAXIMO), orden));
        return PaginaResponse.de(page, AuditoriaResponse::from);
    }

    @Transactional(readOnly = true)
    public AuditoriaResponse detalle(Integer id) {
        // La transacción de lectura sigue abierta aquí, así que el usuario (carga perezosa) se resuelve al mapear
        return auditoriaRepository.findById(id)
                .map(AuditoriaResponse::from)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el registro de auditoría " + id));
    }

    // ---------------------------------------------------------------- configuración (CU22)

    @Transactional
    public ConfiguracionAuditoriaResponse obtenerConfiguracion() {
        return ConfiguracionAuditoriaResponse.from(configuracionActual());
    }

    @Transactional
    public ConfiguracionAuditoriaResponse actualizarConfiguracion(ConfiguracionAuditoriaRequest request) {
        ConfiguracionAuditoria config = configuracionActual();
        String cambios = "retención %d→%d días; creaciones %s→%s; exportaciones %s→%s".formatted(
                config.getRetencionDias(), request.retencionDias(),
                config.isRegistrarCreaciones(), request.registrarCreaciones(),
                config.isRegistrarExportaciones(), request.registrarExportaciones());

        config.setRetencionDias(request.retencionDias());
        config.setRegistrarCreaciones(request.registrarCreaciones());
        config.setRegistrarExportaciones(request.registrarExportaciones());
        Integer uid = usuarioActual.id();
        config.setActualizadoPor(uid == null ? null : usuarioRepository.getReferenceById(uid));
        config.setFechaActualizacion(LocalDateTime.now(clock));
        configuracionRepository.save(config);

        // CONFIGURAR es obligatoria: el cambio queda registrado aunque se desactiven otras acciones (CU22, paso 8)
        registrar(AccionAuditoria.CONFIGURAR, "AUDITORIA", null, cambios);
        return ConfiguracionAuditoriaResponse.from(config);
    }

    /** Elimina los registros más antiguos que la retención configurada. Acción manual del Administrador. */
    @Transactional
    public DepuracionResponse depurar() {
        LocalDateTime limite = LocalDateTime.now(clock).minusDays(configuracionActual().getRetencionDias());
        int eliminados = auditoriaRepository.eliminarAnterioresA(limite);
        registrar(AccionAuditoria.DEPURAR, "AUDITORIA", null,
                "Se eliminaron %d registros anteriores a %s".formatted(eliminados, limite.toLocalDate()));
        return new DepuracionResponse(eliminados, limite);
    }

    private ConfiguracionAuditoria configuracionActual() {
        return configuracionRepository.findById(ConfiguracionAuditoria.ID_UNICO)
                .orElseGet(() -> configuracionRepository.save(new ConfiguracionAuditoria()));
    }
}
