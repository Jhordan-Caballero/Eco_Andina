package com.gestion.residuos.Eco_Andino.mantenimiento;

import com.gestion.residuos.Eco_Andino.entity.Gestor;
import com.gestion.residuos.Eco_Andino.entity.Transportista;
import com.gestion.residuos.Eco_Andino.mantenimiento.MantenimientoService.VerificacionArchivos;
import com.gestion.residuos.Eco_Andino.mantenimiento.MantenimientoService.Vigencia;
import com.gestion.residuos.Eco_Andino.repository.DocumentoRepository;
import com.gestion.residuos.Eco_Andino.repository.GestorRepository;
import com.gestion.residuos.Eco_Andino.repository.TransportistaRepository;
import com.gestion.residuos.Eco_Andino.service.ArchivoService;
import com.gestion.residuos.Eco_Andino.service.VistaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Revisiones de mantenimiento: archivos adjuntos y vigencias de autorizaciones. */
class MantenimientoServiceTest {
    private static final LocalDate VENCIDA = LocalDate.of(2026, 11, 1);
    private static final LocalDate POR_VENCER = LocalDate.of(2026, 12, 10);
    private static final LocalDate VIGENTE = LocalDate.of(2027, 6, 30);

    private DocumentoRepository documentos;
    private TransportistaRepository transportistas;
    private GestorRepository gestores;
    private ArchivoService archivos;
    private MantenimientoService servicio;

    @BeforeEach
    void preparar() {
        documentos = mock(DocumentoRepository.class);
        transportistas = mock(TransportistaRepository.class);
        gestores = mock(GestorRepository.class);
        archivos = mock(ArchivoService.class);
        VistaService vistas = mock(VistaService.class);
        when(vistas.estadoVigencia(VENCIDA)).thenReturn("VENCIDA");
        when(vistas.estadoVigencia(POR_VENCER)).thenReturn("POR_VENCER");
        when(vistas.estadoVigencia(VIGENTE)).thenReturn("VIGENTE");
        servicio = new MantenimientoService(documentos, transportistas, gestores, archivos, vistas);
    }

    @Test
    @DisplayName("Detecta adjuntos que faltan en disco y archivos huérfanos")
    void detectaFaltantesYHuerfanos() {
        when(documentos.findRutasArchivo()).thenReturn(List.of("a.pdf", "b.pdf"));
        when(archivos.archivosEnDisco()).thenReturn(Set.of("b.pdf", "c.png"));

        VerificacionArchivos resultado = servicio.verificarArchivos();

        assertEquals(List.of("a.pdf"), resultado.faltantes());
        assertEquals(List.of("c.png"), resultado.huerfanos());
        assertFalse(resultado.correcta());
    }

    @Test
    @DisplayName("Si la BD y la carpeta coinciden la verificación es correcta")
    void verificacionCorrecta() {
        when(documentos.findRutasArchivo()).thenReturn(List.of("a.pdf"));
        when(archivos.archivosEnDisco()).thenReturn(Set.of("a.pdf"));

        assertTrue(servicio.verificarArchivos().correcta());
    }

    @Test
    @DisplayName("Avisa solo de las autorizaciones vencidas o por vencer")
    void revisaVigencias() {
        when(transportistas.findAll()).thenReturn(List.of(
                transportista("Transportes Andes", VENCIDA), transportista("Ruta Sur", VIGENTE)));
        when(gestores.findAll()).thenReturn(List.of(gestor("EcoGestión", POR_VENCER)));

        List<Vigencia> alertas = servicio.revisarVigencias();

        assertEquals(2, alertas.size());
        assertEquals("Transportes Andes", alertas.get(0).empresa());
        assertEquals("VENCIDA", alertas.get(0).estado());
        assertEquals("Gestor", alertas.get(1).tipo());
        assertEquals("POR_VENCER", alertas.get(1).estado());
    }

    private static Transportista transportista(String empresa, LocalDate vigencia) {
        Transportista t = new Transportista();
        t.setEmpresa(empresa);
        t.setAutorizacion("AUT-" + empresa.length());
        t.setVigencia(vigencia);
        return t;
    }

    private static Gestor gestor(String empresa, LocalDate vigencia) {
        Gestor g = new Gestor();
        g.setEmpresa(empresa);
        g.setAutorizacion("EO-RS-" + empresa.length());
        g.setVigencia(vigencia);
        return g;
    }
}
