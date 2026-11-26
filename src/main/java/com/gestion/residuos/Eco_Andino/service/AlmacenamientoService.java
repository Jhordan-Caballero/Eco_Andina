package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.AlmacenDtos.ContenedorDetalle;
import com.gestion.residuos.Eco_Andino.dto.AlmacenDtos.MovimientoDto;
import com.gestion.residuos.Eco_Andino.dto.AlmacenDtos.MovimientoRequest;
import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.ContenedorVista;
import com.gestion.residuos.Eco_Andino.entity.Contenedor;
import com.gestion.residuos.Eco_Andino.entity.Generacion;
import com.gestion.residuos.Eco_Andino.entity.Movimiento;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import com.gestion.residuos.Eco_Andino.repository.ContenedorRepository;
import com.gestion.residuos.Eco_Andino.repository.MovimientoRepository;
import com.gestion.residuos.Eco_Andino.repository.ObservacionRepository;
import com.gestion.residuos.Eco_Andino.util.Codigos;
import com.gestion.residuos.Eco_Andino.util.Numeros;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Almacenamiento temporal: consulta de existencias (CU11), traslados internos y pesajes (CU10). */
@Service
@RequiredArgsConstructor
public class AlmacenamientoService {
    private final ContenedorRepository contenedorRepository;
    private final MovimientoRepository movimientoRepository;
    private final ObservacionRepository observacionRepository;
    private final CatalogoService catalogos;
    private final VistaService vistas;
    private final SesionService sesion;
    private final AuditoriaService auditoria;

