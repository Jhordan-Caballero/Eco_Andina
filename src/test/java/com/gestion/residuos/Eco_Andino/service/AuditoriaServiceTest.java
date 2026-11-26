package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.entity.ConfiguracionAuditoria;
import com.gestion.residuos.Eco_Andino.repository.AuditoriaRepository;
import com.gestion.residuos.Eco_Andino.repository.ConfiguracionAuditoriaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Depuración de la auditoría según la retención configurada (CU22, tarea de mantenimiento nocturna). */
class AuditoriaServiceTest {

    @Test
    @DisplayName("Borra solo los registros anteriores a hoy menos los días de retención")
    void depuraSegunRetencion() {
        AuditoriaRepository auditorias = mock(AuditoriaRepository.class);
        ConfiguracionAuditoriaRepository configuraciones = mock(ConfiguracionAuditoriaRepository.class);
        SesionService sesion = mock(SesionService.class);

        ConfiguracionAuditoria configuracion = new ConfiguracionAuditoria();
        configuracion.setRetencionDias(90);
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 25, 2, 0);
        LocalDateTime limite = ahora.minusDays(90);

        when(configuraciones.findById(ConfiguracionAuditoria.ID)).thenReturn(Optional.of(configuracion));
        when(sesion.ahora()).thenReturn(ahora);
        when(auditorias.eliminarAnterioresA(limite)).thenReturn(12);

        int eliminados = new AuditoriaService(auditorias, configuraciones, sesion).depurar();

        assertEquals(12, eliminados);
        verify(auditorias).eliminarAnterioresA(limite);
    }
}
