package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.AreaDto;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.ContenedorBasico;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.ContenedorVista;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.GestorDto;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.ResiduoDto;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.TransportistaDto;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.VehiculoDto;
import com.gestion.residuos.Eco_Andino.dto.DocumentoDtos.DocumentoDto;
import com.gestion.residuos.Eco_Andino.dto.GeneracionDtos.GeneracionVista;
import com.gestion.residuos.Eco_Andino.dto.GeneracionDtos.RecoleccionMini;
import com.gestion.residuos.Eco_Andino.dto.AlmacenDtos.MovimientoDto;
import com.gestion.residuos.Eco_Andino.dto.IncidenciaDtos.IncidenciaDto;
import com.gestion.residuos.Eco_Andino.dto.IncidenciaDtos.ObservacionDto;
import com.gestion.residuos.Eco_Andino.dto.IncidenciaDtos.SeguimientoDto;
import com.gestion.residuos.Eco_Andino.dto.RecoleccionDtos.Aprobacion;
import com.gestion.residuos.Eco_Andino.dto.RecoleccionDtos.Cierre;
import com.gestion.residuos.Eco_Andino.dto.RecoleccionDtos.Destino;
import com.gestion.residuos.Eco_Andino.dto.RecoleccionDtos.RecoleccionVista;
import com.gestion.residuos.Eco_Andino.dto.RecoleccionDtos.Recepcion;
import com.gestion.residuos.Eco_Andino.dto.RecoleccionDtos.Requisito;
import com.gestion.residuos.Eco_Andino.dto.RecoleccionDtos.Salida;
import com.gestion.residuos.Eco_Andino.entity.*;
import com.gestion.residuos.Eco_Andino.repository.DocumentoRepository;
import com.gestion.residuos.Eco_Andino.repository.GeneracionRepository;
import com.gestion.residuos.Eco_Andino.repository.MovimientoRepository;
import com.gestion.residuos.Eco_Andino.repository.RecoleccionRepository;
import com.gestion.residuos.Eco_Andino.util.Numeros;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Arma las respuestas (DTO) que consume el frontend y calcula lo que no se guarda: ocupación de cada
 * contenedor, plazos de almacenamiento, vigencia de autorizaciones y requisitos de cierre de una orden.
 * Nunca devuelve entidades JPA: así no se exponen datos internos (por ejemplo, el hash de la contraseña).
 */
@Service
@RequiredArgsConstructor
public class VistaService {
    public static final List<String> ORDENES_ACTIVAS = List.of(
            Recoleccion.PENDIENTE, Recoleccion.PROGRAMADA, Recoleccion.EN_TRANSITO, Recoleccion.RECIBIDA);

    private final GeneracionRepository generacionRepository;
    private final MovimientoRepository movimientoRepository;
    private final RecoleccionRepository recoleccionRepository;
    private final DocumentoRepository documentoRepository;
    private final SesionService sesion;

    /**
     * Foto de los datos operativos para hacer los cálculos de una petición sin consultar la BD por cada
     * contenedor. El volumen de una planta es pequeño, así que cabe en memoria.
     */
    public final class Instantanea {
        private final List<Generacion> generaciones;
        private final List<Movimiento> movimientos;
        private final List<Recoleccion> recolecciones;
        private final List<Documento> documentos;
        private final Map<Integer, Recoleccion> ordenPorGeneracion = new HashMap<>();

        private Instantanea() {
            this.generaciones = generacionRepository.findAll();
            this.movimientos = movimientoRepository.findAll();
            this.recolecciones = recoleccionRepository.findAll();
            this.documentos = documentoRepository.findAll();
            for (Recoleccion orden : recolecciones) {
                for (DetalleRecoleccion detalle : orden.getDetalles()) {
                    ordenPorGeneracion.put(detalle.getGeneracion().getIdGeneracion(), orden);
                }
            }
        }

        public List<Generacion> generaciones() {
            return generaciones;
        }

        public List<Movimiento> movimientos() {
            return movimientos;
        }

        public List<Recoleccion> recolecciones() {
            return recolecciones;
        }

        public List<Documento> documentos() {
            return documentos;
        }

        public Optional<Recoleccion> ordenDe(Generacion generacion) {
            return Optional.ofNullable(ordenPorGeneracion.get(generacion.getIdGeneracion()));
        }

        /** El residuo ya salió de la planta: su orden está en tránsito, recibida o cerrada. */
        public boolean retirada(Generacion generacion) {
            return ordenDe(generacion).map(o -> Recoleccion.RETIRADA.contains(o.getEstado())).orElse(false);
        }

        public List<Documento> documentosDe(String referencia) {
            return documentos.stream()
                    .filter(d -> Objects.equals(d.getReferencia(), referencia))
                    .sorted(Comparator.comparing(Documento::getFecha).reversed())
                    .toList();
        }
    }

