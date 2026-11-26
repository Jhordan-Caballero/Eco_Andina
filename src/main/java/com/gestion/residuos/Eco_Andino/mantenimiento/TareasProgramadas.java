package com.gestion.residuos.Eco_Andino.mantenimiento;

import com.gestion.residuos.Eco_Andino.service.AuditoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Tareas de mantenimiento automáticas (equivalen a cron jobs, pero viven dentro del backend).
 * Los horarios están en application.properties (app.mantenimiento.*) y usan la hora de Lima.
 * Se pueden ver en /actuator/scheduledtasks y desactivar con MANTENIMIENTO_HABILITADO=false.
 * Un error en una tarea se registra y no detiene la aplicación ni las demás tareas.
 */
@Configuration
@EnableScheduling
public class TareasProgramadas {
    private static final Logger log = LoggerFactory.getLogger(TareasProgramadas.class);

    private final AuditoriaService auditoriaService;
    private final MantenimientoService mantenimientoService;
    private final boolean habilitado;

    public TareasProgramadas(AuditoriaService auditoriaService, MantenimientoService mantenimientoService,
                             @Value("${app.mantenimiento.habilitado:true}") boolean habilitado) {
        this.auditoriaService = auditoriaService;
        this.mantenimientoService = mantenimientoService;
        this.habilitado = habilitado;
    }

    /** Todos los días a las 02:00: borra la auditoría más antigua que la retención de CU22. */
    @Scheduled(cron = "${app.mantenimiento.depuracion-auditoria-cron:0 0 2 * * *}", zone = "${app.timezone:America/Lima}")
    public void depurarAuditoria() {
        ejecutar("depuración de auditoría", auditoriaService::depurar);
    }

    /** Domingos a las 03:00: revisa que los adjuntos de la BD y los de la carpeta coincidan. */
    @Scheduled(cron = "${app.mantenimiento.verificacion-archivos-cron:0 0 3 * * SUN}", zone = "${app.timezone:America/Lima}")
    public void verificarArchivos() {
        ejecutar("verificación de archivos", mantenimientoService::verificarArchivos);
    }

    /** Lunes a viernes a las 07:00: avisa de autorizaciones de transportistas y gestores por vencer. */
    @Scheduled(cron = "${app.mantenimiento.vigencias-cron:0 0 7 * * MON-FRI}", zone = "${app.timezone:America/Lima}")
    public void revisarVigencias() {
        ejecutar("revisión de vigencias", mantenimientoService::revisarVigencias);
    }

    private void ejecutar(String tarea, Runnable accion) {
        if (!habilitado) {
            return;
        }
        long inicio = System.currentTimeMillis();
        try {
            accion.run();
            log.info("Tarea programada '{}' terminada en {} ms", tarea, System.currentTimeMillis() - inicio);
        } catch (RuntimeException e) {
            log.error("Tarea programada '{}' falló", tarea, e);
        }
    }
}
