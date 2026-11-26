package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.AuditoriaDtos.AuditoriaDto;
import com.gestion.residuos.Eco_Andino.dto.AuditoriaDtos.ConfiguracionDto;
import com.gestion.residuos.Eco_Andino.dto.AuditoriaDtos.Eventos;
import com.gestion.residuos.Eco_Andino.entity.Auditoria;
import com.gestion.residuos.Eco_Andino.entity.ConfiguracionAuditoria;
import com.gestion.residuos.Eco_Andino.entity.Usuario;
import com.gestion.residuos.Eco_Andino.repository.AuditoriaRepository;
import com.gestion.residuos.Eco_Andino.repository.ConfiguracionAuditoriaRepository;
import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Bitácora de auditoría (RNF006, CU21 y CU22). Cada servicio llama a {@link #registrar} después de una acción;
 * la configuración decide qué categorías de eventos se guardan.
 */
@Service
@RequiredArgsConstructor
public class AuditoriaService {
    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private static final Set<String> VALIDACIONES = Set.of("VALIDAR", "OBSERVAR", "APROBAR", "RECHAZAR");

    private final AuditoriaRepository auditoriaRepository;
    private final ConfiguracionAuditoriaRepository configuracionRepository;
    private final SesionService sesion;

    /** Registra una acción del usuario autenticado. */
    @Transactional
    public void registrar(String accion, String entidad, String registro, String detalle) {
        registrarComo(sesion.usuario(), accion, entidad, registro, detalle);
    }

    @Transactional
    public void registrarComo(Usuario usuario, String accion, String entidad, String registro, String detalle) {
        if (!"Auditoría".equals(entidad) && !categoriaActiva(accion)) {
            return;
        }
        Auditoria auditoria = new Auditoria();
        auditoria.setFecha(sesion.ahora());
        auditoria.setUsuario(usuario);
        auditoria.setNombreUsuario(usuario.getNombreCompleto());
        auditoria.setAccion(accion);
        auditoria.setEntidad(entidad);
        auditoria.setRegistro(StringUtils.abbreviate(registro, 120));
        auditoria.setDetalle(StringUtils.abbreviate(detalle, 500));
        auditoriaRepository.save(auditoria);
        log.info("Auditoría: {} {} {} por {}", accion, entidad, TextoUtil.paraLog(registro), usuario.getUsername());
    }

    @Transactional(readOnly = true)
    public List<AuditoriaDto> listar() {
        return auditoriaRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha", "idAuditoria")).stream()
                .map(a -> new AuditoriaDto(a.getIdAuditoria(), a.getFecha(), a.getNombreUsuario(), a.getAccion(),
                        a.getEntidad(), a.getRegistro(), a.getDetalle()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ConfiguracionDto configuracion() {
        return aDto(obtenerConfiguracion());
    }

    @Transactional
    public ConfiguracionDto guardarConfiguracion(ConfiguracionDto valores) {
        ConfiguracionAuditoria actual = obtenerConfiguracion();
        Eventos nuevos = valores.eventos();
        List<String> cambios = new ArrayList<>();
        comparar(cambios, actual.isValidaciones(), nuevos.validaciones(), "validaciones");
        comparar(cambios, actual.isModificaciones(), nuevos.modificaciones(), "creación y modificación");
        comparar(cambios, actual.isCierres(), nuevos.cierres(), "cierres");
        comparar(cambios, actual.isAccesos(), nuevos.accesos(), "inicios de sesión");
        int retencion = Integer.parseInt(valores.retencionDias());
        if (actual.getRetencionDias() != retencion) {
            cambios.add("retención de " + retencion + " días");
        }

        actual.setValidaciones(nuevos.validaciones());
        actual.setModificaciones(nuevos.modificaciones());
        actual.setCierres(nuevos.cierres());
        actual.setAccesos(nuevos.accesos());
        actual.setRetencionDias(retencion);
        configuracionRepository.save(actual);

        registrar("MODIFICAR", "Auditoría", "Configuración",
                "Configuración: " + (cambios.isEmpty() ? "sin cambios" : String.join(", ", cambios)));
        return aDto(actual);
    }

    /**
     * Elimina los registros de auditoría más antiguos que la retención configurada en CU22.
     * La ejecuta cada noche la tarea programada de mantenimiento; también puede lanzarla el administrador.
     */
    @Transactional
    public int depurar() {
        int retencion = obtenerConfiguracion().getRetencionDias();
        LocalDateTime limite = sesion.ahora().minusDays(retencion);
        int eliminados = auditoriaRepository.eliminarAnterioresA(limite);
        log.info("Depuración de auditoría: {} registros anteriores a {} eliminados (retención de {} días)",
                eliminados, limite.toLocalDate(), retencion);
        return eliminados;
    }

    /** Depuración lanzada a mano por el administrador: queda registrada en la propia auditoría. */
    @Transactional
    public int depurarManual() {
        int eliminados = depurar();
        registrar("ELIMINAR", "Auditoría", "Depuración",
                "Depuró " + eliminados + " registros según la retención de " + obtenerConfiguracion().getRetencionDias()
                        + " días");
        return eliminados;
    }

    private boolean categoriaActiva(String accion) {
        ConfiguracionAuditoria config = obtenerConfiguracion();
        if (VALIDACIONES.contains(accion)) {
            return config.isValidaciones();
        }
        if ("CERRAR".equals(accion)) {
            return config.isCierres();
        }
        if ("ACCESO".equals(accion)) {
            return config.isAccesos();
        }
        return config.isModificaciones();
    }

    private ConfiguracionAuditoria obtenerConfiguracion() {
        return configuracionRepository.findById(ConfiguracionAuditoria.ID)
                .orElseGet(() -> configuracionRepository.save(new ConfiguracionAuditoria()));
    }

    private static void comparar(List<String> cambios, boolean antes, boolean despues, String etiqueta) {
        if (antes != despues) {
            cambios.add((despues ? "activó " : "desactivó ") + etiqueta);
        }
    }

    private static ConfiguracionDto aDto(ConfiguracionAuditoria c) {
        return new ConfiguracionDto(new Eventos(c.isValidaciones(), c.isModificaciones(), c.isCierres(), c.isAccesos()),
                String.valueOf(c.getRetencionDias()));
    }
}
