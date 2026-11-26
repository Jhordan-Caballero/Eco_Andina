package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.RecoleccionDtos.Requisito;
import com.gestion.residuos.Eco_Andino.entity.Contenedor;
import com.gestion.residuos.Eco_Andino.entity.Documento;
import com.gestion.residuos.Eco_Andino.entity.Gestor;
import com.gestion.residuos.Eco_Andino.entity.Generacion;
import com.gestion.residuos.Eco_Andino.entity.Movimiento;
import com.gestion.residuos.Eco_Andino.entity.Recoleccion;
import com.gestion.residuos.Eco_Andino.entity.Transportista;
import com.gestion.residuos.Eco_Andino.entity.Vehiculo;
import com.gestion.residuos.Eco_Andino.repository.DocumentoRepository;
import com.gestion.residuos.Eco_Andino.repository.GeneracionRepository;
import com.gestion.residuos.Eco_Andino.repository.MovimientoRepository;
import com.gestion.residuos.Eco_Andino.repository.RecoleccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Cálculos de VistaService: requisitos de cierre (RNF003), vigencias (CU15) y ocupación de contenedores (CU10). */
class VistaServiceTest {
    private static final LocalDate HOY = LocalDate.of(2026, 11, 25);

    private GeneracionRepository generaciones;
    private MovimientoRepository movimientos;
    private RecoleccionRepository recolecciones;
    private DocumentoRepository documentos;
    private VistaService vistas;

    @BeforeEach
    void preparar() {
        generaciones = mock(GeneracionRepository.class);
        movimientos = mock(MovimientoRepository.class);
        recolecciones = mock(RecoleccionRepository.class);
        documentos = mock(DocumentoRepository.class);
        SesionService sesion = mock(SesionService.class);
        when(sesion.hoy()).thenReturn(HOY);
        vistas = new VistaService(generaciones, movimientos, recolecciones, documentos, sesion);
    }

    // ------------------------------------------------------------ requisitos de cierre (RNF003)

    @Test
    @DisplayName("Una orden con todo registrado cumple los 5 requisitos de cierre")
    void ordenCompletaCumpleTodo() {
        List<Requisito> requisitos = vistas.requisitosCierre(ordenRecibida(),
                List.of(documento("MANIFIESTO", Documento.VALIDADO), documento("CERTIFICADO", Documento.PENDIENTE)));

        assertEquals(5, requisitos.size());
        assertTrue(requisitos.stream().allMatch(Requisito::cumple));
    }

    @Test
    @DisplayName("Sin certificado no se puede cerrar")
    void faltaCertificado() {
        Map<String, Boolean> requisitos = porClave(vistas.requisitosCierre(ordenRecibida(),
                List.of(documento("GUIA_REMISION", Documento.VALIDADO))));

        assertTrue(requisitos.get("traslado"));
        assertFalse(requisitos.get("certificado"));
    }

    @Test
    @DisplayName("Un documento rechazado no cuenta para el cierre")
    void documentoRechazadoNoCuenta() {
        Map<String, Boolean> requisitos = porClave(vistas.requisitosCierre(ordenRecibida(),
                List.of(documento("MANIFIESTO", Documento.VALIDADO), documento("CERTIFICADO", Documento.RECHAZADO))));

        assertFalse(requisitos.get("certificado"));
    }

    @Test
    @DisplayName("Sin cantidad recibida ni destino tampoco se cumple")
    void faltanDatosDeRecepcion() {
        Recoleccion orden = ordenRecibida();
        orden.setCantidadRecibida(BigDecimal.ZERO);
        orden.setTipoDestino(null);

        Map<String, Boolean> requisitos = porClave(vistas.requisitosCierre(orden, List.of()));

        assertFalse(requisitos.get("cantidad"));
        assertFalse(requisitos.get("destino"));
        assertTrue(requisitos.get("transportista"));
    }

    // ------------------------------------------------------------ vigencias (CU15)

    @Test
    @DisplayName("Clasifica las autorizaciones en vigente, por vencer (30 días) y vencida")
    void estadoVigencia() {
        assertEquals("VENCIDA", vistas.estadoVigencia(HOY.minusDays(1)));
        assertEquals("POR_VENCER", vistas.estadoVigencia(HOY));
        assertEquals("POR_VENCER", vistas.estadoVigencia(HOY.plusDays(30)));
        assertEquals("VIGENTE", vistas.estadoVigencia(HOY.plusDays(31)));
    }

    // ------------------------------------------------------------ ocupación de contenedores (CU10)

    @Test
    @DisplayName("Ocupación = stock inicial + generaciones en planta + traslados y pesajes")
    void ocupacionDelContenedor() {
        Contenedor contenedor = new Contenedor();
        contenedor.setIdContenedor(1);
        contenedor.setCodigo("CT-01");
        contenedor.setStockInicial(new BigDecimal("10"));

        Generacion enPlanta = generacion(1, contenedor, "5");
        Generacion yaRetirada = generacion(2, contenedor, "3");
        Recoleccion enTransito = new Recoleccion();
        enTransito.setEstado(Recoleccion.EN_TRANSITO);
        enTransito.agregarDetalle(yaRetirada);

        Movimiento traslado = new Movimiento();
        traslado.setTipo(Movimiento.TRASLADO);
        traslado.setOrigen("CT-01");
        traslado.setDestino("CT-02");
        traslado.setCantidad(new BigDecimal("2"));
        traslado.setAfectaStock(true);

        Movimiento pesaje = new Movimiento();
        pesaje.setTipo(Movimiento.PESAJE);
        pesaje.setDestino("CT-01");
        pesaje.setAjuste(new BigDecimal("1.5"));
        pesaje.setAfectaStock(true);

        when(generaciones.findAll()).thenReturn(List.of(enPlanta, yaRetirada));
        when(recolecciones.findAll()).thenReturn(List.of(enTransito));
        when(movimientos.findAll()).thenReturn(List.of(traslado, pesaje));
        when(documentos.findAll()).thenReturn(List.of());

        // 10 inicial + 5 en planta (los 3 en tránsito ya salieron) - 2 trasladados + 1,5 de ajuste por pesaje
        assertEquals(14.5, vistas.ocupacion(contenedor, vistas.instantanea()));
    }

    // ------------------------------------------------------------ datos de prueba

    private static Recoleccion ordenRecibida() {
        Recoleccion orden = new Recoleccion();
        orden.setNumeroOrden("OR-1040");
        orden.setEstado(Recoleccion.RECIBIDA);
        orden.setTransportista(new Transportista());
        orden.setVehiculo(new Vehiculo());
        orden.setGestor(new Gestor());
        orden.setCantidadRecibida(new BigDecimal("120.5"));
        orden.setTipoDestino("RECICLAJE");
        return orden;
    }

    private static Documento documento(String tipo, String estado) {
        Documento documento = new Documento();
        documento.setTipo(tipo);
        documento.setEstado(estado);
        documento.setReferencia("OR-1040");
        documento.setFecha(LocalDateTime.of(2026, 11, 20, 10, 0));
        return documento;
    }

    private static Generacion generacion(int id, Contenedor contenedor, String cantidad) {
        Generacion generacion = new Generacion();
        generacion.setIdGeneracion(id);
        generacion.setContenedor(contenedor);
        generacion.setCantidad(new BigDecimal(cantidad));
        return generacion;
    }

    private static Map<String, Boolean> porClave(List<Requisito> requisitos) {
        return requisitos.stream().collect(Collectors.toMap(Requisito::clave, Requisito::cumple));
    }
}
