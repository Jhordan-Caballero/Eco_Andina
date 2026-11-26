package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.ContenedorVista;
import com.gestion.residuos.Eco_Andino.dto.DashboardDtos.*;
import com.gestion.residuos.Eco_Andino.dto.RecoleccionDtos.RecoleccionVista;
import com.gestion.residuos.Eco_Andino.entity.Documento;
import com.gestion.residuos.Eco_Andino.entity.Generacion;
import com.gestion.residuos.Eco_Andino.entity.Incidencia;
import com.gestion.residuos.Eco_Andino.entity.Recoleccion;
import com.gestion.residuos.Eco_Andino.repository.ContenedorRepository;
import com.gestion.residuos.Eco_Andino.repository.IncidenciaRepository;
import com.gestion.residuos.Eco_Andino.util.Numeros;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Panel general: indicadores, órdenes recientes, alertas del almacén y pendientes según el rol. */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final List<String> ACTIVAS = List.of(Recoleccion.PENDIENTE, Recoleccion.PROGRAMADA, Recoleccion.EN_TRANSITO);

    private final ContenedorRepository contenedorRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final VistaService vistas;
    private final SesionService sesion;

    @Transactional(readOnly = true)
    public Resumen resumen() {
        VistaService.Instantanea foto = vistas.instantanea();
        List<RecoleccionVista> ordenes = foto.recolecciones().stream()
                .sorted(Comparator.comparing(Recoleccion::getIdRecoleccion).reversed())
                .map(o -> vistas.recoleccion(o, foto))
                .toList();
        long recibidas = ordenes.stream().filter(o -> Recoleccion.RECIBIDA.equals(o.estado())).count();
        long cerradas = ordenes.stream().filter(o -> Recoleccion.CERRADA.equals(o.estado())).count();
        List<Incidencia> abiertas = incidenciaRepository.findAll().stream()
                .filter(i -> !Incidencia.CERRADA.equals(i.getEstado()))
                .toList();
        List<Alerta> alertas = alertas(foto);
        Integer idUsuario = sesion.usuario().getIdUsuario();

        Kpis kpis = new Kpis(
                new Kpi(Numeros.porcentaje(cerradas, cerradas + recibidas),
                        "Órdenes recibidas que cerraron con su documentación completa"),
                new Kpi(ordenes.stream().filter(o -> ACTIVAS.contains(o.estado())).count(),
                        "Pendientes, programadas o en tránsito"),
                new Kpi(abiertas.size(), "Abiertas o con acción correctiva en curso"),
                new Kpi(alertas.stream().filter(a -> "capacidad".equals(a.tipo())).count(),
                        "Contenedores al 85% o más de su capacidad"));

        List<OrdenReciente> recientes = ordenes.stream().limit(5)
                .map(o -> new OrdenReciente(o.numeroOrden(), String.join(", ", o.areas()), String.join(", ", o.residuos()),
                        Numeros.cantidad(o.cantidadTotal(), o.unidad()),
                        o.transportista() == null ? null : o.transportista().empresa(), o.estado()))
                .toList();

        Pendientes pendientes = new Pendientes(
                foto.generaciones().stream().filter(g -> Generacion.PENDIENTE.equals(g.getEstadoValidacion())).count(),
                foto.generaciones().stream().filter(g -> Generacion.OBSERVADA.equals(g.getEstadoValidacion())
                        && g.getResponsable().getIdUsuario().equals(idUsuario)).count(),
                contar(ordenes, Recoleccion.PENDIENTE),
                contar(ordenes, Recoleccion.PROGRAMADA),
                contar(ordenes, Recoleccion.EN_TRANSITO),
                recibidas,
                foto.documentos().stream().filter(d -> Documento.PENDIENTE.equals(d.getEstado())).count(),
                abiertas.stream().filter(i -> i.getResponsable() == null).count());

        return new Resumen(kpis, recientes, alertas, pendientes);
    }

    private List<Alerta> alertas(VistaService.Instantanea foto) {
        List<ContenedorVista> contenedores = contenedorRepository.findAll().stream()
                .map(c -> vistas.contenedor(c, foto))
                .toList();
        List<Alerta> alertas = new ArrayList<>();
        contenedores.stream()
                .filter(ContenedorVista::alertaPlazo)
                .sorted(Comparator.comparing(ContenedorVista::plazoRestante))
                .forEach(c -> alertas.add(new Alerta("plazo-" + c.codigo(), "plazo",
                        c.residuo().nombre() + " · " + c.codigo(),
                        c.plazoRestante() < 0
                                ? "Superó el plazo máximo hace " + Math.abs(c.plazoRestante()) + " días"
                                : "El plazo máximo de almacenamiento vence en " + c.plazoRestante() + " días",
                        null)));
        contenedores.stream()
                .filter(ContenedorVista::alertaCapacidad)
                .sorted(Comparator.comparingInt(ContenedorVista::porcentaje).reversed())
                .forEach(c -> alertas.add(new Alerta("capacidad-" + c.codigo(), "capacidad",
                        c.codigo() + " · " + c.ubicacion(),
                        c.residuo().nombre() + " · " + Numeros.cantidad(c.ocupacion(), c.unidad()) + " de "
                                + Numeros.cantidad(Numeros.valor(c.capacidad()), c.unidad()),
                        Math.min(100, c.porcentaje()))));
        return alertas;
    }

    private static long contar(List<RecoleccionVista> ordenes, String estado) {
        return ordenes.stream().filter(o -> estado.equals(o.estado())).count();
    }
}
