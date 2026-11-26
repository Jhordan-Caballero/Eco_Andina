package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.*;
import com.gestion.residuos.Eco_Andino.entity.Gestor;
import com.gestion.residuos.Eco_Andino.entity.Recoleccion;
import com.gestion.residuos.Eco_Andino.entity.Transportista;
import com.gestion.residuos.Eco_Andino.entity.Vehiculo;
import com.gestion.residuos.Eco_Andino.exception.ConflictoException;
import com.gestion.residuos.Eco_Andino.exception.RecursoNoEncontradoException;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import com.gestion.residuos.Eco_Andino.repository.GestorRepository;
import com.gestion.residuos.Eco_Andino.repository.TransportistaRepository;
import com.gestion.residuos.Eco_Andino.repository.VehiculoRepository;
import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/** Transportistas y sus vehículos (CU06) y gestores autorizados (CU07). */
@Service
@RequiredArgsConstructor
public class TransportistaService {
    private static final Set<String> ESTADOS_VEHICULO = Set.of("OPERATIVO", "EN_MANTENIMIENTO", "INACTIVO");

    private final TransportistaRepository transportistaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final GestorRepository gestorRepository;
    private final VistaService vistas;
    private final AuditoriaService auditoria;

    @Transactional(readOnly = true)
    public List<TransportistaDto> listarTransportistas() {
        VistaService.Instantanea foto = vistas.instantanea();
        return transportistaRepository.findAll(Sort.by("empresa")).stream()
                .map(t -> vistas.transportista(t, foto))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GestorDto> listarGestores() {
        VistaService.Instantanea foto = vistas.instantanea();
        return gestorRepository.findAll(Sort.by("empresa")).stream()
                .map(g -> vistas.gestor(g, foto))
                .toList();
    }

    @Transactional
    public TransportistaDto guardarTransportista(Integer id, TransportistaRequest datos) {
        String autorizacion = TextoUtil.normalizarCodigo(datos.autorizacion());
        transportistaRepository.findByAutorizacionIgnoreCase(autorizacion)
                .filter(otro -> !otro.getIdTransportista().equals(id))
                .ifPresent(otro -> {
                    throw new ConflictoException("Ya existe un transportista con la autorización " + autorizacion + ".");
                });

        Transportista transportista = id == null ? new Transportista() : buscarTransportista(id);
        transportista.setEmpresa(datos.empresa().trim());
        transportista.setAutorizacion(autorizacion);
        transportista.setContacto(TextoUtil.normalizar(datos.contacto()));
        transportista.setVigencia(datos.vigencia());
        transportistaRepository.save(transportista);

        auditoria.registrar(id == null ? "CREAR" : "MODIFICAR", "Transportista", transportista.getEmpresa(),
                id == null ? "Registró la EO-RS " + autorizacion : "Actualizó los datos y la vigencia de la autorización");
        return vistas.transportista(transportista, vistas.instantanea());
    }

    @Transactional
    public VehiculoDto guardarVehiculo(Integer idTransportista, Integer idVehiculo, VehiculoRequest datos) {
        Transportista transportista = buscarTransportista(idTransportista);
        String placa = TextoUtil.normalizarCodigo(datos.placa());
        CatalogoService.validarValor(ESTADOS_VEHICULO, datos.estado(), "El estado del vehículo no es válido");
        vehiculoRepository.findByPlacaIgnoreCase(placa)
                .filter(otro -> !otro.getIdVehiculo().equals(idVehiculo))
                .ifPresent(otro -> {
                    throw new ConflictoException("La placa " + placa + " ya está registrada.");
                });

        Vehiculo vehiculo;
        if (idVehiculo == null) {
            vehiculo = new Vehiculo();
            vehiculo.setTransportista(transportista);
            transportista.getVehiculos().add(vehiculo);
        } else {
            vehiculo = vehiculoRepository.findById(idVehiculo)
                    .filter(v -> v.getTransportista().getIdTransportista().equals(idTransportista))
                    .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado"));
            boolean enUso = vistas.instantanea().recolecciones().stream()
                    .anyMatch(o -> o.getVehiculo().getIdVehiculo().equals(idVehiculo)
                            && List.of(Recoleccion.PROGRAMADA, Recoleccion.EN_TRANSITO).contains(o.getEstado()));
            if (enUso && !Vehiculo.OPERATIVO.equals(datos.estado())) {
                throw new SolicitudInvalidaException(placa + " tiene una recolección programada o en tránsito.");
            }
        }
        vehiculo.setPlaca(placa);
        vehiculo.setTipo(TextoUtil.normalizar(datos.tipo()));
        vehiculo.setCapacidad(datos.capacidad());
        vehiculo.setEstado(datos.estado());
        vehiculoRepository.save(vehiculo);

        auditoria.registrar(idVehiculo == null ? "CREAR" : "MODIFICAR", "Transportista", transportista.getEmpresa(),
                (idVehiculo == null ? "Registró" : "Actualizó") + " el vehículo " + placa);
        return vistas.vehiculo(vehiculo);
    }

    @Transactional
    public GestorDto guardarGestor(Integer id, GestorRequest datos) {
        String autorizacion = TextoUtil.normalizarCodigo(datos.autorizacion());
        gestorRepository.findByAutorizacionIgnoreCase(autorizacion)
                .filter(otro -> !otro.getIdGestor().equals(id))
                .ifPresent(otro -> {
                    throw new ConflictoException("Ya existe un gestor con la autorización " + autorizacion + ".");
                });

        Gestor gestor = id == null ? new Gestor() : buscarGestor(id);
        gestor.setEmpresa(datos.empresa().trim());
        gestor.setAutorizacion(autorizacion);
        gestor.setProcesoDestino(TextoUtil.normalizar(datos.procesoDestino()));
        gestor.setVigencia(datos.vigencia());
        gestorRepository.save(gestor);

        auditoria.registrar(id == null ? "CREAR" : "MODIFICAR", "Gestor", gestor.getEmpresa(),
                id == null ? "Registró la EO-RS " + autorizacion : "Actualizó los datos y la vigencia de la autorización");
        return vistas.gestor(gestor, vistas.instantanea());
    }

    public Transportista buscarTransportista(Integer id) {
        return transportistaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Transportista no encontrado"));
    }

    public Gestor buscarGestor(Integer id) {
        return gestorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Gestor no encontrado"));
    }
}