    public Instantanea instantanea() {
        return new Instantanea();
    }

    // ---------------------------------------------------------------- catálogos

    public ResiduoDto residuo(Residuo r) {
        if (r == null) {
            return null;
        }
        return new ResiduoDto(r.getIdResiduo(), r.getCodigo(), r.getNombre(), r.getCategoria(), r.getPeligrosidad(),
                r.getUnidad(), r.getPlazoMaximoDias(), r.getTratamiento(), r.getRequisitos());
    }

    public AreaDto area(AreaGeneradora a, Long registros30) {
        if (a == null) {
            return null;
        }
        return new AreaDto(a.getIdArea(), a.getCodigo(), a.getNombre(), a.getProceso(), nombre(a.getResponsable()),
                a.getEstado(), registros30);
    }

    public AreaDto area(AreaGeneradora a) {
        return area(a, null);
    }

    public ContenedorBasico contenedorBasico(Contenedor c) {
        return new ContenedorBasico(c.getIdContenedor(), c.getCodigo(), c.getTipo(), c.getCapacidad(),
                c.getResiduoPermitido().getIdResiduo(), c.getUbicacion(), c.getEstado(), c.getStockInicial(),
                c.getFechaStock());
    }

    /** Ocupación actual del contenedor: carga inicial + generaciones aún en planta + traslados y pesajes. */
    public double ocupacion(Contenedor c, Instantanea foto) {
        double total = Numeros.valor(c.getStockInicial());
        for (Generacion g : foto.generaciones()) {
            if (g.getContenedor().getIdContenedor().equals(c.getIdContenedor()) && !foto.retirada(g)) {
                total += Numeros.valor(g.getCantidad());
            }
        }
        for (Movimiento m : foto.movimientos()) {
            if (!m.isAfectaStock()) {
                continue;
            }
            if (Movimiento.PESAJE.equals(m.getTipo())) {
                if (c.getCodigo().equals(m.getDestino())) {
                    total += Numeros.valor(m.getAjuste());
                }
            } else {
                if (c.getCodigo().equals(m.getDestino())) {
                    total += Numeros.valor(m.getCantidad());
                }
                if (c.getCodigo().equals(m.getOrigen())) {
                    total -= Numeros.valor(m.getCantidad());
                }
            }
        }
        return Math.max(0, Math.round(total * 100) / 100.0);
    }

    public ContenedorVista contenedor(Contenedor c, Instantanea foto) {
        Residuo residuo = c.getResiduoPermitido();
        double capacidad = Numeros.valor(c.getCapacidad());
        double ocupacion = ocupacion(c, foto);

        List<LocalDateTime> fechas = new ArrayList<>();
        if (Numeros.valor(c.getStockInicial()) > 0 && c.getFechaStock() != null) {
            fechas.add(c.getFechaStock());
        }
        foto.generaciones().stream()
                .filter(g -> g.getContenedor().getIdContenedor().equals(c.getIdContenedor()) && !foto.retirada(g))
                .map(Generacion::getFecha)
                .forEach(fechas::add);
        LocalDateTime fechaIngreso = ocupacion > 0 ? fechas.stream().min(Comparator.naturalOrder()).orElse(null) : null;
        long diasAlmacenado = fechaIngreso == null ? 0 : diasDesde(fechaIngreso);
        Long plazoRestante = fechaIngreso == null ? null : residuo.getPlazoMaximoDias() - diasAlmacenado;
        int porcentaje = capacidad > 0 ? (int) Math.round(ocupacion / capacidad * 100) : 0;
        boolean mantenimiento = Contenedor.MANTENIMIENTO.equals(c.getEstado());

        String situacion;
        if (mantenimiento) {
            situacion = "MANTENIMIENTO";
        } else if (ocupacion >= capacidad) {
            situacion = "LLENO";
        } else if (ocupacion > 0) {
            situacion = "EN_USO";
        } else {
            situacion = "DISPONIBLE";
        }

        return new ContenedorVista(c.getIdContenedor(), c.getCodigo(), c.getTipo(), c.getCapacidad(),
                residuo.getIdResiduo(), c.getUbicacion(), c.getEstado(), c.getStockInicial(), c.getFechaStock(),
                residuo(residuo), residuo.getUnidad(), ocupacion, Math.max(0, Math.round((capacidad - ocupacion) * 100) / 100.0),
                porcentaje, fechaIngreso, diasAlmacenado, plazoRestante, situacion,
                !mantenimiento && porcentaje >= 85,
                !mantenimiento && plazoRestante != null && plazoRestante <= 5);
    }