    /** Contenedores ordenados por urgencia: primero los que vencen su plazo, luego los más llenos. */
    @Transactional(readOnly = true)
    public List<ContenedorVista> listar() {
        VistaService.Instantanea foto = vistas.instantanea();
        return contenedorRepository.findAll().stream()
                .map(c -> vistas.contenedor(c, foto))
                .sorted(Comparator.comparingInt(AlmacenamientoService::prioridad).reversed()
                        .thenComparing(Comparator.comparingInt(ContenedorVista::porcentaje).reversed()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoDto> movimientos() {
        return movimientoRepository.findAll().stream()
                .sorted(Comparator.comparing(Movimiento::getFecha).reversed())
                .map(vistas::movimiento)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContenedorDetalle detalle(Integer idContenedor) {
        Contenedor contenedor = catalogos.buscarContenedor(idContenedor);
        VistaService.Instantanea foto = vistas.instantanea();
        String codigo = contenedor.getCodigo();
        return new ContenedorDetalle(
                vistas.contenedor(contenedor, foto),
                foto.generaciones().stream()
                        .filter(g -> g.getContenedor().getIdContenedor().equals(idContenedor) && !foto.retirada(g))
                        .sorted(Comparator.comparing(Generacion::getFecha))
                        .map(g -> vistas.generacion(g, foto))
                        .toList(),
                foto.movimientos().stream()
                        .filter(m -> codigo.equals(m.getOrigen()) || codigo.equals(m.getDestino()))
                        .sorted(Comparator.comparing(Movimiento::getFecha).reversed())
                        .limit(6)
                        .map(vistas::movimiento)
                        .toList(),
                observacionRepository.findByReferenciaOrderByFechaDesc(codigo).stream()
                        .map(vistas::observacion)
                        .toList());
    }

    /** CU10: traslado entre contenedores o pesaje de verificación. */
    @Transactional
    public MovimientoDto registrar(MovimientoRequest datos) {
        return switch (datos.tipo()) {
            case Movimiento.TRASLADO -> trasladar(datos);
            case Movimiento.PESAJE -> pesar(datos);
            default -> throw new SolicitudInvalidaException("El tipo de movimiento debe ser TRASLADO o PESAJE.");
        };
    }

    private MovimientoDto trasladar(MovimientoRequest datos) {
        if (datos.idDestino() == null) {
            throw new SolicitudInvalidaException("Selecciona el contenedor de destino.");
        }
        VistaService.Instantanea foto = vistas.instantanea();
        Contenedor origen = catalogos.buscarContenedor(datos.idOrigen());
        Contenedor destino = catalogos.buscarContenedor(datos.idDestino());
        ContenedorVista vOrigen = vistas.contenedor(origen, foto);
        ContenedorVista vDestino = vistas.contenedor(destino, foto);
        double cantidad = Numeros.valor(datos.cantidad());

        if (origen.getIdContenedor().equals(destino.getIdContenedor())) {
            throw new SolicitudInvalidaException("El contenedor de destino debe ser distinto al de origen.");
        }
        if (cantidad <= 0) {
            throw new SolicitudInvalidaException("La cantidad a trasladar debe ser mayor a 0.");
        }
        if (!origen.getResiduoPermitido().getIdResiduo().equals(destino.getResiduoPermitido().getIdResiduo())) {
            throw new SolicitudInvalidaException(destino.getCodigo() + " no admite "
                    + origen.getResiduoPermitido().getNombre().toLowerCase(Locale.ROOT) + ".");
        }
        if (Contenedor.MANTENIMIENTO.equals(destino.getEstado())) {
            throw new SolicitudInvalidaException(destino.getCodigo() + " está en mantenimiento.");
        }
        if (cantidad > vOrigen.ocupacion()) {
            throw new SolicitudInvalidaException(origen.getCodigo() + " solo contiene "
                    + Numeros.cantidad(vOrigen.ocupacion(), vOrigen.unidad()) + ".");
        }
        if (cantidad > vDestino.disponible()) {
            throw new SolicitudInvalidaException(destino.getCodigo() + " solo tiene "
                    + Numeros.cantidad(vDestino.disponible(), vDestino.unidad()) + " disponibles.");
        }

        Movimiento movimiento = nuevo(Movimiento.TRASLADO, origen, datos);
        movimiento.setDestino(destino.getCodigo());
        movimiento.setCantidad(datos.cantidad());
        guardar(movimiento);
        auditoria.registrar("REGISTRAR", "Movimiento", movimiento.getCodigo(), "Trasladó "
                + Numeros.cantidad(cantidad, vOrigen.unidad()) + " de " + origen.getCodigo() + " a " + destino.getCodigo());
        return vistas.movimiento(movimiento);
    }

    private MovimientoDto pesar(MovimientoRequest datos) {
        Contenedor contenedor = catalogos.buscarContenedor(datos.idOrigen());
        ContenedorVista vista = vistas.contenedor(contenedor, vistas.instantanea());
        double peso = Numeros.valor(datos.cantidad());
        if (peso > Numeros.valor(contenedor.getCapacidad())) {
            throw new SolicitudInvalidaException("El peso verificado supera la capacidad de " + contenedor.getCodigo() + ".");
        }

        BigDecimal ajuste = Numeros.redondear(peso - vista.ocupacion());
        Movimiento movimiento = nuevo(Movimiento.PESAJE, contenedor, datos);
        movimiento.setDestino(contenedor.getCodigo());
        movimiento.setCantidad(datos.cantidad());
        movimiento.setAjuste(ajuste);
        guardar(movimiento);
        auditoria.registrar("REGISTRAR", "Movimiento", movimiento.getCodigo(), "Pesaje de " + contenedor.getCodigo()
                + ": " + Numeros.cantidad(peso, vista.unidad()) + " (ajuste " + (ajuste.signum() >= 0 ? "+" : "")
                + Numeros.cantidad(ajuste.doubleValue(), vista.unidad()) + ")");
        return vistas.movimiento(movimiento);
    }

    private Movimiento nuevo(String tipo, Contenedor origen, MovimientoRequest datos) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipo(tipo);
        movimiento.setFecha(sesion.ahora());
        movimiento.setResiduo(origen.getResiduoPermitido());
        movimiento.setOrigen(origen.getCodigo());
        movimiento.setReferencia(origen.getCodigo());
        movimiento.setUsuario(sesion.usuario());
        movimiento.setMotivo(datos.motivo().trim());
        movimiento.setAfectaStock(true);
        return movimiento;
    }

    private void guardar(Movimiento movimiento) {
        movimientoRepository.save(movimiento);
        movimiento.setCodigo(Codigos.movimiento(movimiento.getIdMovimiento()));
    }

    private static int prioridad(ContenedorVista c) {
        return (c.alertaPlazo() ? 2 : 0) + (c.alertaCapacidad() ? 1 : 0);
    }
}
