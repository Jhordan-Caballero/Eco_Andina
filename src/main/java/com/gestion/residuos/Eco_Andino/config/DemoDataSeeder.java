package com.gestion.residuos.Eco_Andino.config;

import com.gestion.residuos.Eco_Andino.entity.*;
import com.gestion.residuos.Eco_Andino.repository.*;
import com.gestion.residuos.Eco_Andino.util.Codigos;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Datos de demostración de la planta (catálogos, generaciones, movimientos, órdenes, incidencias, documentos y
 * auditoría), iguales a los que usaba el prototipo del frontend. Las fechas son relativas al día en que se
 * inicia el sistema, así las alertas de plazos y vigencias siempre se ven. Solo se cargan si no hay residuos.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final ResiduoRepository residuoRepository;
    private final AreaGeneradoraRepository areaRepository;
    private final ContenedorRepository contenedorRepository;
    private final GeneracionRepository generacionRepository;
    private final MovimientoRepository movimientoRepository;
    private final TransportistaRepository transportistaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final GestorRepository gestorRepository;
    private final RecoleccionRepository recoleccionRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final ObservacionRepository observacionRepository;
    private final DocumentoRepository documentoRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final ConfiguracionAuditoriaRepository configuracionRepository;
    private final Clock clock;

    private final Map<String, Usuario> usuarios = new HashMap<>();
    private final List<Residuo> residuos = new ArrayList<>();
    private final List<AreaGeneradora> areas = new ArrayList<>();
    private final List<Contenedor> contenedores = new ArrayList<>();
    private final List<Generacion> generaciones = new ArrayList<>();
    private final List<Vehiculo> vehiculos = new ArrayList<>();
    private final List<Transportista> transportistas = new ArrayList<>();
    private final List<Gestor> gestores = new ArrayList<>();
    private final List<Recoleccion> ordenes = new ArrayList<>();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!configuracionRepository.existsById(ConfiguracionAuditoria.ID)) {
            configuracionRepository.save(new ConfiguracionAuditoria());
        }
        if (residuoRepository.count() > 0) {
            return;
        }
        for (String username : List.of("admin", "aquispe", "rhuaman", "ltorres", "jccori", "mvaldivia", "psalas")) {
            usuarioRepository.findByUsernameIgnoreCase(username).ifPresent(u -> usuarios.put(username, u));
        }
        if (usuarios.size() < 7) {
            log.warn("Seed de demostración omitido: faltan los usuarios de prueba");
            return;
        }
        sembrarCatalogos();
        sembrarGeneraciones();
        sembrarTransporte();
        sembrarOrdenes();
        sembrarMovimientos();
        sembrarIncidenciasYObservaciones();
        sembrarDocumentos();
        sembrarAuditoria();
        log.info("Seed: datos de demostración cargados ({} generaciones, {} órdenes)", generaciones.size(), ordenes.size());
    }

    // ---------------------------------------------------------------- catálogos

    private void sembrarCatalogos() {
        residuo("RES-001", "Aceite usado", "PELIGROSO", "ALTA", "L", 30, "Refinación y reciclaje", "Cilindro hermético rotulado sobre bandeja antiderrame");
        residuo("RES-002", "Chatarra metálica", "APROVECHABLE", "BAJA", "kg", 90, "Valorización de metales", "Separar de otros residuos y almacenar bajo techo");
        residuo("RES-003", "Envases con residuo químico", "PELIGROSO", "ALTA", "unid", 30, "Disposición en relleno de seguridad", "Envases cerrados, rotulados y con hoja de seguridad");
        residuo("RES-004", "Guantes y EPP contaminado", "PELIGROSO", "MEDIA", "kg", 15, "Incineración controlada", "Bolsa roja dentro de contenedor con tapa");
        residuo("RES-005", "Papel y cartón", "APROVECHABLE", "BAJA", "kg", 60, "Reciclaje", "Material seco y libre de grasa");
        residuo("RES-006", "Plástico PET", "APROVECHABLE", "BAJA", "kg", 60, "Reciclaje", "Envases vacíos y compactados");
        residuo("RES-007", "Residuos comunes", "COMUN", "BAJA", "kg", 7, "Relleno sanitario", "Bolsa negra en contenedor tapado");
        residuo("RES-008", "Material absorbente con hidrocarburos", "PELIGROSO", "ALTA", "kg", 30, "Incineración controlada", "Cilindro con tapa hermética rotulado");
        residuo("RES-009", "Residuos biocontaminados", "ESPECIAL", "ALTA", "kg", 7, "Esterilización e incineración", "Bolsa roja en recipiente rígido con tapa");
        residuo("RES-010", "Lodos de mantenimiento", "PELIGROSO", "MEDIA", "kg", 30, "Tratamiento fisicoquímico", "Cilindro hermético sin filtraciones");
        residuo("RES-011", "Madera de embalaje", "APROVECHABLE", "BAJA", "kg", 90, "Reutilización o compostaje", "Sin clavos expuestos ni tratamiento químico");

        area("AR-PRO", "Producción", "Fabricación y ensamblaje", "rhuaman", AreaGeneradora.ACTIVA);
        area("AR-MAN", "Mantenimiento", "Mantenimiento de maquinaria y equipos", "ltorres", AreaGeneradora.ACTIVA);
        area("AR-ALM", "Almacenes", "Recepción y despacho de materiales", "ltorres", AreaGeneradora.ACTIVA);
        area("AR-OFI", "Oficinas", "Administración y tópico", "rhuaman", AreaGeneradora.ACTIVA);
        area("AR-PAT", "Patio de residuos", "Acopio y almacenamiento temporal", "ltorres", AreaGeneradora.ACTIVA);
        area("AR-LAB", "Laboratorio de calidad", "Control de calidad de insumos", "aquispe", AreaGeneradora.INACTIVA);

        contenedor("CT-101", "Cilindro metálico 200 L", 200, 1, "Patio de residuos · zona A", Contenedor.DISPONIBLE, 20, hace(27, 10, 0));
        contenedor("CT-102", "Jaula metálica 1 t", 1000, 2, "Patio de residuos · zona B", Contenedor.DISPONIBLE, 310, hace(18, 15, 0));
        contenedor("CT-103", "Contenedor con tapa 240 L", 40, 3, "Patio de residuos · zona A", Contenedor.DISPONIBLE, 6, hace(8, 11, 0));
        contenedor("CT-104", "Cilindro con tapa 200 L", 120, 4, "Mantenimiento · taller", Contenedor.DISPONIBLE, 60, hace(9, 16, 0));
        contenedor("CT-105", "Contenedor azul 240 L", 80, 5, "Oficinas · primer piso", Contenedor.DISPONIBLE, 28, hace(12, 9, 0));
        contenedor("CT-106", "Contenedor blanco 240 L", 60, 6, "Producción · línea 2", Contenedor.DISPONIBLE, 0, null);
        contenedor("CT-107", "Contenedor negro 1100 L", 300, 7, "Patio de residuos · zona C", Contenedor.DISPONIBLE, 105, hace(1, 8, 0));
        contenedor("CT-108", "Cilindro con tapa 200 L", 100, 8, "Mantenimiento · taller", Contenedor.DISPONIBLE, 0, null);
        contenedor("CT-109", "Recipiente rojo 60 L", 20, 9, "Oficinas · tópico", Contenedor.DISPONIBLE, 0, null);
        contenedor("CT-110", "Cilindro metálico 200 L", 200, 1, "Patio de residuos · zona A", Contenedor.MANTENIMIENTO, 0, null);
        contenedor("CT-111", "Cilindro hermético 200 L", 150, 10, "Mantenimiento · taller", Contenedor.DISPONIBLE, 0, null);
    }

    private void residuo(String codigo, String nombre, String categoria, String peligrosidad, String unidad, int plazo,
                         String tratamiento, String requisitos) {
        Residuo r = new Residuo();
        r.setCodigo(codigo);
        r.setNombre(nombre);
        r.setCategoria(categoria);
        r.setPeligrosidad(peligrosidad);
        r.setUnidad(unidad);
        r.setPlazoMaximoDias(plazo);
        r.setTratamiento(tratamiento);
        r.setRequisitos(requisitos);
        residuos.add(residuoRepository.save(r));
    }

    private void area(String codigo, String nombre, String proceso, String responsable, String estado) {
        AreaGeneradora a = new AreaGeneradora();
        a.setCodigo(codigo);
        a.setNombre(nombre);
        a.setProceso(proceso);
        a.setResponsable(usuarios.get(responsable));
        a.setEstado(estado);
        areas.add(areaRepository.save(a));
    }

    private void contenedor(String codigo, String tipo, double capacidad, int residuo, String ubicacion, String estado,
                            double stock, LocalDateTime fechaStock) {
        Contenedor c = new Contenedor();
        c.setCodigo(codigo);
        c.setTipo(tipo);
        c.setCapacidad(BigDecimal.valueOf(capacidad));
        c.setResiduoPermitido(residuos.get(residuo - 1));
        c.setUbicacion(ubicacion);
        c.setEstado(estado);
        c.setStockInicial(BigDecimal.valueOf(stock));
        c.setFechaStock(fechaStock);
        contenedores.add(contenedorRepository.save(c));
    }

    // ---------------------------------------------------------------- generaciones

    private void sembrarGeneraciones() {
        generacion(1, 2, 2, 300, hace(26, 10, 30), "rhuaman", "BUEN_ESTADO", true, Generacion.VALIDADA, null, "gen-0101-chatarra.jpg", hace(26, 15, 0), null);
        generacion(4, 5, 5, 35, hace(19, 9, 15), "rhuaman", "BUEN_ESTADO", true, Generacion.VALIDADA, null, null, hace(19, 12, 0), null);
        generacion(3, 4, 4, 60, hace(14, 11, 0), "ltorres", "BUEN_ESTADO", true, Generacion.VALIDADA, null, "gen-0103-epp.jpg", hace(14, 16, 0), null);
        generacion(1, 3, 3, 18, hace(9, 8, 40), "rhuaman", "BUEN_ESTADO", true, Generacion.VALIDADA, null, "gen-0104-envases.jpg", hace(9, 10, 0), null);
        generacion(1, 2, 2, 120, hace(6, 14, 10), "rhuaman", "BUEN_ESTADO", true, Generacion.VALIDADA, null, null, hace(6, 17, 0), null);
        generacion(2, 1, 1, 40, hace(5, 9, 30), "ltorres", "BUEN_ESTADO", true, Generacion.VALIDADA, null, "gen-0106-aceite.jpg", hace(5, 11, 0), null);
        generacion(2, 1, 1, 88, hace(4, 10, 45), "ltorres", "BUEN_ESTADO", true, Generacion.VALIDADA, null, "gen-0107-aceite.jpg", hace(4, 15, 0), null);
        generacion(2, 8, 8, 30, hace(4, 16, 20), "ltorres", "DETERIORADO", true, Generacion.OBSERVADA,
                "La tapa del cilindro está rota.", "gen-0108-absorbente.jpg", hace(3, 11, 5),
                "Rotular el cilindro y reemplazar la tapa antes de volver a enviar el registro.");
        generacion(1, 6, 6, 22, hace(3, 10, 0), "rhuaman", "BUEN_ESTADO", true, Generacion.VALIDADA, null, null, hace(3, 11, 0), null);
        generacion(2, 4, 4, 50, hace(3, 15, 30), "ltorres", "BUEN_ESTADO", true, Generacion.VALIDADA, null, "gen-0110-epp.jpg", hace(3, 17, 0), null);
        generacion(4, 7, 7, 45, hace(2, 17, 10), "rhuaman", "BUEN_ESTADO", true, Generacion.PENDIENTE, null, null, null, null);
        generacion(1, 2, 2, 210, hace(1, 11, 25), "rhuaman", "BUEN_ESTADO", true, Generacion.PENDIENTE,
                "Incluye retazos con restos de pintura separados en saco aparte.", "gen-0112-chatarra.jpg", null, null);
        generacion(4, 9, 9, 4, hace(1, 15, 40), "rhuaman", "BUEN_ESTADO", true, Generacion.PENDIENTE, null, "gen-0113-bio.jpg", null, null);
        generacion(2, 10, 11, 35, haceHoras(3), "ltorres", "BUEN_ESTADO", false, Generacion.PENDIENTE,
                "Se detectó agua en el cilindro; se drenó antes del pesaje.", "gen-0114-lodos.jpg", null, null);
    }

    private void generacion(int area, int residuo, int contenedor, double cantidad, LocalDateTime fecha, String responsable,
                            String condicion, boolean segregacion, String estado, String observaciones, String evidencia,
                            LocalDateTime fechaValidacion, String observacionValidacion) {
        Generacion g = new Generacion();
        g.setArea(areas.get(area - 1));
        g.setResiduo(residuos.get(residuo - 1));
        g.setContenedor(contenedores.get(contenedor - 1));
        g.setCantidad(BigDecimal.valueOf(cantidad));
        g.setFecha(fecha);
        g.setResponsable(usuarios.get(responsable));
        g.setCondicionEnvase(condicion);
        g.setSegregacionCorrecta(segregacion);
        g.setEstadoValidacion(estado);
        g.setObservaciones(observaciones);
        g.setEvidencia(evidencia);
        if (fechaValidacion != null) {
            g.setValidadoPor(usuarios.get("aquispe"));
            g.setFechaValidacion(fechaValidacion);
        }
        g.setObservacionValidacion(observacionValidacion);
        generacionRepository.save(g);
        g.setCodigo(Codigos.generacion(g.getIdGeneracion()));
        generaciones.add(g);
    }

    // ---------------------------------------------------------------- transporte

    private void sembrarTransporte() {
        Transportista andina = transportista("Transportes Andina SAC", "EO-RS-00312-2024-MINAM", "Jorge Ccori Apaza · 959 412 377", dia(160));
        vehiculo(andina, "V2A-845", "Camión furgón", 4000, "OPERATIVO");
        vehiculo(andina, "V3B-112", "Camión cisterna", 3000, "OPERATIVO");
        Transportista ecotrans = transportista("EcoTrans Perú EIRL", "EO-RS-00458-2023-MINAM", "Lucía Paredes Zea · 954 118 260", dia(20));
        vehiculo(ecotrans, "VBK-307", "Camión baranda", 6000, "OPERATIVO");
        vehiculo(ecotrans, "VBK-411", "Camión furgón", 3500, "EN_MANTENIMIENTO");
        Transportista sur = transportista("Recolectora Sur SAC", "EO-RS-00127-2022-MINAM", "Raúl Chávez Pinto · 958 330 941", dia(-45));
        vehiculo(sur, "V7C-520", "Camión compactador", 8000, "OPERATIVO");

        gestor("Gestora Ambiental del Sur SAC", "EO-RS-00089-2023-MINAM", "Disposición final en relleno de seguridad", dia(300));
        gestor("Reciclajes Arequipa SAC", "EO-RS-00274-2024-MINAM", "Valorización de metales, papel y plásticos", dia(180));
        gestor("Termo Ambiental Perú SAC", "EO-RS-00415-2024-MINAM", "Tratamiento térmico (incineración controlada)", dia(25));
    }

    private Transportista transportista(String empresa, String autorizacion, String contacto, LocalDate vigencia) {
        Transportista t = new Transportista();
        t.setEmpresa(empresa);
        t.setAutorizacion(autorizacion);
        t.setContacto(contacto);
        t.setVigencia(vigencia);
        transportistas.add(transportistaRepository.save(t));
        return t;
    }

    private void vehiculo(Transportista transportista, String placa, String tipo, double capacidad, String estado) {
        Vehiculo v = new Vehiculo();
        v.setPlaca(placa);
        v.setTipo(tipo);
        v.setCapacidad(BigDecimal.valueOf(capacidad));
        v.setEstado(estado);
        v.setTransportista(transportista);
        transportista.getVehiculos().add(v);
        vehiculos.add(vehiculoRepository.save(v));
    }

    private void gestor(String empresa, String autorizacion, String proceso, LocalDate vigencia) {
        Gestor g = new Gestor();
        g.setEmpresa(empresa);
        g.setAutorizacion(autorizacion);
        g.setProcesoDestino(proceso);
        g.setVigencia(vigencia);
        gestores.add(gestorRepository.save(g));
    }

    // ---------------------------------------------------------------- órdenes de recolección

    private void sembrarOrdenes() {
        // OR-1037: cerrada
        Recoleccion o1 = orden(1, hace(24, 9, 0), dia(-21), 2, 3, 2, null);
        aprobar(o1, hace(23, 9, 0));
        salida(o1, hace(21, 13, 10), "GR-0001-004512", "Llegada con 3 horas de retraso.");
        recepcion(o1, hace(21, 17, 0), 298, null, "VALORIZACION", "Reciclaje de metales");
        o1.setFechaCierre(hace(20, 10, 0));
        o1.setCerradoPor(usuarios.get("aquispe"));
        o1.setEstado(Recoleccion.CERRADA);

        // OR-1038: cerrada
        Recoleccion o2 = orden(2, hace(17, 9, 0), dia(-15), 2, 3, 2, null);
        aprobar(o2, hace(16, 9, 0));
        salida(o2, hace(15, 9, 20), "GR-0001-004588", null);
        recepcion(o2, hace(15, 14, 0), 35, null, "VALORIZACION", "Reciclaje de papel y cartón");
        o2.setFechaCierre(hace(14, 10, 0));
        o2.setCerradoPor(usuarios.get("aquispe"));
        o2.setEstado(Recoleccion.CERRADA);

        // OR-1039: recibida, pendiente de cierre (falta el certificado)
        Recoleccion o3 = orden(3, hace(6, 9, 0), dia(-3), 1, 1, 3, null);
        aprobar(o3, hace(5, 10, 0));
        salida(o3, hace(3, 8, 15), "MRSP-2026-0398", null);
        recepcion(o3, hace(2, 16, 30), 58, "Diferencia de 2 kg por humedad del material.", "TRATAMIENTO", "Incineración controlada");
        o3.setEstado(Recoleccion.RECIBIDA);

        // OR-1040: en tránsito
        Recoleccion o4 = orden(4, hace(3, 9, 0), dia(0), 1, 1, 1, "Residuo peligroso: verificar rotulado de cada envase.");
        aprobar(o4, hace(2, 15, 0));
        salida(o4, haceHoras(2), "MRSP-2026-0412", null);
        o4.setEstado(Recoleccion.EN_TRANSITO);

        // OR-1041: programada (aprobada)
        Recoleccion o5 = orden(5, hace(2, 10, 0), dia(1), 2, 3, 2, null);
        aprobar(o5, hace(1, 9, 0));
        o5.setEstado(Recoleccion.PROGRAMADA);

        // OR-1042: pendiente de aprobación
        orden(6, haceHoras(1), dia(2), 1, 2, 1, "Coordinar el acceso del camión cisterna por la puerta 2.");
        // Los cambios posteriores al save se guardan solos al confirmar la transacción (entidades gestionadas)
    }

    private Recoleccion orden(int generacion, LocalDateTime solicitud, LocalDate programada, int transportista,
                              int vehiculo, int gestor, String observaciones) {
        Recoleccion o = new Recoleccion();
        o.setEstado(Recoleccion.PENDIENTE);
        o.setFechaSolicitud(solicitud);
        o.setSolicitadoPor(usuarios.get("aquispe"));
        o.setFechaProgramada(programada);
        o.setTransportista(transportistas.get(transportista - 1));
        o.setVehiculo(vehiculos.get(vehiculo - 1));
        o.setGestor(gestores.get(gestor - 1));
        o.setObservaciones(observaciones);
        o.agregarDetalle(generaciones.get(generacion - 1));
        recoleccionRepository.save(o);
        o.setNumeroOrden(Codigos.orden(o.getIdRecoleccion()));
        ordenes.add(o);
        return o;
    }

    private void aprobar(Recoleccion o, LocalDateTime fecha) {
        o.setFechaAprobacion(fecha);
        o.setAprobadoPor(usuarios.get("aquispe"));
    }

    private void salida(Recoleccion o, LocalDateTime fecha, String documento, String observaciones) {
        o.setFechaSalida(fecha);
        o.setDocumentoSalida(documento);
        o.setSalidaPor(usuarios.get("jccori"));
        o.setObservacionesSalida(observaciones);
    }

    private void recepcion(Recoleccion o, LocalDateTime fecha, double cantidad, String observaciones, String tipoDestino,
                           String tratamiento) {
        o.setFechaRecepcion(fecha);
        o.setCantidadRecibida(BigDecimal.valueOf(cantidad));
        o.setRecepcionConforme(true);
        o.setRecibidoPor(usuarios.get("mvaldivia"));
        o.setObservacionesRecepcion(observaciones);
        o.setTipoDestino(tipoDestino);
        o.setTratamientoDestino(tratamiento);
    }

    // ---------------------------------------------------------------- movimientos

    private void sembrarMovimientos() {
        List<Movimiento> movimientos = new ArrayList<>();
        // Ingreso de cada generación a su contenedor
        for (Generacion g : generaciones) {
            movimientos.add(movimiento(Movimiento.INGRESO, g.getFecha(), g.getResiduo(), g.getArea().getNombre(),
                    g.getContenedor().getCodigo(), g.getCantidad().doubleValue(), g.getCodigo(), g.getResponsable(),
                    "Registro de generación"));
        }
        // Salida de las órdenes que ya dejaron la planta
        for (Recoleccion o : ordenes) {
            if (o.getFechaSalida() == null) {
                continue;
            }
            for (DetalleRecoleccion d : o.getDetalles()) {
                Generacion g = d.getGeneracion();
                movimientos.add(movimiento(Movimiento.SALIDA, o.getFechaSalida(), g.getResiduo(), g.getContenedor().getCodigo(),
                        o.getTransportista().getEmpresa() + " · " + o.getVehiculo().getPlaca(), g.getCantidad().doubleValue(),
                        o.getNumeroOrden(), usuarios.get("jccori"), "Salida por recolección"));
            }
        }
        // Históricos de la carga inicial (ya reflejados en el stock inicial, por eso no afectan la ocupación)
        movimientos.add(movimiento(Movimiento.TRASLADO, hace(4, 9, 0), residuos.get(0), "CT-110", "CT-101", 20, "CT-110",
                usuarios.get("ltorres"), "Contenedor en mantenimiento"));
        movimientos.add(movimiento(Movimiento.PESAJE, hace(2, 8, 30), residuos.get(3), "CT-104", "CT-104", 110, "CT-104",
                usuarios.get("ltorres"), "Pesaje de verificación semanal"));

        movimientos.sort(Comparator.comparing(Movimiento::getFecha));
        for (Movimiento m : movimientos) {
            movimientoRepository.save(m);
            m.setCodigo(Codigos.movimiento(m.getIdMovimiento()));
        }
    }

    private Movimiento movimiento(String tipo, LocalDateTime fecha, Residuo residuo, String origen, String destino,
                                  double cantidad, String referencia, Usuario usuario, String motivo) {
        Movimiento m = new Movimiento();
        m.setTipo(tipo);
        m.setFecha(fecha);
        m.setResiduo(residuo);
        m.setOrigen(origen);
        m.setDestino(destino);
        m.setCantidad(BigDecimal.valueOf(cantidad));
        if (Movimiento.PESAJE.equals(tipo)) {
            m.setAjuste(BigDecimal.ZERO);
        }
        m.setReferencia(referencia);
        m.setUsuario(usuario);
        m.setMotivo(motivo);
        m.setAfectaStock(false);
        return m;
    }

    // ---------------------------------------------------------------- incidencias y observaciones

    private void sembrarIncidenciasYObservaciones() {
        Incidencia rechazo = incidencia("RECHAZO", "El gestor rechazó 2 envases sin rotular de una orden anterior.", 1, null,
                "MEDIA", Incidencia.CERRADA, hace(34, 16, 0), "mvaldivia", "rhuaman",
                "Se rotularon los envases y se reprogramó su recolección.", hace(31, 9, 0));
        rechazo.agregarSeguimiento(usuarios.get("aquispe"), "Incidencia cerrada.", hace(31, 9, 0));

        Incidencia retraso = incidencia("RETRASO", "La unidad de EcoTrans llegó 3 horas después de lo programado para la orden "
                        + ordenes.get(0).getNumeroOrden() + ".", 5, ordenes.get(0).getNumeroOrden(), "BAJA",
                Incidencia.CERRADA, hace(21, 13, 30), "aquispe", "aquispe",
                "Se acordó con EcoTrans una ventana horaria fija de 8:00 a 10:00.", hace(20, 10, 30));
        retraso.agregarSeguimiento(usuarios.get("aquispe"), "Se coordinó con el transportista.", hace(21, 15, 0));
        retraso.agregarSeguimiento(usuarios.get("aquispe"), "Incidencia cerrada: acuerdo firmado.", hace(20, 10, 30));

        incidencia("ENVASE_DANADO", "Cilindro de material absorbente con la tapa rota y sin rótulo.", 2,
                generaciones.get(7).getCodigo(), "MEDIA", Incidencia.ABIERTA, hace(3, 11, 20), "aquispe", null, null, null);

        incidencia("MEZCLA", "Se encontraron envases con residuo químico mezclados con la chatarra del patio.", 1, "CT-102",
                "MEDIA", Incidencia.ABIERTA, hace(1, 15, 5), "rhuaman", null, null, null);

        Incidencia derrame = incidencia("DERRAME", "Derrame de aproximadamente 3 L de aceite usado al trasvasar al cilindro CT-101.",
                2, "CT-101", "ALTA", Incidencia.EN_SEGUIMIENTO, hace(2, 10, 20), "ltorres", "ltorres",
                "Contención con material absorbente, limpieza de la zona y capacitación en trasvase seguro.", null);
        derrame.agregarSeguimiento(usuarios.get("aquispe"), "Se asignó responsable y se aplicó contención inmediata.", hace(2, 11, 0));

        observacion(hace(21, 17, 10), "mvaldivia", "Gestor autorizado", "RECEPCION", ordenes.get(0).getNumeroOrden(), "Material recibido conforme y sin restos de otros residuos.");
        observacion(hace(4, 9, 10), "ltorres", "Almacenero", "ALMACENAMIENTO", "CT-110", "Se trasladó el aceite del CT-110 al CT-101 para dar mantenimiento al cilindro.");
        observacion(hace(2, 16, 40), "mvaldivia", "Gestor autorizado", "RECEPCION", ordenes.get(2).getNumeroOrden(), "Se recibieron 58 kg; la diferencia de 2 kg se debe a la humedad del material.");
        observacion(hace(2, 8, 45), "ltorres", "Almacenero", "ALMACENAMIENTO", "CT-104", "El cilindro de EPP está cerca de su capacidad; conviene programar recolección.");
        observacion(hace(1, 11, 40), "rhuaman", "Generador", "GENERACION", generaciones.get(11).getCodigo(), "Los retazos con restos de pintura se separaron en un saco aparte.");
        observacion(haceHoras(2), "jccori", "Transportista", "TRASLADO", ordenes.get(3).getNumeroOrden(), "Salida con 30 minutos de retraso por la verificación del manifiesto.");
    }

    private Incidencia incidencia(String tipo, String descripcion, int area, String referencia, String severidad, String estado,
                                  LocalDateTime fecha, String reportadoPor, String responsable, String accion,
                                  LocalDateTime cierre) {
        Incidencia i = new Incidencia();
        i.setTipo(tipo);
        i.setDescripcion(descripcion);
        i.setArea(areas.get(area - 1));
        i.setReferencia(referencia);
        i.setSeveridad(severidad);
        i.setEstado(estado);
        i.setFecha(fecha);
        i.setReportadoPor(usuarios.get(reportadoPor));
        i.setResponsable(responsable == null ? null : usuarios.get(responsable));
        i.setAccionCorrectiva(accion);
        i.setFechaCierre(cierre);
        incidenciaRepository.save(i);
        i.setCodigo(Codigos.incidencia(i.getIdIncidencia()));
        return i;
    }

    private void observacion(LocalDateTime fecha, String autor, String rol, String ambito, String referencia, String texto) {
        Observacion o = new Observacion();
        o.setFecha(fecha);
        o.setAutor(usuarios.get(autor));
        o.setRol(rol);
        o.setAmbito(ambito);
        o.setReferencia(referencia);
        o.setTexto(texto);
        observacionRepository.save(o);
    }

    // ---------------------------------------------------------------- documentos (solo la referencia, sin archivo)

    private void sembrarDocumentos() {
        String or1037 = ordenes.get(0).getNumeroOrden();
        String or1038 = ordenes.get(1).getNumeroOrden();
        String or1039 = ordenes.get(2).getNumeroOrden();
        String or1040 = ordenes.get(3).getNumeroOrden();
        documento("GUIA_REMISION", "guia-GR-0001-004512.pdf", or1037, "Guía de remisión del traslado", hace(21, 13, 15), "jccori", Documento.VALIDADO, null);
        documento("CERTIFICADO", "certificado-CV-2026-0187.pdf", or1037, "Certificado de valorización de metales", hace(21, 17, 30), "mvaldivia", Documento.VALIDADO, null);
        documento("GUIA_REMISION", "guia-GR-0001-004588.pdf", or1038, "Guía de remisión del traslado", hace(15, 9, 25), "jccori", Documento.VALIDADO, null);
        documento("CERTIFICADO", "certificado-CV-2026-0201.pdf", or1038, "Certificado de valorización de papel y cartón", hace(15, 15, 0), "mvaldivia", Documento.VALIDADO, null);
        documento("EVIDENCIA_FOTO", "gen-0108-absorbente.jpg", generaciones.get(7).getCodigo(), "Fotografía del cilindro de material absorbente", hace(4, 16, 25), "ltorres", Documento.RECHAZADO, "La fotografía no muestra el rótulo del cilindro.");
        documento("MANIFIESTO", "manifiesto-MRSP-2026-0398.pdf", or1039, "Manifiesto de la salida de EPP contaminado", hace(3, 8, 20), "jccori", Documento.VALIDADO, null);
        documento("ACTA_RECEPCION", "acta-recepcion-" + or1039 + ".pdf", or1039, "Acta de recepción firmada por el gestor", hace(2, 16, 45), "mvaldivia", Documento.VALIDADO, null);
        documento("EVIDENCIA_FOTO", "gen-0112-chatarra.jpg", generaciones.get(11).getCodigo(), "Fotografía del pesaje de chatarra", hace(1, 11, 30), "rhuaman", Documento.PENDIENTE, null);
        documento("MANIFIESTO", "manifiesto-MRSP-2026-0412.pdf", or1040, "Manifiesto de la salida de envases químicos", haceHoras(2), "jccori", Documento.PENDIENTE, null);
    }

    private void documento(String tipo, String nombre, String referencia, String descripcion, LocalDateTime fecha,
                           String subidoPor, String estado, String motivoRechazo) {
        Documento d = new Documento();
        d.setTipo(tipo);
        d.setNombreArchivo(nombre);
        d.setReferencia(referencia);
        d.setDescripcion(descripcion);
        d.setFecha(fecha);
        d.setSubidoPor(usuarios.get(subidoPor));
        d.setEstado(estado);
        d.setMotivoRechazo(motivoRechazo);
        documentoRepository.save(d);
    }

    // ---------------------------------------------------------------- auditoría

    private void sembrarAuditoria() {
        auditoria(hace(20, 10, 0), "aquispe", "CERRAR", "Recolección", ordenes.get(0).getNumeroOrden(), "Cerró la orden con certificado CV-2026-0187");
        auditoria(hace(20, 10, 30), "aquispe", "CERRAR", "Incidencia", "INC-0002", "Cerró la incidencia de retraso en recolección");
        auditoria(hace(15, 15, 0), "mvaldivia", "ADJUNTAR", "Documento", "certificado-CV-2026-0201.pdf", "Adjuntó el certificado de valorización");
        auditoria(hace(14, 10, 0), "aquispe", "CERRAR", "Recolección", ordenes.get(1).getNumeroOrden(), "Cerró la orden con certificado CV-2026-0201");
        auditoria(hace(7, 11, 0), "admin", "MODIFICAR", "Transportista", "EcoTrans Perú EIRL", "Registró el vehículo VBK-411 en mantenimiento");
        auditoria(hace(6, 8, 0), "admin", "DESACTIVAR", "Usuario", "jperez", "Desactivó la cuenta del usuario");
        auditoria(hace(5, 10, 0), "admin", "CREAR", "Residuo", "RES-011", "Registró el residuo Madera de embalaje");
        auditoria(hace(4, 9, 30), "admin", "MODIFICAR", "Contenedor", "CT-110", "Cambió el estado a En mantenimiento");
        auditoria(hace(3, 11, 5), "aquispe", "OBSERVAR", "Generación", generaciones.get(7).getCodigo(), "Observó el registro: rotular el cilindro y reemplazar la tapa");
        auditoria(hace(3, 17, 0), "aquispe", "VALIDAR", "Generación", generaciones.get(9).getCodigo(), "Validó el registro de generación");
        auditoria(hace(2, 16, 30), "mvaldivia", "REGISTRAR", "Recolección", ordenes.get(2).getNumeroOrden(), "Registró la recepción de 58 kg y su destino");
        auditoria(hace(1, 9, 0), "aquispe", "APROBAR", "Recolección", ordenes.get(4).getNumeroOrden(), "Aprobó la orden de recolección");
        auditoria(hace(1, 15, 5), "rhuaman", "CREAR", "Incidencia", "INC-0004", "Reportó una mezcla de residuos en el patio");
        auditoria(haceHoras(1), "aquispe", "CREAR", "Recolección", ordenes.get(5).getNumeroOrden(), "Programó la recolección de 40 L de aceite usado");
        auditoria(haceHoras(3), "ltorres", "CREAR", "Generación", generaciones.get(13).getCodigo(), "Registró 35 kg de lodos de mantenimiento");
        auditoria(haceHoras(2), "jccori", "REGISTRAR", "Recolección", ordenes.get(3).getNumeroOrden(), "Registró la salida con el manifiesto MRSP-2026-0412");
    }

    private void auditoria(LocalDateTime fecha, String usuario, String accion, String entidad, String registro, String detalle) {
        Auditoria a = new Auditoria();
        a.setFecha(fecha);
        a.setUsuario(usuarios.get(usuario));
        a.setNombreUsuario(usuarios.get(usuario).getNombreCompleto());
        a.setAccion(accion);
        a.setEntidad(entidad);
        a.setRegistro(registro);
        a.setDetalle(detalle);
        auditoriaRepository.save(a);
    }

    // ---------------------------------------------------------------- fechas relativas

    private LocalDateTime hace(int dias, int hora, int minuto) {
        return LocalDate.now(clock).minusDays(dias).atTime(hora, minuto);
    }

    private LocalDateTime haceHoras(int horas) {
        return LocalDateTime.now(clock).minusHours(horas).withSecond(0).withNano(0);
    }

    private LocalDate dia(int desplazamiento) {
        return LocalDate.now(clock).plusDays(desplazamiento);
    }
}