    public VehiculoDto vehiculo(Vehiculo v) {
        if (v == null) {
            return null;
        }
        return new VehiculoDto(v.getIdVehiculo(), v.getPlaca(), v.getTipo(), v.getCapacidad(), v.getEstado());
    }

    public TransportistaDto transportista(Transportista t, Instantanea foto) {
        if (t == null) {
            return null;
        }
        List<Recoleccion> ordenes = foto.recolecciones().stream()
                .filter(o -> o.getTransportista().getIdTransportista().equals(t.getIdTransportista()))
                .toList();
        return new TransportistaDto(t.getIdTransportista(), t.getEmpresa(), t.getAutorizacion(), t.getContacto(),
                t.getVigencia(), t.getVehiculos().stream().map(this::vehiculo).toList(), estadoVigencia(t.getVigencia()),
                contar(ordenes, ORDENES_ACTIVAS), contar(ordenes, List.of(Recoleccion.CERRADA)));
    }

    public GestorDto gestor(Gestor g, Instantanea foto) {
        if (g == null) {
            return null;
        }
        List<Recoleccion> ordenes = foto.recolecciones().stream()
                .filter(o -> o.getGestor().getIdGestor().equals(g.getIdGestor()))
                .toList();
        return new GestorDto(g.getIdGestor(), g.getEmpresa(), g.getAutorizacion(), g.getProcesoDestino(),
                g.getVigencia(), estadoVigencia(g.getVigencia()), contar(ordenes, ORDENES_ACTIVAS),
                contar(ordenes, List.of(Recoleccion.CERRADA)));
    }

    /** VIGENTE, POR_VENCER (30 días o menos) o VENCIDA. */
    public String estadoVigencia(LocalDate vigencia) {
        long dias = ChronoUnit.DAYS.between(sesion.hoy(), vigencia);
        if (dias < 0) {
            return "VENCIDA";
        }
        return dias <= 30 ? "POR_VENCER" : "VIGENTE";
    }

    // ---------------------------------------------------------------- operación

    public GeneracionVista generacion(Generacion g, Instantanea foto) {
        Residuo residuo = g.getResiduo();
        RecoleccionMini orden = foto.ordenDe(g)
                .map(o -> new RecoleccionMini(o.getIdRecoleccion(), o.getNumeroOrden(), o.getEstado()))
                .orElse(null);
        return new GeneracionVista(g.getIdGeneracion(), g.getCodigo(), g.getArea().getIdArea(),
                residuo.getIdResiduo(), g.getContenedor().getIdContenedor(), g.getCantidad(), g.getFecha(),
                nombre(g.getResponsable()), g.getCondicionEnvase(), g.isSegregacionCorrecta(),
                g.getEstadoValidacion(), texto(g.getObservaciones()), g.getEvidencia(), nombre(g.getValidadoPor()),
                g.getFechaValidacion(), texto(g.getObservacionValidacion()), area(g.getArea()), residuo(residuo),
                residuo.getUnidad(), contenedorBasico(g.getContenedor()), orden);
    }

    public MovimientoDto movimiento(Movimiento m) {
        return new MovimientoDto(m.getIdMovimiento(), m.getCodigo(), m.getTipo(), m.getFecha(),
                m.getResiduo().getIdResiduo(), m.getOrigen(), m.getDestino(), m.getCantidad(), m.getAjuste(),
                m.getReferencia(), nombre(m.getUsuario()), m.getMotivo(), m.isAfectaStock(), residuo(m.getResiduo()));
    }

    public RecoleccionVista recoleccion(Recoleccion o, Instantanea foto) {
        List<GeneracionVista> detalles = o.getDetalles().stream()
                .map(d -> generacion(d.getGeneracion(), foto))
                .toList();
        List<String> residuos = detalles.stream().map(d -> d.residuo().nombre()).distinct().toList();
        List<String> areas = detalles.stream().map(d -> d.area().nombre()).distinct().toList();
        double cantidadTotal = detalles.stream().mapToDouble(d -> Numeros.valor(d.cantidad())).sum();
        String unidad = detalles.isEmpty() ? "" : detalles.get(0).unidad();
        boolean peligroso = detalles.stream().anyMatch(d -> "PELIGROSO".equals(d.residuo().categoria()));

        Aprobacion aprobacion = o.getFechaAprobacion() == null ? null
                : new Aprobacion(o.getFechaAprobacion(), nombre(o.getAprobadoPor()));
        Salida salida = o.getFechaSalida() == null ? null
                : new Salida(o.getFechaSalida(), o.getDocumentoSalida(), nombre(o.getSalidaPor()),
                texto(o.getObservacionesSalida()));
        Recepcion recepcion = o.getFechaRecepcion() == null ? null
                : new Recepcion(o.getFechaRecepcion(), o.getCantidadRecibida(), o.getRecepcionConforme(),
                nombre(o.getRecibidoPor()), texto(o.getObservacionesRecepcion()));
        Destino destino = o.getTipoDestino() == null ? null : new Destino(o.getTipoDestino(), o.getTratamientoDestino());
        Cierre cierre = o.getFechaCierre() == null ? null : new Cierre(o.getFechaCierre(), nombre(o.getCerradoPor()));

        List<Documento> documentos = foto.documentosDe(o.getNumeroOrden());
        return new RecoleccionVista(o.getIdRecoleccion(), o.getNumeroOrden(), o.getEstado(), o.getFechaSolicitud(),
                nombre(o.getSolicitadoPor()), o.getFechaProgramada(), o.getTransportista().getIdTransportista(),
                o.getVehiculo().getIdVehiculo(), o.getGestor().getIdGestor(), texto(o.getObservaciones()),
                aprobacion, salida, recepcion, destino, cierre, detalles, residuos, areas,
                Math.round(cantidadTotal * 100) / 100.0, unidad, peligroso, transportista(o.getTransportista(), foto),
                vehiculo(o.getVehiculo()), gestor(o.getGestor(), foto),
                documentos.stream().map(this::documento).toList(), requisitosCierre(o, documentos));
    }

