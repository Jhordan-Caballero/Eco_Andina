package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.CatalogoDtos.*;
import com.gestion.residuos.Eco_Andino.entity.AreaGeneradora;
import com.gestion.residuos.Eco_Andino.entity.Contenedor;
import com.gestion.residuos.Eco_Andino.entity.Residuo;
import com.gestion.residuos.Eco_Andino.entity.Usuario;
import com.gestion.residuos.Eco_Andino.exception.ConflictoException;
import com.gestion.residuos.Eco_Andino.exception.RecursoNoEncontradoException;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import com.gestion.residuos.Eco_Andino.repository.AreaGeneradoraRepository;
import com.gestion.residuos.Eco_Andino.repository.ContenedorRepository;
import com.gestion.residuos.Eco_Andino.repository.ResiduoRepository;
import com.gestion.residuos.Eco_Andino.repository.UsuarioRepository;
import com.gestion.residuos.Eco_Andino.util.Numeros;
import com.gestion.residuos.Eco_Andino.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Catálogos maestros: residuos (CU03), áreas generadoras (CU04) y contenedores (CU05). */
@Service
@RequiredArgsConstructor
public class CatalogoService {
    private static final Set<String> CATEGORIAS = Set.of("COMUN", "APROVECHABLE", "PELIGROSO", "ESPECIAL");
    private static final Set<String> PELIGROSIDADES = Set.of("BAJA", "MEDIA", "ALTA");
    private static final Set<String> UNIDADES = Set.of("kg", "L", "unid");
    private static final Set<String> ESTADOS_AREA = Set.of(AreaGeneradora.ACTIVA, AreaGeneradora.INACTIVA);
    private static final Set<String> ESTADOS_CONTENEDOR = Set.of(Contenedor.DISPONIBLE, Contenedor.MANTENIMIENTO);

    private final ResiduoRepository residuoRepository;
    private final AreaGeneradoraRepository areaRepository;
    private final ContenedorRepository contenedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final VistaService vistas;
    private final AuditoriaService auditoria;

    // ---------------------------------------------------------------- residuos (CU03)

    @Transactional(readOnly = true)
    public List<ResiduoDto> listarResiduos() {
        return residuoRepository.findAll(Sort.by("codigo")).stream().map(vistas::residuo).toList();
    }

    @Transactional
    public ResiduoDto guardarResiduo(Integer id, ResiduoRequest datos) {
        String codigo = TextoUtil.normalizarCodigo(datos.codigo());
        validarValor(CATEGORIAS, datos.categoria(), "La categoría no es válida");
        validarValor(PELIGROSIDADES, datos.peligrosidad(), "La peligrosidad no es válida");
        validarValor(UNIDADES, datos.unidad(), "La unidad debe ser kg, L o unid");
        residuoRepository.findByCodigo(codigo)
                .filter(otro -> !otro.getIdResiduo().equals(id))
                .ifPresent(otro -> {
                    throw new ConflictoException("Ya existe un residuo con el código " + codigo + ".");
                });

        Residuo residuo = id == null ? new Residuo() : buscarResiduo(id);
        residuo.setCodigo(codigo);
        residuo.setNombre(datos.nombre().trim());
        residuo.setCategoria(datos.categoria());
        residuo.setPeligrosidad(datos.peligrosidad());
        residuo.setUnidad(datos.unidad());
        residuo.setPlazoMaximoDias(datos.plazoMaximoDias());
        residuo.setTratamiento(TextoUtil.normalizar(datos.tratamiento()));
        residuo.setRequisitos(TextoUtil.normalizar(datos.requisitos()));
        residuoRepository.save(residuo);

        auditoria.registrar(id == null ? "CREAR" : "MODIFICAR", "Residuo", codigo,
                (id == null ? "Registró" : "Actualizó") + " el residuo " + residuo.getNombre());
        return vistas.residuo(residuo);
    }

    public Residuo buscarResiduo(Integer id) {
        return residuoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Residuo no encontrado"));
    }

    // ---------------------------------------------------------------- áreas (CU04)

