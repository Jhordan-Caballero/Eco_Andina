package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.RecoleccionDtos.Requisito;
import com.gestion.residuos.Eco_Andino.entity.Recoleccion;
import com.gestion.residuos.Eco_Andino.entity.Usuario;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import com.gestion.residuos.Eco_Andino.repository.GeneracionRepository;
import com.gestion.residuos.Eco_Andino.repository.IncidenciaRepository;
import com.gestion.residuos.Eco_Andino.repository.MovimientoRepository;
import com.gestion.residuos.Eco_Andino.repository.ObservacionRepository;
import com.gestion.residuos.Eco_Andino.repository.RecoleccionRepository;
import com.gestion.residuos.Eco_Andino.repository.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Cierre de una orden de recolección (CU14) con la regla de integridad RNF003. */
class RecoleccionServiceTest {
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 25, 16, 30);

    private RecoleccionRepository recolecciones;
    private VistaService vistas;
    private SesionService sesion;
    private AuditoriaService auditoria;
    private VistaService.Instantanea foto;
    private RecoleccionService servicio;
    private Usuario supervisor;

    @BeforeEach
    void preparar() {
        recolecciones = mock(RecoleccionRepository.class);
        vistas = mock(VistaService.class);
        sesion = mock(SesionService.class);
        auditoria = mock(AuditoriaService.class);
        foto = mock(VistaService.Instantanea.class);
        supervisor = new Usuario();

        when(vistas.instantanea()).thenReturn(foto);
        when(foto.documentosDe(anyString())).thenReturn(List.of());
        when(sesion.ahora()).thenReturn(AHORA);
        when(sesion.usuario()).thenReturn(supervisor);

        servicio = new RecoleccionService(recolecciones, mock(GeneracionRepository.class),
                mock(VehiculoRepository.class), mock(MovimientoRepository.class), mock(IncidenciaRepository.class),
                mock(ObservacionRepository.class), mock(TransportistaService.class), mock(DocumentoService.class),
                vistas, sesion, auditoria);
    }

    @Test
    @DisplayName("Con todos los requisitos cumplidos la orden pasa a CERRADA y queda auditada")
    void cierraOrdenCompleta() {
        Recoleccion orden = orden(Recoleccion.RECIBIDA);
        when(vistas.requisitosCierre(eq(orden), any())).thenReturn(requisitos(true));

        servicio.cerrar(7);

        assertEquals(Recoleccion.CERRADA, orden.getEstado());
        assertEquals(AHORA, orden.getFechaCierre());
        assertSame(supervisor, orden.getCerradoPor());
        verify(auditoria).registrar(eq("CERRAR"), eq("Recolección"), eq("OR-1043"), anyString());
    }

    @Test
    @DisplayName("Si falta el certificado no se cierra y se indica qué falta")
    void noCierraSinCertificado() {
        Recoleccion orden = orden(Recoleccion.RECIBIDA);
        when(vistas.requisitosCierre(eq(orden), any())).thenReturn(requisitos(false));

        SolicitudInvalidaException error = assertThrows(SolicitudInvalidaException.class, () -> servicio.cerrar(7));

        assertTrue(error.getMessage().contains("certificado de tratamiento o disposición"), error.getMessage());
        assertEquals(Recoleccion.RECIBIDA, orden.getEstado());
        verify(auditoria, never()).registrar(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Una orden que aún no se recibe no se puede cerrar")
    void noCierraSiNoEstaRecibida() {
        Recoleccion orden = orden(Recoleccion.EN_TRANSITO);

        SolicitudInvalidaException error = assertThrows(SolicitudInvalidaException.class, () -> servicio.cerrar(7));

        assertTrue(error.getMessage().contains("no está en el estado requerido"), error.getMessage());
        assertEquals(Recoleccion.EN_TRANSITO, orden.getEstado());
    }

    private Recoleccion orden(String estado) {
        Recoleccion orden = new Recoleccion();
        orden.setIdRecoleccion(7);
        orden.setNumeroOrden("OR-1043");
        orden.setEstado(estado);
        when(recolecciones.findById(7)).thenReturn(Optional.of(orden));
        return orden;
    }

    private static List<Requisito> requisitos(boolean conCertificado) {
        return List.of(
                new Requisito("transportista", "Transportista y vehículo asignados", true),
                new Requisito("cantidad", "Cantidad recibida registrada", true),
                new Requisito("destino", "Destino y tratamiento registrados", true),
                new Requisito("traslado", "Documento de traslado (manifiesto o guía)", true),
                new Requisito("certificado", "Certificado de tratamiento o disposición", conCertificado));
    }
}