    /**
     * Regla de integridad (RNF003): una orden no se cierra sin transportista, cantidad recibida, destino y
     * documentos de traslado y de tratamiento.
     */
    public List<Requisito> requisitosCierre(Recoleccion o, List<Documento> documentosOrden) {
        List<Documento> vigentes = documentosOrden.stream()
                .filter(d -> !Documento.RECHAZADO.equals(d.getEstado()))
                .toList();
        boolean traslado = vigentes.stream().anyMatch(d -> List.of("MANIFIESTO", "GUIA_REMISION").contains(d.getTipo()));
        boolean certificado = vigentes.stream().anyMatch(d -> "CERTIFICADO".equals(d.getTipo()));
        return List.of(
                new Requisito("transportista", "Transportista y vehículo asignados",
                        o.getTransportista() != null && o.getVehiculo() != null),
                new Requisito("cantidad", "Cantidad recibida registrada",
                        o.getCantidadRecibida() != null && o.getCantidadRecibida().compareTo(BigDecimal.ZERO) > 0),
                new Requisito("destino", "Destino y tratamiento registrados", o.getTipoDestino() != null),
                new Requisito("traslado", "Documento de traslado (manifiesto o guía)", traslado),
                new Requisito("certificado", "Certificado de tratamiento o disposición", certificado));
    }

    public DocumentoDto documento(Documento d) {
        return new DocumentoDto(d.getIdDocumento(), d.getTipo(), d.getNombreArchivo(), d.getReferencia(),
                d.getDescripcion(), d.getFecha(), nombre(d.getSubidoPor()), d.getEstado(), texto(d.getMotivoRechazo()),
                d.getRutaArchivo() != null);
    }

    public IncidenciaDto incidencia(Incidencia i, Instantanea foto) {
        List<SeguimientoDto> seguimiento = i.getSeguimiento().stream()
                .map(s -> new SeguimientoDto(s.getFecha(), nombre(s.getUsuario()), s.getNota()))
                .toList();
        return new IncidenciaDto(i.getIdIncidencia(), i.getCodigo(), i.getTipo(), i.getDescripcion(),
                i.getArea().getIdArea(), texto(i.getReferencia()), i.getSeveridad(), i.getEstado(), i.getFecha(),
                nombre(i.getReportadoPor()), nombre(i.getResponsable()), texto(i.getAccionCorrectiva()), seguimiento,
                i.getFechaCierre(), area(i.getArea()),
                foto.documentosDe(i.getCodigo()).stream().map(this::documento).toList());
    }

    public ObservacionDto observacion(Observacion o) {
        return new ObservacionDto(o.getIdObservacion(), o.getFecha(), nombre(o.getAutor()), o.getRol(), o.getAmbito(),
                texto(o.getReferencia()), o.getTexto());
    }

    // ---------------------------------------------------------------- utilidades

    public long diasDesde(LocalDateTime fecha) {
        return ChronoUnit.DAYS.between(fecha.toLocalDate(), sesion.hoy());
    }

    public static String nombre(Usuario usuario) {
        return usuario == null ? null : usuario.getNombreCompleto();
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private static long contar(List<Recoleccion> ordenes, Collection<String> estados) {
        return ordenes.stream().filter(o -> estados.contains(o.getEstado())).count();
    }

    public static <T> Map<Integer, List<T>> agrupar(Collection<T> items, java.util.function.Function<T, Integer> clave) {
        return items.stream().collect(Collectors.groupingBy(clave));
    }
}
