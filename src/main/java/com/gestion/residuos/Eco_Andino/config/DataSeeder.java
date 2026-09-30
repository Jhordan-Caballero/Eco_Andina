package com.gestion.residuos.Eco_Andino.config;

import com.gestion.residuos.Eco_Andino.entity.Permiso;
import com.gestion.residuos.Eco_Andino.entity.Rol;
import com.gestion.residuos.Eco_Andino.entity.Usuario;
import com.gestion.residuos.Eco_Andino.repository.PermisoRepository;
import com.gestion.residuos.Eco_Andino.repository.RolRepository;
import com.gestion.residuos.Eco_Andino.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Siembra permisos, roles y usuarios de prueba. Solo crea lo que falta: nunca modifica lo existente.
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private record PermisoSeed(String codigo, String nombre, String descripcion) {
    }

    private record RolSeed(String codigo, String nombre, String descripcion, List<String> permisos) {
    }

    private record UsuarioSeed(String username, String nombres, String apellidos, String email, String estado,
                               List<String> roles) {
    }

    private static final List<PermisoSeed> PERMISOS = List.of(
            new PermisoSeed("USUARIO_GESTIONAR", "Gestionar usuarios, roles y permisos",
                    "Crear, editar y desactivar usuarios y administrar sus roles y permisos"),
            new PermisoSeed("CATALOGO_CONSULTAR", "Consultar catálogos (residuos, áreas y contenedores)",
                    "Ver los catálogos maestros de residuos, áreas generadoras y contenedores"),
            new PermisoSeed("RESIDUO_GESTIONAR", "Gestionar catálogo de residuos",
                    "Crear, editar y dar de baja tipos de residuo"),
            new PermisoSeed("AREA_GESTIONAR", "Gestionar áreas generadoras",
                    "Crear, editar y dar de baja áreas generadoras"),
            new PermisoSeed("CONTENEDOR_GESTIONAR", "Gestionar contenedores",
                    "Crear, editar y dar de baja contenedores"),
            new PermisoSeed("TRANSPORTISTA_GESTIONAR", "Gestionar transportistas y vehículos",
                    "Mantener el registro de transportistas y sus vehículos"),
            new PermisoSeed("GESTOR_GESTIONAR", "Gestionar gestores autorizados",
                    "Mantener el registro de gestores autorizados"),
            new PermisoSeed("TRANSPORTISTA_GESTOR_CONSULTAR", "Consultar transportistas y gestores",
                    "Ver transportistas, vehículos y gestores autorizados"),
            new PermisoSeed("GENERACION_REGISTRAR", "Registrar generación y segregación",
                    "Registrar residuos generados con su cantidad, área de origen y segregación"),
            new PermisoSeed("GENERACION_VALIDAR", "Validar generación de residuos",
                    "Revisar y validar los registros de generación"),
            new PermisoSeed("ALMACENAMIENTO_GESTIONAR", "Gestionar almacenamiento y movimientos",
                    "Registrar ingresos, pesajes y movimientos del almacenamiento temporal"),
            new PermisoSeed("ALMACENAMIENTO_CONSULTAR", "Consultar residuos y almacenamiento",
                    "Ver los residuos registrados y su estado de almacenamiento"),
            new PermisoSeed("RECOLECCION_PROGRAMAR", "Programar y aprobar recolecciones",
                    "Programar recolecciones, asignar transportista y aprobar órdenes"),
            new PermisoSeed("RECOLECCION_CONSULTAR", "Consultar órdenes de recolección",
                    "Ver las órdenes de recolección y su estado"),
            new PermisoSeed("TRASLADO_REGISTRAR", "Registrar salida y traslado",
                    "Registrar la salida de los residuos y su traslado"),
            new PermisoSeed("RECEPCION_REGISTRAR", "Registrar recepción y destino",
                    "Confirmar la recepción y el tratamiento, valorización o disposición final"),
            new PermisoSeed("DOCUMENTO_GESTIONAR", "Gestionar documentos y evidencias",
                    "Adjuntar y consultar documentos y evidencias de la trazabilidad"),
            new PermisoSeed("INCIDENCIA_GESTIONAR", "Gestionar incidencias y acciones correctivas",
                    "Registrar incidencias y dar seguimiento a sus acciones correctivas"),
            new PermisoSeed("OBSERVACION_REGISTRAR", "Registrar observaciones operativas",
                    "Registrar observaciones durante la operación"),
            new PermisoSeed("REPORTE_GENERAR", "Generar reportes e indicadores",
                    "Consultar reportes e indicadores de la gestión de residuos"),
            new PermisoSeed("REPORTE_EXPORTAR", "Exportar reportes",
                    "Exportar reportes e indicadores a archivo"),
            new PermisoSeed("AUDITORIA_CONSULTAR", "Consultar auditoría",
                    "Ver el registro de auditoría de las acciones del sistema"),
            new PermisoSeed("AUDITORIA_GESTIONAR", "Gestionar auditoría",
                    "Administrar los parámetros del registro de auditoría"));

    private static final List<RolSeed> ROLES = List.of(
            new RolSeed("ADMINISTRADOR", "Administrador",
                    "Gestiona usuarios, roles, permisos, catálogos y parámetros",
                    List.of("USUARIO_GESTIONAR", "CATALOGO_CONSULTAR", "RESIDUO_GESTIONAR", "AREA_GESTIONAR",
                            "CONTENEDOR_GESTIONAR", "TRANSPORTISTA_GESTIONAR", "GESTOR_GESTIONAR",
                            "TRANSPORTISTA_GESTOR_CONSULTAR", "AUDITORIA_CONSULTAR", "AUDITORIA_GESTIONAR")),
            new RolSeed("GENERADOR", "Generador",
                    "Registra la generación, segregación, cantidad y área de origen",
                    List.of("CATALOGO_CONSULTAR", "GENERACION_REGISTRAR", "ALMACENAMIENTO_CONSULTAR",
                            "DOCUMENTO_GESTIONAR", "OBSERVACION_REGISTRAR")),
            new RolSeed("SUPERVISOR_AMBIENTAL", "Supervisor ambiental",
                    "Valida registros, programa recolecciones, revisa incidencias y reportes",
                    List.of("CATALOGO_CONSULTAR", "GENERACION_VALIDAR", "ALMACENAMIENTO_CONSULTAR",
                            "RECOLECCION_PROGRAMAR", "RECOLECCION_CONSULTAR", "TRANSPORTISTA_GESTOR_CONSULTAR",
                            "DOCUMENTO_GESTIONAR", "INCIDENCIA_GESTIONAR", "REPORTE_GENERAR")),
            new RolSeed("ALMACENERO", "Almacenero",
                    "Controla almacenamiento temporal, pesaje y movimientos",
                    List.of("CATALOGO_CONSULTAR", "ALMACENAMIENTO_GESTIONAR", "ALMACENAMIENTO_CONSULTAR",
                            "DOCUMENTO_GESTIONAR", "OBSERVACION_REGISTRAR")),
            new RolSeed("TRANSPORTISTA", "Transportista",
                    "Retira residuos autorizados y registra el traslado",
                    List.of("RECOLECCION_CONSULTAR", "TRASLADO_REGISTRAR", "DOCUMENTO_GESTIONAR",
                            "OBSERVACION_REGISTRAR")),
            new RolSeed("GESTOR_AUTORIZADO", "Gestor autorizado",
                    "Confirma recepción, tratamiento, valorización o disposición",
                    List.of("RECOLECCION_CONSULTAR", "RECEPCION_REGISTRAR", "DOCUMENTO_GESTIONAR",
                            "OBSERVACION_REGISTRAR")),
            new RolSeed("AUDITOR_GERENCIA", "Auditor / Gerencia",
                    "Consulta indicadores, evidencias y cumplimiento de procedimientos",
                    List.of("REPORTE_GENERAR", "REPORTE_EXPORTAR", "AUDITORIA_CONSULTAR")));

    private static final List<UsuarioSeed> USUARIOS = List.of(
            new UsuarioSeed("admin", "Carlos", "Mendoza Ríos", "admin@ecoandina.pe",
                    Usuario.ESTADO_ACTIVO, List.of("ADMINISTRADOR")),
            new UsuarioSeed("aquispe", "Ana", "Quispe Mamani", "ana.quispe@ecoandina.pe",
                    Usuario.ESTADO_ACTIVO, List.of("SUPERVISOR_AMBIENTAL")),
            new UsuarioSeed("rhuaman", "Rosa", "Huamán Flores", "rosa.huaman@ecoandina.pe",
                    Usuario.ESTADO_ACTIVO, List.of("GENERADOR")),
            new UsuarioSeed("ltorres", "Luis", "Torres Cáceres", "luis.torres@ecoandina.pe",
                    Usuario.ESTADO_ACTIVO, List.of("ALMACENERO", "GENERADOR")),
            new UsuarioSeed("jccori", "Jorge", "Ccori Apaza", "jorge.ccori@ecoandina.pe",
                    Usuario.ESTADO_ACTIVO, List.of("TRANSPORTISTA")),
            new UsuarioSeed("mvaldivia", "Marta", "Valdivia Salas", "marta.valdivia@ecoandina.pe",
                    Usuario.ESTADO_ACTIVO, List.of("GESTOR_AUTORIZADO")),
            new UsuarioSeed("psalas", "Pedro", "Salas Ortiz", "pedro.salas@ecoandina.pe",
                    Usuario.ESTADO_ACTIVO, List.of("AUDITOR_GERENCIA")),
            // Desactivado a propósito: sirve para probar la denegación de acceso
            new UsuarioSeed("jperez", "Juan", "Pérez Rojas", "juan.perez@ecoandina.pe",
                    Usuario.ESTADO_INACTIVO, List.of("GENERADOR")));

    private final PermisoRepository permisoRepository;
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultPassword;

    public DataSeeder(PermisoRepository permisoRepository, RolRepository rolRepository,
                      UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                      @Value("${app.seed.default-password}") String defaultPassword) {
        this.permisoRepository = permisoRepository;
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultPassword = defaultPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, Permiso> permisos = sembrarPermisos();
        Map<String, Rol> roles = sembrarRoles(permisos);
        sembrarUsuarios(roles);
    }

    private Map<String, Permiso> sembrarPermisos() {
        Map<String, Permiso> permisos = new HashMap<>();
        int creados = 0;
        for (PermisoSeed seed : PERMISOS) {
            Permiso permiso = permisoRepository.findByCodigo(seed.codigo()).orElse(null);
            if (permiso == null) {
                permiso = new Permiso();
                permiso.setCodigo(seed.codigo());
                permiso.setNombre(seed.nombre());
                permiso.setDescripcion(seed.descripcion());
                permiso = permisoRepository.save(permiso);
                creados++;
            }
            permisos.put(seed.codigo(), permiso);
        }
        if (creados > 0) {
            log.info("Seed: {} permisos creados", creados);
        }
        return permisos;
    }

    private Map<String, Rol> sembrarRoles(Map<String, Permiso> permisos) {
        Map<String, Rol> roles = new HashMap<>();
        int creados = 0;
        for (RolSeed seed : ROLES) {
            Rol rol = rolRepository.findByCodigo(seed.codigo()).orElse(null);
            if (rol == null) {
                rol = new Rol();
                rol.setCodigo(seed.codigo());
                rol.setNombre(seed.nombre());
                rol.setDescripcion(seed.descripcion());
                for (String codigoPermiso : seed.permisos()) {
                    rol.getPermisos().add(requerido(permisos, codigoPermiso));
                }
                rol = rolRepository.save(rol);
                creados++;
            }
            roles.put(seed.codigo(), rol);
        }
        if (creados > 0) {
            log.info("Seed: {} roles creados", creados);
        }
        return roles;
    }

    private void sembrarUsuarios(Map<String, Rol> roles) {
        int creados = 0;
        for (UsuarioSeed seed : USUARIOS) {
            if (usuarioRepository.existsByUsername(seed.username())) {
                continue;
            }
            Usuario usuario = new Usuario();
            usuario.setUsername(seed.username());
            usuario.setNombres(seed.nombres());
            usuario.setApellidos(seed.apellidos());
            usuario.setEmail(seed.email());
            usuario.setEstado(seed.estado());
            usuario.setPassword(passwordEncoder.encode(defaultPassword));
            for (String codigoRol : seed.roles()) {
                usuario.getRoles().add(requerido(roles, codigoRol));
            }
            usuarioRepository.save(usuario);
            creados++;
        }
        if (creados > 0) {
            log.info("Seed: {} usuarios creados", creados);
        }
    }

    private static <T> T requerido(Map<String, T> catalogo, String codigo) {
        T entidad = catalogo.get(codigo);
        if (entidad == null) {
            throw new IllegalStateException("El seed referencia un código inexistente: " + codigo);
        }
        return entidad;
    }
}