    @Transactional(readOnly = true)
    public List<AreaDto> listarAreas() {
        VistaService.Instantanea foto = vistas.instantanea();
        return areaRepository.findAll(Sort.by("codigo")).stream()
                .map(area -> vistas.area(area, foto.generaciones().stream()
                        .filter(g -> g.getArea().getIdArea().equals(area.getIdArea()))
                        .filter(g -> vistas.diasDesde(g.getFecha()) <= 30)
                        .count()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AreaDto> listarAreasActivas() {
        return areaRepository.findAll(Sort.by("nombre")).stream()
                .filter(a -> AreaGeneradora.ACTIVA.equals(a.getEstado()))
                .map(vistas::area)
                .toList();
    }

    @Transactional
    public AreaDto guardarArea(Integer id, AreaRequest datos) {
        String codigo = TextoUtil.normalizarCodigo(datos.codigo());
        validarValor(ESTADOS_AREA, datos.estado(), "El estado debe ser ACTIVA o INACTIVA");
        areaRepository.findByCodigo(codigo)
                .filter(otra -> !otra.getIdArea().equals(id))
                .ifPresent(otra -> {
                    throw new ConflictoException("Ya existe un área con el código " + codigo + ".");
                });

        AreaGeneradora area = id == null ? new AreaGeneradora() : buscarArea(id);
        area.setCodigo(codigo);
        area.setNombre(datos.nombre().trim());
        area.setProceso(TextoUtil.normalizar(datos.proceso()));
        area.setEstado(datos.estado());
        area.setResponsable(buscarResponsable(datos.responsable()));
        areaRepository.save(area);

        auditoria.registrar(id == null ? "CREAR" : "MODIFICAR", "Área generadora", codigo,
                (id == null ? "Registró" : "Actualizó") + " el área " + area.getNombre());
        return vistas.area(area);
    }

    @Transactional
    public AreaDto cambiarEstadoArea(Integer id) {
        AreaGeneradora area = buscarArea(id);
        boolean activar = !AreaGeneradora.ACTIVA.equals(area.getEstado());
        area.setEstado(activar ? AreaGeneradora.ACTIVA : AreaGeneradora.INACTIVA);
        auditoria.registrar(activar ? "ACTIVAR" : "DESACTIVAR", "Área generadora", area.getCodigo(),
                (activar ? "Activó" : "Desactivó") + " el área " + area.getNombre());
        return vistas.area(area);
    }

    /** Nombres de los usuarios activos que pueden ser responsables de un área. */
    @Transactional(readOnly = true)
    public List<String> listarResponsables() {
        return usuarioRepository.findAll(Sort.by("nombres", "apellidos")).stream()
                .filter(Usuario::isActivo)
                .map(Usuario::getNombreCompleto)
                .toList();
    }

    public AreaGeneradora buscarArea(Integer id) {
        return areaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Área no encontrada"));
    }

    /** El frontend envía el nombre completo del responsable; se busca entre los usuarios activos. */
    public Usuario buscarResponsable(String nombreCompleto) {
        if (StringUtils.isBlank(nombreCompleto)) {
            return null;
        }
        return usuarioRepository.findAll().stream()
                .filter(Usuario::isActivo)
                .filter(u -> u.getNombreCompleto().equalsIgnoreCase(nombreCompleto.trim()))
                .findFirst()
                .orElseThrow(() -> new SolicitudInvalidaException("El responsable no es un usuario activo."));
    }

    // ---------------------------------------------------------------- contenedores (CU05)

    @Transactional(readOnly = true)
    public List<ContenedorVista> listarContenedores() {
        VistaService.Instantanea foto = vistas.instantanea();
        return contenedorRepository.findAll(Sort.by("codigo")).stream()
                .map(c -> vistas.contenedor(c, foto))
                .toList();
    }

    @Transactional
    public ContenedorVista guardarContenedor(Integer id, ContenedorRequest datos) {
        String codigo = TextoUtil.normalizarCodigo(datos.codigo());
        validarValor(ESTADOS_CONTENEDOR, datos.estado(), "El estado debe ser DISPONIBLE o MANTENIMIENTO");
        contenedorRepository.findByCodigo(codigo)
                .filter(otro -> !otro.getIdContenedor().equals(id))
                .ifPresent(otro -> {
                    throw new ConflictoException("Ya existe un contenedor con el código " + codigo + ".");
                });
        Residuo residuo = buscarResiduo(datos.idResiduo());
        VistaService.Instantanea foto = vistas.instantanea();

        Contenedor contenedor;
        if (id == null) {
            contenedor = new Contenedor();
        } else {
            contenedor = buscarContenedor(id);
            double ocupacion = vistas.ocupacion(contenedor, foto);
            if (Numeros.valor(datos.capacidad()) < ocupacion) {
                throw new SolicitudInvalidaException("La capacidad no puede ser menor a la ocupación actual ("
                        + Numeros.cantidad(ocupacion, contenedor.getResiduoPermitido().getUnidad()) + ").");
            }
            if (!residuo.getIdResiduo().equals(contenedor.getResiduoPermitido().getIdResiduo()) && ocupacion > 0) {
                throw new SolicitudInvalidaException(
                        "No se puede cambiar el residuo permitido de un contenedor con residuos almacenados.");
            }
            // Si el código cambia, los movimientos guardan el código anterior: se mantiene la trazabilidad
            if (!contenedor.getCodigo().equals(codigo) && ocupacion > 0) {
                throw new SolicitudInvalidaException("No se puede cambiar el código de un contenedor con residuos almacenados.");
            }
        }
        contenedor.setCodigo(codigo);
        contenedor.setTipo(datos.tipo().trim());
        contenedor.setCapacidad(datos.capacidad());
        contenedor.setResiduoPermitido(residuo);
        contenedor.setUbicacion(TextoUtil.normalizar(datos.ubicacion()));
        contenedor.setEstado(datos.estado());
        contenedorRepository.save(contenedor);

        auditoria.registrar(id == null ? "CREAR" : "MODIFICAR", "Contenedor", codigo,
                id == null ? "Registró el contenedor " + codigo + " (" + contenedor.getTipo() + ")"
                        : "Actualizó el contenedor " + codigo);
        return vistas.contenedor(contenedor, vistas.instantanea());
    }

    @Transactional(readOnly = true)
    public List<ContenedorVista> contenedoresOperativos() {
        VistaService.Instantanea foto = vistas.instantanea();
        return contenedorRepository.findAll(Sort.by("codigo")).stream()
                .filter(c -> !Contenedor.MANTENIMIENTO.equals(c.getEstado()))
                .map(c -> vistas.contenedor(c, foto))
                .sorted(Comparator.comparing(ContenedorVista::codigo))
                .toList();
    }

    public Contenedor buscarContenedor(Integer id) {
        return contenedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Contenedor no encontrado"));
    }

    static void validarValor(Set<String> permitidos, String valor, String mensaje) {
        if (!permitidos.contains(valor)) {
            throw new SolicitudInvalidaException(mensaje);
        }
    }
}
