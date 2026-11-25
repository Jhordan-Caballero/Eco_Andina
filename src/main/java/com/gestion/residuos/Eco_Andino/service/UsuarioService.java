package com.gestion.residuos.Eco_Andino.service;

import com.gestion.residuos.Eco_Andino.dto.UsuarioDtos.*;
import com.gestion.residuos.Eco_Andino.entity.Permiso;
import com.gestion.residuos.Eco_Andino.entity.Rol;
import com.gestion.residuos.Eco_Andino.entity.Usuario;
import com.gestion.residuos.Eco_Andino.exception.ConflictoException;
import com.gestion.residuos.Eco_Andino.exception.RecursoNoEncontradoException;
import com.gestion.residuos.Eco_Andino.exception.SolicitudInvalidaException;
import com.gestion.residuos.Eco_Andino.repository.PermisoRepository;
import com.gestion.residuos.Eco_Andino.repository.RolRepository;
import com.gestion.residuos.Eco_Andino.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/** Gestión de usuarios, roles y permisos (CU02). */
@Service
public class UsuarioService {
    private static final String ADMINISTRADOR = "ADMINISTRADOR";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final PasswordEncoder passwordEncoder;
    private final SesionService sesion;
    private final AuditoriaService auditoria;
    private final String passwordInicial;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                          PermisoRepository permisoRepository, PasswordEncoder passwordEncoder, SesionService sesion,
                          AuditoriaService auditoria, @Value("${app.seed.default-password}") String passwordInicial) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.passwordEncoder = passwordEncoder;
        this.sesion = sesion;
        this.auditoria = auditoria;
        this.passwordInicial = passwordInicial;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listarUsuarios() {
        return usuarioRepository.findAllConRoles().stream().map(this::usuario).toList();
    }

    @Transactional(readOnly = true)
    public List<RolDto> listarRoles() {
        List<Usuario> usuarios = usuarioRepository.findAllConRoles();
        return rolRepository.findAllConPermisos().stream()
                .map(rol -> rol(rol, usuarios.stream().filter(u -> u.getRoles().contains(rol)).count()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PermisoDto> listarPermisos() {
        return permisoRepository.findAll(Sort.by("idPermiso")).stream()
                .map(p -> new PermisoDto(p.getCodigo(), p.getNombre(), p.getCasoUso()))
                .toList();
    }

    @Transactional
    public UsuarioDto guardar(Integer id, UsuarioRequest datos) {
        String username = datos.username().trim().toLowerCase(Locale.ROOT);
        String email = datos.email().trim().toLowerCase(Locale.ROOT);
        usuarioRepository.findByUsernameIgnoreCase(username)
                .filter(otro -> !otro.getIdUsuario().equals(id))
                .ifPresent(otro -> {
                    throw new ConflictoException("El usuario " + username + " ya existe.");
                });
        usuarioRepository.findByEmailIgnoreCase(email)
                .filter(otro -> !otro.getIdUsuario().equals(id))
                .ifPresent(otro -> {
                    throw new ConflictoException("El correo " + email + " ya está registrado.");
                });
        Set<Rol> roles = new HashSet<>();
        for (String codigo : datos.roles()) {
            roles.add(rolRepository.findByCodigo(codigo)
                    .orElseThrow(() -> new SolicitudInvalidaException("El rol " + codigo + " no existe.")));
        }
        String nombresRoles = roles.stream().map(Rol::getNombre).sorted().collect(Collectors.joining(", "));

        Usuario usuario;
        if (id == null) {
            usuario = new Usuario();
            usuario.setEstado(Usuario.ESTADO_ACTIVO);
            usuario.setPassword(passwordEncoder.encode(datos.password() != null ? datos.password() : passwordInicial));
        } else {
            usuario = buscar(id);
            boolean quitaAdmin = esAdmin(usuario) && roles.stream().noneMatch(r -> ADMINISTRADOR.equals(r.getCodigo()));
            if (quitaAdmin && !hayOtroAdminActivo(usuario)) {
                throw new SolicitudInvalidaException("Debe quedar al menos un administrador activo.");
            }
            if (datos.password() != null) {
                usuario.setPassword(passwordEncoder.encode(datos.password()));
            }
        }
        usuario.setUsername(username);
        usuario.setNombres(datos.nombres().trim());
        usuario.setApellidos(datos.apellidos().trim());
        usuario.setEmail(email);
        usuario.getRoles().clear();
        usuario.getRoles().addAll(roles);
        usuarioRepository.save(usuario);

        auditoria.registrar(id == null ? "CREAR" : "MODIFICAR", "Usuario", username,
                id == null ? "Creó la cuenta con rol " + nombresRoles : "Actualizó datos y roles: " + nombresRoles);
        return usuario(usuario);
    }

    @Transactional
    public UsuarioDto cambiarEstado(Integer id) {
        Usuario usuario = buscar(id);
        boolean activar = !usuario.isActivo();
        if (!activar && usuario.getIdUsuario().equals(sesion.usuario().getIdUsuario())) {
            throw new SolicitudInvalidaException("No puedes desactivar tu propia cuenta.");
        }
        if (!activar && esAdmin(usuario) && !hayOtroAdminActivo(usuario)) {
            throw new SolicitudInvalidaException("Debe quedar al menos un administrador activo.");
        }
        usuario.setEstado(activar ? Usuario.ESTADO_ACTIVO : Usuario.ESTADO_INACTIVO);
        auditoria.registrar(activar ? "ACTIVAR" : "DESACTIVAR", "Usuario", usuario.getUsername(),
                (activar ? "Activó" : "Desactivó") + " la cuenta del usuario");
        return usuario(usuario);
    }

    /** Recibe, por cada rol modificado, la lista completa de sus permisos. */
    @Transactional
    public List<RolDto> guardarPermisosRoles(Map<String, List<String>> cambios) {
        List<String> admin = cambios.get(ADMINISTRADOR);
        if (admin != null && !admin.contains("USUARIO_GESTIONAR")) {
            throw new SolicitudInvalidaException("El rol Administrador debe conservar el permiso de gestionar usuarios.");
        }
        Map<String, Permiso> permisos = permisoRepository.findAll().stream()
                .collect(Collectors.toMap(Permiso::getCodigo, p -> p));
        for (Map.Entry<String, List<String>> cambio : cambios.entrySet()) {
            Rol rol = rolRepository.findByCodigo(cambio.getKey())
                    .orElseThrow(() -> new SolicitudInvalidaException("El rol " + cambio.getKey() + " no existe."));
            Set<String> antes = rol.getPermisos().stream().map(Permiso::getCodigo).collect(Collectors.toSet());
            Set<String> despues = new LinkedHashSet<>(cambio.getValue());
            Set<Permiso> nuevos = new HashSet<>();
            for (String codigo : despues) {
                Permiso permiso = permisos.get(codigo);
                if (permiso == null) {
                    throw new SolicitudInvalidaException("El permiso " + codigo + " no existe.");
                }
                nuevos.add(permiso);
            }
            List<String> agregados = despues.stream().filter(c -> !antes.contains(c)).sorted().toList();
            List<String> quitados = antes.stream().filter(c -> !despues.contains(c)).sorted().toList();
            if (agregados.isEmpty() && quitados.isEmpty()) {
                continue;
            }
            rol.getPermisos().clear();
            rol.getPermisos().addAll(nuevos);
            List<String> resumen = new ArrayList<>();
            if (!agregados.isEmpty()) {
                resumen.add("agregó " + String.join(", ", agregados));
            }
            if (!quitados.isEmpty()) {
                resumen.add("quitó " + String.join(", ", quitados));
            }
            auditoria.registrar("MODIFICAR", "Rol", rol.getNombre(), "Permisos: " + String.join("; ", resumen));
        }
        return listarRoles();
    }

    private boolean esAdmin(Usuario usuario) {
        return usuario.getRoles().stream().anyMatch(r -> ADMINISTRADOR.equals(r.getCodigo()));
    }

    private boolean hayOtroAdminActivo(Usuario usuario) {
        return usuarioRepository.findAllConRoles().stream()
                .anyMatch(u -> !u.getIdUsuario().equals(usuario.getIdUsuario()) && u.isActivo() && esAdmin(u));
    }

    private Usuario buscar(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private UsuarioDto usuario(Usuario u) {
        List<Rol> roles = u.getRoles().stream().sorted(Comparator.comparing(Rol::getIdRol)).toList();
        return new UsuarioDto(u.getIdUsuario(), u.getUsername(), u.getNombres(), u.getApellidos(), u.getEmail(),
                u.getEstado(), roles.stream().map(Rol::getCodigo).toList(), u.getUltimoAcceso(), u.getNombreCompleto(),
                roles.stream().map(r -> rol(r, 0)).toList());
    }

    private static RolDto rol(Rol rol, long usuarios) {
        return new RolDto(rol.getCodigo(), rol.getNombre(), rol.getDescripcion(),
                rol.getPermisos().stream().map(Permiso::getCodigo).sorted().toList(), usuarios);
    }
}
